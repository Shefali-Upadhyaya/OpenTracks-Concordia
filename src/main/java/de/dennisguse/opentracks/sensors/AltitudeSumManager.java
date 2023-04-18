package de.dennisguse.opentracks.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.concurrent.TimeUnit;

import de.dennisguse.opentracks.data.models.TrackPoint;

/**
 * Estimates the altitude gain and altitude loss using the device's pressure sensor (i.e., barometer).
 */
public class AltitudeSumManager implements SensorEventListener {

    private static final String TAG = AltitudeSumManager.class.getSimpleName();

    private boolean isConnected = false;

    private float lastAcceptedPressureValueHPa;

    private float lastSeenSensorValueHPa;

    private Float altitudeGainM;
    private Float altitudeLossM;

    public void start(Context context, Handler handler) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (pressureSensor == null) {
            Log.w(TAG, "No pressure sensor available.");
            isConnected = false;
        } else {
            isConnected = sensorManager.registerListener(this, pressureSensor, (int) TimeUnit.SECONDS.toMicros(5), handler);
        }

        lastAcceptedPressureValueHPa = Float.NaN;
        reset();
    }

    public void stop(Context context) {
        Log.d(TAG, "Stop");

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);

        isConnected = false;
        reset();
    }

    @VisibleForTesting
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void fill(@NonNull TrackPoint trackPoint) {
        trackPoint.setAltitudeGain(altitudeGainM);
        trackPoint.setAltitudeLoss(altitudeLossM);
    }

    @Nullable
    public Float getAltitudeGainM() {
        return isConnected ? altitudeGainM : null;
    }

    @VisibleForTesting
    public void setAltitudeGainM(float altitudeGainM) {
        this.altitudeGainM = altitudeGainM;
    }

    @VisibleForTesting
    public void addAltitudeGainM(float altitudeGainM) {
        this.altitudeGainM = this.altitudeGainM == null ? 0f : this.altitudeGainM;
        this.altitudeGainM += altitudeGainM;
    }

    @VisibleForTesting
    public void addAltitudeLossM(Float altitudeLossM) {
        this.altitudeLossM = this.altitudeLossM == null ? 0f : this.altitudeLossM;
        this.altitudeLossM += altitudeLossM;
    }

    @Nullable
    public Float getAltitudeLossM() {
        return isConnected ? altitudeLossM : null;
    }

    @VisibleForTesting
    public void setAltitudeLossM(float altitudeLossM) {
        this.altitudeLossM = altitudeLossM;
    }

    public void reset() {
        Log.d(TAG, "Reset");
        altitudeGainM = null;
        altitudeLossM = null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.w(TAG, "Sensor accuracy changes are (currently) ignored.");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isConnected) {
            Log.w(TAG, "Not connected to sensor, cannot process data.");
            return;
        }
        onSensorValueChanged(event.values[0]);
    }

    @VisibleForTesting
    void onSensorValueChanged(float value_hPa) {
        if (Float.isNaN(lastAcceptedPressureValueHPa)) {
            lastAcceptedPressureValueHPa = value_hPa;
            lastSeenSensorValueHPa = value_hPa;
            return;
        }

        altitudeGainM = altitudeGainM != null ? altitudeGainM : 0;
        altitudeLossM = altitudeLossM != null ? altitudeLossM : 0;

        PressureSensorUtils.AltitudeChange altitudeChange = PressureSensorUtils.computeChangesWithSmoothing_m(lastAcceptedPressureValueHPa, lastSeenSensorValueHPa, value_hPa);
        if (altitudeChange != null) {
            altitudeGainM += altitudeChange.getAltitudeGainM();

            altitudeLossM += altitudeChange.getAltitudeLossM();

            lastAcceptedPressureValueHPa = altitudeChange.getCurrentSensorValueHPa();
        }

        lastSeenSensorValueHPa = value_hPa;

        Log.v(TAG, "altitude gain: " + altitudeGainM + ", altitude loss: " + altitudeLossM);
    }
}
