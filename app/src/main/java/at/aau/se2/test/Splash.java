package at.aau.se2.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
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

        TextView txtLogo = (TextView) findViewById(R.id.logo);
        txtLogo.setTypeface(font);
        txtLogo.setTextSize(90);

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
                } finally {
                    Intent openStart = new Intent("at.aau.se2.test.STARTSCREEN");
                    startActivity(openStart);
                }
            }
        };

        checkWiFiThread.start();
        if(!isConnectedToNetwork()){
            showWiFiDialog();
        }

    }

    private void showWiFiDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Splash.this);

        // set title
        alertDialogBuilder.setTitle("No WiFi-connection");

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.splash_nowificonnection)
                .setCancelable(false)
                .setPositiveButton(R.string.splash_yes,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        //enable wifi
                        WifiManager wifiManager = (WifiManager)getApplication().getSystemService(Context.WIFI_SERVICE);
                        wifiManager.setWifiEnabled(true);
                    }
                })
                .setNegativeButton(R.string.splash_no,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
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

}
