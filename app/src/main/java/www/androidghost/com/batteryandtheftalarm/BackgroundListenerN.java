package www.androidghost.com.batteryandtheftalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class BackgroundListenerN extends BroadcastReceiver {

    String actionName;
    @Override
    public void onReceive(Context context, Intent intent) {

        actionName=intent.getAction();
        if (actionName != null) {

            if(actionName.equals(Intent.ACTION_POWER_CONNECTED)) {
                context.startService(new Intent(context,ForegroundAlarm.class));
            }

            if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.M){
                if(actionName.equals(Intent.ACTION_POWER_DISCONNECTED))
                context.stopService(new Intent(context,ForegroundAlarm.class));
            }

            /*---------------------------------------------fireBase JobDispatcher-----------------------------------------*/
            if(actionName.equals(Intent.ACTION_BOOT_COMPLETED)) {

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                    FirebaseJobDispatcher jobDispatcher=new FirebaseJobDispatcher(new GooglePlayDriver(context));

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
            }
        }
    }
}