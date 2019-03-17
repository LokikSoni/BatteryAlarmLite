package www.androidghost.com.batteryandtheftalarm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import me.itangqi.waveloadingview.WaveLoadingView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    SharedPreferences preferencesAlarm,preferencesTheft,mSharedPreferencesPermission,preferencesLevel,preferencesSongUri,preferencesSelection;
    int mLevel,mTemp,mPower,mStatus,tempId,ring_array[]={R.raw.celesta,R.raw.beep,R.raw.candy,R.raw.soft};
    private static final int REQUEST_CODE = 111;
    String temp;
    Uri ringUri=null;

    FloatingActionButton btnTheft,btnAlarm;
    AdView adViewBanner;
    InterstitialAd interstitialAd;
    Handler handler;
    WaveLoadingView  mWaveLoadingView;
    SeekBar seekLevel;
    RadioGroup groupBottom,groupRing;
    RadioButton rdoRing1,rdoRing2,rdoRing3,rdoRing4,rdoRing5;
    LinearLayout frameLevel;
    TextView txtPercentage,txtCapacity,txtTemp,txtPower;
    BottomSheetBehavior mBottomSheetBehavior;
    MediaPlayer demoRing;
    Toolbar toolbar;
    View mBottomSheetLayout;


    BroadcastReceiver statusBroad=new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //get battery level
            mLevel=intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            getLevel();
            //get battery power type
            mPower=intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);
            getPower();
            //get battery temperature
            mTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
            getTemperature();
            //get battery capacity
            getBatteryCapacity();
            //get battery status
            mStatus=intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        }
    };


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewInit();
        demoRing=new MediaPlayer();
        sharedInit();


        /*----------------------------------------------shared preferences-----------------------------------------*/
        preferencesAlarm = getSharedPreferences("alarms", Context.MODE_PRIVATE);
        preferencesTheft = getSharedPreferences("thefts", Context.MODE_PRIVATE);


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


        /*----------------------------------------------permission dialog-----------------------------------------*/
        mSharedPreferencesPermission=getSharedPreferences("permission",Context.MODE_PRIVATE);
        try{
            if(!mSharedPreferencesPermission.getBoolean("autoStart",false)) {
                if(Build.BRAND.equalsIgnoreCase("xiaomi") || Build.BRAND.equalsIgnoreCase("Letv") || Build.BRAND.equalsIgnoreCase("Honor")|| Build.BRAND.equalsIgnoreCase("oppo") || Build.BRAND.equalsIgnoreCase("vivo")) {
                    new AutoPermissionDialog().show(getSupportFragmentManager(),"My Permission");
                }
            }
        }
      catch (ClassCastException e){
            e.printStackTrace();
      }


        /*--------------------------------------all listener-------------------------------------------------*/
        groupBottom.setOnCheckedChangeListener(this);
        seekLevel.setOnSeekBarChangeListener(this);
        groupRing.setOnCheckedChangeListener(this);
        rdoRing1.setOnClickListener(this);
        rdoRing2.setOnClickListener(this);
        rdoRing3.setOnClickListener(this);
        rdoRing4.setOnClickListener(this);
        rdoRing5.setOnClickListener(this);

        /*--------------------------------------- mWaveLoadingView properties------------------------------------------------*/
        mWaveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        mWaveLoadingView.setCenterTitleColor(Color.parseColor("#0288D1"));
        mWaveLoadingView.setCenterTitleSize(25);
        mWaveLoadingView.setBorderWidth(2);
        mWaveLoadingView.setAmplitudeRatio(20);
        mWaveLoadingView.setAnimDuration(3000);
        mWaveLoadingView.startAnimation();

        /*---------------------------------------------bottomSheet-----------------------------------------*/
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setPeekHeight(0);

        /*---------------------------------------------toolBar-----------------------------------------*/
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });


        registerReceiver(statusBroad,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void viewInit(){
        mWaveLoadingView=findViewById(R.id.waveLoadingView);
        txtCapacity=findViewById(R.id.txtCapacity);
        txtTemp=findViewById(R.id.txtTemp);
        txtPower=findViewById(R.id.txtPower);
        mBottomSheetLayout = findViewById(R.id.bottom_sheet);
        seekLevel=findViewById(R.id.seekBarLevel);
        groupBottom=findViewById(R.id.radGroupLevelRing);
        groupRing=findViewById(R.id.ringGroup);
        rdoRing1=findViewById(R.id.ring1);
        rdoRing2=findViewById(R.id.ring2);
        rdoRing3=findViewById(R.id.ring3);
        rdoRing4=findViewById(R.id.ring4);
        rdoRing5=findViewById(R.id.ring5);
        frameLevel=findViewById(R.id.levelBottom);
        txtPercentage=findViewById(R.id.txtPer);
        btnAlarm=findViewById(R.id.btnAlarm);
        btnTheft=findViewById(R.id.btnTheft);
        btnAlarm.setOnClickListener(this);
        btnTheft.setOnClickListener(this);
        toolbar=findViewById(R.id.toolbar);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_item,menu);
        if(menu instanceof MenuBuilder) {  //To display icon on overflow menu

            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.flip){
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                    .add(R.id.parent,new BackFragment())
                    .addToBackStack(null)
                    .commit();

        }else if(item.getItemId()==R.id.like){
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

        }else if(item.getItemId()==R.id.share){
            Intent shareIntent=new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,"Want to save your Battery Life,Don't Overcharge it and Unplug it at right time by using this Battery Alarm App. https://play.google.com/store/apps/details?id=" +getPackageName());
            startActivity(Intent.createChooser(shareIntent,"Share Via"));

        }else if(item.getItemId()==R.id.feedback){

            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("text/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "java4geek@gmail.com" });
            Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            Email.putExtra(Intent.EXTRA_TEXT, "Dear Developer...," + "");
            startActivity(Intent.createChooser(Email, "Send Feedback:"));
        }
        return true;
    }

    public void sharedInit(){
        //for ring selection
        preferencesSelection=getSharedPreferences("selections",Context.MODE_PRIVATE);
        if(preferencesSelection.getInt("selection",0)==0){

            rdoRing1.setChecked(true);
        }else {
            groupRing.check(preferencesSelection.getInt("selection",0));
        }

        //for ring
        preferencesSongUri=getSharedPreferences("uris",Context.MODE_PRIVATE);

        //for alertLevel
        preferencesLevel=getSharedPreferences("levels", Context.MODE_PRIVATE);
        seekLevel.setProgress(preferencesLevel.getInt("level",90));
        txtPercentage.setText(String.valueOf(preferencesLevel.getInt("level",90)));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(statusBroad!=null) {
            unregisterReceiver(statusBroad);
            statusBroad=null;
        }
        if (adViewBanner != null) {
            adViewBanner.destroy();
        }
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }

        if(demoRing!=null){
            demoRing.reset();
            demoRing.release();
            demoRing=null;
        }
    }

    @Override
    public void onClick(View v) {

          if(v.getId()==R.id.btnAlarm) {

            if (preferencesAlarm.getBoolean("alarm", true)) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    stopService(new Intent(this, ForegroundAlarm.class));
                }
                stopService(new Intent(this, AlarmService.class));

                SharedPreferences.Editor mEditorAlarm = preferencesAlarm.edit();
                mEditorAlarm.putBoolean("alarm", false);
                mEditorAlarm.apply();

                Toast.makeText(this, "Alarm Off", Toast.LENGTH_SHORT).show();
            }
            else {
                startService(new Intent(this, ForegroundAlarm.class));

                SharedPreferences.Editor mEditorAlarm = preferencesAlarm.edit();
                mEditorAlarm.putBoolean("alarm",true);
                mEditorAlarm.apply();

                Toast.makeText(this, "Alarm On", Toast.LENGTH_SHORT).show();
            }
        }
        else if(v.getId()==R.id.btnTheft){

              Intent mStart=new Intent(this,ForegroundTheft.class);
              if(!preferencesTheft.getBoolean("theft", false)){

                  if(mStatus==BatteryManager.BATTERY_STATUS_CHARGING){

                      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                          this.startForegroundService(mStart);
                      }
                      else {
                          startService(mStart);
                      }
                      Toast.makeText(this, "Theft Alarm On", Toast.LENGTH_SHORT).show();

                      SharedPreferences.Editor mEditorTheft = preferencesTheft.edit();
                      mEditorTheft.putBoolean("theft",true);
                      mEditorTheft.apply();
                  }
                  else if(mStatus==BatteryManager.BATTERY_STATUS_DISCHARGING) {
                      Toast.makeText(this, "Connect Charger", Toast.LENGTH_SHORT).show();
                  }
              }
              else if(preferencesTheft.getBoolean("theft", true)) {
                  stopService(mStart);
                  Toast.makeText(this, "Theft Alarm Off", Toast.LENGTH_SHORT).show();

                  SharedPreferences.Editor mEditorTheft = preferencesTheft.edit();
                  mEditorTheft.putBoolean("theft",false);
                  mEditorTheft.apply();
              }
        }
        else {
            //for ringtone array
            SharedPreferences.Editor mEditorUri=preferencesSongUri.edit();
            if(demoRing!=null && demoRing.isPlaying()){
                demoRing.reset();
            }
            switch (v.getId()){
                case R.id.ring1:
                    if(demoRing!=null){
                        demoRing=MediaPlayer.create(this,R.raw.celesta);
                        demoRing.start();
                    }
                    mEditorUri.putString("uri","android.resource://"+getPackageName()+"/raw/"+ring_array[0]);
                    mEditorUri.apply();
                    break;

                case R.id.ring2:
                    if(demoRing!=null){
                        demoRing=MediaPlayer.create(this,R.raw.beep);
                        demoRing.start();
                    }
                    mEditorUri.putString("uri","android.resource://"+getPackageName()+"/raw/"+ring_array[1]);
                    mEditorUri.apply();
                    break;

                case R.id.ring3:
                    if(demoRing!=null){
                        demoRing=MediaPlayer.create(this,R.raw.candy);
                        demoRing.start();
                    }
                    mEditorUri.putString("uri","android.resource://"+getPackageName()+"/raw/"+ring_array[2]);
                    mEditorUri.apply();
                    break;

                case R.id.ring4:
                    if(demoRing!=null){
                        demoRing=MediaPlayer.create(this,R.raw.soft);
                        demoRing.start();
                    }
                    mEditorUri.putString("uri","android.resource://"+getPackageName()+"/raw/"+ring_array[3]);
                    mEditorUri.apply();
                    break;

                case R.id.ring5:
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
                    break;
            }
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
        }, 9000 ); // Show the ad after 15 minutes
    }

    private void getPower(){

        if(mPower==BatteryManager.BATTERY_PLUGGED_USB){
            txtPower.setText("Usb");

        }else if(mPower==BatteryManager.BATTERY_PLUGGED_AC){
            txtPower.setText("Ac");

        }else if(mPower==BatteryManager.BATTERY_PLUGGED_WIRELESS){
            txtPower.setText("Wireless");
        }else {
            txtPower.setText("Battery");
        }
    }

    private void getLevel(){
        mWaveLoadingView.setProgressValue(mLevel);
        mWaveLoadingView.setCenterTitle( String.valueOf(mLevel + "%"));

        if (mLevel < 20) {
            mWaveLoadingView.setWaveColor(Color.RED);
            mWaveLoadingView.setBorderColor(Color.RED);
        }
        else {
            mWaveLoadingView.setWaveColor(Color.parseColor("#ffffff"));
            mWaveLoadingView.setBorderColor(Color.parseColor("#0288D1"));
        }
    }

    private void getTemperature(){
        temp=mTemp + "°C ";
        txtTemp.setText(temp);
    }

    @SuppressLint({"SetTextI18n", "PrivateApi"})
    public void getBatteryCapacity() {
        Object mPowerProfile_=null;
        final String Power_Profile_Class="com.android.internal.os.PowerProfile";
        try {
            mPowerProfile_=Class.forName(Power_Profile_Class).getConstructor(Context.class).newInstance(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try { @SuppressLint("PrivateApi")
        double  batteryCapacity= (Double) Class.forName(Power_Profile_Class).getMethod("getAveragePower",String.class).invoke(mPowerProfile_,"battery.capacity");
            txtCapacity.setText(((long) batteryCapacity)+"mAh");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        if(checkedId==R.id.radLevel){
            frameLevel.setVisibility(View.VISIBLE);
        }
        else if(checkedId==R.id.radTone){
            frameLevel.setVisibility(View.GONE);
        }
        else if(group.getCheckedRadioButtonId()!=R.id.radTone && group.getCheckedRadioButtonId()!=R.id.radLevel ) {

            if(group.getCheckedRadioButtonId()!=R.id.ring5){

                SharedPreferences.Editor mEditorSelection=preferencesSelection.edit();
                mEditorSelection.putInt("selection",group.getCheckedRadioButtonId());
                mEditorSelection.apply();

            }else if(group.getCheckedRadioButtonId()==R.id.ring5){

                tempId=group.getCheckedRadioButtonId();
            }
        }
    }

 /*
     -------------------------------------------for level---------------------------------------------------------
     */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(seekBar.getId()==R.id.seekBarLevel){
            SharedPreferences.Editor mEditorLevel=preferencesLevel.edit();
            mEditorLevel.putInt("level",progress);
            mEditorLevel.apply();
            txtPercentage.setText(String.valueOf(preferencesLevel.getInt("level",0)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Toast.makeText(this, "Apply", Toast.LENGTH_SHORT).show();
    }


    /*
    --------------------------------------for ring--------------------------------------------------------------------
      */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==REQUEST_CODE && resultCode== Activity.RESULT_OK){

            if(data!=null){
                ringUri=data.getData();
                SharedPreferences.Editor mEditorUri=preferencesSongUri.edit();
                mEditorUri.putString("uri",ringUri.toString());
                mEditorUri.apply();

                SharedPreferences.Editor mEditorSelection=preferencesSelection.edit();
                mEditorSelection.putInt("selection",tempId);
                mEditorSelection.apply();

                Toast.makeText(this, "Apply", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "File not selected", Toast.LENGTH_SHORT).show();
            groupRing.check(preferencesSelection.getInt("selection", 0));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){

                Intent getUri=new Intent();
                getUri.setType("audio/*");
                getUri.setAction(Intent.ACTION_OPEN_DOCUMENT);
                getUri.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(getUri,REQUEST_CODE);
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                groupRing.check(preferencesSelection.getInt("selection", 0));
            }
        }
    }

    @Override
    public void onBackPressed() {

        if(getSupportFragmentManager().getBackStackEntryCount()!=0){
            getSupportFragmentManager().popBackStack();

        }else {
            if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            else {
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Exit Application")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }
    }
}
