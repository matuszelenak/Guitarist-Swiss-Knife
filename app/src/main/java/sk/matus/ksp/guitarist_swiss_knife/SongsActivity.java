package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class SongsActivity extends AppCompatActivity {

    class HierarchyCursor{
        final Context context;
        int level = 0;
        String filename;
        ArrayList<String> columns = new ArrayList<>(Arrays.asList("artist", "album", "title", "content"));
        ArrayList<String> regex = new ArrayList<>(Arrays.asList(".*", ".*", ".*", ".*"));
        public HierarchyCursor(final Context context){
            this.context = context;
        }

        public ArrayList<HierarchyCursor> getCursors(){
            ArrayList<HierarchyCursor> cursors = new ArrayList<>();
            SongDatabaseHelper db = new SongDatabaseHelper(context);
            ArrayList<String> queryResults = db.getColumn(columns.get(level),regex.get(0),regex.get(1),regex.get(2));
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

    class SongListEntry extends LinearLayout{
        HierarchyCursor cursor;
        TextView entryValue;
        Context context;

        SongListEntry(Context context){
            super(context);
            this.context = context;
            entryValue = new TextView(context);
            this.addView(entryValue);
        }

        public void setCursor(HierarchyCursor cursor){
            this.cursor = cursor;
            entryValue.setText(cursor.filename);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    performClicking();
                }
            });
        }

        private void performClicking(){
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
            reloadSelection(this.cursor);
        }
    }

    LinearLayout songSelection;
    LinearLayout navigationBar;
    ArrayList<NavigationButton> navigationButtons = new ArrayList<>();

    public void reloadSelection(HierarchyCursor cursor){
        Log.i("THERE ARE", String.format("%d Buttons", navigationButtons.size()));
        navigationBar.removeAllViews();
        songSelection.removeAllViews();
        ArrayList<HierarchyCursor> cursors = cursor.getCursors();
        for (HierarchyCursor hc : cursors){
            SongListEntry entry = new SongListEntry(this);
            entry.setCursor(hc);
            songSelection.addView(entry);
            Log.i("SONGLIST",hc.filename);
        }
        NavigationButton navButton = new NavigationButton(this);
        navButton.setCursor(cursor);
        navigationButtons.add(navButton);
        Log.i("THERE ARE", String.format("%d Buttons", navigationButtons.size()));
        for (NavigationButton nb: navigationButtons) navigationBar.addView(nb);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        songSelection = (LinearLayout) findViewById(R.id.songSelectionView);
        navigationBar = (LinearLayout) findViewById(R.id.navigationBar);

        HierarchyCursor cursor = new HierarchyCursor(this);
        cursor.filename = "Artists";
        NavigationButton rootBtn = new NavigationButton(this);
        rootBtn.setCursor(cursor);
        reloadSelection(cursor);
    }
}
