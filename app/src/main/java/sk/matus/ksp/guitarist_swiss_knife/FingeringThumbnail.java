package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.widget.ImageView;

/**
 * A custom class to show a fingering visualisation
 */
public class FingeringThumbnail extends ImageView {
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
