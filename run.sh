#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

INPUT_PATH="$1"
OUTPUT_DIR="${DIR}/output"
SESSIONS_NUMBER=50
TRACKS_NUMBER=10
SESSION_TIMEOUT_MILLIS=1200000
INFER_TRACK_ID="false"

if [ -z ${INPUT_PATH} ]; then
echo "INPUT_PATH is not defined";
exit 1
fi

rm -rf $OUTPUT_DIR

echo "* * * * * * * * * * * * * * * * * * * * * * * * * * * *"
echo "* RUNNING LASTFM SPARK JOB WITH PARAMETERS: "
echo "* * * * * * * * * * * * * * * * * * * * * * * * * * * *"
echo "* INPUT_PATH = $INPUT_PATH"
echo "* OUTPUT_DIR = $OUTPUT_DIR"
echo "* SESSIONS_NUMBER = $SESSIONS_NUMBER"
echo "* TRACKS_NUMBER = $TRACKS_NUMBER"
echo "* SESSION_TIMEOUT_MILLIS = $SESSION_TIMEOUT_MILLIS"
echo "* INFER_TRACK_ID = $INFER_TRACK_ID"
echo "* * * * * * * * * * * * * * * * * * * * * * * * * * * *"

java -Xmx2G -cp ${DIR}/local_runner/target/scala-2.11/local_runner-assembly-0.1.jar Main \
    $INPUT_PATH \
    $OUTPUT_DIR \
    $SESSIONS_NUMBER \
    $TRACKS_NUMBER \
    $SESSION_TIMEOUT_MILLIS \
    $INFER_TRACK_ID