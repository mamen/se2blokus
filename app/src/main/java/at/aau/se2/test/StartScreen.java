package at.aau.se2.test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class StartScreen extends Activity implements View.OnClickListener {

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
            default:
                throw new ExceptionInInitializerError("Failed to register ClickEvent");
        }
    }

    /*initialise
    * Find Buttons, or other Views
    * set Listeners to Buttons*/
    private void initialise() {
        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        TextView txtLogo = (TextView) findViewById(R.id.logo);
        TextView txtHostGame = (TextView) findViewById(R.id.hostGame);
        TextView txtJoinGame = (TextView) findViewById(R.id.joinGame);
        TextView txtSettings = (TextView) findViewById(R.id.settings);

        txtLogo.setTypeface(font);
        txtHostGame.setTypeface(font);
        txtJoinGame.setTypeface(font);
        txtSettings.setTypeface(font);

        txtLogo.setTextSize(60);
        txtHostGame.setTextSize(40);
        txtJoinGame.setTextSize(40);
        txtSettings.setTextSize(40);

        txtLogo.setOnClickListener(this);
        txtHostGame.setOnClickListener(this);
        txtJoinGame.setOnClickListener(this);
        txtSettings.setOnClickListener(this);
    }


    /*doHosting
    * creates colors used for the Stones
    * Opens a Dialog, with those colors as items
    * When one color is clicked, new Intent with game starts
    * */
    private void doHosting() {
        final CharSequence[] colors = new CharSequence[]{"green", "red", "blue", "yellow"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Intent intent = new Intent("at.aau.se2.test.FULLSCREENACTIVITY");
        builder.setTitle("Pick a color");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte extra;
                which += 1;

                Log.d("asdf", Integer.toString(which));
                switch(which){
                    case 1:
                        extra = 1;
                        break;
                    case 2:
                        extra = 2;
                        break;
                    case 3:
                        extra = 3;
                        break;
                    case 4:
                        extra = 4;
                        break;
                    default:
                        throw new ExceptionInInitializerError("Failed to doHosting");
                }
                intent.putExtra("color", extra);
                intent.putExtra("test", false);
                startActivity(intent);
            }
        });
        builder.show();
    }


    private void goOn(final Boolean host) {
        final Intent intent = new Intent("at.aau.se2.test.CONNECTSCREEN");
        intent.putExtra("host", "" + host);
        startActivity(intent);
    }
}