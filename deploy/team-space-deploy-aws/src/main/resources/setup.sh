#!/bin/bash

#install java
log=/home/$user$/deployer.log
echo "Started JRE download at: "$(date +"%T") >> $log
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jre-8u131-linux-x64.rpm"
echo "Completed JRE download at: "$(date +"%T") >> $log
yum -y localinstall jre-8u131-linux-x64.rpm
echo "Completed JRE install at: "$(date +"%T") >> $log

#run team space app
java -Ddw.server.applicationConnectors[1].keyStorePath=./keys/keystore.jks -jar team-space-server-1.0-SNAPSHOT.jar server config.yml
echo "Started the application at: "$(date +"%T") >> $log