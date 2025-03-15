# Branch Directory Map

This project and associated README file are under active development and may change at any time. Please consider watching 

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Roadmap](#roadmap)
- [Security](#security)
- [Changelog](#changelog)

## Overview

Branch Directory Map is my latest Android app, coded mostly in Java using Android Studio. This app primarily uses the Google Maps SDK for Android. Basically it reads CSV files (converted from XLSX files or other sources) and then parses this data to create an SQLite database, and then contacts Google Maps Geocoding API to get latitude/longitude positions and stores them for future retrieval in the database. Users can see all the valid locations or branches on a Google Map fragment, with custom coloured markers for each CSV file read. Users are able to search for branches based on branch codes or names or addresses, and get information about that branch like distance and ETA, as well as traffic information and a polyline projected on the map to show the optimal route to take. Users can also call branches that have listed phone numbers. There is also a routing feature that lets users add multiple branches to a route and use the Google Maps app for Android to navigate to all the branches on the route, as well as show all information for the whole route. This app is in an alpha status currently, so commercial usage isn't advised just yet, although my ultimate goal is to make this app usable by any company who needs a mapped branch directory for driver navigation. I am a driver for one of the largest rental car companies in the world and I developed this app to help myself and my coworkers navigate between the hundreds of branches and dealerships we deal with, and it has been immensely popular among Android-user drivers. An iOS version is planned in the future once I learn Flutter.

## Features

- use either Directions API for simple routing (uses one request call) or advanced routing features using Routes API (uses two request calls)
- CSV or DB files can either be hard-coded with the app or read from a file by the user using `ActivityResultContracts.GetContent()`
- geocode using either just address, or address + postal code, or Google plus codes (if detected)
- ability to bundle DB files with release with pre-geocoded markers so users don't need to geocode (very costly for hundreds of markers)
- export DB file to Downloads folder once all markers have been geocoded
- uses EncryptedSharedPreferences to store API key for HTTPS requests, since this key cannot be secured without a backend proxy
- conditional approach to dependencies/imports so you can either implement Firebase Remote Config or Java NDK C++ obfuscation to retrieve the insecure API key
- includes two sample CSV files in assets folder, with a sample gradle.properties.example file with the right settings to geocode/map the locations in the CSV files
- optionally uses [RootBeer](https://github.com/scottyab/rootbeer) (by [Scott Alexander-Bown](https://github.com/scottyab)) to prevent root access and denies debugging mode to further protect the insecure requests API key

## Requirements

- Android 6.0 Marshmallow (API 23) or later required (to use `EncryptedSharedPreferences`)
- Google Cloud account (free to make, requires valid credit card, must create a new project)
- two Google API keys, one secure and restricted to the app's name and SHA-1, the other insecure and restricted to Google Directions/Geocoding/Routes API's
- to protect insecure API key: either a Firebase account (must create a new project to not link with Google Cloud) with Remote Config, or Java NDK for C++ obfuscation
- file called `api.dat` in your root project folder with the app API key on the first line, and if using NDK then the requests API key on the second line
- RootBeer (optional, from Maven Central, doesn't require additional setup)

## Installation

Detailed instructions coming soon. For now just clone the repository, unzip it, and load the folder as a project in Android Studio. Then, copy all of `gradle.properties.example` into the auto-generated `gradle.properties` file, and either use the example values and CSV files or make your own configuration and add your CSV files to `app\src\main\assets`. You are now ready to compile and run your first build.

## Roadmap

- ability to convert XLSX files to CSV in the app
- further fields in the database also parsed from the CSV, including notes, hours of operation, manager, etc.
- ability to track users from Firebase for authentication
- unit testing and release configuration including key signing
- user feedback through Firebase to report wrong information, address changes, new branches to add, or bugs
- distribute DB files through Firebase so users don't have to geocode anything themselves, preventing further API costs
- backend proxy option in the works for securing the requests API key
- eventually a Flutter version for iOS implementation
- usage of the Places API to be able to select addresses on the map
- possible hidden debug menu for easy developer access to functions and being able to override build parameters
- maybe Firebase App Check if I ever figure out how to implement it without Play Integrity

## Security

Due to how Google allows usage of its API keys, this app utilizes two API keys. The first is hard-coded in the APK at build time, making it completely exposed. This key is used to render the map fragment. You need to ensure this key is restricted to this app's package name and SHA-1 fingerprint. And the second key is fetched from either Firebase Remote Config or Java NDK, depending on build settings, so this key is at least partially exposed. This key is used for Directions/Geocoding/Routes API requests, so unfortunately this key has to exist as an unencrypted string at some points in the app's lifecycle. For this reason it is highly recommended to use Firebase Remote Config instead of Java NDK. You should also restrict this key to the three API's mentioned. Both API keys are susceptible to being decompiled or leaked from memory inspection, and unfortunately until a backend proxy is implemented this will continue to be an issue. I also highly recommend you set up budget alerts on your Google Cloud billing account for your project, including setting up a Pub/Sub topic to automate shutting down API's before your budget threshold reaches 100%, consult Google's documentation for further information. And lastly, the `api.dat` file must never be committed to a repository, which is why it is already in `.gitignore`.

## Changelog

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
- upcoming: marker icons will render with a letter to show position if on the route
- upcoming: ability to display all marker tables together
- upcoming: revised database scheme to help geocode better including mandatory full-format postal codes and separate plus codes

### 0.1-alpha1 (2025-03-13)
- initial release

## Thank You

Thank you for checking out this project. If you're interesting in collaborating on this project or creating a fork, feel free to reach out to me by sending me an [email](mailto:r_b_inc@yahoo.ca?subject=%5BGITHUB-SAFE%5D). And if you're an employer looking to integrate this app into your fleet and distribute it to drivers, I am available for consulting to provide support and deployment help.