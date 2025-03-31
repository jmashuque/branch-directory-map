@echo off
setlocal EnableDelayedExpansion
if "%~1"=="" (
    echo Error: Missing ADB directory parameter.
    exit /b 1
)
if "%~2"=="" (
    echo Error: Missing device IP address parameter.
    exit /b 1
)
set "adbDir=%~1"
if not "%adbDir:~-1%"=="\" set "adbDir=%adbDir%\"
if not exist "%adbDir%" (
    echo Error: The directory "%adbDir%" does not exist.
    exit /b 1
)
if not exist "%adbDir%adb.exe" (
    echo Error: adb.exe not found in directory "%adbDir%".
    exit /b 1
)
set "ip=%~2"
call :validateIP "%ip%"
if errorlevel 1 (
    echo Error: The device IP address is not valid.
    exit /b 1
)
if "%~3"=="" (
    set "port=5555"
) else (
    echo %~3 | findstr /R "^[0-9][0-9][0-9][0-9][0-9]$" >nul
    if errorlevel 1 (
        echo Error: The port number must be a five-digit number.
        exit /b 1
    )
    set "port=%~3"
)
set "TARGET=adbc.bat"
if exist "%TARGET%" (
    echo Error: Target file "%TARGET%" already exusts.
    exit /b 1
)
(
    echo @echo off
    echo set adbPath=%adbDir%
    echo set deviceIP=%ip%
    echo set defPort=%port%
    echo cd /d "%%adbPath%%"
    echo if "%%1"=="" ^(
    echo     adb connect %%deviceIP%%:%%defPort%%
    echo ^) else ^(
    echo     if "%%1"=="-d" (
    echo         adb disconnect
    echo     ^) else ^(
    echo         adb connect %%deviceIP%%:%%1
    echo         adb tcpip %%defPort%%
    echo         adb disconnect
    echo         adb connect %%deviceIP%%:%%defPort%%
    echo     ^)
    echo ^)
    echo exit /b 0
) > "%TARGET%"
echo Target file "%TARGET%" created successfully.
pause
exit /b 0

:validateIP
set "ip=%~1"
call :countDots "%ip%"
if not "%dotCount%"=="3" exit /b 1
for /f "tokens=1-4 delims=." %%a in ("%ip%") do (
    set "oct1=%%a"
    set "oct2=%%b"
    set "oct3=%%c"
    set "oct4=%%d"
)
if "%oct1%"=="" exit /b 1
if "%oct2%"=="" exit /b 1
if "%oct3%"=="" exit /b 1
if "%oct4%"=="" exit /b 1
for %%i in (oct1 oct2 oct3 oct4) do (
    set "val=!%%i!"
    set /a test=!val! >nul 2>&1
    if errorlevel 1 exit /b 1
    if !val! LSS 0 exit /b 1
    if !val! GEQ 256 exit /b 1
)
exit /b 0

:countDots
set "str=%~1"
set "dotCount=0"
:countLoop
if not "%str%"=="" (
    set "char=%str:~0,1%"
    if "%char%"=="." (
        set /a dotCount+=1
    )
    set "str=%str:~1%"
    goto countLoop
)
exit /b 0
