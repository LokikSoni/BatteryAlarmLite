package www.androidghost.com.batteryandtheftalarm;

import android.content.Intent;
import android.os.Build;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class BackgroundListenerO extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {

  /*--------------------------------------start and stop service in nougat and above version----------------------------*/

        Intent mStart=new Intent(this,ForegroundAlarm.class);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            this.startForegroundService(mStart);
        }
        else {
            startService(mStart);
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
