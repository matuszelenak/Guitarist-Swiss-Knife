package sk.matus.ksp.guitarist_swiss_knife;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ToggleButton;

import java.net.URL;

public class TunerActivity extends AppCompatActivity {

    int frequency = 44100;
    int channel_config = AudioFormat.CHANNEL_IN_MONO;
    int audio_encoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 1024;
    private AudioRecord audioRecord;
    private ProcessAudio processTask;
    private boolean started = false;
    private VisualizationView visualizationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        visualizationView = new VisualizationView(this);
        setContentView(visualizationView);
    }

    private class ProcessAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void...params) {
            try{
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channel_config, audio_encoding);
                Log.i("TEST_BUFF_SIZE",Integer.toString(bufferSize));
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channel_config, audio_encoding, bufferSize);
                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();
                int count = 0;
                int maximum = Integer.MIN_VALUE;
                int minimum = Integer.MAX_VALUE;
                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                    if (count == 100){
                        Log.i("TEST_REC",Integer.toString(bufferReadResult));
                        count = 0;
                    }

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                        maximum = Math.max(maximum,buffer[i]);
                        minimum = Math.min(minimum, buffer[i]);
                    }
                    count++;

                    publishProgress(toTransform);
                    //break;
                }
                Log.i("MAX",Integer.toString(maximum));
                Log.i("MIN",Integer.toString(minimum));

            }catch(Exception e){
                Log.i("TEST_ERR","SHIT HAPPENED");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... waves) {
            Log.i("SUM",Double.toString(sum(waves[0])));
            visualizationView.update(waves[0]);
        }

        public double sum(double[] a){
            double res = 0;
            for (int i = 0; i < a.length; i++) res+=a[i];
            return res;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        audioRecord.stop();
        processTask.cancel(true);
        Log.i("TEST_STOP", "STOPPING");
        started = false;
    }

    @Override
    public void onResume(){
        super.onResume();
        started = true;
        Log.i("TEST_RESUME", "Resuming");
        processTask = new ProcessAudio();
        processTask.execute();
    }

}