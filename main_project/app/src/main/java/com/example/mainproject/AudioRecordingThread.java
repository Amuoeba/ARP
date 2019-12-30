package com.example.mainproject;


import android.media.AudioRecord;
import android.media.MediaRecorder;

import android.media.AudioManager;
import android.media.AudioFormat;

import com.konovalov.vad.VadConfig;
import com.konovalov.vad.Vad;
import com.konovalov.vad.VadListener;


public class AudioRecordingThread {

    private Thread thread;
    private static final int SAMPLE_RATE = 16000;
    private AudioRecord audioRecord;
    private int bufferSize;
    private Listener callback;
    private Vad vad;

    public AudioRecordingThread(Listener callback, VadConfig config){
        this.callback = callback;
        this.vad = new Vad(config);
    }


    public void start(){
        audioRecord = createAudioRecord();
        audioRecord.startRecording();
        thread = new Thread(new ProcessAudio());
        System.out.println("Debug: thread started");
        thread.start();
        System.out.println("Debug: Vad started");
        vad.start();

    }

    private AudioRecord createAudioRecord(){
        int audioSource = MediaRecorder.AudioSource.MIC;
        int samplingRate = SAMPLE_RATE;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(samplingRate,channelConfig,audioFormat);
        this.bufferSize = bufferSize;
        final AudioRecord audioRecord =new AudioRecord(audioSource,samplingRate,channelConfig,audioFormat,bufferSize);

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            System.out.println("Debug: Retruning audio recoreder");
            return audioRecord;
        }else{
            audioRecord.release();
        }
        return null;
    }

    private class ProcessAudio implements Runnable{
        @Override
        public void run(){
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            while (true){
                short[] buffer = new short[bufferSize/8];
                audioRecord.read(buffer,0,buffer.length);
                isSpeechDetected(buffer);
            }
        }

        private void isSpeechDetected(short[] buffer){
            vad.isContinuousSpeech(buffer, new VadListener() {
                @Override
                public void onSpeechDetected() {
                    callback.onSpeechDetected();
                }

                @Override
                public void onNoiseDetected() {
                    callback.onNoiseDetected();
                }
            });
        }
    }

    public interface Listener {
        void onSpeechDetected();

        void onNoiseDetected();
    }

}
