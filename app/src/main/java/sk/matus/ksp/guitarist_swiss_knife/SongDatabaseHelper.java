package sk.matus.ksp.guitarist_swiss_knife;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by whiskas on 7.6.2016.
 */
public class SongDatabaseHelper extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "song_database";

    private static final String TABLE_SONGS = "songs";

    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TYPE = "type";
    private static final String CONTENT = "content";

    public SongDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addSong(Song song){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ALBUM, song.getAlbum());
        values.put(KEY_ARTIST, song.getArtist());
        values.put(KEY_TYPE, song.getType());
        values.put(KEY_TITLE, song.getTitle());
        values.put(CONTENT, song.getContent());

        db.insert(TABLE_SONGS, null, values);
        db.close();
    }

    public ArrayList<Song> getSongs(String artist, String album, String title, String type){
        String selectQuery = "SELECT * FROM \"" + TABLE_SONGS + "\" AS s WHERE s.artist REGEXP \"" + artist + "\" AND s.album REGEXP \"" + album
                + "\" AND s.title REGEXP \"" + title + "\" AND s.type = \"" + type + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
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
        return result;
    }

    public void deleteSongs(String artist, String album, String title, String type){
        Log.i("DELETE", artist+album+title+type);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SONGS + " WHERE artist REGEXP \"" + artist + "\" AND album REGEXP \"" + album
                + "\" AND title REGEXP \"" + title + "\" AND type REGEXP \"" + type + "\"");
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
        Log.i("MODIFY", query.toString());
        db.execSQL(query.toString());
        db.close();
    }

    public ArrayList<String> getColumn(String column, String artist, String album, String title){
        String selectQuery = "SELECT DISTINCT "+ column + " FROM \"" + TABLE_SONGS + "\" AS s WHERE s.artist REGEXP \"" + artist + "\" AND s.album REGEXP \"" + album
                + "\" AND s.title REGEXP \"" + title + "\" ORDER BY " + column;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<String>result = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
}
