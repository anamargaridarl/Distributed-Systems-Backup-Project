#!/bin/bash

echo "Compiling and building test setup"
mkdir -p {test/peer1,test/peer2,test/peer3,test/peer4,test/client}
javac -cp src src/base/PeerInitial.java src/base/TestApp.java
cp --parents $(find ./src -name \*.class) test/peer1/
cp --parents $(find ./src -name \*.class) test/peer2/
cp --parents $(find ./src -name \*.class) test/peer3/
cp --parents $(find ./src -name \*.class) test/peer4/
cp --parents $(find ./src -name \TestApp.class && find ./src -name \PeerInterface.class) test/client/

cp -R keys test/peer1/
cp -R keys test/peer2/
cp -R keys test/peer3/
cp -R keys test/peer4/