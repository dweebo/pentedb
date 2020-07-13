#!/bin/bash
cd ../build
rm -r target
ant
mv pentedb.jar ../mac_os
cd ../resources/pro
if [ -d ../../mac_os/db ] ;
then
  echo db exists, leaving..
  rm -rf ./db
else
  java -cp "../../mac_os/pentedb.jar:../../lib/*" org.pente.gameDatabase.swing.install.FirstInstall
  rm -rf ../../mac_os/db
  mv db ../../mac_os
fi
cd ..
cp -r images ../mac_os
cd ../mmai64
g++ -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -shared CPoint.c Ai.cpp AiWrapper.cpp -o libAi.jnilib
mv libAi.jnilib ../mac_os
cd ../mmai
cp pente.scs ../mac_os
cp pente.tbl ../mac_os
cd ../mac_os

