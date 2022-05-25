# Driving sample app for Android

This example shows how to integrate Sygic Driving library.

## Usage
1. Open project in Android studio.
1. Synchronize gradle project.
1. In `local.properties` file set the license to `sygic.license` key. To get license key, please [contact our support](https://www.sygic.com/enterprise/contact-us).
1. If you want to be able to see your trips on maps, add your Google Maps API key to `google.maps.key` in `local.properties`.
1. Run the app. 

The best way to test the app is to drive a vehicle.

## Migration guide from version 1.x
To update library version in your project from 1.x to 2.x follow these steps:
1. Update driving library version in your build.gradle to latest version.
1. `initialize` method signature was changed. It is no longer asynchronous, so the result of initialization is returned immediately. 
1. Sygic license key must be provided in `initialize` method. To get your license key, please [contact our support](https://www.sygic.com/enterprise/contact-us).
1. If you are using Driving library together with Sygic Maps SDK, you have to use single instance of Sygic `Auth` object. For Driving library this means you have to pass `SygicAuthConfig.UseExternalAuth(authInstance)` to `initialize` method.
1. Some constants like `DetectorState` and `TripState` were changed to enums.
1. Some method overloads (e.g. `onTripUploaded`) were removed.
1. `double` timestamps were replaced by `Date`, `GpsPosition` was replaced by `Location`.
1. `TripValidityCriteria` can be set to discard trips that are too short. Default criterias are set to 90 seconds duration and 400 meters length. When trip is discarded, `onTripDiscarded` event is fired (instead of `onTripFinished`). Discarded trips are not sent to server and are automatically deleted.
1. Please refer to [changelog](https://www.sygic.com/developers/sygic-adas-sdk/changelog) for other changes.
