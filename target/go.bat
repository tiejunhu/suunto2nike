@echo off

setlocal enabledelayedexpansion

for /f "tokens=*" %%F in ('dir /b /on "*-with-dependencies.jar"') do set file=%%F

echo Running %file%

java -jar %file%

echo.
pause