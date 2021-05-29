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
import android.media.AudioManager;
import android.media.Image;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.wekamanager.WekaManager;
import org.w3c.dom.Text;
import weka.classifiers.Classifier;
import weka.core.Instances;

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

    private final int COUNTDOWN_BEFORE_STARTING = 5000; //ms
    private final int RECORDING_PERIOD = 21000; //ms = 21 sec


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

        //Wait for user input
        ImageView type_of_exercise_image = findViewById(R.id.push_ups_type);
        final Button button_fast = findViewById(R.id.fast);
        button_fast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                type_of_exercise_image.setImageResource(R.drawable.normal_push_ups);

                //start activity
                startActivityProcedure("fast_push_ups");
            }
        });

        final Button button_normal = findViewById(R.id.normal_speed);
        button_normal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                type_of_exercise_image.setImageResource(R.drawable.normal_push_ups);

                //start activity
                startActivityProcedure("normal_push_ups");
            }
        });

        final Button button_slow = findViewById(R.id.slow);
        button_slow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                type_of_exercise_image.setImageResource(R.drawable.normal_push_ups);

                //start activity
                startActivityProcedure("slow_push_ups");
            }
        });


        final Button button_half_top = findViewById(R.id.half_top);
        button_half_top.setOnClickListener(v -> {
            // Code here executes on main thread after user presses button

            type_of_exercise_image.setImageResource(R.drawable.half_top_push_ups);

            //start activity
            startActivityProcedure("half_top_push_ups");
        });

        final Button button_half_bottom = findViewById(R.id.half_bottom);
        button_half_bottom.setOnClickListener(v -> {
            // Code here executes on main thread after user presses button

            type_of_exercise_image.setImageResource(R.drawable.half_bottom_push_ups);

            //start activity
            startActivityProcedure("half_bottom_push_ups");
        });

        final Button button_upper_body = findViewById(R.id.upper_body);
        button_upper_body.setOnClickListener(v -> {
            // Code here executes on main thread after user presses button

            type_of_exercise_image.setImageResource(R.drawable.upper_body_push_ups);

            //start activity
            startActivityProcedure("upper_body_push_ups");
        });

        final Button button_lower_body = findViewById(R.id.lower_body);
        button_lower_body.setOnClickListener(v -> {
            // Code here executes on main thread after user presses button

            type_of_exercise_image.setImageResource(R.drawable.lower_body_push_ups);

            //start activity
            startActivityProcedure("lower_body_push_ups");
        });

    }

    private void startActivityProcedure(String type_of_exercise) {

        EditText countdown_number = findViewById(R.id.countdown);

        CountDownTimer samplingActivityCountDown = new CountDownTimer(RECORDING_PERIOD, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdown_number.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT,500);

                stopCollectingData();
            }
        };

        new CountDownTimer(COUNTDOWN_BEFORE_STARTING, 1000) {

            public void onTick(long millisUntilFinished) {
                countdown_number.setText(String.valueOf(millisUntilFinished / 1000));
                //Make a bip
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
            }

            public void onFinish() {
                countdown_number.setText("START");
                //Make a longer bip
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT,150);

                //START RECORDING ACTIVITY
                startCollectingData(type_of_exercise);
                samplingActivityCountDown.start();
            }
        }.start();

    }

    protected void onResume() {
        //Called also when app is launched at first time
        super.onResume();

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        Classifier c = WekaManager.loadClassifier(getResources().openRawResource(R.raw.default_model));
        //Log.d("classify", WekaManager.classify(c, getResources().openRawResource(R.raw.unlabeled)).toString());
        /*
        Double[] sample = {-9.421097861,-9.330559731,-8.719779968,-8.464389801,-10.30920029,1.844810486,0.372928528,9.428476049,0.0,1.0,0.94788093,0.911319017,1.131260037,1.49890995,0.650399029,0.848510921,0.130205893,0.956782019,0.0,1.0,-2.91955052,-2.888979912,-2.699860096,-2.620790005,-3.191930056,0.571140051,0.1181906,2.921941864,
                0.0,63.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.611610651,0.611610651,0.611610651,0.611610651,0.611610651,0.0,0.0,0.611610651,0.0,1.0,0.522665441,0.522665441,0.522665441,0.522665441,0.522665441,0.0,0.0,0.522665441,0.0,63.0,-0.411872119,-0.411872119,-0.411872119,-0.411872119,-0.411872119,0.0,0.0,0.411872119,0.0,1.0};
        Log.d("classify", WekaManager.classify(c, sample));

         */ //DEBUG
    }

    protected void onStop() {
        super.onStop();
        //preprocessingManager.export_csv();
    }

    @Override
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

        Log.d("Debug","[" + sensorEvent.timestamp + "]" + sensorName + ": X: " + sensorEvent.values[0] + "; Y: " + sensorEvent.values[1] + "; Z: " + sensorEvent.values[2] + ";");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("Debug","accuracy" + accuracy);
    }

    public void startCollectingData(String type_of_exercise){
        /*mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
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
        }*/

        preprocessingManager = new PreprocessingManager(context,WINDOW,SAMPLING_RATE,type_of_exercise);

        //start collecting data
        mSensorManager.registerListener(this, mAccelerometer, SAMPLING_RATE);
        mSensorManager.registerListener(this, mRotationVector, SAMPLING_RATE);
        mSensorManager.registerListener(this, mGyroscope, SAMPLING_RATE);
    }

    public void stopCollectingData(){

        mSensorManager.unregisterListener(this);
    }


}