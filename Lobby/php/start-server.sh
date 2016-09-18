#!/bin/bash

# The path to Lobby folder
workingDir=$(dirname "$(readlink -f "$0")")
workingDir="<?DATA_DIR?>" # $workingDir/..

docRoot="$workingDir/lobby"

# Set Document Root
cd $docRoot

LD_LIBRARY_PATH=$LD_LIBRARY_PATH:"$workingDir/php/extensions"

export LD_LIBRARY_PATH

# Run PHP Server
sh "$workingDir/php/php" -t "$docRoot" -c "$workingDir/php/php.ini" -S "$1" "index.php" "LobbyPHPCliServer"
