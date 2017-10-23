#!/usr/bin/perl
=comment multiline_comment

./individual_to_common_format.pl --server-name=woop.ac:1999 --server-ip=62.210.131.155:1999 <<EOF
2016-09-01T15:51:21 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T16:21:15 [195.236.145.100] client connected
EOF
2016-09-01T15:51:21Z woop.ac:1999 62.210.131.155:1999 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T16:21:15Z woop.ac:1999 62.210.131.155:1999 [195.236.145.100] client connected

=cut
use Getopt::Long;
my $zone = "Z";
GetOptions ('zone=s' => \$zone, 'server-name=s' => \$server_name, 'server-ip=s' => \$server_ip);
$server_name or die("Must specify server name, --server-name=<>, such as woop.ac:1999");
$server_ip or die("Must specify server IP --server-ip=<>, such as 62.210.131.155:1999");
while ($line = <>) {
  if ($line =~ /^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}) (.*)$/ ) {
    print "$1$zone $server_name $server_ip $2\n";
  }
}
