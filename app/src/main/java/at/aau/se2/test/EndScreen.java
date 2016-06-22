package at.aau.se2.test;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

public class EndScreen extends AppCompatActivity {

    private boolean isHost;
    private String hostEnd;
    private String winner;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen);
        Bundle extras = getIntent().getExtras();
        isHost = extras.getBoolean("isHost");
        hostEnd = extras.getString("hostEnd");
        winner = extras.getString("winner");


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        TextView txtLogo = (TextView) findViewById(R.id.logo);
        TextView txtWinnerName = (TextView) findViewById(R.id.winner_name);
        TextView txtMainMenu = (TextView) findViewById(R.id.mainMenu);
        TextView txtWinnerHeading = (TextView) findViewById(R.id.winner_heading);

        txtLogo.setTypeface(font);
        txtWinnerName.setTypeface(font);
        txtMainMenu.setTypeface(font);
        txtWinnerHeading.setTypeface(font);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txtLogo.setTextSize(60);
        txtMainMenu.setTextSize(40);
        txtWinnerName.setTextSize(20);
        txtWinnerHeading.setTextSize(40);


        txtMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent("at.aau.se2.test.STARTSCREEN");
                GoogleApiClient apiClient = Connection.getInstance().getApiClient();
                if(!isHost){
                    Nearby.Connections.disconnectFromEndpoint(apiClient, hostEnd);
                    Nearby.Connections.stopDiscovery(apiClient, getString(R.string.service_id));
                }
                else{
                    Nearby.Connections.stopAllEndpoints(apiClient);
                }
                apiClient.disconnect();
                startActivity(intent);

            }
        });

        //TODO: set winner name and score
        txtWinnerName.setText(winner);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.finish();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 10);
    }
}
