#!/bin/sh
java -Xss768k -Dbns.home=$PWD/`dirname $0` -Drestartcommmand=start.sh -jar botsnscouts.jar
