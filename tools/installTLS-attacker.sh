cd attacker
git clone https://github.com/tls-attacker/TLS-Attacker.git
cd TLS-Attacker
export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")
mvn clean install -DskipTests=true