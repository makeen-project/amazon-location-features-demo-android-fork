package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.junit.*

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentDefaultRouteTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, true, false)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        val preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())

        // Default route options disabled by default.
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_AVOID_TOLLS)
        preferenceManager.removeValue(KEY_AVOID_FERRIES)

        mActivityRule.launchActivity(null)
    }

    @Test
    fun checkAllOptionsDisabled() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            goToDefaultRouteSettings()

            checkDefaultRouteOptions(avoidTollsShouldBe = false, avoidFerriesShouldBe = false)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    @Test
    fun checkAvoidTollEnabled() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            goToDefaultRouteSettings()

            toggleSwitch(R.id.switch_avoid_tools)

            checkDefaultRouteOptions(avoidTollsShouldBe = true, avoidFerriesShouldBe = false)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    @Test
    fun checkAvoidFerryEnabled() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            goToDefaultRouteSettings()

            toggleSwitch(R.id.switch_avoid_ferries)

            checkDefaultRouteOptions(avoidTollsShouldBe = false, avoidFerriesShouldBe = true)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    @Test
    fun checkAllOptionsEnabled() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            goToDefaultRouteSettings()

            toggleSwitch(R.id.switch_avoid_tools)
            toggleSwitch(R.id.switch_avoid_ferries)

            checkDefaultRouteOptions(avoidTollsShouldBe = true, avoidFerriesShouldBe = true)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToDefaultRouteSettings() {
        onView(
            allOf(
                withText(SETTINGS),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )
            .perform(click())

        Thread.sleep(DELAY_1000)

        onView(
            allOf(
                withId(R.id.cl_route_option),
                isDisplayed()
            )
        )
            .perform(click())
    }

    private fun toggleSwitch(@IdRes switchId: Int) {
        onView(
            allOf(
                withId(switchId),
                isDisplayed()
            )
        )
            .perform(click())
    }

    private fun checkDefaultRouteOptions(avoidTollsShouldBe: Boolean, avoidFerriesShouldBe: Boolean) {
        onView(
            allOf(
                withText(EXPLORE),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )
            .perform(click())

        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)

        val cardDirection =
            mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_direction)
        if (cardDirection.visibility == View.VISIBLE) {
            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(ViewAssertions.matches(isDisplayed()))
            cardDirectionTest.perform(click())
            uiDevice.wait(
                Until.hasObject(By.res("com.aws.amazonlocation:id/edt_search_direction")),
                DELAY_5000
            )
            val edtSearchDirection =
                mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
            if (edtSearchDirection.visibility == View.VISIBLE) {
                typeTextAndSelectSuggestion(R.id.edt_search_direction, SEARCH_TEST_WORD_1)
                typeTextAndSelectSuggestion(R.id.edt_search_dest, SEARCH_TEST_WORD_2)

                uiDevice.wait(
                    Until.hasObject(By.res("com.aws.amazonlocation:id/card_routing_option")),
                    DELAY_10000
                )

                val optionsContainer = mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_list_routes_option)

                if (!optionsContainer.isVisible) {
                    onView(withId(R.id.card_routing_option)).perform(click())
                }

                val switchAvoidToll = mActivityRule.activity.findViewById<SwitchCompat>(R.id.switch_avoid_tools)
                val switchAvoidFerry = mActivityRule.activity.findViewById<SwitchCompat>(R.id.switch_avoid_ferries)

                Thread.sleep(DELAY_1000)

                if (switchAvoidToll.isChecked != avoidTollsShouldBe || switchAvoidFerry.isChecked != avoidFerriesShouldBe) {
                    Assert.fail(TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED)
                }
            } else {
                Assert.fail(TEST_FAILED_SEARCH_DIRECTION)
            }
        } else {
            Assert.fail(TEST_FAILED_DIRECTION_CARD)
        }
    }

    private fun typeTextAndSelectSuggestion(@IdRes viewId: Int, text: String) {
        onView(withId(viewId)).perform(ViewActions.replaceText(text))
        Thread.sleep(DELAY_2000)
        uiDevice.wait(
            Until.hasObject(By.res("com.aws.amazonlocation:id/rv_search_places_suggestion_direction")),
            DELAY_10000
        )
        val rvSearchPlacesSuggestionDirection =
            mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion_direction)
        rvSearchPlacesSuggestionDirection.adapter?.itemCount?.let {
            if (it > 0) {
                onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click()
                    )
                )
            }
        }
    }

    @After
    fun tearDown() {
        mActivityRule.finishActivity()
    }
}
