package com.example.lendtracker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Listens to the accelerometer and calls onShake() when the phone is shaken.
 * In the app: shake the phone on the Home screen to quickly add a new item.
 */
public class ShakeDetector implements SensorEventListener {

    public interface OnShakeListener {
        void onShake();
    }

    private static final float SHAKE_THRESHOLD_G = 2.5f; // how strong the shake must be
    private static final long MIN_TIME_BETWEEN_SHAKES = 1500; // ms

    private final OnShakeListener listener;
    private long lastShakeTime = 0;

    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

        // total force (1.0 = phone lying still)
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_G) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > MIN_TIME_BETWEEN_SHAKES) {
                lastShakeTime = now;
                listener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not needed
    }
}
