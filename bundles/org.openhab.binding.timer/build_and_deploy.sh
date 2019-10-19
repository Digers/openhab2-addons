#!/usr/bin/bash
mvn clean package && cp target/org.openhab.binding.timer-2.5.0-SNAPSHOT.jar ~/openhab/addons
