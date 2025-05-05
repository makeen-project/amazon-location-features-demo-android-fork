package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.SEARCH_TEST_WORD_1
import com.aws.amazonlocation.SEARCH_TEST_WORD_2
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.KEY_AVOID_DIRT_ROADS
import com.aws.amazonlocation.utils.KEY_AVOID_FERRIES
import com.aws.amazonlocation.utils.KEY_AVOID_TOLLS
import com.aws.amazonlocation.utils.KEY_AVOID_TUNNELS
import com.aws.amazonlocation.utils.KEY_AVOID_U_TURNS
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentDefaultRouteTest : BaseTestMainActivity() {

    @Throws(Exception::class)
    override fun before() {
        val preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())

        // Default route options disabled by default.
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        listOf(
            KEY_AVOID_TOLLS,
            KEY_AVOID_FERRIES,
            KEY_AVOID_DIRT_ROADS,
            KEY_AVOID_U_TURNS,
            KEY_AVOID_TUNNELS
        ).forEach { preferenceManager.removeValue(it) }

        super.before()
    }

    @Test
    fun checkDefaultRouteOptionsTest() {
        try {
            checkLocationPermission()
            goToDefaultRouteSettings()

            listOf(
                R.id.switch_avoid_tools,
                R.id.switch_avoid_ferries,
                R.id.switch_avoid_dirt_roads,
                R.id.switch_avoid_u_turns,
                R.id.switch_avoid_tunnels
            ).forEach { toggleSwitch(it) }

            checkDefaultRouteOptions(
                avoidTollsShouldBe = true,
                avoidFerriesShouldBe = true,
                avoidDirtRoadShouldBe = true,
                avoidUTurnShouldBe = true,
                avoidTunnelsShouldBe = true
            )
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToDefaultRouteSettings() {
        waitForView(allOf(withId(R.id.bottom_navigation_main), isDisplayed()))
        clickIfDisplayed(withText(mActivityRule.activity.getString(R.string.menu_setting)))
        clickIfDisplayed(withId(R.id.cl_route_option))
    }

    private fun toggleSwitch(@IdRes switchId: Int) {
        clickIfDisplayed(withId(switchId))
    }

    private fun clickIfDisplayed(matcher: Matcher<View>) {
        waitForView(allOf(matcher, isDisplayed()))?.perform(click())
    }

    private fun checkDefaultRouteOptions(
        avoidTollsShouldBe: Boolean,
        avoidFerriesShouldBe: Boolean,
        avoidDirtRoadShouldBe: Boolean,
        avoidUTurnShouldBe: Boolean,
        avoidTunnelsShouldBe: Boolean
    ) {
        clickIfDisplayed(withText(mActivityRule.activity.getString(R.string.menu_navigate)))
        waitForView(allOf(withContentDescription(AMAZON_MAP_READY), isDisplayed()))

        clickIfDisplayed(withId(R.id.card_direction))
        performSearchAndSelect(R.id.edt_search_direction, SEARCH_TEST_WORD_1)
        performSearchAndSelect(R.id.edt_search_dest, SEARCH_TEST_WORD_2)

        waitForView(allOf(withId(R.id.card_drive_go), isDisplayed()))
        clickIfDisplayed(withId(R.id.card_routing_option))

        getInstrumentation().waitForIdleSync()

        assertSwitchChecked(R.id.switch_avoid_tools, avoidTollsShouldBe)
        assertSwitchChecked(R.id.switch_avoid_ferries, avoidFerriesShouldBe)
        assertSwitchChecked(R.id.switch_avoid_dirt_roads, avoidDirtRoadShouldBe)
        assertSwitchChecked(R.id.switch_avoid_tunnels, avoidTunnelsShouldBe)
        assertSwitchChecked(R.id.switch_avoid_u_turns, avoidUTurnShouldBe)
    }

    private fun performSearchAndSelect(@IdRes editTextId: Int, query: String) {
        waitForView(allOf(withId(editTextId), isDisplayed()))?.perform(click(), replaceText(query))
        waitForView(
            allOf(
                withId(R.id.rv_search_places_suggestion_direction),
                isDisplayed(),
                hasMinimumChildCount(1)
            )
        )?.perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
    }

    private fun assertSwitchChecked(@IdRes switchId: Int, expected: Boolean) {
        onView(withId(switchId)).check { view, _ ->
            val actual = (view as? SwitchCompat)?.isChecked
            Assert.assertTrue(TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED, actual == expected)
        }
    }
}

