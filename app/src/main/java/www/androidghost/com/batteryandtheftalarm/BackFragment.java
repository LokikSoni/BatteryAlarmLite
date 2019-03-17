package www.androidghost.com.batteryandtheftalarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

import java.util.Objects;

public class BackFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    View mBackLayout;
    SharedPreferences preferencesVolume,preferencesRepeat,preferencesVibrate;
    SeekBar seekAlertVolume;
    CheckBox repeatCheck,vibrateCheck;
    TextView txtVolume;
    int maxVolume;
    Context context;
    AdView adViewBanner;
    Toolbar toolbar;

    public BackFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBackLayout=inflater.inflate(R.layout.fragment_back, container, false);
        viewInIt();
        sharedInIt();

        /*----------------------------------------------seekBar-----------------------------------------*/
        AudioManager audioManager= (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            seekAlertVolume.setMax(maxVolume);
        }
        seekAlertVolume.setProgress(preferencesVolume.getInt("volume",15));

        /*----------------------------------------------seekbar text-----------------------------------------*/
        final String mVolume="Alert Volume: "+String.valueOf(preferencesVolume.getInt("volume",15));
        txtVolume.setText(mVolume);


        vibrateCheck.setChecked(preferencesVibrate.getBoolean("vibrate",true));
        repeatCheck.setChecked(preferencesRepeat.getBoolean("repeat",true));

        /*----------------------------------------------listener-----------------------------------------*/
        seekAlertVolume.setOnSeekBarChangeListener(this);
        repeatCheck.setOnCheckedChangeListener(this);
        vibrateCheck.setOnCheckedChangeListener(this);


        /*----------------------------------------------fb ads-----------------------------------------*/
        adViewBanner = new AdView(context, getResources().getString(R.string.bannerDialog), AdSize.BANNER_HEIGHT_50);
        // Find the Ad Container
        LinearLayout adContainer = mBackLayout.findViewById(R.id.banner_container);
        // Add the ad view to your activity layout
        adContainer.addView(adViewBanner);
        // Request an ad
        adViewBanner.loadAd();


        /*----------------------------------------------toolbar-----------------------------------------*/
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
            }
        });

        return mBackLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    private  void viewInIt(){
        toolbar=mBackLayout.findViewById(R.id.toolbar);
        seekAlertVolume=mBackLayout.findViewById(R.id.seekAlertVolume);
        repeatCheck=mBackLayout.findViewById(R.id.checkRepeat);
        vibrateCheck=mBackLayout.findViewById(R.id.checkVibrate);
        txtVolume=mBackLayout.findViewById(R.id.txtVolume);
    }

    private void sharedInIt(){
        preferencesVolume=context.getSharedPreferences("volumes", Context.MODE_PRIVATE);
        preferencesRepeat=context.getSharedPreferences("repeats",Context.MODE_PRIVATE);
        preferencesVibrate=context.getSharedPreferences("vibrates",Context.MODE_PRIVATE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        SharedPreferences.Editor mEditorVolume=preferencesVolume.edit();
        mEditorVolume.putInt("volume",progress);
        mEditorVolume.apply();

        final String mVolume="Alert Volume: "+String.valueOf(progress);
        txtVolume.setText(mVolume);
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(buttonView.getId()==R.id.checkRepeat){
            SharedPreferences.Editor mEditorRepeat=preferencesRepeat.edit();
            mEditorRepeat.putBoolean("repeat",isChecked);
            mEditorRepeat.apply();

        }else if(buttonView.getId()==R.id.checkVibrate){
            SharedPreferences.Editor mEditorVibrate=preferencesVibrate.edit();
            mEditorVibrate.putBoolean("vibrate",isChecked);
            mEditorVibrate.apply();
        }
    }
}
