package www.androidghost.com.batteryandtheftalarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splash extends AppCompatActivity {

    ImageView imageSplash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imageSplash=findViewById(R.id.imgSplash);
        Animation animation= AnimationUtils.loadAnimation(this,R.anim.animation);
        imageSplash.startAnimation(animation);

        Thread thread=new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                  Intent startMain= new Intent(Splash.this,MainActivity.class);
                  startActivity(startMain);
                }
            }
        };
        thread.start();
    }
}
