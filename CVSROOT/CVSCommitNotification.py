#!/usr/bin/env python
####
# Copyright (c) 2000-2002 Bill Bumgarner, Christian Pekeler; CodeFab.
# Copyright (c) 2003 Charles Swiger; PKIx.  All rights reserved.  C=US, L=NY.
#
# Please consult the License file. 
####
# 
# The python code was written by Bill Bumgarner <bbum@codefab.com>.  It
# was a port of two nearly identical WebScript scripts written by
# Christian Pekeler <pekeler@codefab.com>.
#
# If you fix bugs, make improvements, or find something better than
# CVS, *please* contact <bbum@codefab.com> with more information!
#
# Added changes from Dirk Olmes <dirk.olmes@pixelpark.com> that
# beautify the emails a bit.
#
# Christian Pekeler <pekeler@codefab.com> - 05/15/2002 now works with
# multiple target emails (need to be comma separated), also changed
# first line to find python

import sys
import os
import tempfile
import string
import pwd
import time

sys.path.append(os.environ['CVSROOT'] + os.sep + "CVSROOT" + os.sep)

from CVSCommitConfiguration import *

def findFirstValue(d, a):
    for t in a:
        if d.has_key(t):
            return d[t]

try:
    import getpass
    def userName():
        return getpass.getuser()
except:
    # if getpass module not available
    # see getuser() documentation
    import pwd # will raise on Windows.  Oh well. This code is crap anyway.
    def userName():
        possibleName = findFirstValue(os.environ, ['LOGNAME', 'USER', 'LNAME', 'USERNAME'])
        if not (possibleName == None):
            return possibleName
        pwEntry = pwd.getpwuid(os.getuid())
        return pwEntry[0]

def fullUserName():
    pwEntry = pwd.getpwuid(os.getuid())
    return pwEntry[4]

if tempfile.tempdir == None:
    possibleTmp = findFirstValue(os.environ, ['TMPDIR', 'TMP', 'TEMP'])
    if not (possibleTmp == None):
        tempfile.tempdir = possibleTmp
    elif os.name == 'posix':
        tempfile.tempdir = '/tmp'
    elif (os.name == 'nt') or (os.name == 'dos'):
        tempfile.tempdir = 'c:\temp'
    else:
        raise SystemError, 'Failed to figure out where the tmp/temp directory is.'

def pathForLastUsedDirFile():
    return os.path.join(tempfile.tempdir, 'cvs-lastdir-' + userName());

def pathForCommonPathFile():
    return os.path.join(tempfile.tempdir, 'cvs-commonpath-' + userName());

def pathForAccumulationFile():
    return os.path.join(tempfile.tempdir, 'cvs-notifaccu-' + userName());    

def commonPath():
    path = pathForCommonPathFile()
    if path and os.path.exists(path):
        return open(path).read()
    else:
        return None

def lastStoredPath():
    path = pathForLastUsedDirFile()
    if path and os.path.exists(path):
        return open(path).read()
    else:
        return None

def logStringIsForLastNotification(aShortDescription):
    lSP = lastStoredPath()
    logStringLines = string.split(aShortDescription, '\n')
    if ( lSP ) and (logStringLines) and len(logStringLines):
        pathOfLogString = string.split(logStringLines[0], ' ')[-1]
        return (pathOfLogString == lSP);
    else:
        return 1 # bail out and return true... better to send email with no info than not at all

def cleanup():
    try:
        os.remove(pathForLastUsedDirFile())
        os.remove(pathForCommonPathFile())
        os.remove(pathForAccumulationFile())
    except:
        pass

def senderEMailAddressString():
    # this seems totally wrong!
    # Yeah.  For now, switch to foo@codefab.com, and maybe this script can
    # look for an env. variable (CVS_MAIL_ADDRESS?) for non-fabbers?  -CWS
    return userName() + "@" + EMAIL_DOMAIN
	
def writeToFile(aPath, contentString):
    fD = open(aPath, 'w+')
    fD.write(contentString)
    fD.flush()
    fD.close()

def main():
    # // args[0] is script name
    # // args[1] is target email(s)
    # // args[2] is cvs commentary on the action
    # // stdin contains long log message from cvs
    logString = sys.stdin.read()
    emailAddresses = sys.argv[1]

    shortDescriptionComponent = commonPath()
    if (not shortDescriptionComponent) or (shortDescriptionComponent == ''):
        shortDescriptionComponent = sys.argv[2]
        if (shortDescriptionComponent[0] == '"') and (shortDescriptionComponent[-1] == '"'):
            shortDescriptionComponent = shortDescriptionComponent[1:-1]

    shortDescription = 'CVS update: ' + shortDescriptionComponent

    try: 
        accumulatedLogString = open(pathForAccumulationFile()).read()
    except:
        accumulatedLogString = None
    if (accumulatedLogString) and (len(accumulatedLogString)):
        accumulatedLogString = accumulatedLogString + '\n\n' + logString
    else:
        accumulatedLogString = logString

    if logStringIsForLastNotification(logString):
        body = 'From: %s\nSubject: %s\nTo: %s\n\n' % (senderEMailAddressString(), shortDescription, emailAddresses)
        body = body + accumulatedLogString

        if len(sys.argv) > 3:
            restOfArgs = sys.argv[3:]
            body = body + "\n\nOther Arguments to %s: %s\n\n" % (sys.argv[0], str(restOfArgs))

        import smtplib
        mailServer = smtplib.SMTP(SMTP_HOST)
        mailServer.sendmail(senderEMailAddressString(), string.split(emailAddresses, ","), body)
        mailServer.quit()
        cleanup()
        sys.stderr.write('CVS repository summary sent via email to "%s"\n' % emailAddresses)
    else:
        writeToFile(pathForAccumulationFile(), accumulatedLogString)

    sys.exit(0)

if __name__ == "__main__":
    main()
