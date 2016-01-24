package sk.matus.ksp.guitarist_swiss_knife;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Collections;

public class TunerActivity extends AppCompatActivity {

    //constants chosen so that the tradeoff between real-time performance and accuracy of FFT is optimal
    int sampleRate = 11025;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 8192;
    private AudioRecord audioRecord;
    private ProcessAudio processTask;
    private boolean started = false;
    private VisualizationView visualizationView;
    private DoubleFFT_1D fft = new DoubleFFT_1D(blockSize);
    private ToneScale toneScale;
    private int measurementCount = 10;
    private int measurementNo = 0;
    private ArrayList<Double>gatheredMaxFreq = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toneScale = new ToneScale(this);
        visualizationView = new VisualizationView(this);
        setContentView(visualizationView);
    }

    /*mostly taken from http://stackoverflow.com/questions/5511250/capturing-sound-for-analysis-and-visualizing-frequencies-in-android
    *
    * A background task that reads blocks of @blockSize audio samples at specified @sampleRate from the microphone input and performs FFT in order to determine frequency*/
    private class ProcessAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void...params) {
            try{
                int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                        channelConfiguration, audioEncoding);
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRate,
                        channelConfiguration, audioEncoding, bufferSize);
                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();
                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // no idea what dark magic was this supposed to do
                    }
                    fft.realForward(toTransform);
                    //normalisation
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

        /*
        * Method called after each block of samples has been read and processed with FFT. Updates the data and UI with current values.
        * The update to the UI is executed every @measurementCount-th the procedure is run - updating it every time results in fast
        * flickering of the UI elements and inconvenience of reading data out of it.
        * @params an array of doubles containing the block of FFT-processed samples*/
        @Override
        protected void onProgressUpdate(double[]... waves) {
            visualizationView.updateWaves(waves[0]);
            double currentMax = findStrongestFreq(waves[0]);
            if (measurementNo != measurementCount){
                gatheredMaxFreq.add(currentMax);
                measurementNo++;
            }
            else
            {
                double overallMax = findPrevalentFreq(gatheredMaxFreq);
                visualizationView.updateFreq(overallMax);
                Tuple<String,String> noteData = toneScale.extractNote(overallMax);
                visualizationView.updateTone(noteData.x, noteData.y);
                gatheredMaxFreq = new ArrayList<>();
                measurementNo = 1;
            }
        }

        /*Early naive way of figuring out current sampleRate from the spectrum
        * @param an array of sampleRate amplitudes
        * @return : the sampleRate with the highest amplitude
        * */
        private double findStrongestFreq(double[] freqList){
            double maximum = -1;
            int index= -1;
            for (int i = 0; i < freqList.length; i++){
                if (freqList[i]>maximum){
                    maximum = freqList[i];
                    index = i;
                }
            }
            return (index* sampleRate /(blockSize*2));
        }

        /*
        * Finds the largest group of frequencies that are close to each other and returns their average.
        * The practical effect is, that occasional high-amplitude noises don't affect the result of the measurement
        * @param : ArrayList of gathered frequencies to filter
        * @return : the average from the largest group of similar frequencies*/
        private double findPrevalentFreq(ArrayList<Double> frequencies){
            Collections.sort(frequencies);
            ArrayList<ArrayList<Double>>groups = new ArrayList<>();
            double prev = 0.00000001;
            for (int i = 0; i < frequencies.size(); i++){
                if (frequencies.get(i)/prev > 1.25 || frequencies.get(i)/prev < 0.75){
                    groups.add(new ArrayList<Double>());
                }
                groups.get(groups.size()-1).add(frequencies.get(i));
                prev = frequencies.get(i);
            }
            int largest = 0;
            int index=0;
            for (int i = 0; i < groups.size(); i++){
                if (groups.get(i).size() > largest){
                    largest = groups.get(i).size();
                    index = i;
                }
            }
            double sum = 0;
            for (Double freq : groups.get(index)){
                sum += freq;
            }
            return sum/(double)groups.get(index).size();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        started = false;
        if (audioRecord!=null) audioRecord.stop();
        if (processTask!=null) processTask.cancel(true);
        Log.i("TEST_STOP", "STOPPING");
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