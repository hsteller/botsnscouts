<html>
<head>
<title>Bots'n'Scouts - Known Bugs</title>
</head>
<?php include('bnsheader.pht');?>
<CENTER>
<H1>Known Bugs and other things to do</H1>
</CENTER>
If you find bugs in our prerelease, please reproduce them and then <a href="mailto:botsnscouts-devel@lists.sf.net">mail us</A> a description and search a file named botsnscouts.log and attach it to your mail.<BR>
Since this is a Pre-Release we do some logging to make it easier to find bugs if you report them.
You may safely delete the file botsnscouts.log.
<H2>Known Bugs in V0.8pre:</H2>
<UL>
<LI>Starting local players hangs the game! Fixed in V0.8pre2.
<LI>On real multi-user operating systems you cannot save tiles if you haven't installed Bots'n'Scouts yourself. (Will be fixed in  in V0.8)
<LI>Layout still is nicer with higher monitor resolution. 800 x 600 causes some minor problems.
<LI>Disconnecting during a game may cause problems
<LI>The limitation to a maximum of 6 flags and 8 players applies to V0.8pre, too.
<LI> We still haven't written FAQ yet.
</UL>
<P>
<H2>There are more things to do...</H2>
We still are currently working on:
<UL>
<LI>more sounds
<LI>refactoring our source code which has a lot of cruft
<LI>in the long term: option cards
<LI>more cool features (know of any? <a href="mailto:botsnscouts-devel@lists.sorceforge.net">contact</a> us!)
</UL>
<H2>Known Bugs in V0.7.1: (Don't use it anymore!)</H2>
<UL>
<LI>There appear to be problems occuring only with JDK1.3! <BR>Try JDK1.2 or wait until we fixed it, sorry.  
<LI>On real multi-user operating systems you cannot save tiles if you haven't installed Bots'n'Scouts yourself.
<LI>Sometimes the scrollbars don't work. Try resizing the window or use the right mouse button to scroll.
<LI>Layout is nicer with higher monitor resolution. 800 x 600 causes some minor problems.
<LI>Adjust the size of the board and the number of auto-bots to the power of your computer. 
<LI>Confirming "game over" at the end of the game _IMMEDIATELY_ stops everything,
including the server. Don't confirm if the other players want to continue. 
<LI>You may set only up to six flags and no more than eight players. The names must not contain anything but letters from 'A' to 'z', sorry.
<LI>Linux: "fullscreen.sh" is experimental. Read README.fullscreen which comes with your distribution.
<LI> We haven't written FAQ yet.
</UL>
<P>
<?php include('bnsfooter.pht');?>
