package com.example.mainproject;


import android.media.AudioRecord;
import android.media.MediaRecorder;

import android.media.AudioManager;
import android.media.AudioFormat;



public class AudioRecordingThread {

    private static final int SAMPLE_RATE = 16000;

    public AudioRecordingThread(AudoRecievedListener listener){
        mlistener = listener;
    };

    private AudoRecievedListener mlistener;
    private Thread mThread;


    public void startRecording() {

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });
        mThread.start();
    }


    private void record(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        int audioSource = MediaRecorder.AudioSource.MIC;
        int samplingRate = SAMPLE_RATE;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(samplingRate,channelConfig,audioFormat);

        short[] buffer = new short[bufferSize/2];
        AudioRecord myRecord = new AudioRecord(audioSource,samplingRate,channelConfig,audioFormat,bufferSize);

        myRecord.startRecording();

        int noAllRead = 0;
        while(true){
            int bufferResults = myRecord.read(buffer,0,buffer.length);
            noAllRead += bufferResults;
            mlistener.onAudioDataReceived(buffer);
        }

    }

}
