package www.androidghost.com.batteryandtheftalarm;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class AppStartUp extends Application
{
    NotificationChannel mForegroundChanel;
    NotificationManager managerForeground;
    public static final String mChannelId="broadCast";

    @Override
    public void onCreate() {
        super.onCreate();
     /*---------------------------------------------fireBase JobDispatcher-----------------------------------------*/

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            FirebaseJobDispatcher jobDispatcher=new FirebaseJobDispatcher(new GooglePlayDriver(this));

            Job myJob = jobDispatcher.newJobBuilder()
                    .setService(BackgroundListenerO.class)
                    .setTag("backgroundListener")
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.NOW)
                    .setReplaceCurrent(false)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .setConstraints(Constraint.DEVICE_CHARGING)
                    .build();

            jobDispatcher.mustSchedule(myJob);
        }

        /*---------------------------------------------notification chanel for oreo-----------------------------------------*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mForegroundChanel=new NotificationChannel(mChannelId,"myBroadCast", NotificationManager.IMPORTANCE_HIGH);
            mForegroundChanel.setDescription("Alarm Activated Automatically");

            managerForeground= getSystemService(NotificationManager.class);
            managerForeground.createNotificationChannel(mForegroundChanel);
        }
    }
}
