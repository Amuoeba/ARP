package com.example.labsone_aware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
//import androidx.work.Worker;


import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Temperature;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Temperature_Provider;

// Check WorkManager ... bundles together different tasks so that interval apps are executed in bundless together (wich saves energy)
// Check context
// Start sesrvice as foreground service overcomes the issue of manager shutting down the services


class SensingWorker extends Worker{


    public SensingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public Result doWork(){

        return Result.success();
    }
}

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Aware.startAWARE(this);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER,20000);
        Aware.setSetting(this,Aware_Preferences.THRESHOLD_ACCELEROMETER,0.02f);

        Aware.startAccelerometer(this);

        Accelerometer.AWARESensorObserver mySensorObserver = new Accelerometer.AWARESensorObserver() {
            @Override
            public void onAccelerometerChanged(ContentValues data) {
                System.out.println(data);
                final double x = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0);
                final double y = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1);
                final double z = data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tvAccXValue = (TextView) findViewById(R.id.tv_acc_x_value);
                        tvAccXValue.setText(Double.toString(x));
                        TextView tvAccYValue = (TextView) findViewById(R.id.tv_acc_y_value);
                        tvAccYValue.setText(Double.toString(y));
                        TextView tvAccZValue = (TextView) findViewById(R.id.tv_acc_z_value);
                        tvAccZValue.setText(Double.toString(z));
                    }
                });
            }
        };

        Temperature.AWARESensorObserver tempObserver = new Temperature.AWARESensorObserver() {
            @Override
            public void onTemperatureChanged(ContentValues data) {
                final double temp = data.getAsDouble(Temperature_Provider.Temperature_Data.TEMPERATURE_CELSIUS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tempField = (TextView) findViewById(R.id.tv_temp_value);
                        tempField.setText(Double.toString(temp));
                    }
                });


            }
        };



        Temperature.setSensorObserver(tempObserver);
        Accelerometer.setSensorObserver(mySensorObserver);

    }

    @Override
    protected void onResume(){
        super.onResume();
        Aware.startTemperature(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        Aware.stopAccelerometer(this);
        Aware.stopTemperature(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Aware.stopAccelerometer(this);
        Aware.stopTemperature(this);
    }




}
