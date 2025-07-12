@echo off
setlocal

if "%~1"=="" (
    echo Error: Missing ADB directory parameter.
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
powershell -NoProfile -Command "if (!('%ip%' -match '^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$')) { Write-Host 'Error: The device IP address is not valid.'; exit 1; } $octets = '%ip%'.Split('.'); foreach ($octet in $octets) { if ($octet -lt 0 -or $octet -gt 255) { Write-Host 'Error: The device IP address is not valid.'; exit 1; } }"
if errorlevel 1 exit /b 1

set "port=%~3"
if "%port%"=="" (
    set "port=5555"
) else (
    powershell -NoProfile -Command "if (!('%port%' -match '^\d+$')) { Write-Host 'Error: The port number must be a number.'; exit 1; } if ('%port%'.Length -gt 5) { Write-Host 'Error: The port number must be no more than 5 digits.'; exit 1; }"
    if errorlevel 1 exit /b 1
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
    echo set "currDir=%%cd%%"
    echo set args=%%*
    echo cd "%%adbPath%%"
    echo if "%%~1"=="" ^(
    echo     adb connect %%deviceIP%%:%%defPort%%
    echo ^) else ^(
    echo     if "%%~1"=="disc" ^(
    echo         adb disconnect
    echo     ^) else ^(
    echo         if "%%~1"=="list" ^(
    echo             adb devices
    echo         ^) else ^(
    echo             if "%%~1"=="-a" ^(
    echo                 adb ^!args:~3^!
    echo             ^) else ^(
    echo                 if "%%~1"=="stop" ^(
    echo                     cd "%%currDir%%"
    echo                     gradlew --stop
    echo                 ^) else ^(
    echo                     if "%%~1"=="-ga" ^(
    echo                         cd "%%currDir%%"
    echo                         gradlew ^!args:~4^!
    echo                     ^) else ^(
    echo                         if "%%~1"=="kill" ^(
    echo                             adb emu kill
    echo                         ^) else ^(
    echo                             if "%%~1"=="-ea" ^(
    echo                                 adb emu ^!args:~4^!
    echo                             ^) else ^(
    echo                                 if "%%~1"=="auto" ^(
    echo                                     if "%%~2"=="on" ^(
    echo                                         setx ADB_MDNS_AUTO_CONNECT ""
    echo                                         set ADB_MDNS_AUTO_CONNECT=
    echo                                         echo Auto Connect is ON. Restart Android Studio to take effect.
    echo                                         adb kill-server
    echo                                         adb start-server
    echo                                     ^) else ^(
    echo                                         if "%%~2"=="off" ^(
    echo                                             setx ADB_MDNS_AUTO_CONNECT 0
    echo                                             set ADB_MDNS_AUTO_CONNECT=0
    echo                                             echo Auto Connect is OFF. Restart Android Studio to take effect.
    echo                                             adb kill-server
    echo                                             adb start-server
    echo                                         ^) else ^(
    echo                                             echo Error: Second parameter for auto must be on or off.
    echo                                             exit /b 1
    echo                                         ^)
    echo                                     ^)
    echo                                 ^) else ^(
    echo                                     if "%%~1"=="pair" ^(
    echo                                         powershell -NoProfile -Command "if ('%%~2' -match '^\d{5}$') { exit 0 } else { exit 1 }"
    echo                                         if errorlevel 1 ^(
    echo                                             echo Error: The port number must be a 5 digit number.
    echo                                             exit /b 1
    echo                                         ^)
    echo                                         powershell -NoProfile -Command "if ('%%~3' -match '^\d{6}$') { exit 0 } else { exit 1 }"
    echo                                         if errorlevel 1 ^(
    echo                                             echo Error: The pairing code must be a 6 digit number.
    echo                                             exit /b 1
    echo                                         ^)
    echo                                         adb pair %%deviceIP%%:%%~2 %%~3
    echo                                     ^) else ^(
    echo                                         powershell -NoProfile -Command "if ('%%args%%' -match '^\d{5}$') { exit 0 } else { exit 1 }"
    echo                                         if errorlevel 1 ^(
    echo                                             echo Error: The port number must be a 5 digit number.
    echo                                             exit /b 1
    echo                                         ^)
    echo                                         adb connect %%deviceIP%%:%%~1
    echo                                         adb tcpip %%defPort%%
    echo                                         adb disconnect
    echo                                         adb connect %%deviceIP%%:%%defPort%%
    echo                                     ^)
    echo                                 ^)
    echo                             ^)
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
endlocal
exit /b 0