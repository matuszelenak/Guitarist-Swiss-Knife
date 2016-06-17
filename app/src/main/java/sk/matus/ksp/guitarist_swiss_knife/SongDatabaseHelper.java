package sk.matus.ksp.guitarist_swiss_knife;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that extracts the database on the first run and manipulates it later.
 */
public class SongDatabaseHelper extends SQLiteAssetHelper {

    class ChordEntry{
        int id;
        String name;
    }

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "song_database";

    private static final String TABLE_SONGS = "songs";
    private static final String TABLE_CHORDS = "chords";
    private static final String TABLE_SONG_CHORDS = "song_chords";
    private static final String TABLE_FILTER = "filter";

    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TYPE = "type";
    private static final String CONTENT = "content";

    private static final String KEY_CHORD_NAME = "name";

    private static final String KEY_SONG_ID = "song_id";
    private static final String KEY_CHORD_ID = "chord_id";

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public SongDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private int getSongId(SQLiteDatabase db, String artist, String album, String title, String type){
        int result = -1;
        String selectQuery = "SELECT id FROM \"" + TABLE_SONGS + "\" AS s WHERE s.artist REGEXP \"" + artist + "\" AND s.album REGEXP \"" + album
                + "\" AND s.title REGEXP \"" + title + "\" AND s.type = \"" + type + "\"";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    private int getChordId(SQLiteDatabase db, String name){
        int result = -1;
        String selectQuery = "SELECT id FROM " + TABLE_CHORDS + " WHERE name=\""+ name +"\";";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    private void addChord(SQLiteDatabase db, String name){
        ContentValues values = new ContentValues();
        values.put(KEY_CHORD_NAME, name);
        db.insert(TABLE_CHORDS, null, values);
    }

    /**
     * Adds a binding between a song and a chord.
     * Binding means that the song used the chord.
     * @param db Initialized database object
     * @param song_id The ID of the song as found in the songs table
     * @param chord_id The ID of the chord as found in the chords table
     */
    private void addSongChordBinding(SQLiteDatabase db, int song_id, int chord_id){
        ContentValues values = new ContentValues();
        values.put(KEY_SONG_ID, song_id);
        values.put(KEY_CHORD_ID, chord_id);
        try{
            db.insert(TABLE_SONG_CHORDS, null, values);
        }
        catch (SQLiteConstraintException e){
            e.printStackTrace();
        }
    }

    /**
     * Adds a song to the database along with all the metadata connected with doing so.
     * (In case the song used a chord not previously seen, it saves it; adds binding of the song
     * to the chords it uses)
     * @param song The song to be added to the database
     */
    public void addSong(Song song){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(KEY_ALBUM, song.getAlbum());
        values.put(KEY_ARTIST, song.getArtist());
        values.put(KEY_TYPE, song.getType());
        values.put(KEY_TITLE, song.getTitle());
        values.put(CONTENT, song.getContent());
        db.insert(TABLE_SONGS, null, values);

        int song_id = getSongId(db, song.getArtist(), song.getAlbum(), song.getTitle(), song.getType());
        Document doc = Jsoup.parse(song.content);
        Elements chords = doc.select("span");
        for (Element e : chords){
            int chord_id = getChordId(db, e.text());
            if (chord_id == -1){
                addChord(db, e.text());
                chord_id = getChordId(db, e.text());
            }
            addSongChordBinding(db, song_id, chord_id);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /**
     * Method that obtains a list of all chords currently used by the songs
     * in the database
     * @return An ArrayList of ChordEntry records
     */
    public ArrayList<ChordEntry> getChords(){
        ArrayList<ChordEntry> result = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String selectQuery = "SELECT id,name FROM " + TABLE_CHORDS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                ChordEntry entry = new ChordEntry();
                entry.id = cursor.getInt(0);
                entry.name = cursor.getString(1);
                result.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return result;
    }

    /**
     * Method that returns a list of songs from the database
     * according to the filtering parameters (regex)
     * @param artist Regex string that should match the songs artist field
     * @param album Regex string that should match the songs album field
     * @param title Regex string that should match the songs title field
     * @param type Regex string that should match the songs type field
     * @return An ArrayList of Song instances that satisfy the parameters filtering and are in the database
     */
    public ArrayList<Song> getSongs(String artist, String album, String title, String type){
        String selectQuery = "SELECT * FROM \"" + TABLE_SONGS + "\" AS s WHERE s.artist REGEXP \"" + artist + "\" AND s.album REGEXP \"" + album
                + "\" AND s.title REGEXP \"" + title + "\" AND s.type = \"" + type + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<Song>result = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Song song = new Song();
                song.setId(cursor.getInt(0));
                song.setArtist(cursor.getString(1));
                song.setAlbum(cursor.getString(2));
                song.setTitle(cursor.getString(3));
                song.setType(cursor.getString(4));
                song.setContent(cursor.getString(5));
                result.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return result;
    }

    /**
     * Deletes songs that match the filtering parameters from the database
     * @param artist Regex string that should match the songs artist field
     * @param album Regex string that should match the songs album field
     * @param title Regex string that should match the songs title field
     * @param type Regex string that should match the songs type field
     */
    public void deleteSongs(String artist, String album, String title, String type){
        Log.i("DELETE", artist+album+title+type);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.execSQL("DELETE FROM " + TABLE_SONGS + " WHERE artist REGEXP \"" + artist + "\" AND album REGEXP \"" + album
                + "\" AND title REGEXP \"" + title + "\" AND type REGEXP \"" + type + "\"");
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /**
     * Modifies the tags of the songs that satisfy the regex search pattern specified by parameters
     * @param modifyParams Tag parameters values that should replace the current values
     * @param selectParams Regex search patterns that specify the set of songs to be modified
     */
    public void modifySongs(HashMap<String, String> modifyParams, HashMap<String, String> selectParams){
        StringBuilder query = new StringBuilder();
        query.append("UPDATE ").append(TABLE_SONGS).append(" SET ");
        for (HashMap.Entry<String, String> entry : modifyParams.entrySet()) {
            query.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\", ");
        }
        query.delete(query.length()-2, query.length());
        query.append(" WHERE ");
        for (HashMap.Entry<String, String> entry : selectParams.entrySet()) {
            query.append(entry.getKey()).append(" REGEXP \"").append(entry.getValue()).append("\" AND ");
        }
        query.delete(query.length()-5, query.length());
        query.append(";");
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        Log.i("MODIFY", query.toString());
        db.execSQL(query.toString());
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }


    /**
     * Method that obtains only a single tag column from the songs in the database.
     * In addition to filtering parameters of the method, the content of the
     * 'filter' table is taken into account, which filters songs according to the set
     * of chords that they contain (obviously only matters for the Chord type of a song)
     * @param artist Regex string that should match the songs artist field
     * @param album Regex string that should match the songs album field
     * @param title Regex string that should match the songs title field
     * @param column Column to be extracted from the song tags
     * @return An ArrayList of strings containing the requested tag column
     */
    public ArrayList<String> getColumn(String column, String artist, String album, String title){
        String selectQuery = "SELECT DISTINCT "+ column + " FROM \"" + TABLE_SONGS + "\" AS s WHERE s.artist REGEXP \"" + artist + "\" AND s.album REGEXP \"" + album
                + "\" AND s.title REGEXP \"" + title + "\""
                + " AND NOT EXISTS(" +
                "SELECT * FROM "+ TABLE_SONG_CHORDS +" sc " +
                "WHERE sc."+KEY_SONG_ID+"=s.id AND sc."+KEY_CHORD_ID+" NOT IN " + TABLE_FILTER
                + ")"
                +" ORDER BY + " + column;

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<String>result = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return result;
    }

    /**
     * Sets the content of the 'filter' table
     * @param chordFilter An Array containing the chord_ids as found in the 'chords' table
     */
    public void setFilter(ArrayList<Integer> chordFilter){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.delete(TABLE_FILTER, "", null);
        for (int i : chordFilter){
            ContentValues values = new ContentValues();
            values.put(KEY_CHORD_ID, i);
            db.insert(TABLE_FILTER, null, values);
        }
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_FILTER, null);
        if (cursor.moveToFirst()) {
            do {
                Log.i("DATA", Integer.toString(cursor.getInt(0)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }
}
