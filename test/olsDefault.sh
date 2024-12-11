docker kill olsdefault
docker rm olsdefault
docker run -d --name olsdefault -p 4433:443 -p 7081:7080 litespeedtech/litespeed:latest

docker cp ../setups/shared/cert/keys/ olsdefault:/keys/
docker cp ../setups/site-content/siteA olsdefault:/srv/
docker cp ../setups/site-content/siteB olsdefault:/srv/

docker exec -it olsdefault sh -c "echo -n admin: > /usr/local/lsws/admin/conf/htpasswd"
docker exec -it olsdefault sh -c "/usr/local/lsws/admin/fcgi-bin/admin_php5 -q /usr/local/lsws/admin/misc/htpasswd.php adminadmin >> /usr/local/lsws/admin/conf/htpasswd"

docker exec -it olsdefault sh
# clear
docker logs olsdefault
docker rm olsdefault
