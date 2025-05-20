docker build -t authdotsche . -f dockerfile
docker run --rm -v ./out:/code/out --privileged --name authdotsche authdotsche:latest