package sk.matus.ksp.guitarist_swiss_knife;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The activity that contains all of the chord related tools:Finding chords and finger layouts.
 */
public class ChordActivity extends AppCompatActivity {

    private RadioButton rootChooserButton;
    private Dialog rootChooserDialog;
    private Chord currentChord;
    private TextView scaleView;
    private TextView chordView;
    private LinearLayout fingeringsContainer;
    private ImageView currentFingeringView;
    private TextView currentChordName;
    /**
     * An instance of ToneUtils class for resolving tone related queries.
     */
    private ToneUtils toneUtils;
    /**
    * Describes the baseName note of the scale (and consequently the chord
    * On default set to "C", can be altered by the baseName choosing dialog
    * */
    private Tone root;
    /**
     * An instance of DependencyScheme class which describes what the consistent
     * state of the UI and the chord should be like whenever the user triggers a
     * change through UI components.
     */
    private DependencyScheme scheme;
    /**
     * A HashMap which associates the String tags of accidental buttons with the instances
     * of said buttons in order to make their traversal easier.
     */
    private HashMap<String,ToggleableRadioButton>buttonMapping;
    /**
     * An instance of GuitarNeck on which to perform all the fingering-related calculations
     */
    private GuitarNeck guitarNeck;
    private ArrayList<Fingering>fingerings = new ArrayList<>();
    private Fingering currentFingering;

    private HashMap<String,ToggleableRadioButton> getButtonMapping(ViewGroup parent){
        HashMap<String,ToggleableRadioButton>result = new HashMap<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) instanceof ToggleableRadioButton){
                ToggleableRadioButton trb = (ToggleableRadioButton) parent.getChildAt(i);
                result.put((String)trb.getTag(),trb);
            } else
            if (parent.getChildAt(i) instanceof ViewGroup){
                result.putAll(getButtonMapping((ViewGroup) parent.getChildAt(i)));
            }
        }
        return result;
    }

    private void updateModel(){
        start = System.nanoTime();
        currentChord.clearFlags();
        currentChord.setScale(toneUtils.getScaleTones(root.getPrimaryName()));
        for (HashMap.Entry<String,ToggleableRadioButton>e : scheme.getButtonMapping().entrySet()){
            if (e.getValue().isChecked()) currentChord.setFlag(e.getKey());
        }
        currentChord.constructProgression(root.getPrimaryName());
        fingerings = guitarNeck.findFingerings(currentChord.getSemiToneProgression());
        currentFingering = null;
        if (!fingerings.isEmpty()){
            currentFingering = fingerings.get(0);
        }
    }

    private void updateUI(){
        rootChooserButton.setText(root.getPrimaryName().baseName +root.getPrimaryName().accidental);
        chordView.setText(currentChord.getTextProgression());
        scaleView.setText(toneUtils.getScaleText(root.getPrimaryName()));

        fingeringsContainer.removeAllViews();
        currentFingeringView.setBackgroundColor(getResources().getColor(R.color.colorActivityBackground));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (getResources().getConfiguration().orientation == 2){
            params.setMargins(0,2,15,2);
        }
        else{
            params.setMargins(0,2,2,15);
        }
        for (int i = 0; i < Math.min(15, fingerings.size()); i++){
            final FingeringThumbnail thumb = new FingeringThumbnail(this);
            thumb.setFingering(fingerings.get(i));
            thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            thumb.setAdjustViewBounds(true);
            fingeringsContainer.addView(thumb, params);
            thumb.setImageDrawable(guitarNeck.renderFingeringThumbnail(thumb.fingering, 130));
            thumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFingering(v);
                }
            });

        }
        if (currentFingering!=null) {
            currentFingeringView.setImageDrawable(guitarNeck.renderFingering(currentFingering, 300));
        } else currentFingeringView.setImageDrawable(null);
    }

    private void updateContent(){
        updateModel();
        updateUI();
    }

    /**
     * A method called by the baseName choosing dialog which changes the baseName tone.
     * @param v The View component (RadioButton in this case) which triggered the change*/
    private void setRoot(View v){
        root = toneUtils.getTones().get((int) v.getTag());
        updateContent();
    }

    private boolean nestedUpdate = false;
    private long start;
    /**
     * Whenever a radioButton is clicked, it calls this method.
     * It performs it's action and then resolves dependencies
     * @param v View component that has changed*/
    public void setModifier(View v){
        boolean parentCall = false;
        if (!nestedUpdate){
            nestedUpdate = true;
            parentCall = true;
        }
        ToggleableRadioButton button = (ToggleableRadioButton)v;
        boolean state = button.isChecked();
        HashSet<DependencyTerm> n = new HashSet<>();
        n.add(new DependencyTerm(button.isChecked(), (String) button.getTag()));
        scheme.deriveNew(n, button);
        if (state != button.isChecked()) button.toggle();
        if (parentCall){
            updateContent();
            nestedUpdate = false;
        }
    }

    private void setFingering(View v){
        FingeringThumbnail thumb = (FingeringThumbnail) v;
        currentFingering = thumb.getFingering();
        currentFingeringView.setImageDrawable(guitarNeck.renderFingering(currentFingering, 300));
    }

    /**
    * A method to construct a dialog window in which the user can choose the baseName tone of the chord.
    * @return A dialog window for choosing the baseName tone*/
    private Dialog constructRootDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.root_dialog);
        ArrayList<Tone> tones = toneUtils.getTones();
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 4; j++){
                Button btn = (Button) dialog.findViewById(getResources().getIdentifier(String.format("rootDialogBtn%d",i*4+j),"id",getPackageName()));
                ToneName name = tones.get(i*4+j).getPrimaryName();
                String tone =  name.baseName + name.accidental;
                btn.setText(tone);
                btn.setTag(i*4+j);
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
    * Method triggered when the user clicks on the rootChooserButton radio button. Fires up the baseName choosing dialog
    * @param v The View component that has been clicked
    */
    public void chooseRoot(View v){
        rootChooserDialog.show();
    }

    public void playChord(View v){
        guitarNeck.strum(currentFingering);
    }

    private void initUI(){
        rootChooserButton = (RadioButton) findViewById(R.id.rootChooser);
        scaleView = (TextView) findViewById(R.id.scaleDisplay);
        chordView = (TextView) findViewById(R.id.chordDisplay);
        rootChooserDialog = constructRootDialog();
        currentFingeringView = (ImageView) findViewById(R.id.currentFingeringView);
        currentFingeringView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        currentFingeringView.setAdjustViewBounds(true);
        currentChordName = (TextView) findViewById(R.id.currentChordName);
        fingeringsContainer = (LinearLayout) findViewById(R.id.fingeringContainer);
    }

    private void initModel(){
        toneUtils = new ToneUtils(getResources());
        root = toneUtils.getTones().get(0);
        currentChord = new Chord(toneUtils);
        currentChord.assignFlagMeaning(getResources());
        buttonMapping = getButtonMapping((ViewGroup) (findViewById(R.id.chordModifierContainer)));
        scheme = new DependencyScheme(getResources());
        scheme.setModifierButtons(buttonMapping);
        guitarNeck = new GuitarNeck(this);
        ArrayList<Tone>tuning = new ArrayList<>();
        String[] defaultTuningTemp = getResources().getStringArray(R.array.default_tuning);
        ArrayList<ToneName> defaultTuning = new ArrayList<>();
        for (String s : defaultTuningTemp){
            defaultTuning.add(new ToneName(s.charAt(0),"",0));
        }
        for (ToneName stringTuning : defaultTuning) {
            System.out.println(stringTuning.baseName);
            tuning.add(toneUtils.getTones().get(toneUtils.getSemiTonePosition(stringTuning)));
        }
        guitarNeck.setTuning(tuning);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord);
        initModel();
        initUI();
        updateContent();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //savedInstanceState.putString("baseName",baseName);
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
        //baseName = savedInstanceState.getString("baseName");
        updateContent();
    }
}
