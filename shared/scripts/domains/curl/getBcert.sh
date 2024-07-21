curl -I --tlsv1.3 --insecure --no-progress-meter https://siteB.org --cert shared/cert/keys/clientB.crt --key "shared/cert/keys/clientB.key" --resolve siteB.org:443:127.0.0.1
