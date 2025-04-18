@echo off
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
    echo %TARGET% already exists.
    choice /C YN /M "Overwrite %TARGET%"
    if errorlevel 2 (
        echo Not overwriting %TARGET%.
        exit /b 1
    )
)
(
    echo @echo off
    echo setlocal enabledelayedexpansion
    echo set "adbPath=%adbDir%"
    echo set deviceIP=%ip%
    echo set defPort=%port%
    echo set currDir=%%cd%%
    echo set args=%%*
    echo cd "%%adbPath%%"
    echo if "%%~1"=="" ^(
    echo     adb connect %%deviceIP%%:%%defPort%%
    echo ^) else ^(
    echo     if "%%~1"=="dscn" ^(
    echo         adb disconnect
    echo     ^) else ^(
    echo         if "%%~1"=="-a" ^(
    echo             adb ^!args:~3^!
    echo         ^) else ^(
    echo             if "%%~1"=="stop" ^(
    echo                 cd "%%currDir%%"
    echo                 gradlew --stop
    echo             ^) else ^(
    echo                 if "%%~1"=="-ga" ^(
    echo                     cd "%%currDir%%"
    echo                     gradlew ^!args:~4^!
    echo                 ^) else ^(
    echo                     if "%%~1"=="kill" ^(
    echo                         adb emu kill
    echo                     ^) else ^(
    echo                         if "%%~1"=="-ea" ^(
    echo                             adb emu ^!args:~4^!
    echo                         ^) else ^(
    echo                             adb connect %%deviceIP%%:%%~1
    echo                             adb tcpip %%defPort%%
    echo                             adb disconnect
    echo                             adb connect %%deviceIP%%:%%defPort%%
    echo                         ^)
    echo                     ^)
    echo                 ^)
    echo             ^)
    echo         ^)
    echo     ^)
    echo ^)
    echo endlocal
    echo exit /b 0
) > "%TARGET%"
echo Target file %TARGET% created successfully.
exit /b 0

:validateIP
setlocal enabledelayedexpansion
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
endlocal
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
