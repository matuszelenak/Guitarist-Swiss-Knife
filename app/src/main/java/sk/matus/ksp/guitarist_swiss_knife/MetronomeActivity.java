package sk.matus.ksp.guitarist_swiss_knife;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
* An activity that will act like a metronome.
* This activity will be rebuilt from the ground up since it contains almost nothing
* useful now*/
public class MetronomeActivity extends AppCompatActivity {

    private int MAXBPM = 250;

    private boolean isRunning = false;
    private ToggleButton toggleMetronome;
    private Timer metronomeTimer;
    private Spinner noteCountSpinner;
    private Spinner noteFractionSpinner;
    private SeekBar tempoSeekBar;
    private TextView currentBpmTextView;
    private Button bpmIncreaseButton;
    private Button bpmDecreaseButton;
    private RadioGroup metronomeVisualization;
    private MediaPlayer player = new MediaPlayer();

    private int currentBpm = 60;
    private double currentBeatDuration = 1;
    private int currentNoteFraction = 4;
    private int currentNoteQuantity = 4;
    private int nextBeatPosition = 0;

    private ArrayList<RadioButton>beatVisualization;

    class MetronomeTask extends TimerTask{
        @Override
        public void run(){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (nextBeatPosition == 0) tick("high"); else tick("low");
                    RadioButton rb = (RadioButton)(metronomeVisualization.getChildAt(nextBeatPosition));
                    rb.setChecked(true);
                    nextBeatPosition = (nextBeatPosition + 1) % currentNoteQuantity;
                }
            });
        }
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
                    metronomeTimer.cancel();
                    metronomeTimer.purge();
                    isRunning = false;
                    nextBeatPosition = 0;
                }
                else
                {
                    metronomeTimer = new Timer();
                    metronomeTimer.schedule(new MetronomeTask(),0,(int)(currentBeatDuration*1000.0));
                    isRunning = true;
                }

            }
        });

        metronomeVisualization = (RadioGroup) findViewById(R.id.metronome_radio_group);

        noteCountSpinner = (Spinner)findViewById(R.id.noteCountSpinner);
        ArrayList<Integer> noteCounts = new ArrayList<>();
        for (int i = 1; i < 64; i++) noteCounts.add(i);
        ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, noteCounts); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noteCountSpinner.setAdapter(spinnerArrayAdapter);
        noteCountSpinner.setSelection(2);
        noteCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentNoteQuantity = (int)parent.getItemAtPosition(position);
                renderVisualisation();
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
                currentNoteFraction = (int)parent.getItemAtPosition(position);
                currentBeatDuration = (4.0 / currentNoteFraction) * (60.0 / currentBpm);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tempoSeekBar = (SeekBar)findViewById(R.id.bpmSlider);
        tempoSeekBar.setMax(MAXBPM);
        tempoSeekBar.setProgress(currentBpm);

        currentBpmTextView = (TextView) findViewById(R.id.currentBpmText);
        currentBpmTextView.setText(Integer.toString(currentBpm));

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

    void setBpm(int bpm){
        if (bpm > MAXBPM || bpm <=0 ) return;
        currentBpm = bpm;
        currentBpmTextView.setText(Integer.toString(currentBpm));
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
        super.onPause();
        metronomeTimer.cancel();
        metronomeTimer.purge();
        toggleMetronome = (ToggleButton) findViewById(R.id.toggleButton);
    }

    @Override
    public void onResume(){
        super.onResume();
        metronomeTimer = new Timer();
    }
}
