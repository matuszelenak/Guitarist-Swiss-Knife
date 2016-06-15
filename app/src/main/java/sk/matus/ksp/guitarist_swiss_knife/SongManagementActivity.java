package sk.matus.ksp.guitarist_swiss_knife;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SongManagementActivity extends AppCompatActivity {

    class HierarchyCursor{
        final Context context;
        int level = 0;
        String filename;
        ArrayList<String> columns = new ArrayList<>(Arrays.asList("artist", "album", "title", "type", "content"));
        ArrayList<String> regex = new ArrayList<>(Arrays.asList(".*", ".*", ".*", ".*", ".*"));
        public HierarchyCursor(final Context context){
            this.context = context;
        }

        public ArrayList<HierarchyCursor> getCursors(){
            ArrayList<HierarchyCursor> cursors = new ArrayList<>();
            SongDatabaseHelper db = new SongDatabaseHelper(context);
            ArrayList<String>escapedRegex = getEscapedRegex();
            ArrayList<String> queryResults = db.getColumn(columns.get(level),escapedRegex.get(0),escapedRegex.get(1),escapedRegex.get(2));
            for (String s : queryResults){
                HierarchyCursor newCursor = new HierarchyCursor(context);
                newCursor.level = level + 1;
                for (int i = 0; i < level; i++){
                    newCursor.regex.set(i, regex.get(i));
                }
                newCursor.regex.set(level, s);
                newCursor.filename = s;
                cursors.add(newCursor);
            }
            return cursors;
        }

        public ArrayList<String> getEscapedRegex(){
            ArrayList<String>result = new ArrayList<>();
            for (String s : regex){
                if (!s.equals(".*")) result.add(Pattern.quote(s));
                else result.add(s);
            }
            return result;
        }
    }

    class SongListEntry extends RelativeLayout {
        HierarchyCursor cursor;
        TextView entryValue;
        Context context;
        CheckBox checkBox;

        SongListEntry(Context context){
            super(context);
            this.context = context;
            entryValue = new TextView(context);
            entryValue.setTextSize(30);
            checkBox = new CheckBox(context);
            RadioButton temp = new RadioButton(context);
            temp.setChecked(false);
            temp.setClickable(false);
            int tempID = generateViewId();
            temp.setId(tempID);

            this.addView(temp);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)temp.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_VERTICAL, tempID);
            temp.setLayoutParams(params);

            this.addView(entryValue);
            params = (RelativeLayout.LayoutParams)entryValue.getLayoutParams();
            params.addRule(RelativeLayout.RIGHT_OF, tempID);
            entryValue.setLayoutParams(params);

            this.addView(checkBox);
            params = (RelativeLayout.LayoutParams)checkBox.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            checkBox.setLayoutParams(params);
        }

        public void setCursor(HierarchyCursor cursor){
            this.cursor = cursor;
            entryValue.setText(cursor.filename);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,0,10);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.setElevation(10);
            }
            this.setBackgroundColor(context.getResources().getColor(R.color.colorListItem));
            this.setLayoutParams(params);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    performClicking();
                }
            });
        }

        public void setMarking(boolean marking){
            if (marking) checkBox.setVisibility(VISIBLE); else checkBox.setVisibility(GONE);
        }

        private void performClicking(){
            if (cursor.level == 4){
                Intent intent = new Intent(context, SongViewActivity.class);
                ArrayList<String>escapedRegex = cursor.getEscapedRegex();
                intent.putExtra("artist", escapedRegex.get(0));
                intent.putExtra("album", escapedRegex.get(1));
                intent.putExtra("title", escapedRegex.get(2));
                intent.putExtra("type", cursor.filename);
                startActivity(intent);
            }
            else{
                addNavigationButton(this.cursor);
                reloadSelection(this.cursor);
            }

        }
    }

    class NavigationButton extends Button{
        HierarchyCursor cursor;
        NavigationButton(Context context){
            super(context);
        }
        public void setCursor(HierarchyCursor cursor){
            this.setText(cursor.filename);
            this.cursor = cursor;
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    performClicking();
                }
            });
        }

        private void performClicking(){
            Log.i("ERASING", String.format("From %d to %d", cursor.level, navigationButtons.size()));
            for (NavigationButton nb : navigationButtons){
                Log.i("BUTTON", nb.getText().toString());
            }
            int toErase = navigationButtons.size() - cursor.level;
            for (int i = 0 ; i < toErase; i++) navigationButtons.remove(navigationButtons.size()-1);
            addNavigationButton(this.cursor);
            reloadSelection(this.cursor);
        }
    }

    LinearLayout songSelection;
    LinearLayout navigationBar;
    ArrayList<NavigationButton> navigationButtons = new ArrayList<>();
    HierarchyCursor currentCursor;
    Dialog editDialog;
    boolean marking = false;

    public void addNavigationButton(HierarchyCursor cursor){
        NavigationButton navButton = new NavigationButton(this);
        navButton.setCursor(cursor);
        navigationButtons.add(navButton);
    }

    public void reloadSelection(HierarchyCursor cursor){
        currentCursor = cursor;
        navigationBar.removeAllViews();
        songSelection.removeAllViews();
        ArrayList<HierarchyCursor> cursors = cursor.getCursors();
        for (HierarchyCursor hc : cursors){
            SongListEntry entry = new SongListEntry(this);
            entry.setCursor(hc);
            entry.setMarking(marking);
            songSelection.addView(entry);
        }
        for (NavigationButton nb: navigationButtons) navigationBar.addView(nb);
    }

    private ArrayList<HierarchyCursor>gatherMarked(){
        ArrayList<HierarchyCursor>result = new ArrayList<>();
        for (int i = 0; i < songSelection.getChildCount(); i++){
            SongListEntry sle = (SongListEntry) songSelection.getChildAt(i);
            if (sle.checkBox.isChecked()) result.add(sle.cursor);
        }
        return result;
    }

    public void eraseSelected(View v){
        SongDatabaseHelper db = new SongDatabaseHelper(this);
        for (HierarchyCursor hc : gatherMarked()){
            db.deleteSongs(hc.regex.get(0), hc.regex.get(1),hc.regex.get(2),hc.regex.get(3));
        }
        reloadSelection(currentCursor);
    }

    private Dialog constructEditDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.song_edit_dialog);
        Button dialogButton = (Button) dialog.findViewById(R.id.edit_song_submit);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> modifyParams = new HashMap<>();
                EditText edit_artist = (EditText)dialog.findViewById(R.id.edit_artist_text);
                EditText edit_album = (EditText)dialog.findViewById(R.id.edit_album_text);
                EditText edit_title = (EditText)dialog.findViewById(R.id.edit_title_text);
                EditText edit_type = (EditText)dialog.findViewById(R.id.edit_type_text);
                if (!edit_artist.getText().toString().equals("")) modifyParams.put("artist", edit_artist.getText().toString());
                if (!edit_album.getText().toString().equals("")) modifyParams.put("album", edit_album.getText().toString());
                if (!edit_title.getText().toString().equals("")) modifyParams.put("title", edit_title.getText().toString());
                if (!edit_type.getText().toString().equals("")) modifyParams.put("type", edit_type.getText().toString());
                dialog.dismiss();
                if (!modifyParams.isEmpty()){
                    submitEdit(modifyParams);
                }

            }
        });
        return dialog;
    }

    public void submitEdit(HashMap<String, String> modifyParams){
        SongDatabaseHelper db = new SongDatabaseHelper(this);
        for (HierarchyCursor hc : gatherMarked()){
            HashMap<String, String> selectParams = new HashMap<>();
            ArrayList<String>escapedRegex = hc.getEscapedRegex();
            selectParams.put("artist", escapedRegex.get(0));
            selectParams.put("album", escapedRegex.get(1));
            selectParams.put("title", escapedRegex.get(2));
            selectParams.put("type", escapedRegex.get(3));
            db.modifySongs(modifyParams, selectParams);
        }
        reloadSelection(currentCursor);
    }

    public void editSelected(View v){
        editDialog.show();
    }

    public void search(View v){

    }

    public void toggleMarking(View v){
        marking = !marking;
        reloadSelection(currentCursor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_song);

        //this.deleteDatabase("song_database");

        songSelection = (LinearLayout) findViewById(R.id.songSelectionView);
        navigationBar = (LinearLayout) findViewById(R.id.navigationBar);
        editDialog = constructEditDialog();

        HierarchyCursor currentCursor = new HierarchyCursor(this);
        currentCursor.filename = "Artists";
        addNavigationButton(currentCursor);
        reloadSelection(currentCursor);
    }
}
