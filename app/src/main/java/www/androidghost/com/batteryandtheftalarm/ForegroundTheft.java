package www.androidghost.com.batteryandtheftalarm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.facebook.ads.InterstitialAd;

public class ForegroundTheft extends Service {

    int mStatus;
    MediaPlayer playTheft;
    NotificationCompat.Builder mBuilderForeground;
    InterstitialAd interstitialAd;
    Handler handler;

    BroadcastReceiver broadTheft=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mStatus=intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);

            if(mStatus==BatteryManager.BATTERY_STATUS_DISCHARGING){
                if(playTheft!=null && !playTheft.isPlaying()) {
                    playTheft.start();
                    playTheft.setLooping(true);
                }
            }

            createNotification();
            startForeground(102,mBuilderForeground.build());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        createNotification();
        startForeground(102,mBuilderForeground.build());

        playTheft = MediaPlayer.create(this, Uri.parse("android.resource://"+getPackageName()+"/raw/celesta"));

        /*
        -------------------------------------------volume-------------------------------------------------------
         */
        AudioManager audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        }

        interstitialAd = new InterstitialAd(this, getResources().getString(R.string.interstitialService));
        interstitialAd.loadAd();
        showAdWithDelay();

        registerReceiver(broadTheft,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(broadTheft!=null){
            unregisterReceiver(broadTheft);
            broadTheft=null;
        }
        if (playTheft!=null){
            playTheft.reset();
            playTheft.release();
            playTheft=null;
        }
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }

    private void showAdWithDelay() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Check if interstitialAd has been loaded successfully
                if(interstitialAd == null || !interstitialAd.isAdLoaded()) {
                    return;
                }
                // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
                if(interstitialAd.isAdInvalidated()) {
                    return;
                }
                // Show the ad
                interstitialAd.show();
            }
        }, 1000 * 60 * 10); // Show the ad after 15 minutes
    }


    public void createNotification(){
        mBuilderForeground=new NotificationCompat.Builder(this, AppStartUp.mChannelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Battery Alarm")
                .setContentText("Theft Activated")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(Color.parseColor("#1767B9"))
                .setOnlyAlertOnce(true);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilderForeground.setContentIntent(contentIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
