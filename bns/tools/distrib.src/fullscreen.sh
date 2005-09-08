#!/bin/sh
########################
# FullScreen-Starter
########################
# use this script to start Bots'n'Scouts in
# fullscreen modus on Display :1
#######################
# this script can also be started from the
# text console
######################
# note that you need to have xterm installed
# in order to use this mode
######################
# in some Linux distributions you can start this
# mode only as root.
# see also README.fullscreen
# ------------ apply to your enviroment --------
# X-Display that will be used
ONDISPLAY=1 
BASENAME=$PWD/`dirname $0`
CLASSPATH=$BASENAME/classes:$BASENAME/lib/sixlegs.jar:$BASENAME/lib/log4j.jar:$BASENAME/lib/jakarta-regexp-1.2.jar:$BASENAME/lib/pngenc.jar
# where your java-VM is
JAVAPATH=/usr/local/jdk1.4/bin 
# ----------- don't change anything from here on
xinit -bg black -fg green -j -e sh -c "xsetroot -solid black; $JAVAPATH/java -Xss768k -cp $CLASSPATH de.botsnscouts.BotsNScouts" -- :$ONDISPLAY  

