package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

class GuitarString{
    int stringIndex;
    int currentFret;
    Context context;
    SemiTone startTone;
    MediaPlayer player = new MediaPlayer();
    public GuitarString(Context context,int index){
        this.context = context;
        this.stringIndex = index;
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                return true;
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
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
            result = result.getHigherSemitone();
        }
        return result;
    }

    public boolean pick(){
        int id = context.getResources().getIdentifier(String.format("guitar_%d_%d", stringIndex, currentFret), "raw", context.getPackageName());
        if (id == 0) return false;
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(id);
        try{
            player.reset();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            afd.close();
        } catch (Exception e){
            player.reset();
        }
        return true;
    }
}