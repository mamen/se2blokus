package at.aau.se2.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

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
 * Created by Tobias on 31.05.2016.
 */
public class ColorScreen extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener,
        View.OnClickListener{


    private GoogleApiClient apiClient;
    private boolean isHost;
    private boolean isConnected = true;
    private String remoteHostEndpoint;
    private List<String> remotePeerEndpoints;
    private HashMap<String, String> ID_Name_Map = new HashMap<String, String>();
    private byte playerID;

    private Button buttonGreen;
    private Button buttonRed;
    private Button buttonBlue;
    private Button buttonYellow;
    private Button startButton;
    private boolean selected = false;
    private int selectCount = 0;
    private String turn;

    private byte color = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //debugging("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorscreen);
        Connection.getInstance().setColorScreen(this);
        Bundle extras = getIntent().getExtras();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ID_Name_Map = ((HashMap<String, String>)extras.getSerializable("map"));
        isHost = extras.getBoolean("host");
        remoteHostEndpoint = extras.getString("hostEnd");
        //get the api from the Singleton
        apiClient = Connection.getInstance().getApiClient();
        remotePeerEndpoints = Connection.getInstance().getRemotePeerEndpoints();
        setupView();
        startButton.setVisibility(View.INVISIBLE);
        startButton.setClickable(false);

        if(isHost){
            turn = randomStartingLineup(ID_Name_Map.size()==2);
            sendMessage("N-"+turn);
        }
    }

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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.reconnect();
    }

    @Override
    public void onConnectionRequest(String s, String s1, String s2, byte[] bytes) {

    }

    @Override
    public void onEndpointFound(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onEndpointLost(String s) {

    }

    private void sendMessage( String message ) {
        debugging("sendMessage");
        if(!remotePeerEndpoints.isEmpty()) {
            if (isHost) {
                Nearby.Connections.sendReliableMessage(apiClient, remotePeerEndpoints, (message).getBytes());
                debugging("hostie_");
                //messageAdapter.add(message);
                //messageAdapter.notifyDataSetChanged();
            }
        }
        else {
            if (remoteHostEndpoint != null) {
                debugging("not hostie_");
                Nearby.Connections.sendReliableMessage(apiClient, remoteHostEndpoint, (message).getBytes());
            }
        }
    }


    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        debugging("colorInRightActivity");
        String message = new String( payload );


        if (message.startsWith("COLOROK-")) {
            if (!selected) {
                debugging("color chosen");
                String[] messArray = message.split("-");
                if (messArray.length == 2) {
                    int id = Integer.parseInt(messArray[1]);
                    setButtonSelected(endpointId, (Button) findViewById(id));

                } else {
                    debugging("message array has wrong format");
                }
                if (isHost) {
                    sendMessage(message);
                    debugging("sendcolor");
                }
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



        else if (message.startsWith("FULLSCREEN")){
            if(selected){
                startButton.performClick();
            }
        }

        else {
            debugging("FEHLER");
        }
    }


    public void disableButton(Button b){
        b.setAlpha(.5f);
        b.setClickable(false);
    }

    public void setButtonSelected(String endpointId, Button b){
        disableButton(b);
        String buttontext = b.getText().toString();
        String selector = ID_Name_Map.get(endpointId);
        buttontext += " selected by "+ selector;
    }

    public void disableAllOtherButtons(Button b){
        Set<Button> buttonset = new HashSet<Button>();
        buttonset.add(buttonGreen);
        buttonset.add(buttonRed);
        buttonset.add(buttonBlue);
        buttonset.add(buttonYellow);
        buttonset.remove(b);

        for(Button bt : buttonset){
            disableButton(bt);
        }
    }

    public void enableButton(Button b){
        b.setAlpha(1);
        b.setClickable(true);
    }

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
        }
    }

    public void resetButton(Button b){
        b.setBackgroundResource(android.R.drawable.btn_default);
    }

    private void handleButtons(int viewID){

        if(isHost){
            turn = randomStartingLineup(ID_Name_Map.size()==2);
            sendMessage("N-"+turn);
        }

        debugging("handle buttons");
        switch( viewID ) {
            case R.id.button_green:
                colorButton(buttonGreen, "green");
                disableAllOtherButtons(buttonGreen);
                color = 1;
                sendMessage("COLOROK-"+R.id.button_green);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_red:
                colorButton(buttonRed, "red");
                disableAllOtherButtons(buttonRed);
                color = 2;
                sendMessage("COLOROK-"+R.id.button_red);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_blue:
                colorButton(buttonBlue, "blue");
                disableAllOtherButtons(buttonBlue);
                color = 3;
                sendMessage("COLOROK-"+R.id.button_blue);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_yellow:
                colorButton(buttonYellow, "yellow");
                disableAllOtherButtons(buttonYellow);
                color = 4;
                sendMessage("COLOROK-"+R.id.button_yellow);
                selectCount++;
                selected = true;
                checkGameStart();
                break;

            case R.id.button_startgame:
                debugging("want to start that now");
                final Intent intent = new Intent("at.aau.se2.test.FULLSCREENACTIVITY");
                intent.putExtra("map", ID_Name_Map);
                intent.putExtra("host", isHost);
                intent.putExtra("hostEnd", remoteHostEndpoint);
                intent.putExtra("color", color);
                intent.putExtra("turn", turn);
                intent.putExtra("test", true);
                startActivity(intent);
                if (isHost) {
                    sendMessage("FULLSCREEN");
                }
                break;
        }


    }

    private String randomStartingLineup(boolean twoplayer){
        Byte[] array;
        String s;
        if(twoplayer){
            array = new Byte[]{1, 2};
            Collections.shuffle(Arrays.asList(array));
            s = "" + array[0] + "" + array[1] + "";
        }
        else {
            array = new Byte[]{1, 2, 3, 4};
            Collections.shuffle(Arrays.asList(array));
            s = "" + array[0] + "" + array[1] + "" +array[2] + "" + array[3] + "";
        }

        return s;
    }

    private void checkGameStart(){
        if(isHost) {
            if (selectCount == ID_Name_Map.size()) {
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

    }

    private void debugging(String debMessage) {
        Log.d("tobiasho", debMessage+"_"+isHost);
    }
}
