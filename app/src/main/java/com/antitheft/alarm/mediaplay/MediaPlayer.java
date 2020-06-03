package com.antitheft.alarm.mediaplay;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import androidx.annotation.IntDef;

import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

public final class MediaPlayer {

    /** Stream type*/
    public final static int TYPE_MUSIC = AudioManager.STREAM_MUSIC;
    public final static int TYPE_ALARM = AudioManager.STREAM_ALARM;
    public final static int TYPE_RING = AudioManager.STREAM_RING;
    public final static int TYPE_NOTIFICATION = AudioManager.STREAM_NOTIFICATION;
    @IntDef({TYPE_MUSIC, TYPE_ALARM, TYPE_RING, TYPE_NOTIFICATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TYPE {}

    private SoundPool soundPool;
    private int streamID;
    private HashMap<String, Integer> soundIdList = new HashMap<>();

    private MediaPlayer(@TYPE int streamType, int maxStream, HashMap<String, Integer> soundList) {
        AudioManager audioManager = (AudioManager) AppContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        soundPool = new SoundPool(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), TYPE_MUSIC, 100);
        for (Map.Entry<String, Integer> entry : soundList.entrySet()) {
            int id = soundPool.load(AppContext.getContext(), entry.getValue(), 1);
            soundIdList.put(entry.getKey(), id);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

            }
        });
    }

    private int getPlaySoundId(String curPlayResName) {
        for (Map.Entry<String, Integer> entry : soundIdList.entrySet()) {
            if (entry.getKey().equals(curPlayResName)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    public void play(String resName, boolean isLoop) {
        streamID = soundPool.play(getPlaySoundId(resName), 1, 1, 1, isLoop ? -1 : 0, 1);
    }

    public void release() {
        soundIdList.clear();
        soundPool.autoPause();
        soundPool.stop(streamID);
        soundPool.release();
        soundPool = null;
    }

    public void stop() {
        soundPool.stop(streamID);
    }

    public void setMaxStream() {
        AudioManager audioManager = (AudioManager) AppContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume
                (AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
    }

    public static class Builder {
        private int streamType;
        private int maxStream;
        private HashMap<String, Integer> mediaResList = new HashMap<>();

        public Builder addSound(String name, int resId) {
            mediaResList.put(name, resId);
            return this;
        }

        public Builder setStreamType(@TYPE int streamType) {
            this.streamType = streamType;
            return this;
        }

        public Builder setMaxStream(int maxStream) {
            this.maxStream = maxStream;
            return this;
        }

        public MediaPlayer builder() {
            return new MediaPlayer(streamType, maxStream, mediaResList);
        }
    }

}
