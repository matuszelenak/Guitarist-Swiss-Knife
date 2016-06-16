package sk.matus.ksp.guitarist_swiss_knife;

import java.util.ArrayList;

/**
 * Created by whiskas on 16.6.2016.
 */
public interface AsyncResponse {
    void processFinish(ArrayList<ScrapeUGActivity.ResultEntry> output);
}
