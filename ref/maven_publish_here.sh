#!/bin/sh
current=`pwd`
version="1.9.0-GOLLUM"
xchangeDir="$current/../../xchange"
core="$xchangeDir/xchange-core"
bitstamp="$xchangeDir/xchange-bitstamp"
btce="$xchangeDir/xchange-btce"
mtgox="$xchangeDir/xchange-mtgox"
btcchina="$xchangeDir/xchange-btcchina"
rescu="$current/../../rescu"
rescuVer="1.5.1-GOLLUM"

#cd $rescu
#mvn install
#mvn deploy:deploy-file -Durl=file://$current -Dfile=target/rescu-$rescuVer.jar -DgroupId=com.github.mmazi -DartifactId=rescu -Dpackaging=jar -Dversion=$rescuVer

#cd $core
#mvn install
#mvn deploy:deploy-file -Durl=file://$current -Dfile=target/xchange-core-$version.jar -DgroupId=com.xeiam.xchange -DartifactId=xchange-core -Dpackaging=jar -Dversion=$version

#cd $bitstamp
#mvn install
#mvn deploy:deploy-file -Durl=file://$current -Dfile=target/xchange-bitstamp-$version.jar -DgroupId=com.xeiam.xchange -DartifactId=xchange-bitstamp -Dpackaging=jar -Dversion=$version

#cd $btce
#mvn install
#mvn deploy:deploy-file -Durl=file://$current -Dfile=target/xchange-btce-$version.jar -DgroupId=com.xeiam.xchange -DartifactId=xchange-btce -Dpackaging=jar -Dversion=$version

cd $btcchina
mvn install
mvn deploy:deploy-file -Durl=file://$current -Dfile=target/xchange-btcchina-$version.jar -DgroupId=com.xeiam.xchange -DartifactId=xchange-btcchina -Dpackaging=jar -Dversion=$version

#cd $mtgox
#mvn install
#mvn deploy:deploy-file -Durl=file://$current -Dfile=target/xchange-mtgox-$version.jar -DgroupId=com.xeiam.xchange -DartifactId=xchange-mtgox -Dpackaging=jar -Dversion=$version

cd $current
