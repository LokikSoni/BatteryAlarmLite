package www.androidghost.com.batteryandtheftalarm;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;

public class AlarmService extends Service {

    MediaPlayer playerAlarm;
    SharedPreferences preferencesSongUri,preferencesRepeat,preferencesVibrate,preferencesVolume;
    String mSongUri;
    Uri songUri;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedInIt();

        //--------------------------------------------vibrate----------------------------------------------------
        if(preferencesVibrate.getBoolean("vibrate",true)) {
           Vibrator vibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(3000);
            }
        }


        //-------------------------------------------volume-------------------------------------------------------
        AudioManager audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,preferencesVolume.getInt("volume",15),0);
        }


        //--------------------------------------------ringtone---------------------------------------------------
        mSongUri=preferencesSongUri.getString("uri","android.resource://"+getPackageName()+"/raw/celesta");
        playerAlarm=MediaPlayer.create(getApplicationContext(),songUri=Uri.parse(mSongUri));
           if(playerAlarm!=null && !playerAlarm.isPlaying()) {
               playerAlarm.start();
               if(preferencesRepeat.getBoolean("repeat",true)){
                   playerAlarm.setLooping(true);
               }
           }

       //--------------------------------------------start dialog---------------------------------------------------
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.inKeyguardRestrictedInputMode() ) {
            // it is locked
            Intent stopDialog=new Intent(this, DialogActivity.class);
            stopDialog.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            stopDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stopDialog.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(stopDialog);
        }
     }

    private void sharedInIt(){
        preferencesVolume=getSharedPreferences("volumes", Context.MODE_PRIVATE);
        preferencesRepeat=getSharedPreferences("repeats",Context.MODE_PRIVATE);
        preferencesSongUri=getSharedPreferences("uris",Context.MODE_PRIVATE);
        preferencesVibrate=getSharedPreferences("vibrates",Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playerAlarm!=null){
            playerAlarm.reset();
            playerAlarm.release();
            playerAlarm=null;
        }
    }
}
