package de.dennisguse.opentracks.sensors;

import android.hardware.SensorManager;

import androidx.annotation.VisibleForTesting;

public class PressureSensorUtils {

    //Everything above is considered a meaningful change in altitude.
    private static final float ALTITUDE_CHANGE_DIFF_M = 3.0f;

    private static final float EXPONENTIAL_SMOOTHING = 0.3f;

    private static final float p0 = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;

    private PressureSensorUtils() {
    }

    public static class AltitudeChange {

        private final float currentSensorValueHPa;

        private final float altitudeChangeM;

        public AltitudeChange(float currentSensorValueHPa, float altitudeChangeM) {
            this.currentSensorValueHPa = currentSensorValueHPa;
            this.altitudeChangeM = altitudeChangeM;
        }

        public float getCurrentSensorValueHPa() {
            return currentSensorValueHPa;
        }

        public float getAltitudeChangeM() {
            return altitudeChangeM;
        }

        public float getAltitudeGainM() {
            return altitudeChangeM > 0 ? altitudeChangeM : 0;
        }

        public float getAltitudeLossM() {
            return altitudeChangeM < 0 ? -1 * altitudeChangeM : 0;
        }
    }

    /**
     * Applies exponential smoothing to sensor value before computation.
     */
    public static AltitudeChange computeChangesWithSmoothing_m(float lastAcceptedSensorValue_hPa, float lastSeenSensorValue_hPa, float currentSensorValue_hPa) {
        float nextSensorValue_hPa = EXPONENTIAL_SMOOTHING * currentSensorValue_hPa + (1 - EXPONENTIAL_SMOOTHING) * lastSeenSensorValue_hPa;

        return computeChanges(lastAcceptedSensorValue_hPa, nextSensorValue_hPa);
    }

    @VisibleForTesting
    public static AltitudeChange computeChanges(float lastAcceptedSensorValue_hPa, float currentSensorValueHPa) {
        float lastSensorValue_m = SensorManager.getAltitude(p0, lastAcceptedSensorValue_hPa);
        float currentSensorValue_m = SensorManager.getAltitude(p0, currentSensorValueHPa);

        float altitudeChangeM = currentSensorValue_m - lastSensorValue_m;
        if (Math.abs(altitudeChangeM) < ALTITUDE_CHANGE_DIFF_M) {
            return null;
        }

        // Limit altitudeC change by ALTITUDE_CHANGE_DIFF and computes pressure value accordingly.
        AltitudeChange altitudeChange = new AltitudeChange(currentSensorValueHPa, altitudeChangeM);
        if (altitudeChange.getAltitudeChangeM() > 0) {
            return new AltitudeChange(getBarometricPressure(lastSensorValue_m + ALTITUDE_CHANGE_DIFF_M), ALTITUDE_CHANGE_DIFF_M);
        } else {
            return new AltitudeChange(getBarometricPressure(lastSensorValue_m - ALTITUDE_CHANGE_DIFF_M), -1 * ALTITUDE_CHANGE_DIFF_M);
        }
    }

    /*
     * Barometeric pressure to altitude estimation; inverts of SensorManager.getAltitude(float, float)
     * https://de.wikipedia.org/wiki/Barometrische_H%C3%B6henformel#Internationale_H%C3%B6henformel
     * {\color{White} p(h)} = p_0 \cdot \left( 1 - \frac{0{,}0065 \frac{\mathrm K}{\mathrm m} \cdot h}{T_0\ } \right)^{5{,}255}
     */
    @VisibleForTesting
    public static float getBarometricPressure(float altitude_m) {
        return (float) (p0 * Math.pow(1.0 - 0.0065 * altitude_m / 288.15, 5.255f));
    }
}
