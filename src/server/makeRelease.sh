#!/bin/sh
srcRoot=$PWD/..

echo Compiling...
echo '[CORE]'
cd $srcRoot/core
mvn install > /dev/null

echo '[SERVER]'
cd $srcRoot/server
serverVersion=`mvn clean | grep "Building" | awk '{print $4}'`
mvn compile assembly:single > /dev/null
mvn -Dmaven.test.skip=true install > /dev/null
serverTarget=$PWD/BitcoinTrader/server-$serverVersion
mkdir -p $serverTarget
cp target/*with-dependencies.jar $serverTarget/server.jar
git archive HEAD ./log  | tar -x -C $serverTarget

echo '[CLIENTS]'
echo ' - Text'
cd $srcRoot/client
clientVersion=`mvn clean | grep "Building" | awk '{print $4}'`
mvn -Dmaven.test.skip=true compile assembly:single > /dev/null
cp target/*.jar $serverTarget/../client-$clientVersion.jar
echo ' - Swing'
cd $srcRoot/swingClient
clientVersion=`mvn clean | grep "Building" | awk '{print $4}'`
mvn -Dmaven.test.skip=true compile assembly:single > /dev/null
cp target/*with-dependencies.jar $serverTarget/../swingClient-$clientVersion.jar

echo '[PLUGINS]'
echo ' - Scalper'
mkdir -p $serverTarget/plugins/scalper/conf
mkdir -p $serverTarget/plugins/scalper/data
mkdir -p $serverTarget/plugins/scalper/log
cd $srcRoot/plugins/scalper
mvn clean > /dev/null
mvn -Dmaven.test.skip=true package > /dev/null
cp target/*.jar $serverTarget/plugins/scalper
cp $srcRoot/server/db/* $serverTarget/plugins/scalper/data
cd $serverTarget/plugins/scalper/data
./resetDB.sh

echo ' - EMA Scalper'
mkdir -p $serverTarget/plugins/emaScalper/conf
mkdir -p $serverTarget/plugins/emaScalper/data
mkdir -p $serverTarget/plugins/emaScalper/log
cd $srcRoot/plugins/emaScalper
mvn clean > /dev/null
mvn -Dmaven.test.skip=true package > /dev/null
cp target/*.jar $serverTarget/plugins/emaScalper
cp $srcRoot/server/db/* $serverTarget/plugins/emaScalper/data
cd $serverTarget/plugins/emaScalper/data
./resetDB.sh

echo ' - ExchangeUtils'
mkdir -p $serverTarget/plugins/exchangeUtil/log
cd $srcRoot/plugins/exchangeUtil
mvn clean > /dev/null
mvn -Dmaven.test.skip=true package > /dev/null
cp target/*.jar $serverTarget/plugins/exchangeUtil


# Back to the current dir
cd $srcRoot
# Create run scripts
echo '#!/bin/sh' > $serverTarget/startRmiRegistry.sh
echo 'CLASSPATH=server.jar rmiregistry $* &' > $serverTarget/startRmiRegistry.sh
chmod +x $serverTarget/startRmiRegistry.sh
