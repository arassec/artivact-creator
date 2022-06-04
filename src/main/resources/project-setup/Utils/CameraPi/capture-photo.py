import RPi.GPIO as GPIO
import os
import signal
import subprocess
import sys
import time
from sh import gphoto2 as gp
from time import sleep

SLEEP_TIME = 1
TARGET_DIR = sys.argv[1]
NUM_IMAGES = int(sys.argv[2])
TURN_DEGREES = int(sys.argv[3])
# 510 for ASIN B08X6PWRLW
# 2550 for https://www.thingiverse.com/thing:4167615
# 2300 for Golle No. 1
TURN_DEGREES_TOTAL = int(sys.argv[4])

GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)

A = 7
B = 11
C = 13
D = 15

GPIO.setup(A, GPIO.OUT)
GPIO.setup(B, GPIO.OUT)
GPIO.setup(C, GPIO.OUT)
GPIO.setup(D, GPIO.OUT)

storageCommand = ["--set-config", "capturetarget=1"]
autofocusCommand = ["--set-config", "autofocus=0"]
clearCommand = ["--folder", "/store_00010001/DCIM/100D3200", "-R", "--delete-all-files"]
captureCommand = ["--trigger-capture"]
downloadCommand = ["--get-all-files"]

################################################
# Sets up the GPIO Pins
################################################
def gpioSetup(a,b,c,d):
	GPIO.output(A, a)
	GPIO.output(B, b)
	GPIO.output(C, c)
	GPIO.output(D, d)
	time.sleep(0.001)

################################################
# Turns the table right by the specified amount
################################################
def rightTurn(deg):

	full_circle = TURN_DEGREES_TOTAL
	degree = full_circle/360*deg
	gpioSetup(0,0,0,0)

	while degree > 0.0:
		gpioSetup(1,0,0,1)
		gpioSetup(0,0,0,1)
		gpioSetup(0,0,1,1)
		gpioSetup(0,0,1,0)
		gpioSetup(0,1,1,0)
		gpioSetup(0,1,0,0)
		gpioSetup(1,1,0,0)
		gpioSetup(1,0,0,0)
		degree -= 1

################################################
# Kill gphoto2 process that starts whenever we connect the camera
################################################
def killGphoto2Process():
	p = subprocess.Popen(['ps', '-A'], stdout=subprocess.PIPE)
	out, err = p.communicate()

	# Search for line that has the process ID to kill
	for line in out.splitlines():
		if b'gvfsd-gphoto2' in line:
			# Kill the process
			pid = int(line.split(None,1)[0])
			os.kill(pid, signal.SIGKILL)

################################################
# Creates the target directory if required.
################################################
def createSaveFolder():
	try:
		os.makedirs(TARGET_DIR)
	except:
		print("Target directory not created (may already exist).")
	os.chdir(TARGET_DIR)

################################################
# Captures an image and copies it.
################################################
def captureImage():
	gp(captureCommand)
	sleep(1)
	gp(downloadCommand)
	sleep(3)
	gp(clearCommand)
	sleep(1)

################################################
# Renames the copied image file.
################################################
def renameFile():
	for filename in os.listdir("."):
		if len(filename) < 13:
			if filename.startswith("DSC_") and filename.endswith(".JPG"):
				os.rename(filename, (IMAGE_NAME + ".JPG"))
				print("Renamed the JPG")

################################################
################################################
# MAIN
################################################
################################################
killGphoto2Process()
gp(storageCommand)
gp(autofocusCommand)

try:
	gp(clearCommand)
except:
	print("Camera dir not cleared (might not exist).")

createSaveFolder()

for x in range(NUM_IMAGES):
	gp(captureCommand)
	sleep(1)
	rightTurn(TURN_DEGREES)

gp(downloadCommand)
gp(clearCommand)

gpioSetup(0,0,0,0)
