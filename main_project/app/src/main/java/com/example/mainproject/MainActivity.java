package com.example.mainproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.anand.brose.graphviewlibrary.GraphView;
import com.anand.brose.graphviewlibrary.WaveSample;

import com.konovalov.vad.VadConfig;




class DebugUtills {
    public static void printeEnvVariables(){
        Map<String, String> env = System.getenv();
        // Java 8
        //env.forEach((k, v) -> System.out.println(k + ":" + v));

        // Classic way to loop a map
        for (Map.Entry<String, String> entry : env.entrySet()) {
            System.out.println("Env variable: " + entry.getKey() + " : " + entry.getValue());
        }
    }
}



public class MainActivity extends AppCompatActivity implements View.OnClickListener, AudioRecordingThread.Listener{

    private AudioManager myAudioManager;
    // Permission related
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int PERMISSION_RECORD_AUDIO = 0;


    // VAD configuration
    private VadConfig.SampleRate DEFAULT_SAMPLE_RATE = VadConfig.SampleRate.SAMPLE_RATE_16K;
    private VadConfig.FrameSize DEFAULT_FRAME_SIZE = VadConfig.FrameSize.FRAME_SIZE_160;
    private VadConfig.Mode DEFAULT_MODE = VadConfig.Mode.VERY_AGGRESSIVE;
    private int DEFAULT_SILENCE_DURATION = 500;
    private int DEFAULT_VOICE_DURATION = 500;
    private VadConfig config;
    private AudioRecordingThread recorder;

    // User interface
    private Button next_button;
    private Button next_button_2;

//    private AudioRecordingThread recThread;
    Thread mThread;
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


        next_button = findViewById(R.id.next1);
        next_button.setOnClickListener(this);

        next_button_2 = findViewById(R.id.next2);
        next_button_2.setOnClickListener(this);



        config = VadConfig.newBuilder()
                .setSampleRate(DEFAULT_SAMPLE_RATE)
                .setFrameSize(DEFAULT_FRAME_SIZE)
                .setMode(DEFAULT_MODE)
                .setSilenceDurationMillis(DEFAULT_SILENCE_DURATION)
                .setVoiceDurationMillis(DEFAULT_VOICE_DURATION)
                .build();

        recorder = new AudioRecordingThread(this,config);


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


            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    record();
                }
            });
            mThread.start();
            recorder.start();
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.next1:
                System.out.println("Debug: next clicked");
                break;

            case R.id.next2:
                System.out.println("Debug: 2 clicked");
                break;
        }
    }

    @Override
    public void onSpeechDetected(){
//        System.out.println("Debug: speech detected");
        runOnUiThread(()->{
            TextView isSpeech = findViewById(R.id.is_speech_value);
            isSpeech.setText("YES");
        });
    }

    @Override
    public void onNoiseDetected(){
        runOnUiThread(()->{
            TextView isSpeech = findViewById(R.id.is_speech_value);
            isSpeech.setText("NO");
        });

//        System.out.println("Debug: noise detected");
    }



    private void record(){
        int audioSource = MediaRecorder.AudioSource.MIC;
        int samplingRate = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(samplingRate,channelConfig,audioFormat);
//        bufferSize=samplingRate*2;

        short[] buffer = new short[bufferSize/8];
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

            int[] sample = new int[160];
            int sample_counter = 0;

            if(noAllRead%(buffer.length) == 0){
                for (int i = 0;i<bufferResults;i++){
                    int val = buffer[i];
                    pointList.add(new WaveSample(System.currentTimeMillis()-startTime,val));
                }
            }
        }
    }
}
