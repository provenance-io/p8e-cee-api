#!/bin/sh
set -e

if [ -d "/vault/secrets" ]; then
  for env_file in $(find /vault/secrets -name '*.env'); do
    . ${env_file}
  done
fi

# Create the trust store and key store for java
if [ "$KAFKA_SECURITY_PROTOCOL" != "" ]; then
  echo -n ${KAFKA_TRUSTSTORE} | base64 -d > ${KAFKA_TRUSTSTORE_PATH}
  echo -n ${KAFKA_KEYSTORE} | base64 -d > ${KAFKA_KEYSTORE_PATH}
fi

if [ "$JMX_ENABLED" = true ]; then
    JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=7199 -Dcom.sun.management.jmxremote.rmi.port=7199 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=$POD_IP -Dcom.sun.management.jmxremote.local.only=false"
fi

java $JAVA_OPTS -jar $1
