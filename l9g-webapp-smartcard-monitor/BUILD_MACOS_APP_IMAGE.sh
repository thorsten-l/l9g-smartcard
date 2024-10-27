#!/bin/bash

export JAVA_HOME=`/usr/libexec/java_home -v 21`
export PATH=$JAVA_HOME/bin:$PATH


mvn clean package

mkdir -p private/installer
cp config.yaml private/installer
cp target/l9g-webapp-smartcard-monitor.jar private/installer

cd private/installer
rm -fr ../smartcard-monitor.app

jpackage --input . \
  --dest .. \
  --name smartcard-monitor \
  --main-jar l9g-webapp-smartcard-monitor.jar \
  --main-class l9g.webapp.smartcardmonitor.Application \
  --type app-image \
  --vendor "l9g" \
  --java-options '-jar $APPDIR/l9g-webapp-smartcard-monitor.jar' \
  --app-version 1.0.0 \
  --mac-package-identifier l9g.webapp.smartcardmonitor \
  --mac-package-name SC-Monitor
