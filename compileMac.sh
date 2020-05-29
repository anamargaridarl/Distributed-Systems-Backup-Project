#!/usr/bin/env bash

echo "Compiling and building test setup"
mkdir -p test/peer1/base test/peer2/base test/peer3/base test/peer4/base test/client/base
javac -cp src src/base/PeerInitial.java src/base/TestApp.java
cp -p $(find $PWD -name \*.class) test/peer1/base/
cp -p $(find $PWD -name \*.class) test/peer2/base/
cp -p $(find $PWD -name \*.class) test/peer3/base/
cp -p $(find $PWD -name \*.class) test/peer4/base/
cp -p $(find $PWD -name \TestApp.class && find "." -name \PeerInterface.class) test/client/base/

cp -R keys test/peer1/
cp -R keys test/peer2/
cp -R keys test/peer3/
cp -R keys test/peer4/
