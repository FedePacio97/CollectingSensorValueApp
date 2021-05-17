package com.example.sensortrial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mRotationVector;

    private Context context;

    private final int SAMPLING_RATE = 100000; //Us //0.1s = 10Hz
    private final int WINDOW = 5000000; //Us //5s

    private PreprocessingManager preprocessingManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        context = getApplicationContext();

        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            //ABORT!
        }

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            //ABORT!
        }

        preprocessingManager = new PreprocessingManager(context,WINDOW,SAMPLING_RATE);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SAMPLING_RATE);
        mSensorManager.registerListener(this, mRotationVector, SAMPLING_RATE);
        mSensorManager.registerListener(this, mGyroscope, SAMPLING_RATE);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected void onStop() {
        super.onStop();
        //preprocessingManager.export_csv();
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        String sensorName = sensorEvent.sensor.getName();

        if(sensorEvent.sensor.getStringType().equals("android.sensor.gyroscope"))
            preprocessingManager.log_gyroscope(sensorEvent.timestamp,
                    sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]);
        if(sensorEvent.sensor.getStringType().equals("android.sensor.accelerometer"))
            preprocessingManager.log_accelerometer(sensorEvent.timestamp,
                    sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]);
        if(sensorEvent.sensor.getStringType().equals("android.sensor.rotation_vector"))
            preprocessingManager.log_orientation(sensorEvent.timestamp,
                    sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]);

        //Log.d("Debug","[" + sensorEvent.timestamp + "]" + sensorName + ": X: " + sensorEvent.values[0] + "; Y: " + sensorEvent.values[1] + "; Z: " + sensorEvent.values[2] + ";");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("Debug","accuracy" + accuracy);
    }


}