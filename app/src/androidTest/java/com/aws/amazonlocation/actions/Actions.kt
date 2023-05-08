package com.aws.amazonlocation.actions

import android.graphics.Point
import android.os.SystemClock
import android.view.MotionEvent
import android.view.MotionEvent.PointerProperties
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.InjectEventSecurityException
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import com.aws.amazonlocation.NESTED_SCROLL_ERROR
import org.hamcrest.Matcher
import org.hamcrest.Matchers

fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return Matchers.allOf(
                ViewMatchers.isDescendantOfA(ViewMatchers.isAssignableFrom(NestedScrollView::class.java)),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
            )
        }

        override fun getDescription(): String {
            return NESTED_SCROLL_ERROR
        }

        override fun perform(uiController: UiController?, view: View?) {
            try {
                val nestedScrollView = view?.let {
                    findFirstParentLayoutOfClass(
                        it,
                        NestedScrollView::class.java,
                    )
                } as NestedScrollView?
                if (nestedScrollView != null) {
                    view?.let { nestedScrollView.scrollTo(0, it.top) }
                } else {
                    throw java.lang.Exception("Unable to find NestedScrollView parent.")
                }
            } catch (e: java.lang.Exception) {
                throw PerformException.Builder()
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(e)
                    .build()
            }
            uiController?.loopMainThreadUntilIdle()
        }
    }
}

private fun findFirstParentLayoutOfClass(view: View, parentClass: Class<out View>): View? {
    var parent: ViewParent = FrameLayout(view.context)
    var incrementView: ViewParent? = null
    var i = 0
    while (parent.javaClass != parentClass) {
        parent = if (i == 0) {
            findParent(view)
        } else {
            if (incrementView != null) {
                findParent(incrementView)
            } else {
                return null
            }
        }
        incrementView = parent
        i++
    }
    return parent as View
}

private fun findParent(view: View): ViewParent {
    return view.parent
}

private fun findParent(view: ViewParent): ViewParent {
    return view.parent
}

@Suppress("DEPRECATION")
fun clickXYPercent(x: Float, y: Float): ViewAction {
    return GeneralClickAction(
        Tap.SINGLE,
        { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)

            val width = view.width
            val height = view.height

            val xPoint = (width / 100) * x
            val yPoint = (height / 100) * y

            floatArrayOf(xPoint, yPoint)
        },
        Press.FINGER,
    )
}

fun clickOnViewChild(viewId: Int) = object : ViewAction {
    override fun getConstraints() = null

    override fun getDescription() = "Click on a child view with specified id."

    override fun perform(uiController: UiController, view: View) = ViewActions.click()
        .perform(uiController, view.findViewById(viewId))
}

fun pinchOut(): ViewAction? {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isEnabled()
        }

        override fun getDescription(): String {
            return "Pinch out"
        }

        override fun perform(uiController: UiController, view: View) {
            val middlePosition: Point = getCenterPoint(view)
            val startDelta = 0 // How far from the center point each finger should start
            val endDelta =
                500 // How far from the center point each finger should end (note: Be sure to have this large enough so that the gesture is recognized!)
            val startPoint1 = Point(middlePosition.x - startDelta, middlePosition.y)
            val startPoint2 = Point(middlePosition.x + startDelta, middlePosition.y)
            val endPoint1 = Point(middlePosition.x - endDelta, middlePosition.y)
            val endPoint2 = Point(middlePosition.x + endDelta, middlePosition.y)
            performPinch(uiController, startPoint1, startPoint2, endPoint1, endPoint2)
        }
    }
}

fun pinchIn(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isEnabled()
        }

        override fun getDescription(): String {
            return "Pinch in"
        }

        override fun perform(uiController: UiController, view: View) {
            val middlePosition: Point = getCenterPoint(view)
            val startDelta =
                500 // How far from the center point each finger should start (note: Be sure to have this large enough so that the gesture is recognized!)
            val endDelta = 0 // How far from the center point each finger should end
            val startPoint1 = Point(middlePosition.x - startDelta, middlePosition.y)
            val startPoint2 = Point(middlePosition.x + startDelta, middlePosition.y)
            val endPoint1 = Point(middlePosition.x - endDelta, middlePosition.y)
            val endPoint2 = Point(middlePosition.x + endDelta, middlePosition.y)
            performPinch(uiController, startPoint1, startPoint2, endPoint1, endPoint2)
        }
    }
}

private fun getCenterPoint(view: View): Point {
    val locationOnScreen = IntArray(2)
    view.getLocationOnScreen(locationOnScreen)
    val viewHeight = view.height * view.scaleY
    val viewWidth = view.width * view.scaleX
    return Point(
        (locationOnScreen[0] + viewWidth / 2).toInt(),
        (locationOnScreen[1] + viewHeight / 2).toInt(),
    )
}

private fun performPinch(
    uiController: UiController,
    startPoint1: Point,
    startPoint2: Point,
    endPoint1: Point,
    endPoint2: Point,
) {
    val duration = 500
    val eventMinInterval: Long = 10
    val startTime = SystemClock.uptimeMillis()
    var eventTime = startTime
    var event: MotionEvent
    var eventX1: Long
    var eventY1: Long
    var eventX2: Long
    var eventY2: Long
    eventX1 = startPoint1.x.toLong()
    eventY1 = startPoint1.y.toLong()
    eventX2 = startPoint2.x.toLong()
    eventY2 = startPoint2.y.toLong()

    // Specify the property for the two touch points
    val properties = arrayOfNulls<PointerProperties>(2)
    val pp1 = PointerProperties()
    pp1.id = 0
    pp1.toolType = MotionEvent.TOOL_TYPE_FINGER
    val pp2 = PointerProperties()
    pp2.id = 1
    pp2.toolType = MotionEvent.TOOL_TYPE_FINGER
    properties[0] = pp1
    properties[1] = pp2

    // Specify the coordinations of the two touch points
    // NOTE: you MUST set the pressure and size value, or it doesn't work
    val pointerCoords = arrayOfNulls<MotionEvent.PointerCoords>(2)
    val pc1 = MotionEvent.PointerCoords()
    pc1.x = eventX1.toFloat()
    pc1.y = eventY1.toFloat()
    pc1.pressure = 1f
    pc1.size = 1f
    val pc2 = MotionEvent.PointerCoords()
    pc2.x = eventX2.toFloat()
    pc2.y = eventY2.toFloat()
    pc2.pressure = 1f
    pc2.size = 1f
    pointerCoords[0] = pc1
    pointerCoords[1] = pc2

    /*
     * Events sequence of zoom gesture:
     *
     * 1. Send ACTION_DOWN event of one start point
     * 2. Send ACTION_POINTER_DOWN of two start points
     * 3. Send ACTION_MOVE of two middle points
     * 4. Repeat step 3 with updated middle points (x,y), until reach the end points
     * 5. Send ACTION_POINTER_UP of two end points
     * 6. Send ACTION_UP of one end point
     */try {
        // Step 1
        event = MotionEvent.obtain(
            startTime, eventTime,
            MotionEvent.ACTION_DOWN, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0,
        )
        injectMotionEventToUiController(uiController, event)

        // Step 2
        event = MotionEvent.obtain(
            startTime,
            eventTime,
            MotionEvent.ACTION_POINTER_DOWN + (pp2.id shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),
            2,
            properties,
            pointerCoords,
            0,
            0,
            1f,
            1f,
            0,
            0,
            0,
            0,
        )
        injectMotionEventToUiController(uiController, event)

        // Step 3, 4
        val moveEventNumber = duration / eventMinInterval
        val stepX1: Long
        val stepY1: Long
        val stepX2: Long
        val stepY2: Long
        stepX1 = (endPoint1.x - startPoint1.x) / moveEventNumber
        stepY1 = (endPoint1.y - startPoint1.y) / moveEventNumber
        stepX2 = (endPoint2.x - startPoint2.x) / moveEventNumber
        stepY2 = (endPoint2.y - startPoint2.y) / moveEventNumber
        for (i in 0 until moveEventNumber) {
            // Update the move events
            eventTime += eventMinInterval
            eventX1 += stepX1
            eventY1 += stepY1
            eventX2 += stepX2
            eventY2 += stepY2
            pc1.x = eventX1.toFloat()
            pc1.y = eventY1.toFloat()
            pc2.x = eventX2.toFloat()
            pc2.y = eventY2.toFloat()
            pointerCoords[0] = pc1
            pointerCoords[1] = pc2
            event = MotionEvent.obtain(
                startTime, eventTime,
                MotionEvent.ACTION_MOVE, 2, properties,
                pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0,
            )
            injectMotionEventToUiController(uiController, event)
        }

        // Step 5
        pc1.x = endPoint1.x.toFloat()
        pc1.y = endPoint1.y.toFloat()
        pc2.x = endPoint2.x.toFloat()
        pc2.y = endPoint2.y.toFloat()
        pointerCoords[0] = pc1
        pointerCoords[1] = pc2
        eventTime += eventMinInterval
        event = MotionEvent.obtain(
            startTime,
            eventTime,
            MotionEvent.ACTION_POINTER_UP + (pp2.id shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),
            2,
            properties,
            pointerCoords,
            0,
            0,
            1f,
            1f,
            0,
            0,
            0,
            0,
        )
        injectMotionEventToUiController(uiController, event)

        // Step 6
        eventTime += eventMinInterval
        event = MotionEvent.obtain(
            startTime, eventTime,
            MotionEvent.ACTION_UP, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0,
        )
        injectMotionEventToUiController(uiController, event)
    } catch (e: InjectEventSecurityException) {
        throw RuntimeException("Could not perform pinch", e)
    }
}

/**
 * Safely call uiController.injectMotionEvent(event): Detect any error and "convert" it to an
 * IllegalStateException
 */
@Throws(InjectEventSecurityException::class)
private fun injectMotionEventToUiController(uiController: UiController, event: MotionEvent) {
    val injectEventSucceeded = uiController.injectMotionEvent(event)
    check(injectEventSucceeded) { "Error performing event $event" }
}
