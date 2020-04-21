#!/usr/bin/env python3

import socket
import cv2
import numpy as np
import time

HOST = '192.168.1.91'
PORT = 65432

CV_CAP_PROP_FRAME_WIDTH = 3
CV_CAP_PROP_FRAME_HEIGHT = 4
CV_CAP_PROP_BUFFERSIZE = 21
CV_IMWRITE_JPEG_QUALITY = 1

jpg_params = [CV_IMWRITE_JPEG_QUALITY,60]

def get_encoded_frame(cap):
    # Get and encode frame
    ret, frame = cap.read()
    frame = cv2.resize(frame, (150,200))
    encoded = cv2.imencode('.jpg', frame, jpg_params)
    size = len(encoded[1])
    return size, encoded[1].tobytes()

# Start video stream
cap = cv2.VideoCapture(0)
cap.set(CV_CAP_PROP_FRAME_HEIGHT, 240)
cap.set(CV_CAP_PROP_FRAME_WIDTH, 320)
cap.set(CV_CAP_PROP_BUFFERSIZE, 1)
     
# Start socket
while True:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR,1)
        s.bind((HOST, PORT))
        s.listen()
        conn, addr = s.accept()
        with conn:
            print('Connected by', addr)
            while True:
                try:
                    start = time.time()
                    #print("Start")
                    
                    data = conn.recv(4)
                    if not data:
                        break
                    #print(data)
                    ack = time.time()
                    #print("Ack in " + str(ack-start))
                    
                    sz, jpg = get_encoded_frame(cap)
                    msg = str(sz)
                    msg = bytes(msg + '\r\n', 'UTF-8')
                    print(msg)
                    conn.sendall(msg)
                    frsize = time.time()
                    #print("Frame and send in " + str(frsize-ack))
                    
                    data = conn.recv(10)
                    #print(data)
                    conf = time.time()
                    #print("Size confirmation in " + str(conf-frsize))

                    conn.sendall(jpg)
                    imgsent = time.time()
                    #print("Image sent in " + str(imgsent - conf))
                except:
                    print("Error (or disconnect), restarting")