# https://fardog.io/blog/2017/12/30/client-side-certificate-authentication-with-nginx/

# recreate certificates folder
rm -rf ./keys
mkdir keys
cd keys

# --- create authority certificates ---
echo "#creating Authority Certs"
# create server master key
openssl genrsa -out ca.key 4096
# create server cert authority
openssl req -new -x509 -days 365 -key ca.key -out ca.crt -subj "/C=DE/ST=/L=exampleCity/O=/OU=/CN=example"


# --- create server certificates ---
echo "#creating Server Certs"
# create siteA key
openssl genrsa -out siteA.key 4096
openssl req -key siteA.key -new -out siteA.csr -subj "/C=DE/ST=/L=exampleCity/O=/OU=/CN=siteA.example.org"  # create signing request
openssl x509 -req -days 365 -in siteA.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out siteA.crt    # have the authority sign the request
# create siteB key
openssl genrsa -out siteB.key 4096
openssl req -key siteB.key -new -out siteB.csr -subj "/C=DE/ST=/L=exampleCity/O=/OU=/CN=siteB.example.org"  # create signing request
openssl x509 -req -days 365 -in siteB.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out siteB.crt    # have the authority sign the request


# --- create client certificates ---
echo "#creating client Certs"
# create server master key
openssl genrsa -out clientA.key 4096
openssl req -new -key clientA.key -out clientA.csr -subj "/C=DE/ST=/L=exampleCity/O=/OU=/CN=clientA" # create signing request
openssl x509 -req -days 365 -in clientA.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out clientA.example.crt # sign the client certificate

# create pkcs files
openssl pkcs12 -export -out clientA.pfx -inkey clientA.key -in clientA.crt -certfile ca.crt -passout pass: