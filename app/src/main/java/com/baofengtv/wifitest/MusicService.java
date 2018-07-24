package com.baofengtv.wifitest;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MusicService extends Service {
    private static final String TAG = "TestPlayMp3";
    private MediaPlayer mPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"MusicService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            int ret;
            mPlayer=MediaPlayer.create(this,R.raw.liudehua);
            mPlayer.start();
            mPlayer.setLooping(true);

            ret = super.onStartCommand(intent, flags, startId);
            Log.e(TAG,"on startCommand ret: " + ret);
         return ret;
    }

    @Override
    public void onDestroy() {

        if(mPlayer != null){
            mPlayer.release();
        }

        super.onDestroy();
    }
}
