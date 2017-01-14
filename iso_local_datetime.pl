#!/usr/bin/perl
=comment multiline_comment
./iso_local_datetime.pl << EOF
Sep 01 15:51:21 Status at 01-09-2016 15:51:21: 0 ...
Sep 01 16:21:15 [195.236.145.100] client connected
EOF
2016-09-01T15:51:21 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T16:21:15 [195.236.145.100] client connected
=cut

while ($line = <>) {
  if ( $line =~ /Status at (\d\d)-(\d\d)-(\d\d\d\d) (\d\d):(\d\d):(\d\d):/ ) {
    $last_date = "$3-$2-$1";
    $last_time = "$4:$5:$6";
  }
  if ( $last_date && $line =~ /^[A-Z][a-z][a-z] \d\d (.*)/ ) {
    print $last_date ."T". $1."\n";
  } elsif ( $last_date && $last_date =~ /^(2016|2017-01)/ ) {
    print $last_date . "T" . $last_time . " ". $line;
  }
}
