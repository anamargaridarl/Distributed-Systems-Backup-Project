#!/bin/bash
TERMINAL=$1
DIMENSIONS=$(xdpyinfo | grep dimensions | sed -r 's/^[^0-9]*([0-9]+x[0-9]+).*$/\1/')
WIDTH=$(echo "$DIMENSIONS" | sed -r 's/x.*//')
echo "Launching Initial Peer"
cd src
$TERMINAL -e "rmiregistry " &
cd ../test/peer1
$TERMINAL --geometry 118x28+$((3*$WIDTH/4))+0 -e "java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 -cp src base.PeerInitial 1 peer1 5000" &
