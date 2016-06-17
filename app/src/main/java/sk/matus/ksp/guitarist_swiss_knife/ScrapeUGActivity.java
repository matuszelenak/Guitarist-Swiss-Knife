package sk.matus.ksp.guitarist_swiss_knife;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity that manages the data scraping from the ultimate-guitar.com portal
 */
public class ScrapeUGActivity extends AppCompatActivity implements AsyncResponse {

    EditText artistSelection;
    EditText titleSelection;
    Spinner typeSelection;
    LinearLayout resultSelection;
    HashMap<String, Integer> typeMapping = new HashMap<>();
    ArrayList<String>validSuffix = new ArrayList<>(Arrays.asList("ukulele_crd.htm", "_btab.htm","_tab.htm","_dtab.htm", "_crd.htm"));
    ArrayList<String> typeNames = new ArrayList<>(Arrays.asList("Tab", "Bass Tab", "Drum Tab", "Chord", "Ukulele Chord"));
    String currentArtist;
    String currentType;

    boolean marking = false;
    boolean viewExtracted = false;
    boolean saveExtracted = false;

    /**
     * Helper class used for representing the found songs
     */
    class ResultEntry{
        String text, url;
        ResultEntry(String text, String url){
            this.text = text;
            this.url = url;
        }
    }

    /**
     * Extension of the layout that visually represents a found entry on the page
     *
     */
    class ResultEntryView extends RelativeLayout{
        String targetURL;
        String text;
        TextView entryValue;
        Context context;
        CheckBox checkBox;
        ResultEntryView self = null;

        public ResultEntryView(Context context, String text, String url){
            super(context);
            self = this;
            this.text = text;
            this.targetURL = url;
            this.context = context;
            entryValue = new TextView(context);
            entryValue.setTextSize(30);
            entryValue.setText(text);
            checkBox = new CheckBox(context);
            checkBox.setClickable(false);
            checkBox.setVisibility(GONE);
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

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (marking){
                        toggleMark();
                    }
                    else {
                        viewExtracted = true;
                        extractSongs(new ArrayList<>(Collections.singletonList(self)));
                    }
                }
            });

            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    toggleMarking();
                    return true;
                }
            });
        }

        public void toggleMark(){
            checkBox.setChecked(!checkBox.isChecked());
        }


    }

    /**
     * Background process class which extracts the songs from the supplied array of URLs.
     */
    class RetrieveSongContent extends AsyncTask<ArrayList<ResultEntryView>, Void, ArrayList<Song>>{
        public AsyncResponse delegate = null;
        Context context;

        private ProgressDialog pd;

        RetrieveSongContent(Context context){
            super();
            this.context = context;
            pd = new ProgressDialog(context);
            pd.setMessage(context.getResources().getString(R.string.downloading));
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pd.show();
        }

        @SafeVarargs
        protected final ArrayList<Song> doInBackground(ArrayList<ResultEntryView>... passing) {
            ArrayList<Song>result = new ArrayList<>();
            for (ResultEntryView v : passing[0]){
                try{
                    Song song = new Song();
                    song.artist = currentArtist;
                    song.album = context.getResources().getString(R.string.unknown_album);
                    song.type = currentType;
                    song.title = v.text;
                    Document page = Jsoup.connect(v.targetURL).get();
                    page.outputSettings(new Document.OutputSettings().prettyPrint(false));
                    Element songContainer = page.select("pre.js-tab-content").first();
                    song.content = "<pre class=\"js-tab-content\">" + songContainer.html() + "</pre>";
                    result.add(song);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> result) {
            delegate.finishSongExtraction(result);
            pd.dismiss();
        }
    }

    class RetrieveSearchResults extends AsyncTask<ArrayList<String>, Void, ArrayList<ResultEntry>> {

        public AsyncResponse delegate = null;
        private ProgressDialog pd;

        RetrieveSearchResults(Context context){
            super();
            pd = new ProgressDialog(context);
            pd.setMessage(context.getResources().getString(R.string.downloading));
        }

        @Override
        protected void onPostExecute(ArrayList<ResultEntry> result) {
            delegate.processFinish(result);
            pd.dismiss();
        }

        private int getNumberOfPages(Document resultPage){
            int result = 0;
            Element paging = resultPage.getElementsByClass("paging").first();
            if (paging == null){
                return 1;
            }
            Pattern r = Pattern.compile("(.*Next.*)|(.*Prev.*)");
            for (Element e : paging.getAllElements()){
                Matcher m = r.matcher(e.text());
                if (!m.find()) result++;
            }
            return result - 1;
        }

        private boolean verifyURL(String URL){
            for (String s : validSuffix){
                Pattern r = Pattern.compile(".*"+s+"$");
                Matcher m = r.matcher(URL);
                if (m.find()) return true;
            }
            Pattern r = Pattern.compile(".*/tab/[0-9]+");
            Matcher m = r.matcher(URL);
            return m.find();
        }

        private ArrayList<ResultEntry> extractResults(Document resultPage){
            ArrayList<ResultEntry>results = new ArrayList<>();
            Element resultTable = resultPage.getElementsByClass("tresults").first();
            if (resultTable == null){
                return results;
            }
            for (Element column : resultTable.select(".tresults td:eq(1)")){
                Element link = column.getElementsByTag("a").first();
                if (!verifyURL(link.attr("href"))) continue;
                results.add(new ResultEntry(link.text(),link.attr("href")));
            }
            return results;
        }

        @SafeVarargs
        protected final ArrayList<ResultEntry> doInBackground(ArrayList<String>... passing) {
            ArrayList<ResultEntry>totalResults = new ArrayList<>();
            try{
                String artist = passing[0].get(0);
                String title = passing[0].get(1);
                String type = passing[0].get(2);
                Document resultPage = Jsoup.connect(String.format("https://www.ultimate-guitar.com/search.php?view_state=advanced&band_name=%s&song_name=%s&type=%s", artist, title, type)).get();
                int pages = getNumberOfPages(resultPage);
                for (int i = 1; i <= pages; i++){
                    resultPage = Jsoup.connect(String.format(Locale.US, "https://www.ultimate-guitar.com/search.php?view_state=advanced&band_name=%s&song_name=%s&type=%s&page=%d", artist, title, type, i)).get();
                    totalResults.addAll(extractResults(resultPage));
                }

            }
            catch (IOException e){
                e.printStackTrace();
            }
            return totalResults;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pd.show();
        }
    }

    /**
     * Method that toggles marking.
     * Involves toggling the visibility of checkboxes on the result rows on and off.
     */
    public void toggleMarking(){
        marking = !marking;
        for (int i = 0; i < resultSelection.getChildCount(); i++){
            ResultEntryView entry = (ResultEntryView) resultSelection.getChildAt(i);
            if (marking) entry.checkBox.setVisibility(View.VISIBLE); else entry.checkBox.setVisibility(View.GONE);
        }
    }

    /**
     * Gathers marked result rows
     * @return ArrayList of marked rows
     */
    private ArrayList<ResultEntryView>gatherMarked(){
        ArrayList<ResultEntryView> result = new ArrayList<>();
        for (int i = 0; i < resultSelection.getChildCount(); i++){
            ResultEntryView entry = (ResultEntryView) resultSelection.getChildAt(i);
            if (entry.checkBox.isChecked()) result.add(entry);
        }
        return result;
    }

    /**
     * Given an array of result rows, extracts the content of their targets using a background task class.
     * @param resultRows result entries that are to be queried
     */
    private void extractSongs(ArrayList<ResultEntryView> resultRows){
        RetrieveSongContent retriever = new RetrieveSongContent(this);
        retriever.delegate = this;
        retriever.execute(resultRows);
    }

    /**
     * Given an array of ResultEntry objects, it creates the views representing them
     * visually and attaches them to the result list view.
     * @param resultEntries results to be shown
     */
    private void populateResultList(ArrayList<ResultEntry> resultEntries){
        resultSelection.removeAllViews();
        for (ResultEntry re : resultEntries){
            ResultEntryView view = new ResultEntryView(this,re.text,re.url);
            resultSelection.addView(view);
        }
    }

    /**
     * Method that performs the search on the site
     * using the search parameters from the user
     * @param v
     */
    public void searchUG(View v){
        String artist = artistSelection.getText().toString();
        currentArtist = artist;
        currentType = typeNames.get(typeSelection.getSelectedItemPosition());
        String title = titleSelection.getText().toString();
        int type = typeMapping.get(typeSelection.getSelectedItem().toString());
        ArrayList<String> args = new ArrayList<>();
        args.add(artist);
        args.add(title);
        args.add(Integer.toString(type));
        RetrieveSearchResults retriever = new RetrieveSearchResults(this);
        retriever.delegate = this;
        retriever.execute(args);
    }

    /**
     * Method that launches the extraction of the marked results
     * with the intention to save them into database later.
     * @param v
     */
    public void downloadSelected(View v){
        ArrayList<ResultEntryView>marked = gatherMarked();
        saveExtracted = true;
        extractSongs(marked);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrape_ug);

        artistSelection = (EditText) findViewById(R.id.searchArtistUGField);
        titleSelection = (EditText) findViewById(R.id.searchTitleUGField);
        typeSelection = (Spinner) findViewById(R.id.searchTypesUG);
        resultSelection = (LinearLayout) findViewById(R.id.searchResultsView);

        typeMapping.put("Chord",300);
        typeMapping.put("Tab",200);
        typeMapping.put("Ukulele Chord",800);
        typeMapping.put("Bass Tab",400);
        typeMapping.put("Drum Tab",700);
    }

    public void processFinish(ArrayList<ResultEntry> result){
        populateResultList(result);
    }

    public void finishSongExtraction(ArrayList<Song> songs){
        if (songs.isEmpty()) return;
        if (viewExtracted){
            Intent intent = new Intent(this, SongViewActivity.class);
            intent.putExtra("artist", songs.get(0).artist);
            intent.putExtra("album", songs.get(0).album);
            intent.putExtra("title", songs.get(0).title);
            intent.putExtra("type", songs.get(0).type);
            intent.putExtra("content", songs.get(0).content);
            startActivity(intent);
            viewExtracted = false;
        }
        if (saveExtracted){
            SongDatabaseHelper db = new SongDatabaseHelper(this);
            for (Song s : songs){
                   db.addSong(s);
            }
            Toast toast = Toast.makeText(this, String.format("%d %s", songs.size(), getResources().getString(R.string.entries_downloaded)), Toast.LENGTH_SHORT);
            toast.show();
            saveExtracted = false;
        }

    }

}
