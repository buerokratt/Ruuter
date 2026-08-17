#!/bin/sh
set -ex

KEYSTORE=/app/my-keystore.jks
STOREPASS=mypassword
DEFAULT_CACERTS=$(find $JAVA_HOME -name "cacerts" 2>/dev/null | head -1)

if [ -n "$CUSTOM_CA_CERTIFICATE" ]; then
    # Start from JVM's default cacerts so public HTTPS still works
    cp "$DEFAULT_CACERTS" "$KEYSTORE"

    # Change default password ("changeit") to our password
    keytool -storepasswd \
        -keystore "$KEYSTORE" \
        -storepass changeit \
        -new "$STOREPASS"

    # Convert literal \n to real newlines and split into individual certs
    echo "$CUSTOM_CA_CERTIFICATE" | sed 's/\\n/\n/g' > /tmp/ca-chain.pem
    csplit -z -f /tmp/cert- /tmp/ca-chain.pem '/-----BEGIN CERTIFICATE-----/' '{*}' 2>/dev/null

    i=0
    for cert in /tmp/cert-*; do
        keytool -importcert \
            -noprompt \
            -alias "custom-ca-$i" \
            -keystore "$KEYSTORE" \
            -storepass "$STOREPASS" \
            -file "$cert"
        i=$((i + 1))
    done

    echo "Imported $i certificate(s) into $KEYSTORE"
fi

exec "$@"
