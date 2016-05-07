package at.aau.se2.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by KingSeybro on 24.04.2016.
 */
public class Splash extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent openStart = new Intent("at.aau.se2.test.STARTSCREEN");
                    startActivity(openStart);
                }
            }
        };
        timer.start();
    }
}
