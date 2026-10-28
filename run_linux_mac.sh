#!/usr/bin/env sh
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out scr/*.java
java -cp out Main
