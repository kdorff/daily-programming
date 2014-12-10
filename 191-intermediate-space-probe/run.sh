#!/bin/bash

export JAVA_OPTS="$JAVA_OPTS -Xss1g"
groovy spaceprobe.groovy 2>&1
