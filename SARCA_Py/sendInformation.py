from os import device_encoding
from bluetooth.bluez import BluetoothSocket, advertise_service, discover_devices
import bluetooth.bluez
import socket
import bluetooth
import pprint
import time
from bluetooth.btcommon import PORT_ANY, RFCOMM
import dbus
import sys
import subprocess
from bluetooth import *

class COLOR:
    GREEN = '\033[92m'
    BLUE = '\033[96m'
    RED = '\033[91m'
    END = '\033[0m'
green = COLOR.GREEN
blue = COLOR.RED
red = COLOR.RED
e = COLOR.END




## DEFAULT BLUETOOTH DEVICE FOR TESTING: 
test_device = "B4:F7:A1:06:D0:1C"


## INITIALISE DEBUG SCRIPT TO TEST EACH FUNCTION 
debug = True


## INITIALISE SYSTEMBUS TO ESTABLISH CONNECTED DEVICES
bus = dbus.SystemBus()
def proxyObj(bus, path, interface):
    obj = bus.get_object('org.bluez', path)
    return dbus.Interface(obj, interface)

def filter_by_interface(objects, interface_name):
    result = []
    for path in objects.keys():
        interfaces = objects[path]
        for interface in interfaces.keys():
            if interface == interface_name:
                result.append(path)
    return result

## SHOW DEVICES CURRENTLY CONNECTED TO
def showConnected():
    btManager = proxyObj(bus, "/", "org.freedesktop.DBus.ObjectManager")
    objects = btManager.GetManagedObjects()
    btDevices = filter_by_interface(objects, "org.bluez.Device1")

    bt_devices = []
    for device in btDevices:
        obj = proxyObj(bus, device, "org.freedesktop.DBus.Properties")
        bt_devices.append({
            "name:": str(obj.Get("org.bluez.Device1", "Name")),
            "addr:": str(obj.Get("org.bluez.Device1", "Address"))
        })
    pprint.pprint(bt_devices)

## FIND NEARBY DEVICES
def findNearby():
    global devices
    print(green + "\nSearching for devices. Please make sure device is discoverable." + e)
    devices = discover_devices(lookup_names=True, lookup_class=False)
    if not devices:
        print(red + "\nNo devices found. Restarting." + e)
        time.sleep(1)
        findNearby()
    else:
        print("\nFound Devices:")
        print(devices)
            
            
def getip():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        sock.connect(("10.1.1.1", 1))
        IP = sock.getsockname()[0]
    except:
        IP = "127.0.0.1"
    finally:
        sock.close()
    return IP


def bt_connect():
    global client_sock
    print("got here okay")
    client_sock, client_info = server_sock.accept()
    print("connecting to: " + client_info)
    client_sock.send(getip())
    try:
        while True:
            data = client_sock.recv(1024)
            if len(data) == 0: break
            print("received [%s]"%data)
            client_sock.send(getip())
    except IOError as e:
        print(e.args[0])
                    

uuid = 'f22fbd56-631e-442c-8992-78f961e103f6'
server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)
port = server_sock.getsockname()[1]

    
advertise_service(server_sock, "Pi Server", service_id=uuid, service_classes=[uuid, SERIAL_PORT_CLASS], profiles=[SERIAL_PORT_PROFILE])

def dothething():
    serveron = True
    while serveron:
        print("waiting for connection on RFCOMM channel %d" % port)
        bt_connect()
        print("disconnected")
        client_sock.close()
        server_sock.close()











if __name__ == "__main__":
    if debug == True:
        while True:
            message = input("> ")
            if message == "show":
                print(green + "\nConnected Devices" + e)
                showConnected()
            elif message == "new":
                findNearby()
            elif message == "establish":
                bt_connect()
            elif message == "connect":
                dothething()











            elif message == "exit":
                sys.exit()
            else: 
                print("Command not known. Try again.")
                continue


