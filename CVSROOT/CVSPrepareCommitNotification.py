#!/usr/bin/env python
####
# Copyright (c) 2000-2002 Bill Bumgarner, Christian Pekeler; CodeFab.
# Copyright (c) 2003 Charles Swiger; PKIx.  All rights reserved.  C=US, L=NY.
#
# Please consult the LICENSE file. 
####
# 
# The python code was written by Bill Bumgarner <bbum@codefab.com>.  It
# was a port of two nearly identical WebScript scripts written by
# Christian Pekeler <pekeler@codefab.com>.
#
# If you fix bugs, make improvements, or find something better than
# CVS, *please* contact <bbum@codefab.com> with more information!
####

import sys
import os
import tempfile
import string

def findFirstValue(d, a):
    for t in a:
        if d.has_key(t):
            return d[t]

try:
    import getpass
    def uniqueName():
        return getpass.getuser()
except:
    # if getpass module not available
    # see getuser() documentation
    import pwd # will raise on Windows.  Oh well. This code is crap anyway.
    def uniqueName():
        possibleName = findFirstValue(os.environ, ['LOGNAME', 'USER', 'LNAME', 'USERNAME'])
        if not (possibleName == None):
            return possibleName
        pwEntry = pwd.getpwuid(os.getuid())
        return pwEntry[0]

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
    return os.path.join(tempfile.tempdir, 'cvs-lastdir-' + uniqueName());

def pathForCommonPathFile():
    return os.path.join(tempfile.tempdir, 'cvs-commonpath-' + uniqueName());

def newCommonPathWithPath(aPath):
    try:
        commonPath = open(pathForCommonPathFile()).read()
        if (commonPath) and len(commonPath):
            return os.path.commonprefix([commonPath, aPath])
        else:
            return commonPath
    except:
        return aPath

def writeToFile(aPath, contentString):
    try: 
        fD = open(aPath, 'w+')
        fD.write(contentString)
        fD.flush()
        fD.close()
    except:
        return 0

    return 1

def main():
    # // args[0] is script name
    # // args[1] is path without filename
    # // args[2] is filename
    path = sys.argv[1]
    commonPath = newCommonPathWithPath(path)

    success1 = writeToFile(pathForCommonPathFile(), commonPath)
    success2 = writeToFile(pathForLastUsedDirFile(), path)

    sys.exit( not ( success1 and success2) )

if __name__ == "__main__":
    main()
    
