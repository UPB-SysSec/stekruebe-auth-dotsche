docker build -t authdotsche . -f Dockerfile
docker run --rm -v ./out:/code/out --privileged --name authdotsche authdotsche:latest
