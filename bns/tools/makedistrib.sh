#!/usr/bin/zsh
targetdir=~/bns/distrib
ver=0_7

oldpwd=`pwd`
echo "*** Starting GPLifying every source file"
tools/gplify.sh
echo "*** Creating directory-structure under $targetdir"
rm -rf $targetdir 2>/dev/null
mkdir -p $targetdir
mkdir $targetdir/botsnscouts-$ver-src
mkdir $targetdir/botsnscouts-$ver
mkdir $targetdir/botsnscouts-$ver-src/de
mkdir $targetdir/botsnscouts-$ver-src/de/spline
mkdir $targetdir/botsnscouts-$ver-src/de/spline/rr
mkdir $targetdir/botsnscouts-$ver/de
mkdir $targetdir/botsnscouts-$ver/de/spline
mkdir $targetdir/botsnscouts-$ver/de/spline/rr
echo "*** Copying stuff into de/spline/rr"
for i in images conf;do
	cp -a $i $targetdir/botsnscouts-$ver-src/de/spline/rr
	cp -a $i $targetdir/botsnscouts-$ver/de/spline/rr
done
echo "*** Copying stuff into dist-dir"
for i in kacheln;do
	cp -a $i $targetdir/botsnscouts-$ver-src
	cp -a $i $targetdir/botsnscouts-$ver
done
rm -rf $targetdir/botsnscouts-$ver-src/kacheln/CVS
rm -rf $targetdir/botsnscouts-$ver/kacheln/CVS
echo "*** Copying distribution-specific stuff"
for i in tools/distrib/*;do
	cp $i $targetdir/botsnscouts-$ver-src
	cp $i $targetdir/botsnscouts-$ver
done
for i in tools/distrib.src/*;do
	cp $i $targetdir/botsnscouts-$ver-src
done
for i in tools/distrib.binary/*;do
	cp $i $targetdir/botsnscouts-$ver
done
echo "*** Removing nodistrib-files"
for i in $(<kacheln/nodistrib);do
	rm $targetdir/botsnscouts-$ver-src/kacheln/$i 2>/dev/null
	rm $targetdir/botsnscouts-$ver-src/kacheln/$i.* 2>/dev/null
	rm $targetdir/botsnscouts-$ver-src/kacheln/*.thmb 2>/dev/null
	rm $targetdir/botsnscouts-$ver/kacheln/$i 2>/dev/null
	rm $targetdir/botsnscouts-$ver/kacheln/$i.* 2>/dev/null
	rm $targetdir/botsnscouts-$ver/kacheln/*.thmb 2>/dev/null
done
cp *.java $targetdir/botsnscouts-$ver-src/de/spline/rr
for i in $(<nodistrib);do
	rm $targetdir/botsnscouts-$ver-src/de/spline/rr/$i 2>/dev/null
	rm $targetdir/botsnscouts-$ver-src/de/spline/rr/$i.* 2>/dev/null
done
echo "*** Compiling classes"
cd $targetdir/botsnscouts-$ver-src
jikes de/spline/rr/*.java
cp de/spline/rr/*.class $targetdir/botsnscouts-$ver/de/spline/rr
cd $targetdir
echo "*** Zipping up archives"
cd botsnscouts-$ver
jar cmf $oldpwd/tools/manifest botsnscouts.jar de
rm -rf de
cd ..
zip -r -9 botsnscouts-$ver-src.zip botsnscouts-$ver-src >/dev/null
zip -r -9 botsnscouts-$ver.zip botsnscouts-$ver >/dev/null
echo "*** Removing temporary files"
rm -rf *(/)
echo "*** Repairing your cvs directory"
cd $oldpwd
rm *.java
cvs update -d >/dev/null
echo "*** Checking in new release..."
cp $targetdir/*.zip html/download
cvs commit html/download
echo "*** ALL DONE."
