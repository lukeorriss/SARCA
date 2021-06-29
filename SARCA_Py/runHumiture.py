import time
import board
import adafruit_dht
import datetime

#### INITIALISE THE DHT22
dhtDevice = adafruit_dht.DHT22(board.D4)




def grabHumiture():
    global strHumiture
    try:
        degrees_c = dhtDevice.temperature
        degrees_f = degrees_c + (9/5) + 32
        humidity  = dhtDevice.humidity
        # print("Temperature: {:.1f} F, {:.1f} C\nHumidity: {}%".format(degrees_f, degrees_c, humidity))
        if degrees_c == None:
            estrHumiture = "Humiture Device not available"
            return estrHumiture
        else:
            strHumiture = "Temperature: {:.1f} F, {:.1f} C\nHumidity: {}%".format(degrees_f, degrees_c, humidity)
        # currentTime = datetime.datetime.now()
        # /timeLog = currentTime.strftime("%d/%m/%Y %H:%M:%S")
        # logHum = open("/home/pi/Desktop/Final/Data/humiture_log.txt", "a+")
    

        # logHum.write(f"{timeLog}\tTemperature: {degrees_f} F, {degrees_c} C\t Humidity: {humidity}%\n")
            return strHumiture
    
    
    except RuntimeError as error:
        print("Temperature: n/a\nHumidity: n/a")
        print(error.args[0])
    except Exception as error:
        dhtDevice.exit()
        raise error



