<html>
<head>
<title>Bots'n'Scouts - Known Bugs</title>
<?php include('bnsheader.pht');?>
<div class="content" >
<CENTER>
<H1>Known Bugs and other things to do</H1>
</CENTER>
We prefer to be told about bugs and problems via the <a href="http://sourceforge.net/tracker/?group_id=3883&atid=103883">bug tracking system</A> at Sourceforge.<P>
You may tell your wishes for future releases to the <A HREF="http://sourceforge.net/tracker/?atid=353883&group_id=3883&func=browse">Feature Tracker</A>.
<P>
But you may use classical <a href="mailto:botsnscouts-devel@lists.sf.net">e-mail</a> as well.<BR>
<H2>Known Bugs in 0.8.*</H2>
<UL>
<LI>On real multi-user operating systems you cannot save tiles if you haven't installed Bots'n'Scouts yourself.
<LI>Layout still is nicer with higher monitor resolution. 800 x 600 causes some minor problems.
<LI>Disconnecting during a game may cause problems
<LI>The limitation to a maximum of 6 flags and 8 players applies to V0.8pre, too.
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
<P>
<H2>Development</H2>
You want to join development? Mmhpf, there are some problems:
<UL>
<LI>There is a lot of cruft in the source code. A lot of pretty ugly spots. This project has quite some history now... </LI>
<LI>The underlying protocols (network communication and board files) are crap.
We do not understand them anymore nor do we wish to. We want to replace them...<BR>
 ... when we find the time.</LI>
<LI>To make it even worse: You need a sound knowledge of German to read the source code. WE ARE SORRY. (To all young developers out there: Do never start coding and commenting in your native language unless it is not English! Even if you do not think of ever publishing it, it might happen. And you really have a hard time then, getting your code translated.)<BR>
Well, in between the state of translation has improved. When you find a lot of German in the code this is a sign that it has not been touched a long time and is propably ugly anyway.
</UL>
But if you manage: Please use the 
<A HREF="http://sourceforge.net/cvs/?group_id=3883">CVS version</A>
for reading and hacking, and send us patches.<P>
You want to use this project as an example in a university course? Uhhm, see above and read the source...<BR>
There is still hope, we continue to refactor and translate and optimze. Sometimes we might even add new features...
</div>
<?php include('bnsfooter.pht');?>
