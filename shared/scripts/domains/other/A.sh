clear;
echo "GET / HTTP/1.1\r\nHost: siteA.org\r\n\r\n" | timeout 0.1 openssl s_client -tls1_3 -connect siteA.org:443 -state -brief -quiet -sess_out ticket.pem #-CAfile shared/cert/keys/ca_s.crt

echo "-------------------------------------------------------------"

echo "GET / HTTP/1.1\r\nHost: siteA.org\r\n\r\n" | timeout 0.1 openssl s_client -tls1_3 -connect siteA.org:443 -state -brief -quiet -sess_in ticket.pem #-CAfile shared/cert/keys/ca_s.crt
rm ticket.pem