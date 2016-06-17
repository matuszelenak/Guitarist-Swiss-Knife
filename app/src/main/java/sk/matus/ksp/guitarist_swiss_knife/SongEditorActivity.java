package sk.matus.ksp.guitarist_swiss_knife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * An Activity that serves as a simple text editor in which the user
 * is able to add his/her own songs and save them into database
 */
public class SongEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_editor);
    }
}
