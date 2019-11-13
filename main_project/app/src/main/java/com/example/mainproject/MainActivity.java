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

import java.util.ArrayList;
import java.util.List;


import com.anand.brose.graphviewlibrary.GraphView;
import com.anand.brose.graphviewlibrary.WaveSample;


import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private AudioManager myAudioManager;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private static final int PERMISSION_RECORD_AUDIO = 0;
    Thread mThread;


    private AudioRecordingThread recThread;




    private List<WaveSample> pointList = new ArrayList<>();
    private long startTime = 0;


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


        GraphView graphView = findViewById(R.id.graphView);
        graphView.setMaxAmplitude(15000);
        graphView.setMasterList(pointList);
        graphView.startPlotting();



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

//            recThread = new AudioRecordingThread(new AudoRecievedListener() {
//                @Override
//                public void onAudioDataReceived(short[] data) {
//                    for (int i = 0;i<data.length;i=i*10){
//                        int val = data[i];
//                        runOnUiThread(()->{
//                            TextView raw_value = findViewById(R.id.sensor_value);
//                            raw_value.setText(String.valueOf(val));
//
//                        });
//                    }
//                }
//            });
//            recThread.startRecording();


            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    record();
                }
            });
            mThread.start();
        }
    }

    private void record(){
        int audioSource = MediaRecorder.AudioSource.MIC;
        int samplingRate = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(samplingRate,channelConfig,audioFormat);
//        bufferSize=samplingRate*2;

        short[] buffer = new short[bufferSize/2];
        AudioRecord myRecord = new AudioRecord(audioSource,samplingRate,channelConfig,audioFormat,bufferSize);

        myRecord.startRecording();

        runOnUiThread(()->{
            TextView sample_rate = findViewById(R.id.sample_rate);
            sample_rate.setText(String.valueOf(samplingRate));
            TextView buffer_size = findViewById(R.id.buffer_size);
            buffer_size.setText(String.valueOf(buffer.length));
        });

        int noAllRead = 0;

        startTime = System.currentTimeMillis();


        while(true){
//            myRecord.startRecording();
            int bufferResults = myRecord.read(buffer,0,buffer.length);
//            myRecord.stop();
            noAllRead += bufferResults;
            int ii = noAllRead;

            runOnUiThread(()->{
                TextView no_read = findViewById(R.id.no_read_val);
                no_read.setText(String.valueOf(ii));
            });
//            pointList.add(new WaveSample(System.currentTimeMillis()-startTime,buffer[0]));
            if(noAllRead%(buffer.length) == 0){
                for (int i = 0;i<bufferResults;i++){
                    int val = buffer[i];
//                    runOnUiThread(()->{
//                        TextView raw_value = findViewById(R.id.sensor_value);
//                        raw_value.setText(String.valueOf(val));
//
//                    });
                    pointList.add(new WaveSample(System.currentTimeMillis()-startTime,val));
                }
            }



        }
    }
}
