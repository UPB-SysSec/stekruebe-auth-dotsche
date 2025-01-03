docker kill olsdomains
docker rm olsdomains
docker build -q -t closedlitespeed_subdomains -f ../setups/closedlitespeed/subdomains/dockerfile ../setups
docker run -d --name olsdomains -p 4433:443 -p 7081:7080 closedlitespeed_subdomains
docker exec -it olsdomains sh -c "echo -n admin: > /usr/local/lsws/admin/conf/htpasswd"
docker exec -it olsdomains sh -c "/usr/local/lsws/admin/fcgi-bin/admin_php5 -q /usr/local/lsws/admin/misc/htpasswd.php adminadmin >> /usr/local/lsws/admin/conf/htpasswd"
docker exec -it olsdomains sh
# clear
docker logs olsdomains
docker rm olsdomains
