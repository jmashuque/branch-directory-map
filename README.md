# Branch Directory Map

This project and associated README file are under active development and may change at any time. Please consider watching this repository if you're interested in tracking its progress.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Gallery](#gallery)
- [Requirements](#requirements)
- [Installation](#installation)
- [adbc.bat](#adbcbat)
- [Roadmap](#roadmap)
- [Security](#security)
- [Data Collection](#data-collection)
- [Changelog](#changelog)

## Overview

Branch Directory Map is my latest Android app, coded mostly in Java using Android Studio. This app primarily uses the Google Maps SDK for Android and various Google API's. Basically it reads CSV files (converted from XLSX files or other sources) and then parses this data to create an SQLite database, and then contacts Google Maps Geocoding API to get latitude/longitude positions and stores them for future retrieval in the database. Users can see all the valid locations or branches on a Google Map fragment, with custom coloured markers for each CSV file read. Users are able to search for branches based on branch codes or names or addresses, and get information about that branch like distance and ETA, as well as traffic information and a polyline projected on the map to show the optimal route to take. Users can also call branches that have listed phone numbers. There is also a routing feature that lets users add multiple branches to a route and use the Google Maps app for Android to navigate to all the branches on the route, as well as show all information for the whole route. This app is in an alpha status currently, so commercial usage isn't advised just yet, although my ultimate goal is to make this app usable by any company who needs a mapped branch directory for driver navigation. I am a driver for one of the largest rental car companies in the world and I developed this app to help myself and my coworkers navigate between the hundreds of branches and dealerships we deal with, and it has been popular among Android-user drivers. An iOS version is planned in the future once I learn Flutter.

## Features

- use either Directions API for simple routing or advanced routing features using Routes API
- route to up to 25 intermediate branches (maximum for API's) within one route using waypoints
- CSV or DB files can either be hard-coded with the app or read from a file by the user using `ActivityResultContracts.GetContent()`
- geocode using either just address, or address + postal code, or Google plus codes (if detected), and optionally add a custom modifier to all addresses
- ability to bundle DB files with release with pre-geocoded markers so users don't need to geocode (very costly for hundreds of markers per user)
- export DB file to Downloads folder once all markers have been geocoded
- uses `EncryptedSharedPreferences` to store API key for HTTPS requests, since this key cannot be secured without a backend proxy
- conditional approach to dependencies/imports so you can either implement Firebase Remote Config or Java NDK C++ obfuscation to retrieve the insecure API key
- includes two sample CSV files in assets folder, along with the geocoded markers in a DB file, with a sample gradle.properties.example file with the right settings to geocode/map the locations in the CSV files
- optionally uses [RootBeer](https://github.com/scottyab/rootbeer) (by [Scott Alexander-Bown](https://github.com/scottyab)) to prevent root access and denies debugging mode to further protect the insecure requests API key
- ability to change map layers including traffic and satellite views, various traffic routing models, route preferences like tolls/highways/ferries, and a custom template for dark themed map

## Gallery

Coming soon.

## Requirements

- Android Studio 2024 or later
- Android 6.0 Marshmallow (API 23) or later required (to use `EncryptedSharedPreferences`)
- Google Cloud account (free to make, requires valid credit card, must create a new project, $100 USD credit per month for non-commercial use)
- two Google API keys, one secure and restricted to the Android, the app's package name, SHA-1, and the Maps SDK for Android API, the other insecure and restricted to Google Directions/Geocoding/Routes API's
- to protect insecure API key: either a Firebase account (must create a new project to not link with Google Cloud) with Remote Config, or Java NDK for C++ obfuscation
- file called `api.dat` (included in `.gitignore`) in your root project folder with the app API key on the first line, and if using NDK then the requests API key on the second line, this file should not be bundled with a build or committed to a repository
- if using Firebase Remote Config, get the `google-services.json` from Firebase and place it in your `app\src` folder, and create a new Remote Config parameter called `geocode_api_key` with the value of the requests API key
- RootBeer (optional, from Maven Central, doesn't require additional setup)

## Installation

Detailed instructions coming soon. For now just clone the repository, unzip it, and load the folder as a project in Android Studio. Then, copy all of `gradle.properties.example` into the auto-generated `gradle.properties` file, and either use the example values and CSV files or make your own configuration and add your CSV files to `app\src\main\assets`. Read the [Requirements](#requirements) section for more information. You are now ready to compile and run your first build.

## adbc.bat

I have included a helper batch file `create_adbc.bat` which creates my little adb companion which is a simple script to ease issues with getting adb to connect your test device to Android Studio. Open a new terminal window and run the following command:
```
.\create_adbc "<adb directory>" "<device IP address>" "<optional port number>"
```
Replace the <> values with the appropriate parameters, preserving the double quotes. This will create a file called 'adbc.bat' in the same folder. By default, since API 31, Android randomizes the port number for tcp/ip every time wireless debugging starts. This little script will force port 5555 through adb, but this setting will reset every time you restart your device. To change port to 5555, run the following command:
```
.\adbc "<device port>"
```
Provide the proper wireless debugging port number. `adbc.bat` will change the port number and reconnect. From then on, until your device restarts, you can use `.\adbc` to reconnect from within Android Studio on the default port 5555. After a restart, just provide the port number again. To disconnect from the device, run the following command:
```
.\adbc -d
```

## Roadmap

- ability to convert XLSX files to CSV in the app
- localization support for languages and RTL interface as well as different address formatting
- further fields in the database also parsed from the CSV, including notes, hours of operation, manager, etc.
- ability to track users from Firebase for authentication, as well as voluntary user location reporting
- selecting a marker more than once for a route
- developer mode to separate file reading and geocoding from release going to drivers
- unit testing and release configuration including key signing
- user feedback through Firebase to report wrong information, address changes, new branches to add, or bugs
- distribute DB files through Firebase so users don't have to geocode anything themselves, preventing further API costs
- backend proxy option in the works for securing the requests API key
- eventually a Flutter version for iOS implementation
- usage of the Places API to be able to select addresses on the map, and ability to select waypoints to route through them
- possible hidden debug menu for easy developer access to functions and being able to override build parameters
- maybe Firebase App Check if I ever figure out how to implement it without Play Integrity
- favourites and history function in the SearchView
- consent/EULA/privacy policy/terms and conditions template and activity for end-users
- route optimization feature using Traveling Salesman algorithm for routes with multiple waypoints

## Security

Due to how Google allows usage of its API keys, this app utilizes two API keys. The first is hard-coded in the APK at build time, making it completely exposed. This key is used to render the map fragment. You need to ensure this key is restricted to this app's package name and SHA-1 fingerprint as well as just the Maps SDK for Android API. And the second key is fetched from either Firebase Remote Config or Java NDK, depending on build settings, so this key is at least partially exposed. This key is used for Directions/Geocoding/Routes API requests, so unfortunately this key has to exist as an unencrypted string at some points in the app's lifecycle, making the key vulnerable to snooping. For maximum security it is highly recommended to use Firebase Remote Config instead of Java NDK. You should also restrict this key to the three API's mentioned. Both API keys are susceptible to being decompiled or leaked from memory inspection, and unfortunately until a backend proxy is implemented this will continue to be an issue. I also highly recommend you set up budget alerts on your Google Cloud billing account for your project, including setting up a Pub/Sub topic to automate shutting down API's before your budget threshold reaches 100%, consult Google's documentation for further information. And lastly, the `api.dat` file must never be committed to a repository, which is why it is already in `.gitignore`.

## Data Collection

This app utilizes many Google services including Maps SDK for Android, various Maps API's, Firebase including Remote Config, Cloud, as well as Play Services. All of this implies various forms of data collection that I have no control over. I personally do not have access to any of this data, but the Cloud Billing account holder(s) may access some of this data. You as a developer or employer are responsible for informing your end-users about this data collection in accordance with your local laws.

## Changelog

### 0.1-beta1 (upcoming)
- upcoming: loading screen activity or progress bar for geocoding segment
- upcoming: marker icons will render with a letter to show position if on the route
- upcoming: ability to display all marker tables together
- upcoming: revised database scheme to help geocode better including mandatory full-format postal codes and separate plus codes
- upcoming: proxy option which will disable implementing Firebase Remote Config and `EncryptedSharedPreferences`
- upcoming: option for automatic route optimization using Traveling Salesman algorithm
- upcoming: option to use ambient light sensor to implement dark mode dynamically, similar to the Maps app
- upcoming: specify a Google Map ID to import your custom map style from your cloud project
- upcoming: persistent settings through use of `SharedPreferences`
- upcoming: disable relevant interface features when the API key is missing

### 0.1-alpha3 (2025-03-30)
- feature: specify a string to add to all addresses when geocoding for accuracy's sake, except for addresses containing plus codes
- feature: route information now includes an arrival time in 12-hour format
- feature: allows four styling scenarios (light, dark, light monochrome, dark monochrome) and imports them from `res\raw` folder
- feature: clicking a cluster will center it the first time and decluster the map the second time
- feature: `create_adbc.bat` file included, see [adbc.bat](#adbcbat)
- major fix: using directions API works, requests now include route waypoints as well
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