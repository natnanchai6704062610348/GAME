@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out scr\*.java
if errorlevel 1 (echo Compilation failed. Please install JDK 17 or newer.& pause & exit /b 1)
java -cp out Main
pause
