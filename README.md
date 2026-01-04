# ![](screenshots/ic_launcher.png) Branch Directory Map

This project and associated README file are under active development and may change at any time. Please consider watching this repository if you're interested in tracking its progress.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Gallery](#gallery)
- [Requirements](#requirements)
- [Guide](#guide)
- [adbc.bat](#adbcbat)
- [Roadmap](#roadmap)
- [Accuracy](#accuracy)
- [Security](#security)
- [Data Collection](#data-collection)
- [Known Issues](#known-issues)
- [Changelog](#changelog)

## Overview

Branch Directory Map is an Android app, coded mostly in Java using Android Studio. This app primarily uses the Google Maps SDK for Android and various Google API's. Basically it reads CSV files (converted from XLSX files or other sources) and then parses this data to create an SQLite database, and then contacts Google Maps Geocoding API to get latitude/longitude positions and stores them for future retrieval in the database. Users can see all the valid locations or branches on a Google Map fragment, with custom coloured markers for each CSV file read. Users are able to search for branches based on branch codes or names or addresses, and get information about that branch like distance and ETA, as well as traffic information and a polyline projected on the map to show the optimal route to take. Users can also call branches that have listed phone numbers. There is also a routing feature that lets users add multiple branches to a route and use the Google Maps app for Android to navigate to all the branches on the route, as well as show all information for the whole route. This app is in a beta status currently, so commercial usage isn't advised just yet, although my ultimate goal is to make this app usable by any company who needs a mapped branch directory for driver navigation. I developed this app to help drivers, working for one of the largest rental car companies in the world, navigate between the hundreds of branches and dealerships they deal with, and it has been immensely well received and used.

With the current Essentials tier, Google is surprisingly generous with 10,000 free calls per API for all non-Pro/Enterprise level Google Maps Platform API's. Refer here for Google's latest tier-based pricing and limits: [Platform Pricing & API Costs](https://cloud.google.com/maps-platform/pricing)

## Features

- high backwards compatibility, going as far back as Android Marshmallow (6.0)
- use either Directions API for simple routing or Advanced Routing features using Routes API
- add up to 23 (or 25 if using Advanced Routing) intermediate branches within one route using waypoints
- ability to change map layers including traffic and satellite views, various traffic routing models, route preferences like tolls/highways/ferries
- ability to switch between alternate or monochrome map styles, match system theme or utilize ambient light sensor to determine theme
- pre-code waypoints for markers using comma separated plus codes in the refined address field, useful for routing to markers whose approaching road is incorrectly chosen by Google
- CSV or DB files can either be hard-coded with the app or read from a file by the user using `ActivityResultContracts.GetContent()`
- geocode using either just address, or address + postal code, or Google plus codes (if detected), and optionally add a custom modifier to all addresses
- ability to bundle DB files with release with pre-geocoded markers so users don't need to geocode (very costly for hundreds of markers per user)
- export DB file to Downloads folder once all markers have been geocoded
- uses `EncryptedSharedPreferences` to store API key for HTTPS requests, since this key cannot be secured without a backend proxy
- conditional approach to dependencies/imports so you can either implement Firebase Remote Config or Java NDK C++ obfuscation to retrieve the insecure API key
- includes two sample CSV files in assets folder, along with the geocoded markers in a DB file, with a sample gradle.properties.example file with the right settings to geocode/map the locations in the CSV files
- optionally uses [RootBeer](https://github.com/scottyab/rootbeer) (by [Scott Alexander-Bown](https://github.com/scottyab)) to prevent root access and denies debugging mode to further protect the insecure requests API key

## Gallery

<p align="center">
    <img src="screenshots/Screenshot_20251113_204126.png" alt="" width="150" style="margin-right: 10px;" />
    <img src="screenshots/Screenshot_20251113_204201.png" alt="" width="150" style="margin-right: 10px;" />
    <img src="screenshots/Screenshot_20251113_204314.png" alt="" width="150" style="margin-right: 10px;" />
    <img src="screenshots/Screenshot_20251113_204427.png" alt="" width="150" style="margin-right: 10px;" />
    <img src="screenshots/Screenshot_20251113_204544.png" alt="" width="150" />
</p>

## Requirements

- Android Studio 2024 or later
- Android 6.0 Marshmallow (API 23) or later required (to use `EncryptedSharedPreferences`)
- Google Cloud account (free to make, requires valid credit card, must create a new project)
- two Google API keys: one secure and restricted to only Android, the app's package name plus SHA-1, and the Maps SDK for Android API; the other insecure and restricted to Google Directions/Geocoding/Routes API's
- to protect insecure API key: either a Firebase account (must create a new project to not link with Google Cloud) with Remote Config, or Java NDK for C++ obfuscation
- file called `api.dat` (included in `.gitignore`) in your root project folder with the app API key on the first line, and if using NDK then the requests API key on the second line, this file should not be bundled with a build or committed to a repository
- if using Firebase Remote Config, get the `google-services.json` file from Firebase and place it in your `app\src` folder, and create a new Remote Config parameter called `geocode_api_key` with the value of the requests API key
- RootBeer (optional, from Maven Central, doesn't require additional setup)

## Guide

Clone the repository, unzip it, and load the folder as a project in Android Studio. Then, copy all of `gradle.properties.example` into the auto-generated `gradle.properties` file, and either use the example values and CSV files or make your own configuration and add your CSV files to `app\src\main\assets`. Read the [Requirements](#requirements) section for more information. You are now ready to compile and run your first build.

The following are all the user-modifiable variables in `gradle.properties` and their descriptions:

| Variable             | Type       | Description                                                                                                                                                                                                                                                 |
|----------------------|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ADDRESSINDEX         | int\[]     | x-index(es) of address column                                                                                                                                                                                                                               |
| ADDRESSOFFSET        | int\[]     | y-index(es) of address column                                                                                                                                                                                                                               |
| ADVANCEDROUTES       | bool       | use Routes API instead of Directions API for detailed traffic metrics, higher pricing than basic routing                                                                                                                                                    |
| ALLMARKERS           | bool       | add ability to display all markers together in a table                                                                                                                                                                                                      |
| BADMARKERS           | bool       | show un-geocoded branches in the search results at the bottom, results cannot be selected, in development, do not change                                                                                                                                    |
| BASEDELAY            | int        | minimum delay between queries, do not exceed 50 requests / second for free tier                                                                                                                                                                             |
| CODEDELIM            | str\[]     | delimiter(s) for multiple codes, cannot use the same values from DELIM                                                                                                                                                                                      |
| CODEPREFIX           | str\[]     | prefix(es) for codes, used in search logic and code/name splitting                                                                                                                                                                                          |
| COLOURS              | str\[]     | colour(s) for markers \[azure, cyan, green, magenta, orange, violet, yellow, red is default]                                                                                                                                                                |
| COLUMNSPERROW        | int\[]     | number of columns per row, is not used in the project so far                                                                                                                                                                                                |
| DBFILE               | str        | internal db file name                                                                                                                                                                                                                                       |
| DEFAULTFILE          | str        | default file from FILENAMES, if not specified then first file is up to java                                                                                                                                                                                 |
| DELIM                | str\[]     | delimiter(s) between code and name, use " " for a single space, required if TITLESPLIT is true, code value is omitted for a table if not specified                                                                                                          |
| DUPLICATES           | bool       | allow more than one marker to occupy the same latitude/longitude coordinates                                                                                                                                                                                |
| EMBEDDED             | bool       | use embedded db or csv file(s)                                                                                                                                                                                                                              |
| EMBEDDEDDB           | str        | embedded db file name                                                                                                                                                                                                                                       |
| EXPORT               | bool       | export db file to download folder, uses DBFILE as file name                                                                                                                                                                                                 |
| FILENAMES            | str\[]     | embedded csv file name(s)                                                                                                                                                                                                                                   |
| FIREBASE             | bool       | use firebase remote config to get api key, otherwise use api.dat and store in ndk                                                                                                                                                                           |
| GEOREGION            | str\[]     | string to add to all addresses when geocoding, except for addresses containing plus codes, place in double quotes if values contain commas                                                                                                                  |
| IGNOREROWSBEGIN      | int\[]     | ignore first x row(s)                                                                                                                                                                                                                                       |
| IGNOREROWSEND        | int\[]     | ignore last x non-empty row(s)                                                                                                                                                                                                                              |
| INTERMEDIATES        | int        | max number of intermediate markers, regular routing max is 23 and advanced routing max is 25 as defined by Google, for advanced routing exceeding 10 intermediates will result in higher billing rate, for the external Maps App the max intermediates is 9 |
| LIGHTSENSOR          | bool       | add theme option to use ambient light sensor to dynamically adjust theme to light or dark                                                                                                                                                                   |
| MAPID                | str        | optional map ID to apply styling from your map instances stored on Maps Platform, will override JSON styling, disables alt and mono switches, changes pricing of map usage if used                                                                          |
| MAXTHREADS           | int        | max number of threads to use for geocode requests, do not overload Google servers                                                                                                                                                                           |
| MINCLUSTERSIZE       | int        | min number of markers in a cluster                                                                                                                                                                                                                          |
| MULTIROWSETS         | bool\[]    | use multiple rows per entry                                                                                                                                                                                                                                 |
| NAMEFIRST            | bool\[]    | read name first followed by code from title if TITLESPLIT is false, also reverses how title is displayed                                                                                                                                                    |
| PHONE                | bool\[]    | use phone numbers                                                                                                                                                                                                                                           |
| PHONEINDEX           | int\[]     | x-index(es) of phone number column                                                                                                                                                                                                                          |
| PHONEOFFSET          | int\[]     | y-index(es) of phone number column                                                                                                                                                                                                                          |
| PROXY                | bool       | use a proxy for all requests, in development, do not change                                                                                                                                                                                                 |
| PROXYURL             | str        | proxy url, in development, do not change                                                                                                                                                                                                                    |
| RANDOMDELAY          | int        | max random delay between queries, prevents request collisions, do not exceed 50 requests / second for free tier                                                                                                                                             |
| REFINED              | bool\[]    | use refined address column, such as postal code or google plus code                                                                                                                                                                                         |
| REFINEDADDRESSINDEX  | int\[]     | x-index(es) of refined address column                                                                                                                                                                                                                       |
| REFINEDADDRESSOFFSET | int\[]     | y-index(es) of refined address column                                                                                                                                                                                                                       |
| ROOT                 | bool       | allow root access, for the security of api keys this should be set to false                                                                                                                                                                                 |
| ROWSPERSET           | int\[]     | number of rows per set                                                                                                                                                                                                                                      |
| SETTINGSPERFILE      | bool       | read comma separated values, one set per file name expected, otherwise first value used, settings that can have multiple values are denoted by \[] after type                                                                                               |
| STYLEJSON            | str\[6]    | raw resource file names for map styling, for reference only, do not change, you can alter the files themselves to apply custom styling                                                                                                                      |
| TIMEOUT              | int        | timeout in seconds for firebase api key remote config                                                                                                                                                                                                       |
| TITLEINDEX           | int\[]     | x-index(es) of title column, title includes branch code and/or name                                                                                                                                                                                         |
| TITLEOFFSET          | int\[]     | y-index(es) of title column, title includes branch code and/or name                                                                                                                                                                                         |
| TITLESPLIT           | bool\[]    | combine branch code and name from separate fields, if true then TITLE is assumed to be name and TITLE2 is assumed to be code, DELIM is used as the character between the code and the name internally and when displayed                                    |
| TITLE2INDEX          | int\[]     | x-index(es) of branch code column, only referenced if TITLESPLIT is true, otherwise TITLE is assumed to contain code                                                                                                                                        |
| TITLE2OFFSET         | int\[]     | y-index(es) of branch code column, only referenced if TITLESPLIT is true, otherwise TITLE is assumed to contain code                                                                                                                                        |
| TRAFFICMETRICS       | double\[4] | ratio of duration for light, moderate, heavy, and severe traffic                                                                                                                                                                                            |

All user modifiable variables are located in `gradle.properties`, but there are a few variables in `BranchDirectoryMap.java` that might interest developers:

| Variable             | Description                                                                                                                                                             |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SCREENSHOT_MODE      | runs app in fullscreen mode                                                                                                                                             |
| FORCED_FAIL          | simulates a fail rate (defined below) for geocoding                                                                                                                     |
| PASS_PERCENT         | chance of a branch passing geocoding (0-100)                                                                                                                            |
| SUGGESTION_RATIO     | ratio of screen height to use as searchview suggestion dropdown height                                                                                                  |
| DEFAULT_TRAFFIC_MODE | default mode for traffic, must define for both regular \["No Traffic", "Best Guess", "Optimistic", "Pessimistic"] and advanced \["No Traffic", "Optimal Aware"] routing |
| DEFAULT_MODE         | default map mode \[0 = normal, 1 = satellite, 2 = terrain, 3 = hybrid]                                                                                                  |
| DEFAULT_THEME        | default app theme \[0 = system, 1 = light, 2 = dark, 3 = ambient sensor]                                                                                                |
| LUX_PING_SECONDS     | duration between sensor reading requests, drains battery faster with lower values                                                                                       |
| LUX_OVERRIDES        | if true then values below are used for ambient sensor function, by default lux values are calculated using Sensor.getMaximumRange()                                     |
| LUX_DARK_ON          | use this lux value to determine when to apply dark theme                                                                                                                |
| LUX_DARK_OFF         | use this lux value to determine when to apply light theme                                                                                                               |

## adbc.bat

I have included a helper batch file `create_adbc.bat` which creates my little adb companion (adbc), a simple script to run adb/adb emu/gradlew commands from terminal in Android Studio, including pre-programmed commands. Open a new terminal window and run the following command:
```
.\create_adbc "<adb directory>" <device IP address> <optional port number>
```
Replace the <> values with the appropriate parameters, preserving the double quotes. This will create a file called 'adbc.bat' in the same folder. If your device isn't paired with adb yet, enable wireless debugging on the device, select the Pairing Code option, make note of the debugging port and the 6-digit pairing code, and run the following command:
```
.\adbc pair <debugging port> <pairing code>
```
After a few seconds it should say the pairing was successful. By default, since API 31, Android randomizes the port number for tcp/ip every time wireless debugging starts. This little script will force port 5555, or your specified optional port, through adb. But this setting will reset every time you restart your device. To change port to 5555, or the optional port you specified earlier, run the following command:
```
.\adbc <device port>
```
Provide the proper port number from the wireless debugging settings on your device, it should be 5 digits. `adbc.bat` will change the port number and reconnect. From then on, until your device restarts, you can just call:
```
.\adbc
```
Your device will connect instantly from within Android Studio on the default port 5555, or your optional port, even if wireless debugging isn't explicitly enabled on the device. Make sure the device is connected to the same wireless network, and if connecting is taking a while just turn your device's screen on. After a restart, just provide the wireless debugging port number again. To disconnect from the device, run the following command:
```
.\adbc disc
```
The `adbc.bat` port configuring function does not work when multiple devices/emulators are detected by adb. To list all connected devices, run the following command:
```
.\adbc list
```
By default the latest versions of adb will use mDNS discovery to auto connect to previously paired devices even if they're connected to already, sometimes causing duplicate entries in Android Studio. To toggle this behaviour, use the following command:
```
.\adbc auto <on/off>
```
To directly pass arguments to adb use the following command:
```
.\adbc -a <arguments>
```
You can run the following command to mitigate issues with gradle daemon taking up too much memory or not releasing files to clean them:
```
.\adbc stop
```
This calls the gradle wrapper (gradlew) to stop the gradle daemon (OpenJDK Platform binary) process, it will automatically restart when you sync/clean/build. To run other gradlew commands use the following command:
```
.\adbc -ga <arguments>
```
The script can also run commands for the emulator. For example, to kill the emulator if one instance is running, use the following command:
```
.\adbc kill
```
To run any other emulator commands use the following command:
```
.\adbc -ea <arguments>
```
This batch file, and the `create_adbc.bat` file, require that you run it from your project root folder. To simplify the process even further, consider adding shortcut buttons to the top right toolbar of Android Studio for commands you use frequently. Go to `File > Settings > Tools > External Tools` and press the plus button. The following screenshot shows how to set up the stop command:

<p align="center">
    <img src="screenshots/external_tools.png" alt="" width="300" />
</p>

Now simply right click on any of the buttons on the toolbar and click `Customize Toolbar`. Select `Android Main Toolbar Right` and press the Add button. Under External Tools you'll find your action. At the bottom select an appropriate icon, and press the OK button.

## Roadmap

- ability to convert XLSX files to CSV in the app
- localization support for languages and RTL interface as well as different address formatting
- further fields in the database also parsed from the CSV, including notes, hours of operation, manager, etc.
- ability to track users from Firebase for authentication
- selecting a marker more than once for a route
- developer mode to separate file reading and geocoding from release going to drivers
- unit testing and release configuration including key signing
- user feedback through Firebase to report wrong information, address changes, new branches to add, or bugs
- distribute DB files through Firebase so users don't have to geocode anything themselves, preventing further API costs
- backend proxy option in the works for securing the requests API key
- usage of the Places API to be able to select addresses on the map, and ability to select waypoints to route through them
- possible hidden debug menu for easy developer access to functions and being able to override build parameters
- maybe Firebase App Check if I ever figure out how to implement it without Play Integrity
- favourites and history function in the SearchView
- ability for users to broadcast location, send information to other users, and report road hazards to all users
- consent/EULA/privacy policy/terms and conditions template and activity for end-users
- route optimization feature using Traveling Salesman algorithm for routes with multiple waypoints
- exceed the 23/25 waypoints hard limit by cutting extended routes into segments

## Accuracy

The accuracy of the markers and the routing features of this app is entirely dependent on Google's current map data, which is constantly changing. Please use plus codes in the refined address field to ensure accuracy. Plus codes are Google's proprietary system of alphanumerically coded latitude/longitude coordinates, see [here](https://plus.codes/map) for more information. For addresses whose approach road/entry point is incorrectly calculated by Google, such as branches within large lots that are not navigable according to Google, you may provide multiple plus codes in the refined field, separated by commas. The last plus code is assumed to be the entrance to the branch and will not be routed to, all other plus codes will be considered waypoints in sequence from first to last.

## Security

Due to how Google allows usage of its API keys, this app utilizes two API keys. The first is hard-coded in the APK at build time, making it completely exposed. This key is used to render the map fragment. You need to ensure this key is restricted to this app's package name and SHA-1 fingerprint as well as just the Maps SDK for Android API. And the second key is fetched from either Firebase Remote Config or Java NDK, depending on build settings, so this key is at least partially exposed. This key is used for Directions/Geocoding/Routes API requests, so unfortunately this key has to exist as an unencrypted string at some points in the app's lifecycle, making the key vulnerable to snooping. For maximum security it is highly recommended to use Firebase Remote Config instead of Java NDK. You should also restrict this key to the three API's mentioned. Both API keys are susceptible to being decompiled or leaked from memory inspection, and unfortunately until a backend proxy is implemented this will continue to be an issue. I also highly recommend you set up budget alerts on your Google Cloud billing account for your project, including setting up a Pub/Sub topic to automate shutting down API's before your budget threshold reaches 100%, consult Google's documentation for further information. And lastly, the `api.dat` file must never be committed to a repository, which is why it is already in `.gitignore`.

## Data Collection

This app utilizes many Google services including Maps SDK for Android, various Maps Platform API's, Firebase including Remote Config, Cloud, as well as Play Services. All of this implies various forms of data collection for who knows what purposes, that you nor I have any control over. Such is the price of using Google products. The Cloud Billing account holder(s), most likely you, may access some of this data. You as a developer or employer are responsible for informing your end-users about this data collection in accordance with your local laws.

## Known Issues

There is one known issue I've been trying to resolve. Your logcat may be flooded with `Too many Flogger logs received before configuration. Dropping old logs.` messages. This error is directly related to the Maps SDK as far as I can tell, and has been reported by others using the Maps SDK. This project doesn't utilize Flogger or Compose, so it seems to be on Google's end. I can reproduce this on my phone but not any Google-based emulated devices. For now you can safely ignore this error or suppress the message on logcat by adding `-tag:proxyandroidloggerbackend` to the filter.

## Changelog

### 0.1-beta2 (upcoming)
- upcoming: loading screen activity or progress bar for geocoding segment
- upcoming: marker icons will render with a letter to show position if on the route
- upcoming: ability route to a marker more than once
- upcoming: revised database scheme to help geocode better including mandatory full-format postal codes and separate plus codes
- upcoming: proxy option which will disable implementing Firebase Remote Config and `EncryptedSharedPreferences`
- upcoming: option for automatic route optimization using Traveling Salesman algorithm
- upcoming: persistent settings through use of `SharedPreferences`
- upcoming: option for active speed readout on the map fragment
- upcoming: ability to specify extra information about branches by reading one or more extra CSV columns
- upcoming: history and favourites features for branches
- upcoming: overlay buttons on marker info windows to favourite, refresh, or close the marker
- upcoming: automatic refreshing when marker info windows are open for a specified amount of time
- upcoming: visual waypoint counter displayed on the map fragment when relevant
- upcoming: new option `BADMARKERS` to display branches not successfully geocoded in the suggestion list, but not on the map
- upcoming: proper edge-to-edge and gesture navigation support

### 0.1-beta1-fix (2026-01-03)
- fix: build number no longer padded
- fix: 'All' table option hidden when only one table is loaded
- fix: resolved issue with crashes when loading marker info for branch with multiple waypoints without geocoding
- fix: CMake version made consistent so build succeeds when using NDK
- revised: `libs.versions.toml` updated including AGP version

### 0.1-beta1 (2025-11-13)
- feature: revamped UI with directions button if markers are selected, many minor UI tweaks and fixes
- feature: new theme system replacing dark mode, theme options including system and ambient sensor modes
- feature: option to read and display name before code using `NAMEFIRST`, some refactored code
- feature: ability to have option to display all marker tables together using `ALLMARKERS`
- improved: Material3 migration begins, some refactored code
- improved: search function works better, also refactored for searching all tables
- improved: advanced routing shows toast warnings when tolls/ferries/highways couldn't be avoided
- revised: static declarations of Gson are now moved to the application class
- revised: MyItem class replaced with more efficient CustomItem class, some refactored code
- revised: public and private declarations made consistent
- revised: Compile and Target SDK raised to 36
- revised: new incremental build system implemented
- revised: CMake updated, `libs.versions.toml` updated including AGP version
- revised: changes to documentation, user variable descriptions moved to README
- fix: floating action button placement and visibility handling logic fixed
- fix: route is properly cleared when changing branch tables
- fix: failed waypoint geocoding is now treated the same as a failed marker geocoding
- fix: phone index and offset values ignored when `PHONE` is false
- fix: changes to `MapLoaderTask` to fix logic issues and improve performance
- fix: IME no longer overlaps suggestion list dropdown (in portrait orientation)
- fix: suggestion list dropdown is always alphabetically sorted by code or name

### 0.1-beta1-preview (2025-07-12)
- feature: refined address fields can include multiple plus codes separated by commas, treated as "via" waypoints, refer to [Accuracy](#accuracy) for more information
- improved: changes to `create_adbc.bat` and `adbc.bat` to check argument formatting, resolve project directory paths with spaces, new `pair`, `list`, and `auto` commands, and other tweaks
- improved: root checks now performed every time API key is accessed
- improved: branch code and name can now be read from separate fields or one field
- revised: Address and Refined fields can contain commas
- revised: `libs.versions.toml` updated, including AGP version
- revised: removed unused dependencies, including all of Jetpack Compose, and fragmented comments
- revised: number of waypoints tracked so as to not exceed 23 (or 25 for Advanced Routing), hard limits set by Google
- fix: avoidance options now passed to API's even without selecting waypoints
- fix: refined address included in marker info window if it isn't a plus code
- fix: for now, waypoints are truncated to the last one for routes sent to Maps App due to Google's intermediates limit of 9 and inability to route through waypoints as "via" in the Maps App

### 0.1-alpha4 (2025-04-06)
- feature: specify a Google Map ID to import your custom map style from your cloud project, will disable dark and monochrome toggles, pricing will be higher per map load
- major fix: code shrinking works, `proguard-rules.pro` revised to work with reflection implementation, `fullMode` set to false
- fix: app no longer exports DB if the DB was embedded
- fix: `google-services.json` is conditionally excluded from the build when not using Firebase Remote Config
- fix: some devices no longer display the notification bar over the app's search bar
- fix: disables relevant interface features when the link API key is missing
- fix: nonspecific logic fixes, interface tweaks, code cleaning
- fix: changes to text colours for different elements to improve consistency across light and dark themes
- improved: heavily altered `adbc.bat` to make it more useful, including adding emulator commands and passing arguments to adb/emu/gradlew
- improved: better handling of intents and passing information when opening Google Maps app
- improved: more localization support by using string resources for all Toast messages
- revised: debuggable is now set to false in `build.gradle` and not hardcoded in `AndroidManifest.xml`
- revised: `libs.versions.toml` updated, CMake version updated, Gradle version updated

### 0.1-alpha3 (2025-03-30)
- feature: specify a string to add to all addresses when geocoding for accuracy's sake, except for addresses containing plus codes
- feature: route information now includes an arrival time in 12-hour format
- feature: allows four styling scenarios (light, dark, light monochrome, dark monochrome) and imports them from `res\raw` folder
- feature: clicking a cluster will center it the first time and decluster the map the second time
- feature: `create_adbc.bat` file included, see [adbc.bat](#adbcbat)
- major fix: using Directions API works, requests now include route waypoints as well
- fix: logic for location permission handling, permission will be asked once, and app will now work without location permission with traffic metrics and routing disabled
- fix: MainActivity optimized for devices that restart activities when permissions change
- fix: DialogUtils overhauled, implementations changed, no more window leaks or cancellable dialogs, and localization support added
- fix: documentation fixes, regarding usage of advanced routing, it doesn't actually use two request calls
- fix: proper absorption of configuration changes across all activities
- fix: phone option properly implemented
- fix: general non-specific code cleaning and refactoring across project, lots of unused imports and declarations removed
- improved: app starts up faster after geocoding successfully finishes, location tracking starts faster too
- improved: logic for handling marker info windows and a new custom info window adapter
- revised: Secrets class is largely rewritten to make it entirely conditional for the future proxy option
- revised: anonymous classes replaced with lambdas where possible
- revised: `libs.versions.toml` updated, removed com.google.maps from project
- revised: `gradle.properties.example` and example CSV/DB files updated

### 0.1-alpha2 (2025-03-14)
- feature: conditional deployment of RootBeer, but still highly recommended to use in your build
- major fix: fatal error when using Firebase Remote Config that caused CMake to run and fail, CMake is now properly conditional
- fix: gradle lint implementation and `gradle.properties.example` file
- fix: MainActivity logic and RootBeer implementation
- fix: deselecting a marker now cancels `getInformationTask()`
- upgrade: android gradle plugin to 8.9.0
- revised: used Gemini to generate better real-world addresses for sample CSV asset files
- revised: `libs.versions.toml` to remove unused dependencies and update active ones
- revised: `.gitignore` improved for readability

### 0.1-alpha1 (2025-03-13)
- initial release

## Thank You

Thank you for checking out this project. If you are interested in collaborating on this project or creating a fork, feel free to reach out to me by sending me an [email](mailto:r_b_inc@yahoo.ca?subject=%5BGITHUB-SAFE%5D). And if you're an employer looking to integrate this app into your fleet and distribute it to drivers, I am available for consulting to provide support and deployment help.