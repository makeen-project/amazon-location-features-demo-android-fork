package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.CheckGoButtonClickLiveNavigationTest
import com.aws.amazonlocation.ui.main.CheckGrabGoButtonTest
import com.aws.amazonlocation.ui.main.CheckRouteEstimatedTimeAndDistanceTest
import com.aws.amazonlocation.ui.main.CheckRouteMapAdjustedTest
import com.aws.amazonlocation.ui.main.CheckRouteOptionsTest
import com.aws.amazonlocation.ui.main.CheckRouteUserEnterMyLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentChangeStyleTest
import com.aws.amazonlocation.ui.main.ExploreFragmentGrabChangeStyleTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapStyleSearchFilterTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchResultTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchTotalResultTest
import com.aws.amazonlocation.ui.main.RouteOptionShowingTest
import com.aws.amazonlocation.ui.main.RouteReverseBetweenFormToTest
import com.aws.amazonlocation.ui.main.SearchAddressExactMatchPOICardLocationTest
import com.aws.amazonlocation.ui.main.SearchPlaceDisplayedOnMapPOICircleTest
import com.aws.amazonlocation.ui.main.SearchPlaceDisplayedOnMapTest
import com.aws.amazonlocation.ui.main.SearchResultComparisonTest
import com.aws.amazonlocation.ui.main.SettingRouteOptionAvailableTest
import com.aws.amazonlocation.ui.main.SettingsFragmentChangeDataProviderTest
import com.aws.amazonlocation.ui.main.SettingsFragmentChangeLanguageTest
import com.aws.amazonlocation.ui.main.SettingsFragmentChangeRegionTest
import com.aws.amazonlocation.ui.main.SettingsFragmentContentTest
import com.aws.amazonlocation.ui.main.SettingsFragmentDefaultRouteTest
import com.aws.amazonlocation.ui.main.SettingsMapStyleSearchFilterTest
import com.aws.amazonlocation.ui.main.SimulationStartTrackingHistoryLoggedTest
import com.aws.amazonlocation.ui.main.SimulationStopTrackingHistoryLoggedTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SearchPlaceDisplayedOnMapPOICircleTest::class,
    SearchAddressExactMatchPOICardLocationTest::class,
    SearchPlaceDisplayedOnMapTest::class,
    SearchResultComparisonTest::class,
    ExploreFragmentSearchResultTest::class,
    ExploreFragmentSearchTotalResultTest::class,
    CheckGoButtonClickLiveNavigationTest::class,
    CheckRouteEstimatedTimeAndDistanceTest::class,
    CheckRouteMapAdjustedTest::class,
    CheckRouteOptionsTest::class,
    CheckRouteUserEnterMyLocationTest::class,
    RouteOptionShowingTest::class,
    RouteReverseBetweenFormToTest::class,
    SettingRouteOptionAvailableTest::class,
    SettingsFragmentChangeDataProviderTest::class,
    SettingsFragmentContentTest::class,
    SettingsFragmentDefaultRouteTest::class,
    ExploreFragmentChangeStyleTest::class,
    ExploreFragmentMapStyleSearchFilterTest::class,
    SettingsMapStyleSearchFilterTest::class,
    SimulationStartTrackingHistoryLoggedTest::class,
    SimulationStopTrackingHistoryLoggedTest::class,
    SettingsFragmentChangeRegionTest::class,
    SettingsFragmentChangeLanguageTest::class,
    ExploreFragmentGrabChangeStyleTest::class,
    CheckGrabGoButtonTest::class
)
class DefaultConnectionFlowSuite2
