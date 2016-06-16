package at.aau.se2.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        TextView txt_logo = (TextView) findViewById(R.id.logo);
        txt_logo.setTypeface(font);
        int help = getScreenWidth();
        if (help < 250) {
            txt_logo.setTextSize(70);
        } else if (help >= 250 && help < 700) {
            txt_logo.setTextSize(80);
        } else if (help >= 700 && help < 1200) {
            txt_logo.setTextSize(90);
        } else if (help >= 1200 && help < 1700) {
            txt_logo.setTextSize(90);
        } else {
            txt_logo.setTextSize(100);
        }

        Thread checkWiFiThread = new Thread() {
            @Override
            public void run() {
                try {
                    //wait for connection
                    while (!isConnectedToNetwork()) {
                        sleep(1);
                    }
                    sleep(1000);
                } catch (Exception e) {
                    Log.e("Error",e.getMessage());
                    throw new IllegalStateException();
                } finally {
                    Intent openStart = new Intent("at.aau.se2.test.STARTSCREEN");
                    startActivity(openStart);
                }
            }
        };

        checkWiFiThread.start();
        if (!isConnectedToNetwork()) {
            showWiFiDialog();
        }

    }

    private void showWiFiDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Splash.this);

        // set title
        alertDialogBuilder.setTitle("No WiFi-connection");

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.splash_nowificonnection)
                .setCancelable(false)
                .setPositiveButton(R.string.splash_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //enable wifi
                        WifiManager wifiManager = (WifiManager) getApplication().getSystemService(Context.WIFI_SERVICE);
                        wifiManager.setWifiEnabled(true);
                    }
                })
                .setNegativeButton(R.string.splash_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //disable wifi
                        System.exit(0);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    private int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

}
