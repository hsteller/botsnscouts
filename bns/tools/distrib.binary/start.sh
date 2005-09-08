#!/bin/sh
java -Xss768k -Dbns.home=$PWD/`dirname $0` -jar botsnscouts.jar
