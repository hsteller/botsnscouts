#!/usr/bin/perl
#
# Für Übersetzungen. Merged $ARGV[0] und $ARGV[1] nach 1, so dass vorhandene
# Schlüssel in 1 bleiben und neue hinzugefügt werden.

if (($ARGV[0] eq "") || ($ARGV[1] eq "")){
	print "usage: transl8 <master> <target>";
	exit 5;
}

sub readStuff{

my @orig, $section, $hashref;

$section="NIX";
$hashref=undef;

while (<IN>){
	if ( m/^#/ ){
		print TMP;
	}
	elsif ( m/^$/ ){
		print TMP;
	}
	elsif ( m/^\[([^\]]+)\]$/ ){
		my $new = { }; 
		$new -> { __name } = "$1";
		push @orig,$new;
		$section=$1;
		$hashref=$new;
		#print "section: $1\n";
	}
	elsif ( m/^([^ \t]+)[ \t](.*)$/ ){
		$hashref -> {$1} ="$2";
		#print "Inserting $2 for key $1.\n";
	}
	else {
		print "URGS: unmatched: $_";
		exit 5;
	}
}

return @orig;

}

sub printem{
	(my @orig)=@_;
	my $tmp, $tmp2;

	foreach $tmp (@orig){
		print "drin: ".$tmp->{__name}."\n";
		foreach $tmp2 (keys %{ $tmp }){
			print "$tmp2; $tmp->{$tmp2}\t";
		}
		print "\n";
	}
}

open IN,"<$ARGV[0]";
open TMP,">/dev/null";

@orig = readStuff();

close IN;
close TMP;
open IN,"<$ARGV[1]";
open TMP,">tmp.$$";

@target = readStuff();

$i=-1;
foreach $secref (@orig){
	print TMP "[$secref->{__name}]\n";
	if ($target[$i+1]->{__name} eq $secref->{__name}){
		$i++;
	}
	if ($target[$i]->{__name} eq $secref->{__name}){
		print "$secref->{__name} in target gefunden.\n";
		foreach $key (sort (keys %{ $secref })){
			if ($key eq "__name"){
				next;
			}
			if (! $target[$i]->{$key} eq ""){
				print "$key in $secref->{__name} schon im target.\n";
				print TMP "$key ".$target[$i]->{$key}."\n";
			}
		        else {
				print "$key in $secref->{__name} vom Original.\n";
				print TMP "$key \n#TRANSLATEME $key=".$secref->{$key}."\n";
			}
		}
	}
	else {
		print "$secref->{__name} nicht in target.\n";
		foreach $key (sort (keys %{ $secref })){
			if ($key eq "__name"){
				next;
			}
			print TMP "$key \n#TRANSLATEME $key=".$secref->{$key}."\n";
		}
	}
}

system ("mv -f tmp.$$ $ARGV[1]");
