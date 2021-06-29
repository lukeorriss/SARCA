from bluetooth import *
import socket
import subprocess
import time 

def get_ip():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        sock.connect(('0.0.0.0', 1))
        IPadd = sock.getsockname()[0]
    except:
        IPadd = '127.0.0.1'
    finally:
        sock.close()
    return IPadd


def client_connect():
    global client_sock
    client_sock, client_info = server_sock.accept()
    print("Successfully connected to: ", client_info)
    try:
        while True:
            data = client_sock.recv(1024)
            if len(data) ==0: break
            print("rec: [%s]"%data)
            client_sock.send(getip())
    except IOError as e:
        e.args[0]
        


server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "f22fbd56-631e-442c-8992-78f961e103f6"

advertise_service(server_sock, "Sample Server", service_id = uuid, service_classes = [uuid, SERIAL_PORT_CLASS], profiles = [SERIAL_PORT_PROFILE])

print("Waiting for connection on RFCOMM channel %d" % port)
client_connect()
# print("Disconnected")
# client_sock.close()
# server_sock.close()
