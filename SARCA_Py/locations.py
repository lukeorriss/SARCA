from gps import *
import threading
import datetime

runGPS = None
class GPSlocation_start(threading.Thread):
    def __init__(self):
        global runGPS
        threading.Thread.__init__(self)
        runGPS = gps(mode=WATCH_ENABLE)
        self.current_value = None
        self.running = True
    # def run(self):
    #     global runGPS
    #     while GPSlocation.running:
    #         runGPS.next()
GPSlocation = GPSlocation_start()
GPSlocation.start()



def mainLoop():
    # currentTime = datetime.datetime.now()
    # timeLog = currentTime.strftime("%d/%m/%Y %H:%M:%S")
    # logLoc = open("/home/pi/Desktop/Final/Data/gps_log.txt", "a+")
    

    # logLoc.write(f"{timeLog}\tLatitude: {runGPS.fix.latitude} N\t Longitude: {runGPS.fix.longitude} E\n")
    location = f"Lat: {runGPS.fix.latitude}\tLong: {runGPS.fix.longitude}"
    alt = f"Altitude: {runGPS.fix.altitude}\tSpeed {runGPS.fix.speed} (m/s) / {runGPS.fix.speed * 2.237} (mph)"
    return location, alt
   
        