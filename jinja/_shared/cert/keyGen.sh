cd "$(dirname "$0")"

# recreate certificates folder
mkdir keys
cp ./client_cert_ext.cfg keys/
rm -f ./keys/*.key ./keys/*.crt ./keys/*.pfx ./keys/*.pem
cd keys

# --- create key authorities a and b and s ...
echo "#creating Authority Certs"
openssl genrsa -out user_ca_a.key 4096
openssl req -nodes -new -x509 -days 365 -key user_ca_a.key -out user_ca_a.crt -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=researchCompanyA"
cat user_ca_a.crt user_ca_a.key > user_ca_a.pem

openssl genrsa -out user_ca_b.key 4096
openssl req -nodes -new -x509 -days 365 -key user_ca_b.key -out user_ca_b.crt -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=researchCompanyB"
cat user_ca_b.crt user_ca_b.key > user_ca_b.pem

openssl genrsa -out ca_s.key 4096
openssl req -nodes -new -x509 -days 365 -key ca_s.key -out ca_s.crt -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=researchCompanyS"
cat ca_s.crt ca_s.key > ca_s.pem


# --- create server certificates ---
echo "#creating Server Certs"
openssl genrsa -out siteA.key 4096 # create siteA key
openssl genrsa -out siteB.key 4096 # create siteB key
openssl genrsa -out siteC.key 4096 # create siteB key

# create domainA cert
openssl req -key siteA.key -new -out siteA.csr -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=siteA.org"  # create signing request
openssl x509 -req -days 365 -in siteA.csr -CA ca_s.crt -CAkey ca_s.key -set_serial 01 -out siteA.crt    # have the authority sign the request
cat siteA.crt siteA.key > siteA.pem
# create domainA cert
openssl req -key siteB.key -new -out siteB.csr -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=siteB.org"  # create signing request
openssl x509 -req -days 365 -in siteB.csr -CA ca_s.crt -CAkey ca_s.key -set_serial 01 -out siteB.crt    # have the authority sign the request
cat siteB.crt siteB.key > siteB.pem

openssl req -key siteC.key -new -out siteC.csr -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=siteC.org"  # create signing request
openssl x509 -req -days 365 -in siteC.csr -CA ca_s.crt -CAkey ca_s.key -set_serial 01 -out siteC.crt    # have the authority sign the request
cat siteC.crt siteC.key > siteC.pem

# create subdomainA cert
openssl req -key siteA.key -new -out subdomainA.csr -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=siteA.site.org"  # create signing request
openssl x509 -req -days 365 -in subdomainA.csr -CA ca_s.crt -CAkey ca_s.key -set_serial 01 -out subdomainA.crt    # have the authority sign the request
cat subdomainA.crt siteA.key > subdomainA.pem
# create subdomainB cert
openssl req -key siteB.key -new -out subdomainB.csr -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=siteB.site.org"  # create signing request
openssl x509 -req -days 365 -in subdomainB.csr -CA ca_s.crt -CAkey ca_s.key -set_serial 01 -out subdomainB.crt    # have the authority sign the request
cat subdomainB.crt siteB.key > subdomainB.pem
# create subdomainC cert
openssl req -key siteC.key -new -out subdomainC.csr -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=siteC.site.org"  # create signing request
openssl x509 -req -days 365 -in subdomainC.csr -CA ca_s.crt -CAkey ca_s.key -set_serial 01 -out subdomainC.crt    # have the authority sign the request
cat subdomainC.crt siteC.key > subdomainC.pem

# create site cert (for differentiation by path)
openssl req -key siteA.key -new -out siteAB.csr -subj "/C=DE/ST=/L=researchCity/O=/OU=/CN=siteAB.org"  # create signing request
openssl x509 -req -days 365 -in siteAB.csr -CA ca_s.crt -CAkey ca_s.key -set_serial 01 -out siteAB.crt    # have the authority sign the request
cat siteAB.crt siteA.key > siteAB.pem


# --- create client certificates ---
echo "#creating client Cert"
# the client would only ever have a certificate from user_ca_a, never from user_ca_b
# we create the second certificate for testing purposes
openssl genrsa -out clientA.key 4096
openssl req -new -nodes -key clientA.key -out clientA.csr -subj "/C=DE/ST=/L=researchCity/O=researchCompanyA/OU=/CN=clientA"
openssl x509 -req -days 365 -in clientA.csr -CA user_ca_a.crt -CAkey user_ca_a.key -set_serial 01 -out clientA.crt -CAcreateserial -extfile client_cert_ext.cfg
cat clientA.crt clientA.key > clientA.pem
#openssl pkcs12 -export -out clientA.pfx -inkey clientA.key -in clientA.crt -certfile user_ca_a.crt -passout pass: # create pkcs file
# client cert for site B
openssl genrsa -out clientB.key 4096
openssl req -new -nodes -key clientB.key -out clientB.csr -subj "/C=DE/ST=/L=researchCity/O=researchCompanyB/OU=/CN=clientB"
openssl x509 -req -days 365 -in clientB.csr -CA user_ca_b.crt -CAkey user_ca_b.key -set_serial 01 -out clientB.crt -CAcreateserial -extfile client_cert_ext.cfg
cat clientB.crt clientB.key > clientB.pem
#openssl pkcs12 -export -out clientB.pfx -inkey clientB.key -in clientB.crt -certfile user_ca_b.crt -passout pass: # create pkcs file


# --- cleanup ---
rm *.csr #remove cert signing request files
rm ./client_cert_ext.cfg #removed copy of client_cert_ext.cfg

# --- check ---
echo "--- check ---"
openssl verify -CAfile user_ca_a.pem clientA.pem
openssl verify -CAfile user_ca_b.pem clientB.pem
openssl verify -CAfile ca_s.pem siteA.pem
openssl verify -CAfile ca_s.pem siteB.pem
echo "--- the following 2 should be invalid: ---"
openssl verify -CAfile user_ca_a.pem clientB.pem
openssl verify -CAfile user_ca_b.pem clientA.pem
echo "--- done ---"