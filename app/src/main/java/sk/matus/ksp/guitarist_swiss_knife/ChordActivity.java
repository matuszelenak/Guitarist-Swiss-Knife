package sk.matus.ksp.guitarist_swiss_knife;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.provider.MediaStore;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ChordActivity extends AppCompatActivity {

    RadioButton rootChooser;
    Dialog rootDialog;
    Chord currentChord;
    TextView scaleDisplay;
    TextView chordDisplay;
    RadioGroup typeRG;
    RadioGroup fifthRG;
    RadioGroup seventhRG;
    RadioGroup augDimRG;
    ToneScale toneScale;
    String root = "C";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord);
        toneScale = new ToneScale(this);
        rootChooser = (RadioButton) findViewById(R.id.rootChooser);
        scaleDisplay = (TextView) findViewById(R.id.scaleDisplay);
        chordDisplay = (TextView) findViewById(R.id.chordDisplay);
        typeRG = (RadioGroup) findViewById(R.id.groupType);
        fifthRG = (RadioGroup) findViewById(R.id.group5);
        seventhRG = (RadioGroup) findViewById(R.id.group7);
        augDimRG = (RadioGroup) findViewById(R.id.groupAD);
        currentChord = new Chord(toneScale);
        currentChord.setRoot(root);
        currentChord.setScale(toneScale.constructToneScale(root));
        rootDialog = constructRootDialog();
    }

    private void setRoot(View v){
        root = (String) v.getTag();
        rootChooser.setText(root);
        scaleDisplay.setText(toneScale.printScale(toneScale.constructStringScale(root)));
        updateChord();
    }

    private void updateChord(){
        currentChord.setRoot(root);
        currentChord.setScale(toneScale.constructToneScale(root));
        chordDisplay.setText(currentChord.getProgression());
        Log.i("CHORD", "UPDATED");
    }

    public void setType(View v){
        RadioButton rb = (RadioButton) findViewById(typeRG.getCheckedRadioButtonId());
        currentChord.type = (String)(rb.getTag());
        updateChord();
    }

    public void setFifth(View v){
        RadioButton rb = (RadioButton) findViewById(fifthRG.getCheckedRadioButtonId());
        currentChord.fifth = (String)(rb.getTag());
        updateChord();
    }

    public void setSeventh(View v){
        RadioButton rb = (RadioButton) findViewById(seventhRG.getCheckedRadioButtonId());
        currentChord.seventh = (String)(rb.getTag());
        updateChord();
    }

    public void setAugDim(View v){
        RadioButton rb = (RadioButton) findViewById(augDimRG.getCheckedRadioButtonId());
        switch (augDimRG.getCheckedRadioButtonId()){
            case R.id.buttonAug: {
                RadioButton majorButton = (RadioButton) findViewById(R.id.buttonMajor);
                RadioButton fifthSharp = (RadioButton) findViewById(R.id.button5sharp);
                if (!fifthSharp.isChecked()) fifthSharp.toggle();
                if (!majorButton.isChecked()) majorButton.toggle();
                fifthSharp.performClick();
                majorButton.performClick();
                break;
            }
            case R.id.buttonDim: {
                RadioButton minorButton = (RadioButton) findViewById(R.id.buttonMinor);
                RadioButton fifthFlat = (RadioButton) findViewById(R.id.button5flat);
                if (!fifthFlat.isChecked()) fifthFlat.toggle();
                if (!minorButton.isChecked()) minorButton.toggle();
                minorButton.performClick();
                fifthFlat.performClick();
                break;
            }
        }
        updateChord();
    }

    private Dialog constructRootDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.root_dialog);
        dialog.setTitle("Pick a root tone");
        ArrayList<SemiTone>semiTones = toneScale.getSemiTones();
        for (int i = 0; i < 3; i++){
            LinearLayout row = (LinearLayout)dialog.findViewById(getResources().getIdentifier(String.format("dialog_row_%d",i),"id",getPackageName()));
            for (int j = 0; j < 4; j++){
                Button btn = new Button(this);
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
                row.addView(btn);

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

    public void chooseRoot(View v){
        rootDialog.show();
    }
}
