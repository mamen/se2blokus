package at.aau.se2.test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

public class StartScreen extends Activity implements View.OnClickListener {
    private ImageButton hostGame;
    private ImageButton joinGame;
    private ImageButton settings;

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
            case R.id.hostButton:
                goOn(true);
                break;
            case R.id.joinButton:
                goOn(false);
                break;
            case R.id.settingsButton:
                doHosting();
                break;
        }
    }

    /*initialise
    * Find Buttons, or other Views
    * set Listeners to Buttons*/
    private void initialise() {
        hostGame = (ImageButton) findViewById(R.id.hostButton);
        joinGame = (ImageButton) findViewById(R.id.joinButton);
        settings = (ImageButton) findViewById(R.id.settingsButton);
        hostGame.setOnClickListener(this);
        joinGame.setOnClickListener(this);
        settings.setOnClickListener(this);
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