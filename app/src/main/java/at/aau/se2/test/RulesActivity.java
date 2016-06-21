package at.aau.se2.test;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class RulesActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rules);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        TextView txtLogo = (TextView) findViewById(R.id.logo);
        TextView txtRules = (TextView) findViewById(R.id.rules);

        txtLogo.setTypeface(font);
        txtLogo.setTextSize(60);

        txtRules.setText(Html.fromHtml(getString(R.string.rules)));
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent("at.aau.se2.test.STARTSCREEN");
        startActivity(intent);
    }

}
