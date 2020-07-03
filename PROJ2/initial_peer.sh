#!/bin/bash
TERMINAL=$1
echo "Launching Initial Peer"
cd src
$TERMINAL -e "rmiregistry " &
cd ../test/peer1
$TERMINAL -e "java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 -cp src base.PeerInitial 1 peer1 5000" &
