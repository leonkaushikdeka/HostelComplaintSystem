@echo off
cd /d "%~dp0"
if not exist bin\hcms\Main.class (
    echo Please run compile.bat first.
    pause
    exit /b
)
java -cp bin hcms.Main
pause
