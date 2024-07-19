NAME=$(echo $1 | tr / _)
echo $NAME
if [ -z "$(sudo docker images -q "$NAME" 2> /dev/null)" ]; then
  echo "unable to find image called '$NAME'"
  exit 1
fi

sudo docker run -t --rm -d -p 80:80 -p 443:443 --name "$NAME" "$NAME"
#docker run --rm -d --name "$NAME" "$NAME"
