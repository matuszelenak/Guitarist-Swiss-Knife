package sk.matus.ksp.guitarist_swiss_knife;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * The activity that contains all of the chord related tools:Finding chords and finger layouts.
 */
public class ChordActivity extends AppCompatActivity {

    RadioButton rootChooser;
    Dialog rootDialog;
    Chord currentChord;
    TextView scaleDisplay;
    TextView chordDisplay;
    /**
     * An instance of ToneUtils class for resolving tone related queries.
     */
    ToneUtils toneUtils;
    /*
    * Describes the root note of the scale (and consequently the chord
    * On default set to "C", can be altered by the root choosing dialog*/
    String root = "C";
    /**
     * An instance of DependencyScheme class which describes what the consistent
     * state of the UI and the chord should be like whenever the user triggers a
     * change through UI components.
     */
    DependencyScheme scheme;
    HashMap<String,ToggleableRadioButton>buttonMapping;

    GuitarNeck guitarNeck;
    Fingering currentFingering;

    /**
    * Whenever a radioButton is clicked, it calls this method.
    * It performs it's action and then resolves dependencies
    * @param v View component that has changed*/
    public void setModifier(View v){
        ToggleableRadioButton button = (ToggleableRadioButton)v;
        boolean state = button.isChecked();
        currentChord.clearFlags();
        HashSet<DependencyTerm> n = new HashSet<>();
        n.add(new DependencyTerm(button.isChecked(),(String)button.getTag()));
        scheme.deriveNew(n);
        if (state != button.isChecked()) button.toggle();
        updateChord();
    }

    private HashMap<String,ToggleableRadioButton> extractButtons(ViewGroup parent){
        HashMap<String,ToggleableRadioButton>result = new HashMap<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) instanceof ToggleableRadioButton){
                ToggleableRadioButton trb = (ToggleableRadioButton) parent.getChildAt(i);
                result.put((String)trb.getTag(),trb);
            } else
            if (parent.getChildAt(i) instanceof ViewGroup){
                result.putAll(extractButtons((ViewGroup)parent.getChildAt(i)));
            }
        }
        return result;
    }

    /**
    * A method called by the root choosing dialog which changes the root tone.
    * @param v The View component (RadioButton in this case) which triggered the change*/
    private void setRoot(View v){
        root = (String) v.getTag();
        updateChord();
    }

    /**
    * A method that updates the scale, chord and the visual components that show them.*/
    private void updateChord(){
        rootChooser.setText(root);
        currentChord.setScale(toneUtils.getScaleTones(root));
        for (HashMap.Entry<String,ToggleableRadioButton>e : scheme.getButtonMapping().entrySet()){
            if (e.getValue().isChecked()) currentChord.setFlag(e.getKey());
        }
        chordDisplay.setText(currentChord.getProgression(root));
        scaleDisplay.setText(toneUtils.getScaleText(root));
        ArrayList<Fingering> f = guitarNeck.findFingerings(currentChord.getProgression());
        if (f.isEmpty()){
            currentFingering = null;
            ViewGroup parent = (ViewGroup) findViewById(R.id.fingeringContainer);
            parent.removeAllViews();
            TextView t = (TextView) findViewById(R.id.fingeringText);
            t.setText("");
        } else {
            currentFingering = f.get(0);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ViewGroup parent = (ViewGroup) findViewById(R.id.fingeringContainer);
            parent.removeAllViews();
            for (int i = 0; i < Math.min(30,f.size()); i++){
                final FingeringThumbnail tv = new FingeringThumbnail(this);
                tv.setText(f.get(i).toString());
                tv.setFingering(f.get(i));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setFingering(v);
                    }
                });
                parent.addView(tv,params);
            }
        }
    }

    private void setFingering(View v){
        FingeringThumbnail thumb = (FingeringThumbnail) v;
        currentFingering = thumb.getFingering();
        TextView t = (TextView) findViewById(R.id.fingeringText);
        t.setText(currentFingering.toString());
        System.out.println(thumb.getFingering());
    }
    /**
    * A method to construct a dialog window in which the user can choose the root tone of the chord.
    * @return A dialog window for choosing the root tone*/
    private Dialog constructRootDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.root_dialog);
        dialog.setTitle("Pick a root tone");

        ArrayList<SemiTone>semiTones = toneUtils.getSemiTones();
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 4; j++){
                Button btn = (Button) dialog.findViewById(getResources().getIdentifier(String.format("rootDialogBtn%d",i*4+j),"id",getPackageName()));
                String tone = semiTones.get(i*4+j).getNames().get(0);
                btn.setText(tone);
                btn.setTag(tone);
                btn.setTransformationMethod(null);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setRoot(v);
                        dialog.dismiss();
                    }
                });
            }

        }
        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.customDialogCancel);
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    /**
    * Method triggered when the user clicks on the rootChooser radio button. Fires up the root choosing dialog
    * @param v The View component that has been clicked
    */
    public void chooseRoot(View v){
        rootDialog.show();
    }

    public void playChord(View v){
        guitarNeck.strum(currentFingering);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord);
        rootChooser = (RadioButton) findViewById(R.id.rootChooser);
        scaleDisplay = (TextView) findViewById(R.id.scaleDisplay);
        chordDisplay = (TextView) findViewById(R.id.chordDisplay);
        toneUtils = new ToneUtils(this.getResources());
        rootDialog = constructRootDialog();
        currentChord = new Chord(toneUtils);
        currentChord.assignFlagMeaning(this.getResources());
        buttonMapping = extractButtons((ViewGroup) (findViewById(R.id.chordModifierContainer)));
        scheme = new DependencyScheme(this.getResources());
        scheme.setModifierButtons(buttonMapping);
        guitarNeck = new GuitarNeck(this);
        ArrayList<SemiTone>tuning = new ArrayList<>();
        String[] names = new String[]{"E","A","D","G","B","E"};
        for (int i = 0; i < 6; i++){
            tuning.add(toneUtils.getSemiTones().get(toneUtils.getSemiTonePosition(names[i])));
        }
        guitarNeck.setTuning(tuning);
        updateChord();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("root",root);
        for (String s : currentChord.getFlags()){
            savedInstanceState.putBoolean(s,buttonMapping.get(s).isChecked());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        for (HashMap.Entry<String,ToggleableRadioButton>e : buttonMapping.entrySet()){
            e.getValue().setOnClickListener(null);
            e.getValue().setChecked(savedInstanceState.getBoolean(e.getKey()));
            e.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setModifier(v);
                }
            });
            if (savedInstanceState.getBoolean(e.getKey())) currentChord.setFlag(e.getKey());
        }
        root = savedInstanceState.getString("root");
        updateChord();
    }
}
