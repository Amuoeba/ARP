package com.example.mainproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;


import android.content.pm.PackageManager;
import android.Manifest;

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.os.Bundle;



import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private AudioManager myAudioManager;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private static final int PERMISSION_RECORD_AUDIO = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.RECORD_AUDIO },
                        PERMISSION_RECORD_AUDIO);
                return;
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        1);
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.RECORD_AUDIO },
                        PERMISSION_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else{

            myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            String x = myAudioManager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED);

            runOnUiThread(()->{
                TextView tvAccXValue = findViewById(R.id.raw_available);
                tvAccXValue.setText(x);
            });

            int audioSource = MediaRecorder.AudioSource.MIC;
            int samplingRate = 11025;
            int channelConfig = AudioFormat.CHANNEL_IN_DEFAULT;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(samplingRate,channelConfig,audioFormat);

            short[] buffer = new short[bufferSize/4];
            AudioRecord myRecord = new AudioRecord(audioSource,samplingRate,channelConfig,audioFormat,bufferSize);

            myRecord.startRecording();

            int bufferResults = myRecord.read(buffer,0,bufferSize/4);

            for (int i = 0;i<bufferResults;i++){
                runOnUiThread(()->{
                    TextView raw_value = findViewById(R.id.sensor_value);
                    raw_value.setText(Arrays.toString(buffer));
                });
            }
        }





    }
}
