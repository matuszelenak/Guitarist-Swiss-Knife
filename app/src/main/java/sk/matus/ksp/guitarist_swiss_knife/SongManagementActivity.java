package sk.matus.ksp.guitarist_swiss_knife;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Activity that is responsible for the viewing and manipulation of the
 * in-memory database of the songs
 */
public class SongManagementActivity extends AppCompatActivity {

    /**
     * Class that points to the list of links in the current layer of the organisational subtree
     * of the song database
     */
    class HierarchyCursor{
        final Context context;
        int level = 0;
        String filename;
        ArrayList<String> columns = new ArrayList<>(Arrays.asList("artist", "album", "title", "type", "content"));
        ArrayList<String> regex = new ArrayList<>(Arrays.asList(".*", ".*", ".*", ".*", ".*"));
        public HierarchyCursor(final Context context){
            this.context = context;
        }

        /**
         * Obtains the list of links for its current layer
         * @return ArrayList of HierarchyCursors for the next layer
         */
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

    /**
     * Class that visualizes the 'folders' of the organisational tree
     * that describes the database content
     */
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
            checkBox.setClickable(false);
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
            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    toggleMarking(v);
                    checkBox.setChecked(true);
                    return true;
                }
            });
        }

        /**
         * Attaches a cursor to the view, which enables it to jump into subdirectory
         * @param cursor The cursor to be bound with the view
         */
        public void setCursor(HierarchyCursor cursor){
            this.cursor = cursor;
            entryValue.setText(cursor.filename);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0,5,0,10);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.setElevation(10);
            }
            this.setBackgroundColor(context.getResources().getColor(R.color.colorListItem));
            this.setLayoutParams(params);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (marking) toggleMark();
                    else performClicking();
                }
            });
        }

        public void toggleMark(){
            checkBox.setChecked(!checkBox.isChecked());
        }

        public void setMarking(boolean marking){
            if (marking) checkBox.setVisibility(VISIBLE); else checkBox.setVisibility(GONE);
        }

        /**
         * If the current view is sufficiently deep in the hierarchy, clicking on it
         * launches the song viewer for the song it pointed to, otherwise it simply
         * jumps into deeper level of the directory tree
         */
        private void performClicking(){
            if (cursor.level == 4){
                Intent intent = new Intent(context, SongViewActivity.class);
                ArrayList<String>escapedRegex = cursor.getEscapedRegex();
                SongDatabaseHelper db = new SongDatabaseHelper(context);
                Song song =  db.getSongs(escapedRegex.get(0),escapedRegex.get(1),escapedRegex.get(2),cursor.filename).get(0);
                intent.putExtra("artist", song.artist);
                intent.putExtra("album", song.album);
                intent.putExtra("title", song.title);
                intent.putExtra("type", song.type);
                intent.putExtra("content", song.content);
                startActivity(intent);
            }
            else{
                addNavigationButton(this.cursor);
                reloadSelection(this.cursor);
            }

        }
    }

    /**
     * View class that represents a chord when the user
     * wishes to filter the contents of the database by
     * used chords.Used in the search dialog.
     */
    class ChordSearchEntry extends RelativeLayout{
        TextView entryValue;
        Context context;
        CheckBox checkBox;
        int chord_id;

        ChordSearchEntry(Context context, int id, String name){
            super(context);
            this.context = context;
            this.chord_id = id;
            entryValue = new TextView(context);
            entryValue.setTextSize(25);
            entryValue.setText(name);
            checkBox = new CheckBox(context);
            checkBox.setClickable(false);
            LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0,5,0,5);
            this.setLayoutParams(params);
            this.setBackgroundColor(context.getResources().getColor(R.color.colorListItem));


            this.addView(entryValue);
            params = (RelativeLayout.LayoutParams)entryValue.getLayoutParams();
            params.setMargins(5,0,5,0);
            entryValue.setLayoutParams(params);

            this.addView(checkBox);
            params = (RelativeLayout.LayoutParams)checkBox.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            checkBox.setLayoutParams(params);

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
        }
    }

    /**
     * Button that enables the user to navigate to any layer of the
     * directory tree that has already been visited.
     * Clicking such button is currently the only way of traversing the tree upwards.
     */
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
            marking = false;
            int toErase = navigationButtons.size() - cursor.level;
            for (int i = 0 ; i < toErase; i++) navigationButtons.remove(navigationButtons.size()-1);
            addNavigationButton(this.cursor);
            reloadSelection(this.cursor);
        }
    }

    LinearLayout songSelection;
    LinearLayout navigationBar;
    ArrayList<NavigationButton> navigationButtons = new ArrayList<>();
    ArrayList<Integer>currentAllowedChords = new ArrayList<>();
    HierarchyCursor currentCursor;
    Dialog editDialog;
    Dialog addDialog;
    boolean marking = false;

    public void addNavigationButton(HierarchyCursor cursor){
        NavigationButton navButton = new NavigationButton(this);
        navButton.setCursor(cursor);
        navigationButtons.add(navButton);
    }

    /**
     * Given a cursor, the method reloads the list of entries in the current 'directory'
     * @param cursor The cursor which defines the current directory level
     */
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
        if (gatherMarked().isEmpty()) return;
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getResources().getString(R.string.delete_confirm_title))
                .setMessage(getResources().getString(R.string.delete_confirm_question))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eraseSelected();
                    }

                })
                .setNegativeButton(getResources().getString(R.string.no), null)
                .show();
    }

    public void eraseSelected(){
        SongDatabaseHelper db = new SongDatabaseHelper(this);
        for (HierarchyCursor hc : gatherMarked()){
            db.deleteSongs(hc.regex.get(0), hc.regex.get(1),hc.regex.get(2),hc.regex.get(3));
        }
        reloadSelection(currentCursor);
    }

    /**
     * Constructs a dialog using which the user can modify the tags
     * of the song subset that is equal to the content of the marked subfolders
     * @return Constructed edit dialog
     */
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

    private void startUGScraper(){
        Intent intent = new Intent(this, ScrapeUGActivity.class);
        startActivity(intent);
    }

    private void startSongEditor(){
        Intent intent = new Intent(this, SongEditorActivity.class);
        startActivity(intent);
    }

    /**
     * Constructs a dialog which allows the user to add
     * new song(s) to the database: shows the list of options to do so
     * (built in editor and supported sites from which the content cant be downloaded)
     * @return Dialog for adding songs.
     */
    private Dialog constructAddDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.song_add_dialog);
        Button addOwn = (Button) dialog.findViewById(R.id.songEditorLauncher);
        ImageButton addFromUG = (ImageButton) dialog.findViewById(R.id.songScraperUGLauncher);
        addOwn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSongEditor();
                dialog.dismiss();

            }
        });

        addFromUG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUGScraper();
                dialog.dismiss();
            }
        });

        return dialog;
    }

    /**
     * Clears the filtering based on chords (allows all chords)
     */
    private void clearFilter(){
        SongDatabaseHelper db = new SongDatabaseHelper(this);
        final ArrayList<Integer>allChordsIds = new ArrayList<>();
        for (SongDatabaseHelper.ChordEntry e : db.getChords()){
            allChordsIds.add(e.id);
        }
        db.setFilter(allChordsIds);
    }

    /**
     * Constructs a dialog using which the user can specify the subset
     * of chords that are allowed in the resulting list of songs he/
     * she wishes to search for.
     * @return Dialog for searching songs
     */
    private Dialog constructSearchDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.song_search_dialog);
        final LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.search_chord_list_view);
        final SongDatabaseHelper db = new SongDatabaseHelper(this);
        for (SongDatabaseHelper.ChordEntry e : db.getChords()){
            ChordSearchEntry se = new ChordSearchEntry(this, e.id, e.name);
            if (currentAllowedChords.contains(e.id)) se.checkBox.setChecked(true);
            layout.addView(se);
        }
        Button submit = (Button) dialog.findViewById(R.id.search_chord_button_submit);
        Button cancel = (Button) dialog.findViewById(R.id.search_chord_button_cancel);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer>chordIds = new ArrayList<>();
                currentAllowedChords = new ArrayList<>();
                for (int i = 0; i < layout.getChildCount(); i++){
                    ChordSearchEntry se = (ChordSearchEntry)layout.getChildAt(i);
                    if (se.checkBox.isChecked()){
                        chordIds.add(se.chord_id);
                        currentAllowedChords.add(se.chord_id);
                    }
                }
                db.setFilter(chordIds);
                reloadSelection(currentCursor);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFilter();
                currentAllowedChords = new ArrayList<>();
                reloadSelection(currentCursor);
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public void addSong(View v){
        addDialog.show();
    }

    /**
     * Method that receives the new tag values
     * and updates the content of the database, then refreshes the the list
     * of current directory entries.
     * @param modifyParams The new tag values for the song subset
     */
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
        if (gatherMarked().isEmpty()) return;
        editDialog.show();
    }

    public void search(View v){
        Dialog searchDialog = constructSearchDialog();
        searchDialog.show();
    }

    /**
     * Toggles the marking of the directory entries on and off.
     */
    public void toggleMarking(View v){
        marking = !marking;
        reloadSelection(currentCursor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_song);

        songSelection = (LinearLayout) findViewById(R.id.songSelectionView);
        navigationBar = (LinearLayout) findViewById(R.id.navigationBar);
        editDialog = constructEditDialog();
        addDialog = constructAddDialog();

        HierarchyCursor currentCursor = new HierarchyCursor(this);
        currentCursor.filename = getResources().getString(R.string.artist);
        addNavigationButton(currentCursor);
        clearFilter();
        reloadSelection(currentCursor);
    }
}
