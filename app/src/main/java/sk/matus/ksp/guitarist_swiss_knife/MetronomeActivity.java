package sk.matus.ksp.guitarist_swiss_knife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

/**
* An activity that will act like a metronome. Enough said
* This activity will be rebuilt from the ground up since it contains almost nothing
* useful now*/
public class MetronomeActivity extends AppCompatActivity {


    private boolean isRunning = false;
    private ToggleButton toggleMetronome;
    private EditText metronome_interval_field;
    private Timer metronomeTimer;
    private Switch metronomeSwitch;


    class MetronomeTask extends TimerTask{
        @Override
        public void run(){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    metronomeSwitch.toggle();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);
        toggleMetronome = (ToggleButton) findViewById(R.id.toggleButton);
        metronome_interval_field = (EditText) findViewById(R.id.metronome_interval_field);
        metronomeSwitch = (Switch) findViewById(R.id.metronome_switch);
        toggleMetronome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning){
                    metronomeTimer.cancel();
                    metronomeTimer.purge();
                    isRunning = false;
                }
                else
                {
                    metronomeTimer = new Timer();
                    metronomeTimer.schedule(new MetronomeTask(),0,Integer.parseInt(metronome_interval_field.getText().toString()));
                    isRunning = true;
                }

            }
        });
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
