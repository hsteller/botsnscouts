#!/usr/bin/perl

if (@ARGV != 1){
	print "usage: cleanup.pl <msg-file>";
	exit 5;
}

$file=$ARGV[0];

open OUT,">tmp.$$";
while (<>){
	if (! m/^#TRANSLATEME/ ){
		print OUT;
	}
}
system ("mv -f tmp.$$ $file");
