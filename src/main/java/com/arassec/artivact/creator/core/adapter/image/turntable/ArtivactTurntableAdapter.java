package com.arassec.artivact.creator.core.adapter.image.turntable;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@ConditionalOnProperty(value = "adapter.implementation.turntable", havingValue = "ArtivactTurntable")
public class ArtivactTurntableAdapter implements TurntableAdapter {

    public void rotate(int numPhotos) {

        var turntableFound = new AtomicBoolean(false);
        var finished = new AtomicBoolean(false);

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        final SerialPort liveSerialPort;

        for (SerialPort p : serialPorts) {
            p.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
            p.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written
            p.openPort();
            if (p.isOpen()) {
                liveSerialPort = p;
                log.debug("Found potential artivact turntable at serial port: {}", liveSerialPort.getSystemPortName());

                var resultBuilder = new StringBuilder();

                liveSerialPort.addDataListener(new SerialPortDataListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        int size = event.getSerialPort().bytesAvailable();
                        var buffer = new byte[size];
                        event.getSerialPort().readBytes(buffer, size);
                        resultBuilder.append(new String(buffer));

                        String commandResult = resultBuilder.toString().trim();

                        if (commandResult.startsWith("artivact-tt-v1")) {
                            turntableFound.set(true);
                            resultBuilder.delete(0, resultBuilder.length());
                            try {
                                liveSerialPort.getOutputStream().write(("t" + numPhotos + "\n").getBytes());
                                liveSerialPort.getOutputStream().flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (commandResult.startsWith("done")) {
                            if (!liveSerialPort.closePort()) {
                                throw new IllegalStateException("Could not close serial port to turntable!");
                            }
                            finished.set(true);
                        }
                    }

                    @Override
                    public int getListeningEvents() {
                        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                    }
                });

                try {
                    Thread.sleep(100);

                    liveSerialPort.getOutputStream().write(("v\n").getBytes());
                    liveSerialPort.getOutputStream().flush();
                } catch (IOException e) {
                    log.error("Exception during turntable operation!", e);
                } catch (InterruptedException e) {
                    log.error("Interrupted during turntable operation!", e);
                    Thread.currentThread().interrupt();
                }

                break;
            }
        }

        try {
            Thread.sleep(100);

            while (!finished.get() && turntableFound.get()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for turntable to finish operation!", e);
            Thread.currentThread().interrupt();
        }
    }

}
