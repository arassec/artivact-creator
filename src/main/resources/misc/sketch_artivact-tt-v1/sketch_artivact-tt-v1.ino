#include <Stepper.h>

#define STEPS 2038
#define BUF_LEN 20

byte test,start;

Stepper stepper(STEPS, 8, 10, 9, 11);

void setup() {
  Serial.begin(9600);
    
  stepper.setSpeed(10);

  pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {

  static char sdata[BUF_LEN], *pSdata=sdata;
  byte ch;
  int val;

  if (Serial.available()) {
    ch = Serial.read();

    // -1 for null terminator space
    if ((pSdata - sdata)>=BUF_LEN-1) {
      pSdata--;
      Serial.print("BUFFER OVERRUN\n");
    }

    *pSdata++ = (char)ch;

    if (ch=='\n') {  // Command received and ready.

      pSdata--;       // Don't add \r to string.
      *pSdata = '\0';  // Null terminate the string.

      // Process command in sdata.
      switch( sdata[0] ) {
      case 'v':
        Serial.println("artivact-tt-v1");
        break;
      case 't':
        if (strlen(sdata)>1) {
          val = atoi(&sdata[1]);
        }
        stepper.step(10250/val); // 360° == 10250
        Serial.println("done");
        break;
      } // switch

       pSdata = sdata; // Reset pointer to start of string.
    } // if \n

    digitalWrite(8,LOW);
    digitalWrite(9,LOW);
    digitalWrite(10,LOW);
    digitalWrite(11,LOW);
      
  }  // available
}
