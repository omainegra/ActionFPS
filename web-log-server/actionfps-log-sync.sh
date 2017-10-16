#!/bin/bash
usage() {
    echo "Usage: $0 [target TSV file]"
    echo 
    echo Synchronises ActionFPS logs periodically.
    echo 
    echo CURL_OPTS environment variable is available.
    echo If file does not exist, try to create it.
}

# Silent curl
CURL_OPTS="-s"
set -e
[ -z "$1" ] && { usage; exit 1; }
[ "$1" == "" ] && { usage; exit 0; }
TARGET_FILE="$1"

touch ${TARGET_FILE} || { echo "Failed to touch file ${TARGET_FILE}" ; exit 1; }

# Get start time from the last line of the file
START_TIME=$(grep -v '^$' ${TARGET_FILE} | tail -n 1 | cut -f 1 -d $'\t')

# Get the number of times this time was repeated - so we could skip this many
# lines for that time when resuming
START_TIME_REPETITIONS=$(tail -n 100 ${TARGET_FILE} | grep -F ${START_TIME} | wc -l)
START_TIME=${START_TIME:=2014-12-01T01:01:01Z}

echo Resume from time: ${START_TIME} >> /dev/stderr;

DOWNLOAD_URL="https://actionfps.com/logs.tsv?from=${START_TIME}"

curl --fail ${CURL_OPTS} ${DOWNLOAD_URL} | \
    tail -n +$[ START_TIME_REPETITIONS + 1 ] | \
    grep -v -F ${START_TIME} | \
    tee -a ${TARGET_FILE} | \
    awk 'BEGIN { c = 0; }; { c++ }; END { print c " lines added" >> "/dev/stderr"; }'
