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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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

    private void addSongChordBinding(SQLiteDatabase db, int song_id, int chord_id){
        ContentValues values = new ContentValues();
        values.put(KEY_SONG_ID, song_id);
        values.put(KEY_CHORD_ID, chord_id);
        try{
            db.insert(TABLE_SONG_CHORDS, null, values);
        }
        catch (SQLiteConstraintException e){
            Log.i("SQL", "Binding already present");
        }

    }

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
