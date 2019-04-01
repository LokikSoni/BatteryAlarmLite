package www.androidghost.com.batteryandtheftalarm;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.facebook.ads.InterstitialAd;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class ForegroundAlarm extends Service {

    SharedPreferences preferencesLevel;
    int mLevel,mStatus;
    boolean isActive=true;
    NotificationCompat.Builder mBuilderForeground;
    InterstitialAd interstitialAd;
    Handler handler;

    BroadcastReceiver mCheckStatus=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mLevel=intent.getIntExtra("level",0);
            mStatus=intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
            startAlarm();

            createNotification(mLevel);
            startForeground(101,mBuilderForeground.build());
        }
    };

    private void startAlarm() {

         if(mStatus==BatteryManager.BATTERY_STATUS_CHARGING){
             if(mLevel==preferencesLevel.getInt("level",90)){
                 if(isActive){
                     startService(new Intent(this,AlarmService.class));
                     isActive=false;
                 }
             }
         }else if(mStatus==BatteryManager.BATTERY_STATUS_DISCHARGING){

             //start backgroundListenerO
             if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
            FirebaseJobDispatcher jobDispatcher=new FirebaseJobDispatcher(new GooglePlayDriver(this));
            Job myJob = jobDispatcher.newJobBuilder()
                    .setService(BackgroundListenerO.class)
                    .setTag("backgroundListener")
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(0, 60))
                    .setReplaceCurrent(false)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .setConstraints(Constraint.DEVICE_CHARGING)
                    .build();
               jobDispatcher.mustSchedule(myJob);
             }
               stopSelf();
         }
    }

    @Override
    public void onCreate() {
        super.onCreate();


        createNotification(mLevel);
        startForeground(101,mBuilderForeground.build());


        interstitialAd = new InterstitialAd(this, getResources().getString(R.string.interstitialService));
        interstitialAd.loadAd();
        showAdWithDelay();

        preferencesLevel=getSharedPreferences("levels",Context.MODE_PRIVATE);
        registerReceiver(mCheckStatus,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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

    public void createNotification(int mLevel){

        mBuilderForeground=new NotificationCompat.Builder(this, AppStartUp.mChannelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Charged : "+String.valueOf(mLevel)+"%")
                .setContentText("Alarm Activated")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(Color.parseColor("#1767B9"))
                .setProgress(100,mLevel,false)
                .setOnlyAlertOnce(true);

        try {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilderForeground.setContentIntent(contentIntent);
        }catch (RuntimeException e){
            e.printStackTrace();
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
                KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if( myKM.inKeyguardRestrictedInputMode() ) {
                    // it is locked
                    interstitialAd.show();
                }
            }
        }, 9000 );
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
        if(mCheckStatus!=null){
            unregisterReceiver(mCheckStatus);
            mCheckStatus=null;
        }
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
        //stop alarm
        stopService(new Intent(this,AlarmService.class));
    }
}
