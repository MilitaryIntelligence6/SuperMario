package cn.misection.supermario.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * @author javaman
 */
public class MyMusic {

    private final Context mContext;
    private final MediaPlayer mediaPlayer;
    private String mFileName = "";

    public MyMusic(Context context) {
        super();
        this.mContext = context;
        mediaPlayer = new MediaPlayer();
    }

    public void play(String fileName, boolean looping) {
        if (mFileName.equals(fileName)) {
            return;
        } else {
            try {
                mediaPlayer.reset();
                mFileName = fileName;
                AssetFileDescriptor fd = mContext.getAssets().openFd(fileName);
                mediaPlayer.setDataSource(
                        fd.getFileDescriptor(),
                        fd.getStartOffset(),
                        fd.getLength()
                );
                mediaPlayer.setLooping(looping);
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pause() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


}
