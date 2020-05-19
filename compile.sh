#!/bin/bash

echo "Compiling and building test setup"
mkdir -p {test/peer1,test/peer2,test/peer3,test/peer4,test/client}
cd src
javac base/*.java
javac base/*/*.java
cp --parents $(find -name \*.class) ../test/peer1/
cp --parents $(find -name \*.class) ../test/peer2/
cp --parents $(find -name \*.class) ../test/peer3/
cp --parents $(find -name \*.class) ../test/peer4/
cp --parents $(find -name \TestApp.class && find -name \PeerInterface.class) ../test/client/