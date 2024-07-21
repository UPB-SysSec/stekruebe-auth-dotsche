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
run `python3 main.py` 
(needs permission to build and start docker containers)

