package www.androidghost.com.batteryandtheftalarm;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.pm.PackageInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class DialogActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStop,btnCancel,btnUpdate;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    int fireBaseVersionCode,versionCode;
    String currentVersionCode;
    AdView adViewBanner;
    InterstitialAd interstitialAd;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        this.setFinishOnTouchOutside(false);

        btnUpdate=findViewById(R.id.btnUpdate);
        btnCancel=findViewById(R.id.btnCancel);
        btnStop=findViewById(R.id.btnStopAlarm);
        btnUpdate.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnCancel.setOnClickListener(this);


        /*----------------------------------------------fb ads-----------------------------------------*/
        adViewBanner = new AdView(this, getResources().getString(R.string.bannerMain), AdSize.BANNER_HEIGHT_50);
        // Find the Ad Container
        LinearLayout adContainer = findViewById(R.id.banner_container);
        // Add the ad view to your activity layout
        adContainer.addView(adViewBanner);
        // Request an ad
        adViewBanner.loadAd();

        interstitialAd = new InterstitialAd(this, getResources().getString(R.string.interstitialService));
        interstitialAd.loadAd();
        showAdWithDelay();

        /*----------------------------------------------fireBase remote-----------------------------------------*/

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        }
                        displayUpdateMessage();
                    }
                });

    }

    private void displayUpdateMessage() {
        try{
            fireBaseVersionCode=Integer.valueOf(currentVersionCode=mFirebaseRemoteConfig.getString("android_update_version_code"));

        }catch (NumberFormatException e){
            e.printStackTrace();
        }

        //Get PackageInfo like VersionCode
        try {
            PackageInfo info=getPackageManager().getPackageInfo(this.getPackageName(),0);
            versionCode= (int) PackageInfoCompat.getLongVersionCode(info);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(versionCode<fireBaseVersionCode) {
            btnUpdate.setVisibility(View.VISIBLE);
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
        }, 1000 * 60);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnCancel){
            finish();
        }
        else if(v.getId()==R.id.btnStopAlarm){
            stopService(new Intent(this,AlarmService.class));
            finish();
        }
        else if(v.getId()==R.id.btnUpdate){
            try {
                Uri rate_us_uri= Uri.parse("market://details?id="+getPackageName());
                Intent rateIntent=new Intent(Intent.ACTION_VIEW,rate_us_uri);
                 startActivity(rateIntent);
            }
            catch (ActivityNotFoundException e) {
                Uri rate_us_uri= Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName());
                Intent rateIntent=new Intent(Intent.ACTION_VIEW,rate_us_uri);
               startActivity(rateIntent);
            }
            stopService(new Intent(this,AlarmService.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adViewBanner != null) {
            adViewBanner.destroy();
        }
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }
}