package com.example.ineedpcm;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AudioRecord mAudioRecord;
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int sampleRateInHz = 44100;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int channelCofig = 2;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelCofig, audioFormat);
    private boolean isRecording = false;
    private  RecordThread recordThread;
    private Button btnRecord;
    private Button btnStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnRecord = findViewById(R.id.btn_record);
        btnStop = findViewById(R.id.btn_stop);
        btnRecord.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelCofig, audioFormat, bufferSizeInBytes);
        recordThread = new RecordThread();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_record:
                mAudioRecord.startRecording();
                isRecording = true;
                recordThread.start();
                break;

            case R.id.btn_stop:
                isRecording = false;
                try {
                    recordThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recordThread = null;
                mAudioRecord.stop();
                mAudioRecord.release();

                break;
        }
    }

    class RecordThread extends  Thread {
        @Override
        public void run() {
            File outputFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "vocal.pcm");
            if(!outputFile.exists()) {
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            byte[] buffer = new byte[bufferSizeInBytes];
            while(isRecording) {
                if(mAudioRecord != null) {
                    if(fileOutputStream != null) {
                        try {
                            mAudioRecord.read(buffer, 0, bufferSizeInBytes);
                            Log.e("fuck", buffer.toString());
                            fileOutputStream.write(buffer, 0, bufferSizeInBytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
