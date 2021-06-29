#### External Imports for sensors
import time
import busio
import board
import adafruit_sgp30
import RPi.GPIO as GPIO
from gpiozero import Buzzer
import sys
import datetime

#### External imports for bluetooth server
from bluetooth import *
import socket
import subprocess

### Internal Imports
from runHumiture import grabHumiture
from berryIMUsimple import readIMU
from bmp388 import readSenInfo
from locations import *

## Colour Codes
class COLOR:
    GREEN = '\033[92m'
    BLUE = '\033[96m'
    RED = '\033[91m'
    END = '\033[0m'
green = COLOR.GREEN
blue = COLOR.RED
red = COLOR.RED
e = COLOR.END

## Initialise LED Lights and Buzzer
buzzer = Buzzer(22)
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(5, GPIO.OUT) # Green
GPIO.setup(6, GPIO.OUT) # Blue III
GPIO.setup(13, GPIO.OUT) # Blue II
GPIO.setup(19, GPIO.OUT) # Blue I
GPIO.setup(26, GPIO.OUT) # Red

## Turn LEDs off
def turnLEDoff():
    GPIO.output(5, GPIO.LOW)
    GPIO.output(6, GPIO.LOW)
    GPIO.output(13, GPIO.LOW)
    GPIO.output(19, GPIO.LOW)
    GPIO.output(26, GPIO.LOW)
def turnLEDon():
    GPIO.output(5, GPIO.HIGH)
    GPIO.output(6, GPIO.HIGH)
    GPIO.output(13, GPIO.HIGH)
    GPIO.output(19, GPIO.HIGH)
    GPIO.output(26, GPIO.HIGH)

## Indicate Finished with test:
def finsihedTest():
    turnLEDon()
    time.sleep(0.5)
    turnLEDoff()
    time.sleep(0.5)
    turnLEDon()
    time.sleep(0.5)
    turnLEDoff()
    time.sleep(0.5)
    turnLEDon()
    time.sleep(0.5)
    turnLEDoff()
    time.sleep(0.5)
    turnLEDon()
    time.sleep(0.5)
    turnLEDoff()
    time.sleep(0.5)
    

#### INITIALISE SGP30 SENSOR
initialise = busio.I2C(board.SCL, board.SDA, frequency=100000)
sensor = adafruit_sgp30.Adafruit_SGP30(initialise)
sensor.iaq_init()

### CHANGE THIS VALUE TO CHANGE UPDATE TIME (SECONDS) DEFAULT IS 10
interval_between_refreshes = 20

## Dependencies for rest of script              
iterations = 0
timestamp = 0
ischeckComplete = 0
serveron = False
running = True
failedConnect = 0

## Error Handling
bluetoothError_Text = "Failed to communicate with device. Retrying " + str(failedConnect) + "/10."
bluetoothError_Text_Fatal = "Repeated failed attempts to connect to device. Data will be logged and held until reconnection."

## Debug mode? Use this to bypass test and test individual features
debugMode = False

def checkSensors():
    global ischeckComplete
    turnLEDoff()
    print(green + "Powering On..." + e)
    time.sleep(1)
    GPIO.output(26, GPIO.HIGH)
    time.sleep(1)
    try:
        print("Testing DHT22 (Humiture)...")
        grabHumiture()
        time.sleep(2)
        GPIO.output(19, GPIO.HIGH)
        print(green + "Successfully connected to DHT22. Continuing..." + e)
        time.sleep(1)
        if True:
            print("Testing SGP30 (eCO2 and TVOC)...")
            time.sleep(2)
            if sensor.eCO2 >= 400:
                print(green + "Successfully connected to SGP30. Continuing..." + e)
                GPIO.output(13, GPIO.HIGH)
                time.sleep(1)
                if True: 
                    print("Testing BerryGPS (GPS)...")
                    time.sleep(2)
                    try:
                        readIMU()
                        if True:
                            print(green + "Successfully connected to BerryGPS. Continuing..." + e)
                            GPIO.output(6, GPIO.HIGH)
                            time.sleep(1)
                            if True: 
                                print("Testing BerryIMU (Magnetometer, Barometer, Altitude, Accelerometer, Gyroscope)...")
                                try:
                                    readSenInfo()
                                    if True:
                                        print(green + "Successfully connected to BerryIMU. Checks complete. Starting Application." + e)
                                        ischeckComplete += 1 
                                        time.sleep(2)
                                except:
                                    print(red + "Failed to connect to BerryIMU. Check Wiring." + e)
                                    sys.exit()
                    except:
                        print(red + "Failed to connect to BerryGPS. Check Wiring." + e)
                        sys.exit()
            else:
                print(red + "Failed to connect to SGP30. Check wiring." + e)
                sys.exit()
        
    except:
        print(red + "Failed to connect to DHT22. Check wiring." + e)
        sys.exit()

server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)
port = server_sock.getsockname()[1]
uuid = "f22fbd56-631e-442c-8992-78f961e103f6"
try:
    advertise_service(server_sock, "Sample Server", service_id = uuid, service_classes = [uuid, SERIAL_PORT_CLASS], profiles = [SERIAL_PORT_PROFILE])
except BluetoothError as noroot:
    print(red + "Failed to start service. Are you running with root?." + e)
    
    sys.exit()

def outputSensorsLog():
    print("Current Time: " + rasterisedTim)
    print(rasterisedHum)
    print(rasterisedCO2)
    print(rasterisedGPS[0])
    print(rasterisedGPS[1])
    print(rasterisedIMU[0])
    print(rasterisedIMU[1])
    print(rasterisedIMU[2])
    print(rasterisedIMU[3])
    print(rasterisedSen + "\n")

def outputSensorsLog_Debugged():
    print(red + "DEBUG MODE IS ENABLED" + e)
    print("Only reading some sensors.")
    print(rasterisedHum)
    print(rasterisedCO2)


while running:
    if ischeckComplete != 1 and debugMode == False:
        checkSensors()
        finsihedTest()
        
    else:
        GPIO.output(26, GPIO.HIGH)
        if serveron == False:
            try:
                print(green + "Waiting for connection on RFCOMM: %d"%(port) + e)
                GPIO.output(19, GPIO.HIGH)
                print(red + "PLEASE ENSURE DEVICE DOES NOT SLEEP WHILST CONNECTED. This could cause a loss of connection." + e)
                client_sock, client_info = server_sock.accept()
                serveron = True
                print(green + "Successfully connected to: ", client_info)
                
                print(e)
                connected = True
                GPIO.output(13, GPIO.HIGH)
                GPIO.output(6, GPIO.HIGH)
                
            except KeyboardInterrupt:
                message = input(red + "\nYou tried ending the service. Are you sure?" + e)
                if message.lower().strip() in ['y', 'yes']:
                    server_sock.close()
                    client_sock.close()
                    sys.exit()
                    break
                else:
                    print(red + "Exit cancelled. Continuing." + e)
                    time.sleep(2)
                    continue
        
        elif serveron == True:
            GPIO.output(5, GPIO.HIGH)
            while serveron:
                currentTime   = datetime.datetime.now()
                rasterisedTim = currentTime.strftime("%d/%m/%Y %H:%M:%S")
                rasterisedHum = grabHumiture()
                if rasterisedHum == None:
                    rasterisedHum = "Could not read sensor information."
                rasterisedCO2 = "eCO2 = %d ppm \nTVOC = %d ppb" % (sensor.eCO2, sensor.TVOC)
                rasterisedGPS = mainLoop()
                rasterisedIMU = readIMU()
                rasterisedSen = readSenInfo()
                
                ## PRINT TO CONSOLE AND LOG DATA PER INSTANCE
                if debugMode == False:
                    outputSensorsLog()
                else:
                    outputSensorsLog_Debugged()
                
                
                ## SEND DATA TO BLUETOOTH DEVICE
                while connected:
                    try: 
                        if debugMode == False:
                            #client_sock.send(rasterisedTim)
                            #client_sock.send(rasterisedHum)
                            #client_sock.send(rasterisedCO2)
                            #client_sock.send(rasterisedGPS[0])
                            #client_sock.send(rasterisedGPS[1])
                            #client_sock.send(rasterisedIMU[0])
                            #client_sock.send(rasterisedIMU[1])
                            #client_sock.send(rasterisedIMU[2])
                            #client_sock.send(rasterisedIMU[3])
                            #client_sock.send(rasterisedSen)
                            client_sock.send(rasterisedTim + "#" + rasterisedHum + "#" + rasterisedCO2 + "#" + rasterisedGPS[0] + "#" + rasterisedGPS[1] + "#" + rasterisedIMU[0] + "#" + rasterisedIMU[1] + "#" + rasterisedIMU[2] + "#" + rasterisedIMU[3] + "#" + rasterisedSen)
                            time.sleep(interval_between_refreshes)
                            break
                        else:
                            ## Write the data to send here when debug mode enabled.
                            client_sock.send('incomingMessages.setText("")')
                            client_sock.send(rasterisedHum)
                           # client_sock.send(rasterisedCO2)
                            time.sleep(interval_between_refreshes)
                            break
                    except ConnectionResetError:
                        failedConnect += 1
                        print(red + bluetoothError_Text + e)
                        if failedConnect >= 10:
                            print(red + bluetoothError_Text_Fatal + e)
                            connected = False
                            time.sleep(3)
                            break
                        else:
                            continue
                    except BluetoothError:
                        failedConnect += 1
                        print(red + bluetoothError_Text + e)
                        if failedConnect >= 10:
                            print(red + bluetoothError_Text_Fatal + e)
                            connected = False
                            time.sleep(3)
                            break
                        else:
                            continue
                    except KeyboardInterrupt:
                        message = input(red + "\nYou tried ending the service. Are you sure?" + e)
                        if message.lower().strip() in ['y', 'yes']:
                            server_sock.close()
                            client_sock.close()
                            connected = False
                            sys.exit()
                            break
                        else:
                            print(red + "Exit cancelled. Continuing." + e)
                            time.sleep(2)
                            continue
                    except Exception as error:
                        client_sock.close()
                        print(red)
                        print("\nLost connection to bluetooth device: ", client_info)
                        print(e)
                        serveron = False
                        connected = False
                        print(red + "Restarting server. Ensure your device's bluetooth is enabled." + e)
                        break
                while not connected:
                    GPIO.output(13, GPIO.LOW)
                    GPIO.output(6, GPIO.LOW)
                    outputSensorsLog()
                    time.sleep(interval_between_refreshes)
        else:
            print(red + "Could not establish connection with device. Is the bluetooth adapter enabled?" + e)
        
        









        
        
        

        
        
        

