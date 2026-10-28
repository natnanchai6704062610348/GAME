@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out scr\*.java
if errorlevel 1 (
 echo Compilation failed. JDK 17 or newer is required.
 pause
 exit /b 1
)
java -cp out BossPractice
pause
