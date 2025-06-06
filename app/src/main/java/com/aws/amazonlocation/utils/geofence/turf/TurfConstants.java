package com.aws.amazonlocation.utils.geofence.turf;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

public class TurfConstants {

    private TurfConstants() {
        // Private constructor preventing initialization of this class
    }

    /**
     * The mile is an English unit of length of linear measure equal to 5,280 feet, or 1,760 yards,
     * and standardised as exactly 1,609.344 meters by international agreement in 1959.
     *
     */
    public static final String UNIT_MILES = "miles";

    /**
     * The nautical mile per hour is known as the knot. Nautical miles and knots are almost
     * universally used for aeronautical and maritime navigation, because of their relationship with
     * degrees and minutes of latitude and the convenience of using the latitude scale on a map for
     * distance measuring.
     *
     */
    public static final String UNIT_NAUTICAL_MILES = "nauticalmiles";

    /**
     * The kilometer (American spelling) is a unit of length in the metric system, equal to one
     * thousand meters. It is now the measurement unit used officially for expressing distances
     * between geographical places on land in most of the world; notable exceptions are the United
     * States and the road network of the United Kingdom where the statute mile is the official unit
     * used.
     * <p>
     * In many Turf calculations, if a unit is not provided, the output value will fallback onto using
     * this unit. See {@link #UNIT_DEFAULT} for more information.
     * </p>
     *
     */
    public static final String UNIT_KILOMETERS = "kilometers";

    /**
     * The radian is the standard unit of angular measure, used in many areas of mathematics.
     *
     */
    public static final String UNIT_RADIANS = "radians";

    /**
     * A degree, is a measurement of a plane angle, defined so that a full rotation is 360 degrees.
     *
     */
    public static final String UNIT_DEGREES = "degrees";

    /**
     * The inch (abbreviation: in or &quot;) is a unit of length in the (British) imperial and United
     * States customary systems of measurement now formally equal to 1/36th yard but usually
     * understood as 1/12th of a foot.
     *
     */
    public static final String UNIT_INCHES = "inches";

    /**
     * The yard (abbreviation: yd) is an English unit of length, in both the British imperial and US
     * customary systems of measurement, that comprises 3 feet or 36 inches.
     */
    public static final String UNIT_YARDS = "yards";

    /**
     * The metre (international spelling) or meter (American spelling) is the base unit of length in
     * the International System of Units (SI).
     *
     */
    public static final String UNIT_METERS = "meters";

    /**
     * A centimeter (American spelling) is a unit of length in the metric system, equal to one
     * hundredth of a meter.
     *
     */
    public static final String UNIT_CENTIMETERS = "centimeters";

    /**
     * The foot is a unit of length in the imperial and US customary systems of measurement.
     *
     */
    public static final String UNIT_FEET = "feet";

    /**
     * A centimetre (international spelling) is a unit of length in the metric system, equal to one
     * hundredth of a meter.
     *
     */
    public static final String UNIT_CENTIMETRES = "centimetres";

    /**
     * The metre (international spelling) is the base unit of length in
     * the International System of Units (SI).
     *
     */
    public static final String UNIT_METRES = "metres";

    /**
     * The kilometre (international spelling) is a unit of length in the metric system, equal to one
     * thousand metres. It is now the measurement unit used officially for expressing distances
     * between geographical places on land in most of the world; notable exceptions are the United
     * States and the road network of the United Kingdom where the statute mile is the official unit
     * used.
     *
     */
    public static final String UNIT_KILOMETRES = "kilometres";

    /**
     * Retention policy for the various Turf units.
     *
     */
    @Retention(RetentionPolicy.CLASS)
    @StringDef({
            UNIT_KILOMETRES,
            UNIT_METRES,
            UNIT_CENTIMETRES,
            UNIT_FEET,
            UNIT_CENTIMETERS,
            UNIT_METERS,
            UNIT_YARDS,
            UNIT_INCHES,
            UNIT_DEGREES,
            UNIT_RADIANS,
            UNIT_KILOMETERS,
            UNIT_MILES,
            UNIT_NAUTICAL_MILES
    })
    public @interface TurfUnitCriteria {
    }

    /**
     * The default unit used in most Turf methods when no other unit is specified is kilometers.
     *
     */
    public static final String UNIT_DEFAULT = UNIT_KILOMETERS;
}