package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.webkit.WebSettings;
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
        WebView webView = (WebView) findViewById(R.id.songWebView);
        String toShow = song.content;
        toShow = toShow.replaceAll("\n", "<br>");
        toShow = toShow.replaceAll("''", "'");
        Log.i("WAT", toShow);
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        webView.loadData(toShow, "text/html; charset=utf-8", "utf-8");
    }
}
