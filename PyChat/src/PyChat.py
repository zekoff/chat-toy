'''
PyChat.py
Simple chat server and client, implemented in Python. Uses raw socket
communication over TCP ports. Intended for compatibility with Java
implementation.

For general documentation, see Java implementation.

Created on Oct 27, 2011
@author: Zekoff
'''

import socket
import threading

class Server:
    def __init__(self, host="", port=50007):
        self.HOST = host
        self.PORT = port
        self.connections = []
    def start(self):
        speakerThread = ServerSpeakerThread(self.connections)
        speakerThread.start()
        s = socket.socket()
        # The bind() and listen() methods turn the socket into a server.
        # bind() accepts a tuple pair of host and port.
        s.bind((self.HOST, self.PORT))
        s.listen(1)
        print "Server online at:"
        print "(HOST) " + self.HOST
        print "(PORT) " + str(self.PORT)
        while True:
            # accept() returns a tuple pair (socket, address info)
            newSocket, newSocketAddress = s.accept()
            print "New client connected."
            listenerThread = ServerListenerThread(newSocket, newSocketAddress)
            listenerThread.start()
            self.connections.append(listenerThread)
            
class ServerListenerThread(threading.Thread):
    def __init__(self, newSocket, newSocketAddress):
        self.socket = newSocket
        self.socketAddress = newSocketAddress
        threading.Thread.__init__(self)
    def run(self):
        while True:
            line = self.socket.recv(1024)
            # Python doesn't strip the newline from the received message
            print "CLIENT: " + line.rstrip()
            
class ServerSpeakerThread(threading.Thread):
    def __init__(self, connections):
        self.connections = connections
        threading.Thread.__init__(self)
    def run(self):
        while True:
            line = raw_input()
            # Python raw_input() strips the trailing newline character,
            # which is required for the Java implementation to detect 
            # that a message has been sent. Thus, a newline character
            # is manually appended to each input line for the sake of
            # compatibility.
            for c in self.connections:
                c.socket.send(line + "\n")

class Client:
    def __init__(self, host="127.0.0.1", port=50007):
        self.HOST = host
        self.PORT = port
        self.socket = socket.socket()
        self.socket.connect((self.HOST, self.PORT))
    def start(self):
        print "Client connected to (" + self.HOST + ")"
        listenerThread = ClientListenerThread(self.socket)
        listenerThread.start()
        speakerThread = ClientSpeakerThread(self.socket)
        speakerThread.start()

class ClientListenerThread(threading.Thread):
    def __init__(self, socket):
        self.socket = socket
        threading.Thread.__init__(self)
    def run(self):
        while True:
            line = self.socket.recv(1024)
            print "SERVER: " + line.rstrip()
    
class ClientSpeakerThread(threading.Thread):
    def __init__(self, socket):
        self.socket = socket
        threading.Thread.__init__(self)
    def run(self):
        while True:
            line = raw_input()
            self.socket.send(line + "\n")
            
if __name__ == "__main__":
    print "Enter 1 for Client, 2 for Server:"
    choice = raw_input()
    if choice == 1:
        print "Enter HOST (leave blank for 127.0.0.1):"
        host = raw_input()
        if host == "":
            host = "127.0.0.1"
    print "Enter PORT (leave blank for 50007):"
    port = raw_input()
    if port == "":
        port = 50007
    else:
        port = int(port)
    print "-----"
    try:
        if choice == '1':
            client = Client(host, port)
            client.start()
        if choice == '2':
            server = Server("", port)
            server.start()
    except:
        print "Incorrect configuration."

