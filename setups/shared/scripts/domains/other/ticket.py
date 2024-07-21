import socket
import ssl

HOST = "localhost"
PORT = 1444


context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT) 
#context.verify_mode = ssl.CERT_OPTIONAL  # this should not be necessary
context.check_hostname = False # we are just working on localhost, this should be fine

context.load_verify_locations(cafile='../keys/ca.crt')

sock = socket.socket()
#sock.connect((HOST, PORT))

with context.wrap_socket(sock, server_hostname=HOST) as ssock:
    ssock.connect((HOST, PORT))
    ssock.do_handshake(block=True)
    #print(ssock.version())
    print(ssock.session.has_ticket)
    print(ssock.getpeercert())