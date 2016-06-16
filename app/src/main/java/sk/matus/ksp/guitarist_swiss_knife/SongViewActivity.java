package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongViewActivity extends AppCompatActivity {

    private final static int UP = 1;
    private final static int DOWN = -1;

    private static Handler viewHandler;

    ToneUtils toneUtils;

    Scroller scrollThread;

    Song currentSong;

    WebView webView;
    ImageButton playButton;
    Button transposeUp;
    Button transposeDown;
    SeekBar autoscroll_control;
    int currentYposition = 0;
    private final static int MSG_SCROLL = 1;
    boolean scrolling = false;

    String baseNotes = "CDEFGABH";
    ArrayList<String> rootTones = new ArrayList<>();

    class Scroller extends Thread{
        boolean isRunning = false;
        @Override
        public void run() {
            while (isRunning){
                if (Thread.interrupted()) break;
                viewHandler.sendEmptyMessage(MSG_SCROLL);
                try{
                    Thread.sleep(2500/autoscroll_control.getProgress());
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void displaySong(Song song){
        webView.loadDataWithBaseURL("file:///android_asset/", song.content, "text/html", "utf-8", null);
    }

    public void toggleScrolling(View v){
        scrolling = !scrolling;
        if (scrolling){
            scrollThread = new Scroller();
            scrollThread.isRunning = true;
            scrollThread.start();
            playButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
        else {
            scrollThread.isRunning = false;
            scrollThread.interrupt();
            playButton.setBackgroundResource(android.R.drawable.ic_media_play);
        }
    }

    private String transposeChord(String chord, int direction){
        chord = chord.replaceAll("b", "♭");
        chord = chord.replaceAll("#","♯");
        for (String s : rootTones){
            Pattern p = Pattern.compile("^"+s+".*");
            Matcher m = p.matcher(chord);
            if (m.find()){
                ToneName tn = new ToneName(chord.charAt(0),s.substring(1),0);
                int position = toneUtils.getSemiTonePosition(tn);
                Tone tone = toneUtils.getTones().get(position);
                Tone transposed;
                if (direction == UP){
                    transposed = tone.getHigherTone();
                }
                else
                {
                    transposed = tone.getLowerTone();
                }
                ToneName transposedName = transposed.getPrimaryName();
                String result = transposedName.baseName + transposedName.accidental + chord.substring(s.length());
                result = result.replaceAll("♭","b");
                result = result.replaceAll("♯","#");
                return result;
            }
        }
        return chord;
    }

    public void transposeUp(View v){
        Log.i("HTML", currentSong.content);
        Document doc = Jsoup.parse(currentSong.content, "UTF-8");
        Elements chords = doc.select("span");
        for (Element e : chords){
            Log.i("CHORD", e.text());
            e.text(transposeChord(e.text(), UP));
        }
        currentSong.content = doc.toString();
        displaySong(currentSong);
    }

    public void transposeDown(View v){
        Log.i("HTML", currentSong.content);
        Document doc = Jsoup.parse(currentSong.content, "UTF-8");
        Elements chords = doc.select("span");
        for (Element e : chords){
            Log.i("CHORD", e.text());
            e.text(transposeChord(e.text(), DOWN));
        }
        currentSong.content = doc.toString();
        displaySong(currentSong);
    }

    private void scroll(){
        webView.scrollBy(0,1);
        currentYposition = webView.getScrollY();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_view);

        for (int i = 0; i < baseNotes.length(); i++){
            rootTones.add(baseNotes.charAt(i)+"♭");
            rootTones.add(baseNotes.charAt(i)+"♯");
        }
        for (int i = 0; i < baseNotes.length(); i++){
            rootTones.add(Character.toString(baseNotes.charAt(i)));
        }

        viewHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                final int what = msg.what;
                switch(what) {
                    case MSG_SCROLL: scroll(); break;
                }
            }
        };

        toneUtils = new ToneUtils(getResources());

        scrollThread = new Scroller();

        webView = (WebView) findViewById(R.id.songWebView);
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setBuiltInZoomControls(true);

        playButton = (ImageButton) findViewById(R.id.play_song);
        transposeUp = (Button) findViewById(R.id.transpose_higher);
        transposeDown = (Button) findViewById(R.id.transpose_lower);
        autoscroll_control = (SeekBar) findViewById(R.id.auto_scroll_speed);
        autoscroll_control.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Intent intent = getIntent();
        currentSong = new Song();
        currentSong.artist = intent.getStringExtra("artist");
        currentSong.album = intent.getStringExtra("album");
        currentSong.title = intent.getStringExtra("title");
        currentSong.type = intent.getStringExtra("type");
        currentSong.content = intent.getStringExtra("content");
        String toShow = currentSong.content;
        Log.i("CONTENT", toShow);
        toShow = toShow.replaceAll("\n", "<br>");
        toShow = toShow.replaceAll("''", "'");
        StringBuilder sb = new StringBuilder();
        sb.append("<HTML><HEAD><LINK href=\"style.css\" type=\"text/css\" rel=\"stylesheet\"/></HEAD><body>");
        sb.append(toShow);
        sb.append("</body></HTML>");
        currentSong.content = sb.toString();
        displaySong(currentSong);
    }


}
