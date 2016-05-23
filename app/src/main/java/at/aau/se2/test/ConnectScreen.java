package at.aau.se2.test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener,
        View.OnClickListener {

    //Network fields
    private GoogleApiClient apiClient;
    private static final long CONNECTION_TIME_OUT = 10000L;
    private static int NETWORK_TYPE = ConnectivityManager.TYPE_WIFI;
    private boolean isHost;
    private boolean doHosting;
    private boolean isConnected;
    private String remoteHostEndpoint;
    private List<String> remotePeerEndpoints = new ArrayList<>();
    private HashMap<String, String> ID_Name_Map = new HashMap<>();

    //Graphic fields
    private TextView actStatus;
    private Button connectionButton;
    private EditText textField;
    private Button disconnectButton;
    private Button sendMessageButton;
    private ListView listV;
    private ArrayAdapter<String> messageAdapter;
    private Button startButton;

    private String username = "Guest";
    private String hostName = "Guest";
    private static int participants = 0;


    /**
     * ON STOP verursacht Programmabsturz
     * Disconnect vom Host soll Client disconnecten
     * Liste bzw. Map ersetzt participants
     */


    private CharSequence color;

    /**
     * oncreate Function called after the activity is launched.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Intent i = getIntent();
        doHosting = Boolean.valueOf(i.getStringExtra("host"));
        color = i.getStringExtra("chosen_color");
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();

        debugging("api erstellt");
        setupView();
        if(doHosting){
            connectionButton.setText("Advertise connection");
        }
        else {
            connectionButton.setText("Search for an existing game");
        }
    }

    /**
     * onStart method which connects the apiClient and asks the user for a name
     */
    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
        debugging("api verbunden");

        //Username dialog
        final EditText name = new EditText(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setTitle("Username auswählen");
        builder.setView(name);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = name.getText().toString();
                    if(doHosting) {
                        hostName = username;
                    }
            }
        });
        builder.show();
        debugging("start - "+ username + " + hostname "+ hostName);
    }

    /**
     * Ends the connection
     */
    @Override
    protected void onStop() {
        super.onStop();
        if( apiClient != null && apiClient.isConnected() ) {
            Nearby.Connections.stopAdvertising(apiClient);
            apiClient.disconnect();
            finalizeDisconnection();
        }
        debugging("stop");
    }

    /**
     * Create fields to access/write from/to the graphic elements of the ConnectScreen.
     */
    private void setupView() {
        actStatus = (TextView) findViewById( R.id.text_status );
        connectionButton = (Button) findViewById( R.id.button_connection );
        disconnectButton = (Button) findViewById(R.id.button_disconnection);
        sendMessageButton = (Button) findViewById( R.id.button_send );
        listV = (ListView) findViewById( R.id.list );
        startButton = (Button) findViewById(R.id.button_start);
        startButton.setVisibility(View.INVISIBLE);
        textField = (EditText) findViewById(R.id.editText);


        disconnectButton.setAlpha(.5f);
        disconnectButton.setClickable(false);
        setupButtons();
        setupMessageList();
    }

    /**
     * Set button listeners.
     */
    private void setupButtons() {
        connectionButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);
        sendMessageButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
    }

    /**
     * Set messageList.
     */
    private void setupMessageList() {
        messageAdapter = new ArrayAdapter<>( this, android.R.layout.simple_list_item_1 );
        listV.setAdapter( messageAdapter );
    }

    /**
     * Checks if the device has a (WIFI)-connection to participate in a game.
     *
     * @return true if it is connected, false if not
     */
    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo info = connManager.getNetworkInfo( NETWORK_TYPE );
            if( info != null && info.isConnectedOrConnecting() ) {
                debugging("mit netzwerk verbunden");
                return true;
            }
        debugging("nicht verbunden");
        return false;
    }

    /**
     * Disconnects a device from the others. While a host-disconnection obviously breaks up the whole
     * connection, a normal peer-disconnection does not affect the other participants.
     */
    private void disconnect() {
        if( !isConnectedToNetwork() )
            return;

        if( isHost ) {
            sendMessage( "Shutting down host" );
            Nearby.Connections.stopAdvertising(apiClient);
            Nearby.Connections.stopAllEndpoints(apiClient);

            actStatus.setText( "Not connected" );
            remotePeerEndpoints.clear();
            participants = 0;
            finalizeDisconnection();
            isHost = false;

            debugging("Shutting down NR: "+participants);
        } else {
            if( !isConnected || TextUtils.isEmpty(remoteHostEndpoint) ) {
                Nearby.Connections.stopDiscovery(apiClient, getString( R.string.service_id ) );
                return;
            }

            sendMessage( "Disconnecting" );
            Nearby.Connections.disconnectFromEndpoint(apiClient, remoteHostEndpoint);
            remoteHostEndpoint = null;
            participants--;
            debugging("Disconnect NR: "+participants);
            actStatus.setText( "Disconnected" );
            finalizeDisconnection();
        }


    }

    /**
     * A host method. Advertises a connection to possible peers after checking it checks if the device has a WIFI-connection.
     */
    private void advertise() {
        debugging("start advertising");
        if( !isConnectedToNetwork() ){
            debugging("not connected to wifi");
            actStatus.setText("Please connect the device to WiFi!");
            disconnectButton.setAlpha(0.5f);
            disconnectButton.setClickable(false);
            connectionButton.setAlpha(1f);
            connectionButton.setClickable(true);
            return;
        }

        ID_Name_Map.put(Nearby.Connections.getLocalDeviceId(apiClient), username);
        String name = "Nearby Advertising";


        Nearby.Connections.startAdvertising(apiClient, name, null, CONNECTION_TIME_OUT, this ).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult( Connections.StartAdvertisingResult result ) {
                if( result.getStatus().isSuccess() ) {
                    actStatus.setText("Advertising");
                    participants++;
                    debugging("Starting to advertise NR: "+participants);
                    finalizeConnection();
                }
            }
        });


    }

    /**
     * A peer method. Searches for existing advertising hosts with the same serviceID ("bloxxid").
     * The onConnectionRequest method gets automatically called in the case a connection between a peer and a host would be possible.
     */
    private void discover() {
        debugging("i am discovering");
        if( !isConnectedToNetwork() )
            return;
        String serviceID = getString(R.string.service_id);
        Nearby.Connections.startDiscovery(apiClient, serviceID, 10000L, this).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    actStatus.setText( "Discovering" );
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        debugging("Connected to Wifi");
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.reconnect();
    }

    /**
     * The onConnectionRequest method gets fired each time there is one, but it is only really handled by the host of the connection.
     * The method tries to automatically accept the connection with the other device and subsequently inserts the new peer into the peerlist.
     *
     * @param remoteEndpointId
     * @param remoteDeviceId
     * @param remoteEndpointName
     * @param payload
     */
    @Override
    public void onConnectionRequest(final String remoteEndpointId, final String remoteDeviceId, final String remoteEndpointName, byte[] payload) {
        if( isHost ) {
            debugging("host trying to accept request");
            Nearby.Connections.acceptConnectionRequest(apiClient, remoteEndpointId, payload, this ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if( status.isSuccess() ) {
                        if( !remotePeerEndpoints.contains( remoteEndpointId ) ) {
                            remotePeerEndpoints.add( remoteEndpointId );
                        }

                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        messageAdapter.notifyDataSetChanged();
                        sendMessage(username + " connected!");
                        participants++;
                        debugging("Request accepted NR: "+participants);
                        checkStartGame();

                    }
                }
            });
        } else {
            debugging("no host - not accepting");
            Nearby.Connections.rejectConnectionRequest(apiClient, remoteEndpointId );
        }
    }

    private void checkStartGame(){
        if(isHost){
            if(participants == 2 || participants == 4){
                startButton.setVisibility(View.VISIBLE);
                startButton.setClickable(true);
                startButton.setBackgroundColor(Color.RED);
            }
            else{
                startButton.setVisibility(View.INVISIBLE);
                startButton.setClickable(false);
            }
        }
    }

    private void finalizeConnection(){
        if(isConnected == false) {
            isConnected = true;
            debugging("Device "+Nearby.Connections.getLocalDeviceId(apiClient)+ " connected");
        }
        else{
            debugging("finalize connect falsch aufgerufen");
        }

        String partString = listCurrentParticipants();
        sendMessage(partString);
        checkStartGame();

    }

    private void finalizeDisconnection(){
        if(isConnected == true) {
            isConnected = false;
            debugging("Device "+Nearby.Connections.getLocalDeviceId(apiClient)+ " disconnected");
        }
        else{
            debugging("finalize disconnect falsch aufgerufen");
        }
        String partString =  listCurrentParticipants();
        sendMessage(partString);
        checkStartGame();

    }

    private String listCurrentParticipants(){
        String participants = "";
        for (Map.Entry<String, String> entry : ID_Name_Map.entrySet())
        {
            participants += entry.getValue() + "("+entry.getKey()+")\n";
        }
        sendMessage(participants);
        return participants;
    }

    /**
     * Method to send a message from to all other participants.
     *
     * @param message text to be sent
     */
   private void sendMessage( String message ) {
       if(!remotePeerEndpoints.isEmpty()) {
           if (isHost) {
               Nearby.Connections.sendReliableMessage(apiClient, remotePeerEndpoints, ("host says " + message).getBytes());
               messageAdapter.add(message);
               messageAdapter.notifyDataSetChanged();
           } else {
               Nearby.Connections.sendReliableMessage(apiClient, remoteHostEndpoint, (username + " says: " + message).getBytes());
           }
       }
    }

    /**
     * Automatically sends a connectionRequest to a host-device in case an existing session is found (serviceID has to be the same).
     * This connectionRequest is also automatically accepted by the host.
     *
     * @param endpointId Id of the endpoint
     * @param deviceId deviceId of the endpoint
     * @param serviceId serviceId of the endpoint
     * @param endpointName name of the endpoint
     */
    @Override
    public void onEndpointFound(String endpointId, final String deviceId, final String serviceId, String endpointName) {
        byte[] payload = null;
        debugging("i found something");
        Nearby.Connections.sendConnectionRequest(apiClient, deviceId, endpointId, payload, new Connections.ConnectionResponseCallback() {

            @Override
            public void onConnectionResponse(String s, Status status, byte[] bytes) {
                if( status.isSuccess() ) {
                    ID_Name_Map.put(Nearby.Connections.getLocalDeviceId(apiClient), username);
                    actStatus.setText( "Connected to: " + hostName );
                    Nearby.Connections.stopDiscovery(apiClient, serviceId);
                    sendMessage("NEWPLAYER."+ Nearby.Connections.getLocalDeviceId(apiClient) +"."+username);
                    remoteHostEndpoint = s;
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    finalizeConnection();

                } else {
                    actStatus.setText( "Connection to " + hostName + " failed" );
                }
            }
        }, this );
    }

    /**
     * Sets the isConnected field false in case the endpoint (host) is lost.
     *
     * @param s String parameter
     */
    @Override
    public void onEndpointLost(String s) {
        if( !isHost ) {
            finalizeDisconnection();
        }
    }

    /**
     * Method to handle an incomming message
     *
     * @param endpointId
     * @param payload
     * @param isReliable
     */
    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        String message = new String( payload );
        if(message.startsWith("NEWPLAYER.")){
            String[] messArray = message.split(".");
            String playerID = messArray[1];
            String playerName = messArray[2];
            ID_Name_Map.put(playerID, playerName);

            if( isHost ) {
                sendMessage(message);
            }
        }

        else {
            messageAdapter.add(message );
            messageAdapter.notifyDataSetChanged();

            if( isHost ) {
                sendMessage( message );
            }
        }
        debugging(" ");
    }

    @Override
    public void onDisconnected(String s) {
        if( !isHost ) {
            finalizeDisconnection();
        }
        startButton.setVisibility(View.INVISIBLE);
        debugging("Disconnected NR: "+participants);
    }

    /**
     * Method to handle connection failures.
     *
     * @param connectionResult Parameter containing the error message
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        debugging("no connection possible"+connectionResult);
        if( !isHost ) {
            finalizeDisconnection();
        }
    }

    /**
     * onClick method for the different buttons.
     *      button-connection-click:    evaluates if the current device is hosting or joining - and then decides
     *                                  if it should advertise or discover a connection.
     *
     *      button-disconnection-click: connection is shut down and if it is a host, the whole connection gets dissolved.
     *
     *      button-send-click:          sends a message to all other participants.
     *
     * @param v View which is clicked
     */
    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.button_connection:
                disconnectButton.setAlpha(1f);
                disconnectButton.setClickable(true);

                connectionButton.setAlpha(0.5f);
                connectionButton.setClickable(false);
                if( doHosting ) {
                    isHost = true;
                    advertise();
                    actStatus.setText("Advertising .. "+ username);
                }  else {
                    isHost = false;
                    discover();
                    actStatus.setText("Searching .. "+ username);
                }
                break;

            case R.id.button_disconnection:
                if( isConnected ) {
                    disconnect();
                }
                disconnectButton.setAlpha(0.5f);
                disconnectButton.setClickable(false);
                connectionButton.setAlpha(1f);
                connectionButton.setClickable(true);
                    actStatus.setText("Disconnected ");
                break;

            case R.id.button_send:
                if( !TextUtils.isEmpty( textField.getText() ) && isConnected || ( remotePeerEndpoints != null && !remotePeerEndpoints.isEmpty() ) ) {
                    sendMessage( textField.getText().toString() );
                    textField.setText( "" );
                }
                break;

            case R.id.button_start:
                debugging("i probier jo eh");
                final Intent intent = new Intent("at.aau.se2.test.FULLSCREENACTIVITY");
                intent.putExtra("chosen_color", color);
                startActivity(intent);
        }
    }

    /**
     * Helper method for debugging
     *
     * @param debMessage debug-message
     */
    private void debugging(String debMessage) {
        Log.d("tobiasho", debMessage+"____________ "+output());
    }

    private String output(){
        String output = "";
        for (String key : ID_Name_Map.keySet()) {
            output+=(key + " " + ID_Name_Map.get(key))+" ";
        }
        output += " --- "+participants;
        return output;
    }
}