package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

//import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 16000; // Sample rate in Hz
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private AudioRecord recorder;
    private boolean isRecording = false;
    private ByteArrayOutputStream bufferStream;
    private BufferHttpServer httpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);



        startButton.setOnClickListener(v -> startRecordingAndStreaming());
        stopButton.setOnClickListener(v -> stopRecordingAndStreaming());

        requestPermissions();
    }
    private void displayMessage(String msg) {
        TextView message = findViewById(R.id.textView);
        message.setText(msg);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        message.setText("");
    }

    private void startRecordingAndStreaming() {
        HostAddresses hostAddresses = new HostAddresses();
        TextView hostAddressesV = findViewById(R.id.hostAddresses);
        bufferStream = new ByteArrayOutputStream();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            displayMessage("Permission denied!");
            return;
        }

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

        recorder.startRecording();
        isRecording = true;

        displayMessage("Recording");

        new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (isRecording) {
                int read = recorder.read(buffer, 0, BUFFER_SIZE);
                if (read > 0) {
                    bufferStream.write(buffer, 0, read);
                }
            }
        }).start();

        String hostAddressesS = "";

        httpServer = new BufferHttpServer(8080, bufferStream.toByteArray());
        try {
            httpServer.start();
            if (!HostAddresses.hostAddresses.isEmpty()) {
                for( String address : HostAddresses.hostAddresses) {
                    hostAddressesS += (address + ":" + httpServer.getListeningPort());
                }

                hostAddressesV.setText(hostAddressesS);
            } else {
                hostAddressesV.setText("no hosts");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecordingAndStreaming() {
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            try {
                Thread.sleep(1000); // Sleep for 1000 milliseconds (1 second)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            displayMessage("Recording Stopped");
        }

        if (httpServer != null) {
            httpServer.stop();
            displayMessage("Server Stopped");
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//            } else {
//                // Permission denied
//            }
//        }
//    }
}