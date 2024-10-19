curl -I --tlsv1.3 --insecure --no-progress-meter https://siteB.org:4433 --cert shared/cert/keys/clientB.crt --key "shared/cert/keys/clientB.key" --resolve siteB.org:4433:127.0.0.1
