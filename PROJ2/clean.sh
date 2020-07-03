#!/bin/bash

echo "Cleaning class files"
rm -rf src/base/*.class
rm -rf src/base/*/*.class
rm -rf $(find -name \*.class) test/peer1/base/
rm -rf $(find -name \*.class) test/peer2/base/
rm -rf $(find -name \*.class) test/peer3/base/
rm -rf $(find -name \*.class) test/client/base/
# testing
rm -f $(find -name \*.ser) /test
