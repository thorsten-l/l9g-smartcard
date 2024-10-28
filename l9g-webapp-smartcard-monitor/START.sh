#!/bin/bash

export JAVA_HOME=`/usr/libexec/java_home -v 21`
export PATH=$JAVA_HOME/bin:$PATH

mvn clean package

rm -f smartcard-monitor.log
java -jar target/l9g-webapp-smartcard-monitor.jar
