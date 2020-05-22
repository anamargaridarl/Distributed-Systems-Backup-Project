### Client

* java base.TestApp peer1 BACKUP $PWD/test.txt 2

* java base.TestApp peer1 RESTORE $PWD/test.txt

* java base.TestApp peer1 DELETE $PWD/test.txt

* java base.TestApp peer1 RECLAIM 0

* java base.TestApp peer1 STATE

### Peer
#### Running
* java base.PeerInitial 1.0 1 peer1 239.255.255.255 239.255.255.254 239.255.255.253 5501 5502 5503

* java base.PeerInitial 1.0 2 peer2 239.255.255.255 239.255.255.254 239.255.255.253 5501 5502 5503

* java base.PeerInitial 1.0 3 peer3 239.255.255.255 239.255.255.254 239.255.255.253 5501 5502 5503

* java base.PeerInitial 2.0 1 peer1 239.255.255.255 239.255.255.254 239.255.255.253 5501 5502 5503

* java base.PeerInitial 2.0 2 peer2 239.255.255.255 239.255.255.254 239.255.255.253 5501 5502 5503

* java base.PeerInitial 2.0 3 peer3 239.255.255.255 239.255.255.254 239.255.255.253 5501 5502 5503

### Snooper

* java -jar McastSnooper.jar  239.255.255.255:5501 239.255.255.254:5502 239.255.255.253:5503


###Project2 

* java base.TestApp peer1 BACKUP $PWD/test.txt 2

* java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 base.PeerInitial 1.0 1 peer1 5000


* java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 base.PeerInitial 1.0 2 peer2 5001

* java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 base.PeerInitial 1.0 3 peer3 5002

* java -Djavax.net.ssl.keyStore=keys/server.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore  -Djavax.net.ssl.trustStorePassword=123456 base.PeerInitial 1.0 4 peer4 5003






