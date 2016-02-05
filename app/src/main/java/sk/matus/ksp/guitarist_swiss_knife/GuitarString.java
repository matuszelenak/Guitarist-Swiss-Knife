package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * Created by whiskas on 5.2.2016.
 */
class GuitarString{
    MediaPlayer player;
    int stringIndex;
    int currentFret;
    Context context;
    SemiTone startTone;
    public GuitarString(Context context,int index){
        this.context = context;
        this.stringIndex = index;
    }

    public void setOpenTone(SemiTone tone){
        this.startTone = tone;
    }

    public void setFret(int fretIndex){
        currentFret = fretIndex;
    }

    public SemiTone getSemiTone(){
        if (currentFret == -1) return null;
        SemiTone result = startTone;
        for (int i = 0; i < currentFret; i++){
            result = result.getHigher();
        }
        return result;
    }

    public void pick(){
        if (currentFret == -1) return;
        int id = context.getResources().getIdentifier(String.format("guitar_%d_%d", stringIndex, currentFret), "raw", context.getPackageName());
        player = MediaPlayer.create(context,id);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
    }
}