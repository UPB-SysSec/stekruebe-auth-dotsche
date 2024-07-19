if [ ! -d "$1" ]; then
  echo "unable to find folder at '$1'"
  exit 1
fi

NAME=$(echo $1 | tr / _)
sudo docker build -q -t $NAME -f "$1/dockerfile" .
