#!/bin/bash
on_end () {
  printf '! cancelled\n'
  exit
}
trap "on_end" SIGINT 

DIR=$1
NAME=$2

printf "# name = $DIR\n"
printf "# dir = $NAME\n"

if [ ! -f $DIR/dockerfile ]; then
    printf "\n! unable to find dockerfile or directory '$DIR'\n"
	printf "Hint:\n  usage: \"./scripts/rebuild_run.sh \$DIR \$NAME\"\n"
	exit
fi

sudo docker stop $NAME
sudo docker rmi $NAME
#ln -sf ../../cert $DIR/cert
#ln -sf ../../site-content $DIR/site-content
#ls -l $DIR/cert
set -e
sudo docker build -t $NAME -f $DIR/dockerfile .
sudo docker run -it --rm -p 443-450:443-450 --name $NAME $NAME