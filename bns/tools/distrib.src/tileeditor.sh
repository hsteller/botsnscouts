#!/bin/sh
BASEDIR=$PWD/`dirname $0`
java -Dbns.home=$BASEDIR -classpath $BASEDIR/classes:$BASEDIR/lib/sixlegs.jar:$BASEDIR/lib/log4j.jar:$BASEDIR/lib/jakarta-regexp-1.2.jar de.botsnscouts.board.KachelEditor
