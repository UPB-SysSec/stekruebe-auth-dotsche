# Auth Dotsche
This artifact is a test suite for the handling of TLS session by various web servers. 
We analyze how client authentication behavior changes under session resumption when modifying SNI and host headers.

It is responsible for the results presented in Section 4, Table 2 of the paper.

## Usage
The artifact is a packaged in Docker using Docker-in-docker for convenience.

### Running
Simply run:
```sh
./run.sh
```
the script will:
1. prepare the execution environment of the test framework as a Docker container
2. execute the test framework container
3. the framework starts new Docker containers for the analyzed web servers
4. copy the HTML table to `./out`
