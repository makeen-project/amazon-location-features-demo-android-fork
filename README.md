# Amazon location demo

## Introduction

A demo application contains features like map, searching points of interest, calculating routes, geocode, tracking devices, and triggering geofences using [Amazon Location Service](https://docs.aws.amazon.com/location/latest/developerguide/welcome.html).


## Pre-requisites

- Sign in to [Amazon AWS account](https://aws.amazon.com/)
- Run the [CF template](https://us-west-2.console.aws.amazon.com/cloudformation/home?region=us-west-2#/stacks/create?stackName=amazon-location-resources-setup&templateURL=https://amazon-location-demo-resources.s3.amazonaws.com/location-services.yaml) using your own AWS account and get `IdentityPoolId` and `region` from stack output

## Configuration

- Create `custom.properties` file inside the project root folder and add `IdentityPoolId` and `region` like below

```javascript
DEFAULT_IDENTITY_POOL_ID=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
DEFAULT_REGION=xx-xxxx-x
```

## Resources
### Amazon Location Service
> Maps (Name - Style)
- location.aws.com.demo.maps.Esri.DarkGrayCanvas - VectorEsriDarkGrayCanvas
- location.aws.com.demo.maps.Esri.Imagery - RasterEsriImagery
- location.aws.com.demo.maps.Esri.Light - VectorEsriTopographic
- location.aws.com.demo.maps.Esri.LightGrayCanvas - VectorEsriLightGrayCanvas
- location.aws.com.demo.maps.Esri.Navigation - VectorEsriNavigation
- location.aws.com.demo.maps.Esri.Streets - VectorEsriStreets
- location.aws.com.demo.maps.HERE.Explore - VectorHereExplore
- location.aws.com.demo.maps.HERE.Contrast - VectorHereContrast
- location.aws.com.demo.maps.HERE.ExploreTruck - VectorHereExploreTruck
- location.aws.com.demo.maps.HERE.Hybrid - HybridHereExploreSatellite
- location.aws.com.demo.maps.HERE.Imagery - RasterHereExploreSatellite

> Place indexes (Name)
- location.aws.com.demo.places.Esri.PlaceIndex
- location.aws.com.demo.places.HERE.PlaceIndex

> Route calculators (Name)
- location.aws.com.demo.routes.Esri.RouteCalculator
- location.aws.com.demo.routes.HERE.RouteCalculator

> Geofence collections (Name)
- location.aws.com.demo.geofences.GeofenceCollection

> Trackers (Name)
- location.aws.com.demo.trackers.Tracker

## Security

## Contribute
See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.


## License

This library is licensed under the MIT-0 License. See the LICENSE file.

