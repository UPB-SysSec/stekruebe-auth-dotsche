import socket
import ssl

hostname = 'localhost'
port = 1444
resource = '/'

context = ssl.SSLContext(ssl.PROTOCOL_TLSv1_2)

sock = socket.create_connection((hostname, port))
ssock = context.wrap_socket(sock, server_hostname=hostname)

#send - receive

ssr = ssock.session
print(ssock.session_reused) # False
ssock.close()

# Connecting Again
# Here we're using the previous session ssr in wrap_socket()

sock = socket.create_connection((hostname, port))
ssock = context.wrap_socket(sock, server_hostname=hostname, session=ssr)

#send - receive

print(ssock.session_reused) # True if server supports session resumption otherwise False
print(ssock.session.has_ticket)
print(ssock.session.id)
ssock.close()