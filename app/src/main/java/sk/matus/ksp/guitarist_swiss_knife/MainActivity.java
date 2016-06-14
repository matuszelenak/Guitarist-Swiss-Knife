package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
* The Activity that appears on the start-up.
* Contains shortcuts that start individual sub-activities of the app*/
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startMetronome(View view){
        Intent metronome_intent = new Intent(this, MetronomeActivity.class);
        startActivity(metronome_intent);
    }


    public void startTunerActivity(View view){
        Intent tunerIntent = new Intent(this, TunerActivity.class);
        startActivity(tunerIntent);
    }

    public void startChordActivity(View view){
        Intent chordIntent = new Intent(this, ChordActivity.class);
        startActivity(chordIntent);
    }

    public void startSongActivity(View view){
        Intent chordIntent = new Intent(this, SongManagementActivity.class);
        startActivity(chordIntent);
    }
}

