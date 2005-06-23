#!/bin/sh
java -Xss640k -Dbns.home=$PWD/`dirname $0` -jar botsnscouts.jar
