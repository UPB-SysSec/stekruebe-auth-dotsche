docker kill olsdomains
docker run --rm -d --name olsdomains -p 4433:443 -p 7081:7080  openlitespeed_domains
docker exec -it olsdomains sh -c "echo -n admin: > /usr/local/lsws/admin/conf/htpasswd"
docker exec -it olsdomains sh -c "/usr/local/lsws/admin/fcgi-bin/admin_php -q /usr/local/lsws/admin/misc/htpasswd.php adminadmin >> /usr/local/lsws/admin/conf/htpasswd"