package at.aau.se2.test;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EndScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen);


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        TextView txtLogo = (TextView) findViewById(R.id.logo);
        TextView txtWinnerName = (TextView) findViewById(R.id.winner_name);
        TextView txtWinnerScore = (TextView) findViewById(R.id.winner_score);
        TextView txtMainMenu = (TextView) findViewById(R.id.mainMenu);

        txtLogo.setTypeface(font);
        txtWinnerName.setTypeface(font);
        txtWinnerScore.setTypeface(font);
        txtMainMenu.setTypeface(font);

        txtLogo.setTextSize(60);
        txtWinnerName.setTextSize(20);
        txtWinnerScore.setTextSize(20);
        txtMainMenu.setTextSize(40);

        //TODO: set winner name and score
        txtWinnerName.setText("asdf");
        txtWinnerScore.setText("1234");

    }
}
