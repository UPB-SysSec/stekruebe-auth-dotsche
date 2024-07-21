NAME=$(echo $1 | tr / _)

if [ -z "$(sudo docker ps -f NAME=$NAME -q 2> /dev/null)" ]; then
  echo "unable to find container called '$NAME'"
  exit 1
fi

sudo docker exec -it $NAME sh