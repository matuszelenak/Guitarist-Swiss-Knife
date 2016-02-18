package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.util.ArrayList;

class GuitarString{
    private Context context;
    private int stringIndex;
    private int pressedFret;
    private SemiTone openTone;
    private int fretCount;
    private ArrayList<SemiTone>semiTones = new ArrayList<>();
    private MediaPlayer player = new MediaPlayer();
    public GuitarString(Context context, int index, int fretCount){
        this.context = context;
        this.stringIndex = index;
        this.fretCount = fretCount;
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
        this.openTone = tone;
        SemiTone addedTone = tone;
        semiTones = new ArrayList<>();
        for (int i = 0; i < fretCount; i++){
            semiTones.add(addedTone);
            addedTone = addedTone.getHigherSemitone();
        }
    }

    public void setFret(int fretIndex){
        if (fretIndex >= fretCount) return;
        pressedFret = fretIndex;
    }

    public SemiTone getTone(){
        if (semiTones == null) return null;
        return semiTones.get(pressedFret);
    }

    public SemiTone getOpenTone(){
        return openTone;
    }

    public boolean pick(){
        int id = context.getResources().getIdentifier(String.format("guitar_%d_%d", stringIndex, pressedFret), "raw", context.getPackageName());
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