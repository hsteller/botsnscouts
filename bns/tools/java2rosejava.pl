#/usr/local/bin/perl

opendir(DIR,".") || die "couldn't open directory";
@javas=grep { /.java$/ && -f "$_" } readdir(DIR);
closedir DIR;

#print "javafiles:\n";
#print @javas;
#print "\n";

$classes=`grep \"public class\" *.java`;

%classtrans=(
	Thread => "java.lang.Thread",
	Frame => "java.awt.Frame",
	String => "java.lang.String",
	RuntimeException => "java.lang.RuntimeException",
	Component => "java.awt.Component",
	Frame => "java.awt.Frame",
	Exception => "java.lang.Exception",
	Canvas => "java.awt.Canvas",
	Image => "java.awt.Image",
	WindowListener => "java.awt.event.WindowListener",
	BufferedReader => "java.io.BufferedReader",
	Socket => "java.net.Socket",
	Vector => "java.util.Vector",
	ActionListener => "java.awt.event.ActionListener",
	PrintWriter => "java.io.PrintWriter",
	Color => "java.awt.Color",
	CropImageFilter => "java.awt.image.CropImageFilter",
	InputStream => "java.io.InputStream",
	ServerSocket => "java.net.ServerSocket",
	Integer => "java.lang.Integer",
	Font => "java.awt.Font",
	
);

while ($classes=~/public class ([^ \n{]+)[ \n{]/mg){
	$classtrans{$1}="rr.$1";
}

$classes="\n".`grep \"protected class\" *.java`;

print "classes=\n".$classes."\n\n";

while ($classes=~/\n([^.]+)\.java:[ \t]*protected class ([^ \n{]+)[ \n{]/g){
	$classtrans{$2}="rr.$1.$2";
}

print "translation table:\n";
foreach (sort keys(%classtrans)){
	print $_." => ".$classtrans{$_}."\n";
}

umask 0000;
mkdir("rr",0777) || die "couldn't create directory";
foreach (@javas){
	print "Beginne mit $_\n";
	open(FOO,">rr/$_") || die "couldn't open rr/$_";
	print FOO "package rr;\n\n";
	open(BAR,"<$_") || die "couldn't open $_";
	while (<BAR>){
		foreach $s (keys %classtrans){
			s/( extends ) *$s(\W)/$1$classtrans{$s}$2/g;
			s/( implements ) *$s(\W)/$1$classtrans{$s}$2/g;
			s/(public\s+(final\s+)?(static\s+)?)$s( *(\[\] *)?[^( ])/$1$classtrans{$s}$4/g;
			s/(protected\s+(final\s+)?(static\s+)?)$s( *(\[\] *)?[^( ])/$1$classtrans{$s}$4/g;
			s/(private\s+(final\s+)?(static\s+)?)$s( *(\[\] *)?[^( ])/$1$classtrans{$s}$4/g;
			s/^(\s*(final\s+)?(static\s+)?)$s( *(\[\] *)?\w+ *[=;])/$1$classtrans{$s}$4/g;
		}

		print FOO $_;
	}
	close FOO;
	close BAR;
}

