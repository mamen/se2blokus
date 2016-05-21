package at.aau.se2.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class Splash extends Activity {

    private TextView txt_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        txt_logo = (TextView) findViewById(R.id.logo);
        txt_logo.setTypeface(font);
        txt_logo.setTextSize(90);

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(2000);
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
