#!/bin/bash

#run team space app
java -Ddw.server.applicationConnectors[1].keyStorePath=./keys/keystore.jks -jar team-space-server-1.0-SNAPSHOT.jar server config.yml>/dev/null &