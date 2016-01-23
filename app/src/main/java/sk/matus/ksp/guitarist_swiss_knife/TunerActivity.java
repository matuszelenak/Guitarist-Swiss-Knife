package sk.matus.ksp.guitarist_swiss_knife;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ToggleButton;

import org.jtransforms.fft.DoubleFFT_1D;

import java.net.URL;

public class TunerActivity extends AppCompatActivity {

    //constants chosen so that the tradeoff between real-time performance and accuracy of FFT is optimal
    int frequency = 11025;
    int channel_config = AudioFormat.CHANNEL_IN_MONO;
    int audio_encoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 8192;
    private AudioRecord audioRecord;
    private ProcessAudio processTask;
    private boolean started = false;
    private VisualizationView visualizationView;
    private DoubleFFT_1D fft = new DoubleFFT_1D(blockSize);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        visualizationView = new VisualizationView(this);
        setContentView(visualizationView);
    }

    /*mostly taken from http://stackoverflow.com/questions/5511250/capturing-sound-for-analysis-and-visualizing-frequencies-in-android*/
    private class ProcessAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void...params) {
            try{
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channel_config, audio_encoding);
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channel_config, audio_encoding, bufferSize);
                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();
                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // no idea what dark magic was this supposed to do
                    }
                    fft.realForward(toTransform);
                    //normalising
                    for (int i = 0; i<toTransform.length; i++){
                        toTransform[i] = Math.abs(toTransform[i]/blockSize);
                    }
                    publishProgress(toTransform);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... waves) {
            visualizationView.updateWaves(waves[0]);
            visualizationView.updateFreq(findPrevalentFreq(waves[0]));
        }

        /*Early naive way of figuring out current frequency from the spectrum
        * TODO : make it more reliable*/
        private int findPrevalentFreq(double[] freqlist){
            double maximum = -1;
            int index= -1;
            for (int i = 0; i < freqlist.length; i++){
                if (freqlist[i]>maximum){
                    maximum = freqlist[i];
                    index = i;
                }
            }
            return (index*frequency/(blockSize*2));
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