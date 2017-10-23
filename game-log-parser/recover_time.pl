#!/usr/bin/perl
=comment multiline_comment

./recover_time.pl <<EOF
heredoc> Status at 01-09-2016 15:51:21: 0 ...
heredoc> [195.236.145.100] client connected
heredoc> EOF
2016-09-01T15:51:21 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T15:51:21 [195.236.145.100] client connected

=cut

while ($line = <>) {
  if ( $line =~ /Status at (\d\d)-(\d\d)-(\d\d\d\d) (\d\d):(\d\d):(\d\d):/ ) {
    $last_date = "$3-$2-$1";
    $last_time = "$4:$5:$6";
  }
  if ( $line =~ /^[A-Z][a-z][a-z] \d\d (.*)/ ) {}
   elsif ( $last_date ) {
    print $last_date . "T" . $last_time . " ". $line;
  }
}
