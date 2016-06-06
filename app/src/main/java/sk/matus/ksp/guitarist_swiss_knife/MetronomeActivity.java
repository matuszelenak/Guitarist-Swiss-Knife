package sk.matus.ksp.guitarist_swiss_knife;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
* An activity that will act like a metronome.
* This activity will be rebuilt from the ground up since it contains almost nothing
* useful now*/
public class MetronomeActivity extends AppCompatActivity {

    class Metronome extends Thread{

        boolean running = false;

        @Override
        public void run() {
            while (running){
                if (nextBeatPosition == 0) tick("high"); else tick("low");
                final RadioButton rb = (RadioButton)(metronomeVisualization.getChildAt(nextBeatPosition));
                MetronomeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rb != null) rb.setChecked(true);
                    }
                });

                nextBeatPosition = (nextBeatPosition + 1) % currentNoteQuantity;
                try{
                    Thread.sleep((int)(currentBeatDuration*1000.0));
                }
                catch (InterruptedException e){
                    Log.i("WHY", "WHYYYY");
                    e.printStackTrace();
                }

            }
        }
    }

    class Tempo{
        String name;
        int lower_bound;
        int upper_bound;
        Tempo(String name, int upper_bound, int lower_bound){
            this.name = name;
            this.upper_bound = upper_bound;
            this.lower_bound = lower_bound;
        }
    }


    private ToggleButton toggleMetronome;
    private Spinner noteCountSpinner;
    private Spinner noteFractionSpinner;
    private SeekBar tempoSeekBar;
    private TextView currentBpmTextView;
    private Button bpmIncreaseButton;
    private Button bpmDecreaseButton;
    private RadioGroup metronomeVisualization;
    private MediaPlayer player = new MediaPlayer();

    private boolean isRunning = false;
    private int maxBpm;
    private int currentBpm;
    private int currentNoteFraction;
    private int currentNoteQuantity;
    private double currentBeatDuration;
    private int nextBeatPosition = 0;

    private Metronome metronome = new Metronome();

    private ArrayList<Tempo> tempos;

    String lookupTempoName(int bpm){
        for (Tempo t : tempos){
            if (t.lower_bound <= bpm && t.upper_bound >= bpm) return t.name;
        }
        return "";
    }

    void loadTempoNames(){
            InputStream io = getResources().openRawResource(R.raw.tempo_pairs);
            try {
                tempos = readJsonStream(io);
            }
            catch (IOException e){
                e.printStackTrace();
            }
    }

    public ArrayList<Tempo> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readTonesArray(reader);
        }
        finally {
            reader.close();
        }
    }

    /**
     * A method to read a semitone array
     * @param reader The JsonReader to use for reading*/
    private ArrayList<Tempo> readTonesArray(JsonReader reader) throws IOException{
        ArrayList<Tempo>temp = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            temp.add(readTempo(reader));
        }
        reader.endArray();
        return temp;
    }

    private Tempo readTempo(JsonReader reader) throws IOException{
        reader.beginObject();
        String name = "";
        int lower = 0;
        int upper = 0;
        while (reader.hasNext()){

            String varName = reader.nextName();
            switch (varName){
                case "name":
                    name = reader.nextString();
                    break;
                case "lower":
                    lower = reader.nextInt();
                    break;
                case "upper":
                    upper = reader.nextInt();
                    break;
                default: reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new Tempo(name, upper, lower);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);
        toggleMetronome = (ToggleButton) findViewById(R.id.metronome_toggle_button);
        toggleMetronome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning){
                    stopTimer();
                    nextBeatPosition = 0;
                }
                else
                {
                    startTimer();
                }

            }
        });

        currentBpm = getResources().getInteger(R.integer.init_bpm);
        maxBpm = getResources().getInteger(R.integer.max_bpm);
        currentNoteFraction = getResources().getInteger(R.integer.init_note_fraction);
        currentNoteQuantity =  getResources().getInteger(R.integer.init_note_quantity);
        currentBeatDuration = (4.0 / currentNoteFraction) * (60.0 / currentBpm);

        metronomeVisualization = (RadioGroup) findViewById(R.id.metronome_radio_group);

        noteCountSpinner = (Spinner)findViewById(R.id.noteCountSpinner);
        ArrayList<Integer> noteCounts = new ArrayList<>();
        for (int i = 1; i < 32; i++) noteCounts.add(i);
        ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, noteCounts); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noteCountSpinner.setAdapter(spinnerArrayAdapter);
        noteCountSpinner.post(new Runnable() {
            @Override
            public void run() {
                noteCountSpinner.setSelection(3);
            }
        });
        noteCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean temp = isRunning;
                stopTimer();
                currentNoteQuantity = (int)parent.getItemAtPosition(position);
                renderVisualisation();
                if (temp) startTimer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        noteFractionSpinner = (Spinner)findViewById(R.id.noteFractionSpinner);
        ArrayList<Integer> noteFractions = new ArrayList<>();
        noteCountSpinner.setAdapter(spinnerArrayAdapter);
        for (int i = 1; i < 6; i++){
            noteFractions.add(1<<i);
        }
        ArrayAdapter<Integer> spinnerArrayAdapterFraction = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, noteFractions); //selected item will look like a spinner set from XML
        spinnerArrayAdapterFraction.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noteFractionSpinner.setAdapter(spinnerArrayAdapterFraction);
        noteFractionSpinner.setSelection(1);
        noteFractionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean temp = isRunning;
                stopTimer();
                currentNoteFraction = (int)parent.getItemAtPosition(position);
                currentBeatDuration = (4.0 / currentNoteFraction) * (60.0 / currentBpm);
                if (temp) startTimer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tempoSeekBar = (SeekBar)findViewById(R.id.bpmSlider);
        tempoSeekBar.setMax(maxBpm);
        tempoSeekBar.setProgress(currentBpm);

        currentBpmTextView = (TextView) findViewById(R.id.currentBpmText);

        bpmIncreaseButton = (Button) findViewById(R.id.bmpIncreaseButton);
        bpmDecreaseButton = (Button) findViewById(R.id.bpmDecreaseButton);

        bpmIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBpm(currentBpm + 1);
            }
        });

        bpmDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBpm(currentBpm - 1);
            }
        });

        tempoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setBpm(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        loadTempoNames();

        setBpm(currentBpm);

        stopTimer();

        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                return true;
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

    }

    void stopTimer(){
        isRunning = false;
        if (metronome != null){
            metronome.running = false;
        }
    }

    void startTimer(){
        nextBeatPosition = 0;
        if (metronome != null){
            metronome.running = false;
        }
        metronome = new Metronome();
        metronome.running = true;
        isRunning = true;
        metronome.start();
    }

    void setBpm(int bpm){
        if (bpm > maxBpm || bpm <=0 ) return;
        currentBpm = bpm;
        currentBpmTextView.setText(String.format("%d bpm (%s)", currentBpm, lookupTempoName(currentBpm)));
        tempoSeekBar.setProgress(currentBpm);
        currentBeatDuration = (4.0 / currentNoteFraction) * (60.0 / currentBpm);
    }

    void renderVisualisation(){
        metronomeVisualization.removeAllViews();
        for (int i = 0; i < currentNoteQuantity; i++) {
            RadioButton trb = new RadioButton(this);
            metronomeVisualization.addView(trb);
        }
    }

    public boolean tick(String type){
        int id = getResources().getIdentifier(String.format("metronome_%s", type), "raw", getPackageName());
        if (id == 0) return false;
        AssetFileDescriptor afd = getResources().openRawResourceFd(id);
        try{
            player.reset();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            afd.close();
        } catch (Exception e){
            player.reset();
        }
        return true;
    }

    @Override
    public void onPause() {
        Log.i("LIFECYCLE","PAUSING");
        super.onPause();
        toggleMetronome = (ToggleButton) findViewById(R.id.metronome_toggle_button);
        if (isRunning) {
            stopTimer();
            isRunning = true;
        }
    }

    @Override
    public void onResume(){
        Log.i("LIFECYCLE","RESUMING");
        super.onResume();
        if (isRunning){
            startTimer();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i("LIFECYCLE","SAVINGINSTANCE");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("NoteCount",noteCountSpinner.getSelectedItemPosition());
        savedInstanceState.putInt("NoteFraction", noteFractionSpinner.getSelectedItemPosition());
        savedInstanceState.putInt("nextBeat", nextBeatPosition);
        savedInstanceState.putBoolean("Running", isRunning);
        savedInstanceState.putInt("BPM", currentBpm);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i("LIFECYCLE","RESTORINGINSTANCE");
        isRunning = savedInstanceState.getBoolean("Running");
        noteCountSpinner.setSelection(savedInstanceState.getInt("NoteCount"));
        noteFractionSpinner.setSelection(savedInstanceState.getInt("NoteFraction"));
        nextBeatPosition = savedInstanceState.getInt("nextBeat");
        setBpm(savedInstanceState.getInt("BPM"));
        if (isRunning){
            toggleMetronome.toggle();
        }
    }
}
