#!/bin/sh
set -ex

KEYSTORE=/app/my-keystore.jks
STOREPASS=mypassword

echo "$CUSTOM_CA_CERTIFICATE" | sed 's/\\n/\n/g' > /tmp/ca-chain.pem

if [ -n "$CUSTOM_CA_CERTIFICATE" ]; then
	# Split and import each cert
	i=0
	csplit -z -f /tmp/cert- /tmp/ca-chain.pem '/-----BEGIN CERTIFICATE-----/' '{*}' 2>/dev/null

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
