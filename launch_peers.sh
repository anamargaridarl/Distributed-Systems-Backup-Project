#!/bin/bash
TERMINAL=$1
DIMENSIONS=$(xdpyinfo | grep dimensions | sed -r 's/^[^0-9]*([0-9]+x[0-9]+).*$/\1/')
WIDTH=$(echo "$DIMENSIONS" | sed -r 's/x.*//')
HEIGHT=$(echo "$DIMENSIONS" | sed -r 's/.*x//')
echo "Launching Peers"
cd test/client
$TERMINAL --geometry 118x28+$(($WIDTH/2))+0 &
cd ../peer2
$TERMINAL --geometry 118x28+$(($WIDTH/2))+$(($HEIGHT/2+10)) -e "java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 -cp src base.PeerInitial 2 peer2 5001 localhost 5000"  &
cd ../peer3
$TERMINAL --geometry 118x28+$((3*$WIDTH/4))+$(($HEIGHT/2+10)) -e "java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 -cp src base.PeerInitial 3 peer3 5002 localhost 5000" &
