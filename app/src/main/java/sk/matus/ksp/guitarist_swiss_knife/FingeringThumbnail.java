package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.widget.TextView;

/**
 * Created by whiskas on 5.2.2016.
 */
public class FingeringThumbnail extends TextView {
    Fingering fingering;
    public FingeringThumbnail(Context context){
        super(context);
    }

    public void setFingering(Fingering fingering){
        this.fingering = fingering;
    }

    public Fingering getFingering() {
        return fingering;
    }
}
