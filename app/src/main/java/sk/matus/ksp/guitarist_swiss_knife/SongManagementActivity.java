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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
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
            ArrayList<String>escapedRegex = new ArrayList<>();
            for (String s : regex){
                if (!s.equals(".*")) escapedRegex.add(Pattern.quote(s));
                else escapedRegex.add(s);
            }
            ArrayList<String> queryResults = db.getColumn(columns.get(level),escapedRegex.get(0),escapedRegex.get(1),escapedRegex.get(2));
            Log.i("GETTING CURSOR", columns.get(level)+escapedRegex.get(0)+escapedRegex.get(1)+escapedRegex.get(2));
            for (String s : queryResults){
                Log.i("CURSOR",s);
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
            temp.setChecked(true);
            temp.setClickable(false);
            int tempID = generateViewId();
            temp.setId(tempID);
            this.addView(temp);

            this.addView(entryValue);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)entryValue.getLayoutParams();
            params.addRule(RelativeLayout.RIGHT_OF, tempID);

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
                ArrayList<String>escapedRegex = new ArrayList<>();
                for (String s : cursor.regex){
                    if (!s.equals(".*")) escapedRegex.add(Pattern.quote(s));
                    else escapedRegex.add(s);
                }
                intent.putExtra("artist", escapedRegex.get(0));
                intent.putExtra("album", escapedRegex.get(1));
                intent.putExtra("title", escapedRegex.get(2));
                intent.putExtra("type", cursor.filename);
                startActivity(intent);
            }
            else
                addNavigationButton(this.cursor);
                reloadSelection(this.cursor);
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

    public void eraseSelected(View v){

    }

    public void editSelected(View v){

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

        this.deleteDatabase("song_database");

        songSelection = (LinearLayout) findViewById(R.id.songSelectionView);
        navigationBar = (LinearLayout) findViewById(R.id.navigationBar);

        HierarchyCursor currentCursor = new HierarchyCursor(this);
        currentCursor.filename = "Artists";
        addNavigationButton(currentCursor);
        reloadSelection(currentCursor);
    }
}
