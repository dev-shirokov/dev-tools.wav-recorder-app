package com.dev_tools.wav_recorder_app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavRecorder {

    String folderPath = null;
    String tempRawFile = "temp_audio_data.raw";
    String tempWavFile = "final_record.wav";

    private static final int AUDIO_SOURCE_MIC = MediaRecorder.AudioSource.MIC;

    private static final int SAMPLING_RATE_IN_HZ = 8000;

    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final int BPP = 16;

    private static final int BUFFER_SIZE =
            AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);

    AudioRecord recorder = null;
    Thread recordingThread = null;

    public WavRecorder(String folderPath){
        this.folderPath = folderPath;
        recorder = new AudioRecord(AUDIO_SOURCE_MIC,SAMPLING_RATE_IN_HZ,CHANNEL_CONFIG,AUDIO_FORMAT, BUFFER_SIZE);
    }

    public void start(){
        try{
            if(recorder.getState() != AudioRecord.STATE_INITIALIZED){
                throw new RuntimeException("AudioRecord is not initialized");
            }

            recorder.startRecording();

            recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
            recordingThread.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void stop(){

        try{
            if(recorder != null) {

                if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                    recorder.stop();
                    recorder.release();
                }

                recorder = null;
                recordingThread = null;

                createWavFile(getPath(tempRawFile),getPath(tempWavFile));
                deleteTempFile(getPath(tempRawFile));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private class RecordingRunnable implements Runnable{

        @Override
        public void run() {
            FileOutputStream fileOutputStream = null;

            try{
                fileOutputStream = new FileOutputStream(getPath(tempRawFile));

                byte[] data = new byte[BUFFER_SIZE];

                int read;
                while (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED){
                    read = recorder.read(data, 0, BUFFER_SIZE);

                    if(read < 0){
                        throw new RuntimeException("Reading of audio buffer failed: " +getBufferReadFailureReason(read));
                    }

                    fileOutputStream.write(data);
                }
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String getBufferReadFailureReason(int errorCode) {
            switch (errorCode) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    return "ERROR_INVALID_OPERATION";
                case AudioRecord.ERROR_BAD_VALUE:
                    return "ERROR_BAD_VALUE";
                case AudioRecord.ERROR_DEAD_OBJECT:
                    return "ERROR_DEAD_OBJECT";
                case AudioRecord.ERROR:
                    return "ERROR";
                default:
                    return "Unknown (" + errorCode + ")";
            }
        }
    }

    private void createWavFile(String tempPath, String wavPath){
        try {
            FileInputStream fileInputStream = new FileInputStream(tempPath);
            FileOutputStream fileOutputStream = new FileOutputStream(wavPath);

            byte[] data = new byte[BUFFER_SIZE];
            int channels = 1;
            long byteRate = BPP * SAMPLING_RATE_IN_HZ * channels / 8;
            long totalAudioLen = fileInputStream.getChannel().size();
            long totalDataLen = totalAudioLen + 36;

            writeWavHeader(fileOutputStream, totalAudioLen, totalDataLen, channels, byteRate);
            while (fileInputStream.read(data) != -1) {
                fileOutputStream.write(data);
            }

            fileInputStream.close();
            fileOutputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deleteTempFile(String tempPath){
        final File file = new File(tempPath);
        if(file.exists()){
            file.delete();
        }
    }

    private String getPath(String name){
        return folderPath + "/" + name;
    }

    private void writeWavHeader(FileOutputStream fileOutputStream, long totalAudioLen, long totalDataLen, int channels, long byteRate){
        try {
            byte[] header = new byte[44];
            header[0] = 'R'; // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f'; // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16; // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1; // format = 1
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) ((long) SAMPLING_RATE_IN_HZ & 0xff);
            header[25] = (byte) (((long) SAMPLING_RATE_IN_HZ >> 8) & 0xff);
            header[26] = (byte) (((long) SAMPLING_RATE_IN_HZ >> 16) & 0xff);
            header[27] = (byte) (((long) SAMPLING_RATE_IN_HZ >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (2 * 16 / 8); // block align
            header[33] = 0;
            header[34] = BPP; // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
            fileOutputStream.write(header, 0, 44);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
