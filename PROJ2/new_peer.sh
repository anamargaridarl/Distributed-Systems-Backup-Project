#!/bin/bash
TERMINAL=$1
echo "Launching New Peer"
cd test/peer4
$TERMINAL -e "java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 -cp src base.PeerInitial 4 peer4 5003 localhost 5000"
