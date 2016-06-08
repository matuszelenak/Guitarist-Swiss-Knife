package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SongViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_view);
        SongDatabaseHelper db = new SongDatabaseHelper(this);
        Intent intent = getIntent();
        Song song = db.getSongs(intent.getStringExtra("artist"),intent.getStringExtra("album"),intent.getStringExtra("title"),intent.getStringExtra("type")).get(0);
        TextView text = (TextView) findViewById(R.id.songTextView);
        text.setText(song.content);
    }
}
