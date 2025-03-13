# Branch Directory Map

This project and associated README file are under active development and projected to change at any time.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Roadmap](#roadmap)
- [Changelog](#changelog)

## Overview

Branch Directory Map is my latest Android app, coded mostly in Java using Android Studio. This app primarily uses the Google Maps SDK for Android. Basically it reads CSV files (converted from XLSX files or other sources) and then parses this data to create an SQLite database, and then contacts Google Maps Geocoding API to get latitude/longitude positions and stores them for future retrieval in the database. Users can see all the valid locations or branches on a Google Map fragment, with custom coloured markers for each CSV file read. Users are able to search for branches based on branch codes or names or addresses, and get information about that branch like distance and ETA, as well as traffic information and a polyline projected on the map to show the optimal route to take. Users can also call branches that have listed phone numbers. There is also a routing feature that lets users add multiple branches to a route and use the Google Maps app for Android to navigate to all the branches on the route, as well as show all information for the whole route. This app is in an alpha status currently, so commercial usage isn't advised just yet, although my ultimate goal is to make this app usable by any company who needs a mapped branch directory for driver navigation. I am a driver for one of the largest rental car companies in the world and I developed this app to help myself and my coworkers navigate between the hundreds of branches and dealerships we deal with, and it has been immensely popular among Android-user drivers. An iOS version is planned in the future once I learn Flutter.

## Features

- use either Directions API for simple routing (uses one request call) or advanced routing features using Routes API (uses two request calls)
- ability to bundle .db files with release with pre-geocoded markers so users don't need to geocode (very costly for hundreds of markers)
- uses EncryptedSharedPreferences to store API key for HTTPS requests, since this key cannot be secured without a backend proxy
- conditional approach to dependencies/imports so you can either implement Firebase Remote Config or Java NDK C++ obfuscation to retrieve the insecure API key
- includes two sample CSV files in assets folder, with a sample gradle.properties.example file with the right settings to geocode/map the locations in the CSV files
- uses [RootBeer](https://github.com/scottyab/rootbeer) to prevent root access and denies debugging mode to further protect the insecure requests API key

## Requirements

- Android 6.0 Marshmallow (API 23) or later required to use `EncryptedSharedPreferences`
- Google Cloud account (free to make, requires valid credit card, must create a new project)
- Two Google API keys, one secure and restricted to the app's name and SHA-1, the other insecure and restricted to Google Directions/Geocoding/Routes API's
- To protect insecure API key: either a Firebase account (must create a new project to not link with Google Cloud) with Remote Config, or Java NDK for C++ obfuscation
- File called api.dat in your root project folder with the app API key on the first line, and if using NDK then the requests API key on the second line
- RootBeer (from Maven Central, doesn't require additional setup)

## Roadmap

- further fields in the database including notes such as "new branch" or "branch closed"
- ability to track users from Firebase
- user feedback through Firebase to report wrong information or address changes
- distribute .db files through Firebase so users don't have to geocode anything themselves, preventing further API costs
- backend proxy option in the works for securing the requests API key
- Eventually a Flutter version for iOS implementation

## Changelog

### 0.1-alpha1 (2025-03-13)
- initial release

## Thank You

Thank you for checking out this project. If you're interesting in collaborating on this project or creating a fork, feel free to reach out to me by sending me an [email](mailto:r_b_inc@yahoo.ca?subject=%5BGITHUB-SAFE%5D). And if you're an employer looking to integrate this app into your fleet and distribute it to drivers, I am available for consulting to provide support and deployment help.