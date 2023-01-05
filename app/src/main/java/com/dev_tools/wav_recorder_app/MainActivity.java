package com.dev_tools.wav_recorder_app;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private Button btnStart, btnStop, btnPlay, btnStopPlay;

    private TextView textStatus;

    private MediaPlayer mPlayer;

    private WavRecorder wavRecorder = null;

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private static final String workingDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get views by id
        textStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btn_start_rec);
        btnStop = findViewById(R.id.btn_stop_rec);
        btnPlay = findViewById(R.id.btn_play_rec);
        btnStopPlay = findViewById(R.id.btn_stop_playing);

        //setting buttons background color
        btnStop.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btnStopPlay.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start audio recording.
                startRecording();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stop audio recording.
                stopRecording();

            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // play the recorded audio
                playAudio();
            }
        });
        btnStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stop playing the recorded audio
                stopPlaying();
            }
        });
    }

    private void startRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record and store the audio.
        if (CheckPermissions()) {

            //setting buttons background color
            btnStop.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            btnStart.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            btnPlay.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            btnStopPlay.setBackgroundColor(getResources().getColor(R.color.colorAccent));

            wavRecorder = new WavRecorder(workingDirectoryPath);
            wavRecorder.start();

            textStatus.setText("Recording Started");
        } else {

            // if audio recording permissions are
            // not granted by user this method will
            // ask for runtime permission for mic and storage.
            RequestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    //play the recorded audio
    public void playAudio() {
        btnStop.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btnStart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btnStopPlay.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        // using media player class for playing recorded audio
        mPlayer = new MediaPlayer();
        try {

            // set the data source which will be our file name
            mPlayer.setDataSource(workingDirectoryPath +"/"+wavRecorder.tempWavFile);

            //prepare media player
            mPlayer.prepare();

            // start media player.
            mPlayer.start();
            textStatus.setText("Recording Started Playing");

        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
    }

    public void stopRecording() {
        btnStop.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btnStart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnStopPlay.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        wavRecorder.stop();

        textStatus.setText("Recording Stopped");
    }

    public void stopPlaying() {

        mPlayer.release();
        mPlayer = null;

        btnStop.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        btnStart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnStopPlay.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        textStatus.setText("Recording Play Stopped");
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        if(wavRecorder != null){
            wavRecorder = null;
        }
    }
}

