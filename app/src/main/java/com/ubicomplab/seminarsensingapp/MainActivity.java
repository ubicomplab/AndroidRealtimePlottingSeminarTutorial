package com.ubicomplab.seminarsensingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Sensor accelerometer;
    private TextView indicatorY;
    private TextView indicatorX;
    private TextView indicatorZ;
    private Button button;
    private float accel_x = 0;
    private float accel_y = 0;
    private float accel_z = 0;
    private float accel_w = 0;
    private float timestamp = 0;
    private boolean RUNNING = false;

    private CustomCanvas plot;

    public void updatePlot(float newAccelX, float newAccelY, float newAccelZ) throws Exception {
        plot.updatePlot(newAccelX, newAccelY, newAccelZ);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                plot.invalidate();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        indicatorX = findViewById(R.id.indicatorX);
        indicatorY = findViewById(R.id.indicatorY);
        indicatorZ = findViewById(R.id.indicatorZ);
        button = findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RUNNING) {
                    RUNNING = false;
                } else {
                    RUNNING = true;
                }
            }
        });


        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            sensorManager.registerListener(
                    accelerometerListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.i("oops!", "No accelerometer found!");
        }

        // Initialize plot view with the size of the display.
        plot = findViewById(R.id.custom_canvas);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        // Values are set before init() as they are used in init().
        //plot.maxRange = accelerometer.getMaximumRange();
        plot.screenHeight = screenHeight;
        plot.screenWidth = screenWidth;
        // Set the number of segments to divide the plot view into in both X and Y directions.
        int timeWindowSize = 50;
        int numAccelBins = 200;
        plot.init(numAccelBins, timeWindowSize);

    }

    // Defines the behavior of the sensor when the accuracy or actual measurement change.
    private final SensorEventListener accelerometerListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Runs once per measurement form the sensor.
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                timestamp = event.timestamp;
                accel_x = event.values[0];
                accel_y = event.values[1];
                accel_z = event.values[2];

                // This updates anytime the sensor value changes. To get updates at a regular
                // frequency, you'll need to move the following code to a countdown timer.
                indicatorX.setText("X: " + accel_x);
                indicatorY.setText("Y: " + accel_y);
                indicatorZ.setText("Z: " + accel_z);
                if (RUNNING) {
                    //Log.i("accel readings", "X: " + accel_x + " Y: " + accel_y + " Z: " + accel_z);
                    try {
                        updatePlot(accel_x, accel_y, accel_z);
                    } catch (Exception e) {
                        Log.i("oops", "Could not update plot! Go figure out why...");
                    }
                }
            }
        }

    };
}