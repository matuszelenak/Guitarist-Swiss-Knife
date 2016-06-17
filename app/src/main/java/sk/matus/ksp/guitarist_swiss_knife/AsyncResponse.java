package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;

/**
 * Interface that specifies the actions of the activity which uses a Async task for calculating data.
 * These actions are to be called by the AsyncTask when it returns its result.
 */
public interface AsyncResponse {
    void processFinish(ArrayList<ScrapeUGActivity.ResultEntry> output);

    void finishSongExtraction(ArrayList<Song> output);
}
