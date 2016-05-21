package at.aau.se2.test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

public class StartScreen extends Activity implements View.OnClickListener {

    private TextView txt_logo;
    private TextView txt_hostGame;
    private TextView txt_joinGame;
    private TextView txt_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_screen);
        initialise();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hostGame:
                goOn(true);
                break;
            case R.id.joinGame:
                goOn(false);
                break;
            case R.id.settings:
                doHosting();
                break;
        }
    }

    /*initialise
    * Find Buttons, or other Views
    * set Listeners to Buttons*/
    private void initialise() {
        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        txt_logo = (TextView) findViewById(R.id.logo);
        txt_hostGame = (TextView) findViewById(R.id.hostGame);
        txt_joinGame = (TextView) findViewById(R.id.joinGame);
        txt_settings = (TextView) findViewById(R.id.settings);

        txt_logo.setTypeface(font);
        txt_hostGame.setTypeface(font);
        txt_joinGame.setTypeface(font);
        txt_settings.setTypeface(font);

        txt_logo.setTextSize(90);
        txt_hostGame.setTextSize(40);
        txt_joinGame.setTextSize(40);
        txt_settings.setTextSize(40);

        txt_logo.setOnClickListener(this);
        txt_hostGame.setOnClickListener(this);
        txt_joinGame.setOnClickListener(this);
        txt_settings.setOnClickListener(this);
    }


    /*doHosting
    * creates colors used for the Stones
    * Opens a Dialog, with those colors as items
    * When one color is clicked, new Intent with game starts
    * */
    private void doHosting() {
        final CharSequence colors[] = new CharSequence[]{"green", "red", "blue", "yellow"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Intent intent = new Intent("at.aau.se2.test.FULLSCREENACTIVITY");
        builder.setTitle("Pick a color");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intent.putExtra("chosen_color", colors[which]);
                startActivity(intent);
            }
        });
        builder.show();
    }




    private void goOn(final Boolean host){
        final CharSequence colors[] = new CharSequence[]{"green", "red", "blue", "yellow"};
        final Intent intent = new Intent("at.aau.se2.test.CONNECTSCREEN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a color");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intent.putExtra("chosen_color", colors[which]);
                intent.putExtra("host",""+host);
                startActivity(intent);
            }
        });
        builder.show();
    }
}