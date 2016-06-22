package at.aau.se2.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity to handle a synchronous color-pick.
 */
public class ColorScreen extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener,
        View.OnClickListener{


    private GoogleApiClient apiClient;
    private boolean isHost;
    //private boolean isConnected = true;
    private String remoteHostEndpoint;
    private List<String> remotePeerEndpoints;
    private boolean doubleBackToExitPressedOnce = false;
    private HashMap<String, String> idNameMap = new HashMap<>();
    //private byte playerID;

    private Button buttonGreen;
    private Button buttonRed;
    private Button buttonBlue;
    private Button buttonYellow;
    private Button startButton;
    private boolean selected = false;
    private int selectCount = 0;
    private String turn;

    private static final String[] messageCodes = {"COLOROK-","FULLSCREEN","N-"};

    private byte color = -1;
    private String setColors = "";


    /**
     * Sets up the screen and all the fields with the information from the previous screens.
     *
     * @param savedInstanceState saved instance bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //debugging("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorscreen);
        Connection.getInstance().setColorScreen(this);
        Bundle extras = getIntent().getExtras();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        idNameMap = (HashMap<String, String>)extras.getSerializable("map");
        isHost = extras.getBoolean("host");
        remoteHostEndpoint = extras.getString("hostEnd");
        //get the api from the Singleton
        apiClient = Connection.getInstance().getApiClient();
        remotePeerEndpoints = Connection.getInstance().getRemotePeerEndpoints();
        setupView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startButton.setVisibility(View.INVISIBLE);
        startButton.setClickable(false);
    }

    /**
     *  Create fields to access/write from/to the graphic elements of the connectScreen.
     */
    private void setupView() {
        buttonGreen = (Button) findViewById( R.id.button_green );
        buttonRed = (Button) findViewById(R.id.button_red);
        buttonBlue = (Button) findViewById( R.id.button_blue );
        buttonYellow = (Button) findViewById(R.id.button_yellow);
        startButton = (Button) findViewById(R.id.button_startgame);

        buttonGreen.setOnClickListener(this);
        buttonRed.setOnClickListener(this);
        buttonBlue.setOnClickListener(this);
        buttonYellow.setOnClickListener(this);
        startButton.setOnClickListener(this);
    }

    public void setTurnString(String s){
        turn = s;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle){
        throw new UnsupportedOperationException();
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.reconnect();
    }

    @Override
    public void onConnectionRequest(String s, String s1, String s2, byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onEndpointFound(String s, String s1, String s2, String s3) {
        throw new UnsupportedOperationException();
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
        }, 15);
    }

    public void endGame(){
        dev("endCOLOR");
        Toast.makeText(getApplicationContext(), "Player Connection lost - going back to StartScreen ...", Toast.LENGTH_SHORT).show();
        Thread endGameThread = new Thread() {
            @Override
            public void run() {
                try {
                    //wait for connection
                        sleep(2000);
                } catch (Exception e) {
                    Log.e("Error",e.getMessage());
                    throw new IllegalStateException();
                } finally {
                    Intent openStart = new Intent("at.aau.se2.test.STARTSCREEN");
                    startActivity(openStart);
                }
            }
        };
    }

    @Override
    public void onEndpointLost(String s) {

        dev("onEndpointLostCOLOR");
        throw new UnsupportedOperationException();
    }

    /**
     * Sends messages to handle the communication. Those messages are received by the other
     * players in the "ConnectScreen", where they have to be handled depending on their prefixes.
     *
     * @param message Message-String
     */
    private void sendMessage( String message ) {
        debugging("sendMessage");
        if(!remotePeerEndpoints.isEmpty()) {
            if (isHost) {
                Nearby.Connections.sendReliableMessage(apiClient, remotePeerEndpoints, (message).getBytes());
            }
        }
        else {
            if (remoteHostEndpoint != null) {
                Nearby.Connections.sendReliableMessage(apiClient, remoteHostEndpoint, (message).getBytes());
            }
        }
    }


    /**
     * Gets called from the correlating ConnectScreen "onMessageReceived" Function. Handles the messages
     * which are to be processed from that class, but they just have to be passed here by the same
     * function but in the ConnectScreen.
     *
     * @param endpointId full id, has to be split to match that one in the idMap
     * @param payload bytearray with the message
     * @param isReliable is it reliable or not
     */
    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        debugging("colorInRightActivity");
        String message = new String( payload );

        if (message.startsWith(messageCodes[0])) {
                debugging("color chosen", 1);
                String[] messArray = message.split("-");
                debugging(messArray.length+"", 1);
                debugging(messArray.toString()+"", 1);
                if (messArray.length == 3) {
                    int id = Integer.parseInt(messArray[1]);
                    String endpointSplit = messArray[2];
                    setButtonSelected(endpointSplit, (Button) findViewById(id));

                } else {
                    debugging("message array has wrong format");
                }
                if (isHost) {
                    sendMessage(message);
                    debugging("sendcolor");
                }
            selectCount++;
            checkGameStart();
        }


            /*else if (message.startsWith("COLORREM-")) {
                debugging("color unchosen");
                String[] messArray = message.split("-");
                if (messArray.length == 2) {
                    String colorID = messArray[1];
                } else {
                    debugging("message array has wrong format");
                }
                if (isHost) {
                    sendMessage(message);
                    debugging("sendremcolor");
                }*/



        else if (message.startsWith(messageCodes[1])){
            if(selected){
                startButton.performClick();
            }
        }

        else {
            debugging("FEHLER");
        }
    }

    /**
     * disable a button
     *
     * @param b button to be disabled
     */
    public void disableButton(Button b){
        b.setAlpha(.5f);
        b.setClickable(false);
    }

    /**
     * Sets a button selected and disables it.
     * Also displays the selectorname in the buttontext.
     *
     * @param endpointId id of the selector
     * @param b button to be selected
     */
    public void setButtonSelected(String endpointId, Button b){
        String localEndpoint = Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0];
        if(!endpointId.equals(localEndpoint)) {
            disableButton(b);
            String buttonText = b.getText().toString();
            String selector = idNameMap.get(endpointId);
            buttonText += " selected by " + selector;
            if (idNameMap.size() == 3) {
                if (b.equals(buttonGreen)) {
                    setColors += "1";
                } else if (b.equals(buttonRed)) {
                    setColors += "2";
                } else if (b.equals(buttonBlue)) {
                    setColors += "3";
                } else {
                    setColors += "4";
                }
            }
            b.setText(buttonText);
        }

    }

    // TODO: Umschreiben für den 2-Spieler-2-Farben-Modus
    /**
     * Disables all buttons but the one the player chose.
     *
     * @param b button which was selected
     */
    public void disableAllOtherButtons(Button b){
        Set<Button> buttonSet = new HashSet<>();
        buttonSet.add(buttonGreen);
        buttonSet.add(buttonRed);
        buttonSet.add(buttonBlue);
        buttonSet.add(buttonYellow);
        buttonSet.remove(b);

        for(Button bt : buttonSet){
            disableButton(bt);
        }
    }

    /*
    public void enableButton(Button b){
        //TODO: Methode wird niemals verwendet
        b.setAlpha(1);
        b.setClickable(true);
    }*/

    /**
     * Button gets colored in the case it is selected.
     *
     * @param b button to be filled with color
     * @param col color
     */
    public void colorButton(Button b, String col){
        switch(col){
            case "green":
                b.setBackgroundColor(Color.GREEN);
                break;
            case "red":
                b.setBackgroundColor(Color.RED);
                break;
            case "blue":
                b.setBackgroundColor(Color.BLUE);
                break;
            case "yellow":
                b.setBackgroundColor(Color.YELLOW);
                break;
            default:
                throw new ExceptionInInitializerError("Color failure");
        }
    }

    /*
    public void resetButton(Button b){
        //TODO: Methode wird niemals verwendet
        b.setBackgroundResource(android.R.drawable.btn_default);
    }*/

    /**
     * Handles the button clicks. Sends the random generated
     * turn-String (starting order) to the other players.
     * In the case the Start-Game button gets clicked, all the necessary information is passed to an intent.
     *
     * @param viewID id of the view that was clicked
     */
    private void handleButtons(int viewID){

        debugging("handle buttons");
        switch( viewID ) {
            case R.id.button_green:
                colorButton(buttonGreen, "green");
                disableAllOtherButtons(buttonGreen);
                color = 1;
                sendMessage(messageCodes[0]+R.id.button_green+"-"+Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0]);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_red:
                colorButton(buttonRed, "red");
                disableAllOtherButtons(buttonRed);
                color = 2;
                sendMessage(messageCodes[0]+R.id.button_red+"-"+Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0]);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_blue:
                colorButton(buttonBlue, "blue");
                disableAllOtherButtons(buttonBlue);
                color = 3;
                sendMessage(messageCodes[0]+R.id.button_blue+"-"+Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0]);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_yellow:
                colorButton(buttonYellow, "yellow");
                disableAllOtherButtons(buttonYellow);
                color = 4;
                sendMessage(messageCodes[0]+R.id.button_yellow+"-"+Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0]);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_startgame:

                if(isHost){
                    turn = randomStartingLineup(idNameMap.size());
                    sendMessage(messageCodes[2]+turn);
                }

                debugging("want to start that now");
                final Intent intent = new Intent("at.aau.se2.test.FULLSCREENACTIVITY");
                intent.putExtra("map", idNameMap);
                intent.putExtra("host", isHost);
                intent.putExtra("hostEnd", remoteHostEndpoint);
                intent.putExtra("color", color);
                intent.putExtra("turn", turn);
                intent.putExtra("test", true);
                intent.putExtra("setColors", setColors);
                startActivity(intent);
                if (isHost) {
                    sendMessage(messageCodes[1]);
                }
                break;

            default:
                throw new ExceptionInInitializerError("Buttonhandler failure");
        }


    }

    /**
     * Generates a random string containing numbers for the starting order depending on
     * the count of players (2 or 4)
     *
     * @param count boolean indicating if there are two or 4 players
     * @return turn string
     */
    private static String randomStartingLineup(int count){
        Byte[] array;
        String s;
        switch(count){
            case 2:
                array = new Byte[]{1, 2};
                Collections.shuffle(Arrays.asList(array));
                s = "" + array[0] + "" + array[1] + "";
                break;
            case 3:
                array = new Byte[]{1, 2, 3};
                Collections.shuffle(Arrays.asList(array));
                s = "" + array[0] + "" + array[1] + "" +array[2];
                break;
            case 4:
                array = new Byte[]{1, 2, 3, 4};
                Collections.shuffle(Arrays.asList(array));
                s = "" + array[0] + "" + array[1] + "" +array[2] + "" + array[3] + "";
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return s;
    }

    /**
     * Checks if a game start is possible. In case it it, there is a start button popping up
     * at the hosts screen.
     *
     */
    private void checkGameStart(){
        if(isHost) {
            if (selectCount == idNameMap.size()) {
                startButton.setVisibility(View.VISIBLE);
                startButton.setClickable(true);
                startButton.setBackgroundColor(Color.BLACK);
                startButton.setTextColor(Color.WHITE);
            } else {
                startButton.setVisibility(View.INVISIBLE);
                startButton.setClickable(false);
            }
        }
    }

    @Override
    public void onDisconnected(String s) {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        handleButtons(v.getId());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        throw new UnsupportedOperationException();
    }

    private void debugging(String debMessage) {
        Log.d("tobiasho", debMessage+"_"+isHost);
    }

    private void dev(String debMessage) {
        Log.d("asdfconn", debMessage);
    }

    private void debugging(String debMessage, int t) {
        Log.d("forthat", debMessage+"_"+isHost);
    }
}
