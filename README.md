## Setup
- expects docker, openSSL(optional) and curl(optional)
- expects TLS-Attacker
```sh
cd attacker
git clone https://github.com/tls-attacker/TLS-Attacker.git
cd TLS-Attacker
mvn clean install -DskipTests=true
```


## Running
Simply run:
```sh
./run.sh
```
the script will:
1. prepare the execution environment of the test framework as a docker container
2. execute the test framework container
3. the framework starts new docker containers for the analyzed web servers
