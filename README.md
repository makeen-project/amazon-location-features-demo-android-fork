
# Amazon Location Services

This repository contains code for an Android application that demonstrates how to use [Amazon Location Services](https://aws.amazon.com/location/). The app showcases features like [displaying maps](https://aws.amazon.com/location/features/#Maps), searching for [points of interest](https://aws.amazon.com/location/features/#Places), calculating [routes](https://aws.amazon.com/location/features/#Routes), [geocoding](https://aws.amazon.com/location/features/#Places), [device tracking](https://aws.amazon.com/location/features/#Trackers), and creating [geofences](https://aws.amazon.com/location/features/#Geofences).

To use this code, you'll need an [AWS account](https://aws.amazon.com/) and resources to be deployed within your account. This is automated using [AWS CloudFormation](https://aws.amazon.com/cloudformation/), and this readme explains the full setup process.

Once configured, you can build and run the app locally or execute automated tests. The project includes unit and end-to-end UI tests.

The app supports multiple map styles and providers like [Esri](https://docs.aws.amazon.com/location/latest/developerguide/esri.html), [HERE Technologies](https://docs.aws.amazon.com/location/latest/developerguide/HERE.html), [GrabMaps](https://docs.aws.amazon.com/location/latest/developerguide/grab.html), and [Open Data](https://docs.aws.amazon.com/location/latest/developerguide/open-data.html).

Overall, this repo will help you get started with location-based features on Android using Amazon Location Services.

Please refer to the [AWS Geospatial repository](https://github.com/aws-geospatial/) for other demo apps, including [iOS](https://github.com/aws-geospatial/amazon-location-features-demo-ios), [React](https://github.com/aws-geospatial/amazon-location-samples-react), and [web](https://github.com/aws-geospatial/amazon-location-features-demo-web), as well as additional resources.

## Requirements

Below are the requirements for development, running and testing.

#### Development Tools

1. [Android Studio](https://developer.android.com/studio)
2. Java 11 or above.

#### Pre-requisites
1. [Create your AWS Account](https://repost.aws/knowledge-center/create-and-activate-aws-account) if needed, or sign in to your existing [AWS account](https://aws.amazon.com/)
2. Run the [AWS CloudFormation template](https://us-east-1.console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/create?stackName=amazon-location-default-unauth-resources&templateURL=https://amazon-location-demo-resources.s3.us-west-2.amazonaws.com/default-unauth-resources-template.yaml) or use the template from `/extra/default-unauth-resources-template.yaml` to create a CloudFormation stack on AWS in the `us-east-1` region and get `IdentityPoolId`, `PinPointAppId`, `WebSocketUrl` from the stack's "outputs" tab.
    - `IdentityPoolId` value will be added to `custom.properties` file against `DEFAULT_IDENTITY_POOL_ID`.
    - `PinPointAppId` value will be added to `custom.properties` file against `ANALYTICS_APP_ID`.
    - `WebSocketUrl` value will be added to `custom.properties` file against `SIMULATION_WEB_SOCKET_URL`.
    - Take region from IdentityPoolId (Character before ':') that value will be added to `custom.properties` file against `DEFAULT_REGION`.
3. Run the [AWS CloudFormation template](https://ap-southeast-1.console.aws.amazon.com/cloudformation/home?region=ap-southeast-1#/stacks/create?stackName=amazon-location-default-unauth-resources&templateURL=https://amazon-location-demo-resources.s3.us-west-2.amazonaws.com/default-unauth-resources-template.yaml) or use the template from `/extra/default-unauth-resources-template.yaml` to create a cloudformation stack on AWS in `ap-southeast-1` region and get `IdentityPoolId`, `WebSocketUrl` from stack output's tab.
    - `IdentityPoolId` value will be added to `custom.properties` file against `DEFAULT_IDENTITY_POOL_ID_AP`.
    - `WebSocketUrl` value will be added to `custom.properties` file against `SIMULATION_WEB_SOCKET_URL_AP`.
4. Run the [AWS CloudFormation template](https://eu-west-1.console.aws.amazon.com/cloudformation/home?region=eu-west-1#/stacks/create?stackName=amazon-location-default-unauth-resources&templateURL=https://amazon-location-demo-resources.s3.us-west-2.amazonaws.com/default-unauth-resources-template.yaml) or use the template from `/extra/default-unauth-resources-template.yaml` to create a cloudformation stack on AWS in `eu-west-1` region and get `IdentityPoolId`, `WebSocketUrl` from stack output's tab.
    - `IdentityPoolId` value will be added to `custom.properties` file against `DEFAULT_IDENTITY_POOL_ID_EU`.
    - `WebSocketUrl` value will be added to `custom.properties` file against `SIMULATION_WEB_SOCKET_URL_EU`.
5. After adding all above details in `custom.properties` file in Android studio then open `Build -> Clean project` after this run project.
6. Run the [AWS CloudFormation template](https://us-east-1.console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/create?stackName=amazon-location-resources-setup&templateURL=https://amazon-location-resources-setup.s3.amazonaws.com/dev/main-cf-template.yaml) or use the template from `/extra/main-cf-template.yaml` using your own AWS account and get below data.
    - `IdentityPoolId` value will be added to `custom.properties` file against `IDENTITY_POOL_ID`.
    - `UserDomain` value will be added to `custom.properties` file against `USER_DOMAIN`.
    - `UserPoolClientId` value will be added to `custom.properties` file against `USER_POOL_CLIENT_ID`.
    - `UserPoolId` value will be added to `custom.properties` file against `USER_POOL_ID`.
    - `WebSocketUrl` value will be added to `custom.properties` file against `WEB_SOCKET_URL`.

Follow this [Document](https://location.aws.com/demo/help) for detailed info to create & configure a new AWS CloudFormation template.

The required values can be found from the `Outputs` tab on your stack page created in step 2, 3, 4 and 5 above.
## Configure

Create *`custom.properties`* file inside the project root folder and add the details as below.

| KEY to be added in custom.properties | Corresponding Key from stack output                    |
|--------------------------------------|--------------------------------------------------------|
| DEFAULT_IDENTITY_POOL_ID             | IdentityPoolId                                         |
| DEFAULT_REGION                       | Take region from IdentityPoolId (Character before ':') |
| DEFAULT_IDENTITY_POOL_ID_EU          | IdentityPoolId form eu-west-1 region                   |
| DEFAULT_IDENTITY_POOL_ID_AP          | IdentityPoolId form ap-southeast-1 region              |
| SIMULATION_WEB_SOCKET_URL            | Simulation WebSocketUrl                                |
| SIMULATION_WEB_SOCKET_URL_EU         | Simulation WebSocketUrl form eu-west-1 region          |
| SIMULATION_WEB_SOCKET_URL_AP         | Simulation WebSocketUrl form ap-southeast-1 region     |
| ANALYTICS_APP_ID                     | AnalyticsAppId                                         |
| IDENTITY_POOL_ID                     | IdentityPoolId                                         |
| USER_DOMAIN                          | UserDomain                                             |
| USER_POOL_CLIENT_ID                  | UserPoolClientId                                       |
| USER_POOL_ID                         | UserPoolId                                             |
| WEB_SOCKET_URL                       | WebSocketUrl                                           |

#### For Build (Required for building and running the app)

```
DEFAULT_IDENTITY_POOL_ID=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
DEFAULT_REGION=xx-xxxx-x
DEFAULT_IDENTITY_POOL_ID_EU=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
DEFAULT_IDENTITY_POOL_ID_AP=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
SIMULATION_WEB_SOCKET_URL=xxxxxxxxxxxx-xxx.xxx.xx-xxxx-x.xxxxxxxxxx.com
SIMULATION_WEB_SOCKET_URL_EU=xxxxxxxxxxxx-xxx.xxx.xx-xxxx-x.xxxxxxxxxx.com
SIMULATION_WEB_SOCKET_URL_AP=xxxxxxxxxxxx-xxx.xxx.xx-xxxx-x.xxxxxxxxxx.com
ANALYTICS_APP_ID=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### Optional values to add after above if you want to run tests locally. (This can be a different stack only for testing)

```
IDENTITY_POOL_ID=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
USER_DOMAIN=https://xxxxxxxxxxxx.xxxx.xx-xxxx-x.amazoncognito.com
USER_POOL_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
USER_POOL_ID=xx-xxxx-x_xxxxxxxxx
WEB_SOCKET_URL=xx....x-xxx.iot.xx-xxxx-x.amazonaws.com
USER_LOGIN_NAME=<aws username>
USER_LOGIN_PASSWORD=<aws password>
```

## Run Locally

To run the application locally, use either an [Android Emulator](https://developer.android.com/studio/run/emulator) or a physical device connected with [USB debugging enabled](https://developer.android.com/studio/debug/dev-options#Enable-debugging).

    1. Clone the project.
    2. Open the project in Android Studio.
    3. Select 'Run Configuration' as 'app' if not already selected.
    4. Click on run button.

## Running Tests

To run tests locally remember to add the values in `secrets.properties` mentioned above in configure section.

### Unit Tests

UnitTests are configured to run with [JaCoCo](https://www.jacoco.org/jacoco/) and can be executed using various commands having different uses as below:

| Command                                         | Use                                                                                                             |
|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| ./gradlew testDebugUnitTest                     | Runs unit tests without JaCoCo coverage report.                                                                 |
| ./gradlew testDebugUnitTestCoverage             | Runs unit tests with JaCoCo coverage report.                                                                    |
| ./gradlew testDebugUnitTestCoverageVerification | Runs unit tests with JaCoCo code coverage report and verifies if minimum code coverage constraint is satisfied. |

The code coverage report can be found at the following path:

    app_root_dir/app/build/reports/jacoco/testDebugUnitTestCoverage


### E2E Tests

E2E tests can be executed by the following gradle commands. Test suites are configured to cover all the functionalities:

``` 
Note: 
    1. Start the emulator before executing the commands.
    2. If working on windows(Powershell/cmd) wrap the argument in double quotes.
        eg: ./gradlew app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite"
```


    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite2
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SimulationStartTrackingHistoryLoggedTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SimulationStopTrackingHistoryLoggedTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite3
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.AWSConnectionSuite
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.AWSSignInSuite
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStartTrackingTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStopTrackingTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingDeleteTrackingHistoryTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStartTrackingHistoryLoggedTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStartTrackingMapDisplayTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.GeofenceAddTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingGeofenceEnterTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingGeofenceExitTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingDeleteTrackingHistoryTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.GeofenceEditTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.GeofenceDeleteTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SettingSignOutTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SettingSignInTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SettingAWSDisconnectingTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.ConnectToAWSTest

## Resources
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
- location.aws.com.demo.maps.Grab.StandardLight - VectorGrabStandardLight
- location.aws.com.demo.maps.Grab.StandardDark - VectorGrabStandardDark
- location.aws.com.demo.maps.OpenData.StandardLight - VectorOpenDataStandardLight
- location.aws.com.demo.maps.OpenData.StandardDark - VectorOpenDataStandardDark
- location.aws.com.demo.maps.OpenData.VisualizationLight - VectorOpenDataVisualizationLight
- location.aws.com.demo.maps.OpenData.VisualizationDark - VectorOpenDataVisualizationDark

> Place indexes (Name)
- location.aws.com.demo.places.Esri.PlaceIndex
- location.aws.com.demo.places.HERE.PlaceIndex
- location.aws.com.demo.places.Grab.PlaceIndex

> Route calculators (Name)
- location.aws.com.demo.routes.Esri.RouteCalculator
- location.aws.com.demo.routes.HERE.RouteCalculator
- location.aws.com.demo.routes.Grab.RouteCalculator

> Geofence collections (Name)
- location.aws.com.demo.geofences.GeofenceCollection

> Trackers (Name)
- location.aws.com.demo.trackers.Tracker
## Contributing

See [CONTRIBUTING](./CONTRIBUTING.md) for more information.


## License

This library is licensed under the MIT-0 License. See [LICENSE](./LICENSE).

