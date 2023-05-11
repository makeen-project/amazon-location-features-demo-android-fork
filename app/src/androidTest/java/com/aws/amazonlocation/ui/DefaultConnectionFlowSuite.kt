package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.AfterSearchDirectionButtonWorkingTest
import com.aws.amazonlocation.ui.main.ExploreFragmentEnableLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentLiveNavigationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapCurrentLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapFunctionWithoutAwsLoginTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapLoadingTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapLocateMeButtonTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapZoomInOutTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMaxZoomInOutTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchByCategoriesTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchCollapseTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchExistsTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchGeocodeReversedTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchLocationByAddressTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchLocationByGeocodeTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchNonExistingLocationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExploreFragmentEnableLocationTest::class,
//    ExploreFragmentMapLoadingTest::class,
//    ExploreFragmentMapCurrentLocationTest::class,
//    ExploreFragmentLiveNavigationTest::class,
//    ExploreFragmentMapFunctionWithoutAwsLoginTest::class,
//    ExploreFragmentMapLocateMeButtonTest::class,
//    ExploreFragmentMapZoomInOutTest::class,
//    ExploreFragmentMaxZoomInOutTest::class,
//    AfterSearchDirectionButtonWorkingTest::class,
//    ExploreFragmentSearchByCategoriesTest::class,
//    ExploreFragmentSearchCollapseTest::class,
//    ExploreFragmentSearchExistsTest::class,
//    ExploreFragmentSearchGeocodeReversedTest::class,
//    ExploreFragmentSearchLocationByAddressTest::class,
//    ExploreFragmentSearchLocationByGeocodeTest::class,
//    ExploreFragmentSearchNonExistingLocationTest::class
)
class DefaultConnectionFlowSuite
