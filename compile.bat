@echo off
cd /d "%~dp0"
if not exist bin mkdir bin
echo Compiling Hostel Complaint Management System...
javac -d bin src\hcms\*.java src\hcms\model\*.java src\hcms\exceptions\*.java src\hcms\threads\*.java src\hcms\data\*.java src\hcms\gui\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Check that the JDK is installed and javac is on the PATH.
) else (
    echo Compilation successful. Run run.bat to start the program.
)
pause
