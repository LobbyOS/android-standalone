#!/bin/bash

workingDir=$(dirname "$(readlink -f "$0")")
workingDir="$workingDir/.."

pid=`pgrep -f php-cli`

if [ -z "$pid" ]; then
  false
else
  kill $pid
fi
