import RPi.GPIO as GPIO
import time
from gpiozero import Buzzer

buzzer = Buzzer(22)
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(5, GPIO.OUT) 
GPIO.setup(6, GPIO.OUT)
GPIO.setup(13, GPIO.OUT)
GPIO.setup(19, GPIO.OUT)
GPIO.setup(26, GPIO.OUT)

# GPIO.output(5, GPIO.LOW)  RED 
# GPIO.output(6, GPIO.LOW)  BLUE 3
# GPIO.output(13, GPIO.LOW) BLUE 2
# GPIO.output(19, GPIO.LOW) BLUE 1 
# GPIO.output(26, GPIO.LOW) GREEN

# GPIO.output(5, GPIO.HIGH)
# GPIO.output(6, GPIO.HIGH)
# GPIO.output(13, GPIO.HIGH)
# GPIO.output(19, GPIO.HIGH)
# GPIO.output(26, GPIO.HIGH)
# time.sleep(0.2)
time.sleep(3)
def run():
    GPIO.output(26, GPIO.HIGH)
    time.sleep(1)
    


run()