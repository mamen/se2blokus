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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tobias on 27.04.2016.
 */


public class ConnectScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener,
        View.OnClickListener {

    //Network fields
    private GoogleApiClient apiClient;
    private Connection connection;
    private static final long CONNECTION_TIME_OUT = 10000L;
    private static int NETWORK_TYPE = ConnectivityManager.TYPE_WIFI;
    private boolean isHost;
    private boolean doHosting;
    private boolean isConnected;
    private String remoteHostEndpoint;
    private List<String> remotePeerEndpoints = new ArrayList<>();
    private HashMap<String, String> ID_Name_Map = new HashMap<String, String>();

    //Graphic fields
    private TextView actStatus;
    private Button connectionButton;
    private EditText textField;
    private Button disconnectButton;
    private ListView listV;
    private ArrayAdapter<String> messageAdapter;
    private Button startButton;

    private String username = "Guest";
    private String hostName = "Guest";
    private static int participants = 0;


    /**
     *
     * Finden sich nicht mehr ..
     */


    /**
     * oncreate Function called after the activity is launched.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //debugging("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Intent i = getIntent();
        doHosting = Boolean.valueOf(i.getStringExtra("host"));
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        //debugging("api erstellt");
        setupView();

        if (doHosting == true) {
            connectionButton.setText("Advertise connection");
        } else {
            connectionButton.setText("Search for an existing game");
        }
    }

    /**
     * onStart method which connects the apiClient and asks the user for a name
     */
    @Override
    protected void onStart() {
        //debugging("onStart");
        super.onStart();
        apiClient.connect();
        //debugging("api verbunden");

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
                if (doHosting) {
                    hostName = username;
                }
                //
                // debugging("start - "+ username + " + hostname "+ hostName);
            }
        });
        builder.show();

    }

    /**
     * Ends the connection
     */
    @Override
    protected void onStop() {
        //debugging("onStop");
        super.onStop();
        /*
        if( apiClient != null && apiClient.isConnected() ) {
            Nearby.Connections.stopAdvertising(apiClient);
            disconnect();
            apiClient.disconnect();
            finalizeDisconnection();
        }
        //debugging("stop");*/
    }

    /**
     * Create fields to access/write from/to the graphic elements of the connectScreen.
     */
    private void setupView() {
        actStatus = (TextView) findViewById(R.id.text_status);
        connectionButton = (Button) findViewById(R.id.button_connection);
        disconnectButton = (Button) findViewById(R.id.button_disconnection);
        listV = (ListView) findViewById(R.id.list);
        startButton = (Button) findViewById(R.id.button_start);
        startButton.setVisibility(View.INVISIBLE);


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
        startButton.setOnClickListener(this);
    }

    /**
     * Set messageList.
     */
    private void setupMessageList() {
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listV.setAdapter(messageAdapter);
    }

    /**
     * Checks if the device has a (WIFI)-connection to participate in a game.
     *
     * @return true if it is connected, false if not
     */
    private boolean isConnectedToNetwork() {
        //debugging("isConnectedToNetwork");
        ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getNetworkInfo(NETWORK_TYPE);
        if (info != null && info.isConnectedOrConnecting()) {
            //debugging("mit netzwerk verbunden");
            return true;
        }
        //debugging("nicht verbunden");
        return false;
    }

    /**
     * Disconnects a device from the others. While a host-disconnection obviously breaks up the whole
     * connection, a normal peer-disconnection does not affect the other participants.
     */
    private void disconnect() {
        debugging("disconnect");
        if (!isConnectedToNetwork() || isConnected == false)
            return;
        //debugging("ishost? "+isHost);
        if (isHost) {
            ID_Name_Map.clear();
            sendMessage("Shutting down host");
            Nearby.Connections.stopAdvertising(apiClient);
            Nearby.Connections.stopAllEndpoints(apiClient);
            actStatus.setText("Not connected");
            remotePeerEndpoints.clear();
            //participants = 0;
            finalizeDisconnection();
            //isHost = false;

            //debugging("Shutting down NR: "+ID_Name_Map.size());
        } else {
            if (!isConnected || TextUtils.isEmpty(remoteHostEndpoint)) {
                Nearby.Connections.stopDiscovery(apiClient, getString(R.string.service_id));
                return;
            }
            //debugging("komme ich hier her?");
            //ID_Name_Map.remove(Nearby.Connections.getLocalDeviceId(apiClient));
            ID_Name_Map.clear();
            sendMessage("Disconnecting");
            Nearby.Connections.disconnectFromEndpoint(apiClient, remoteHostEndpoint);
            remoteHostEndpoint = null;
            //participants--;
            //debugging("Disconnect NR: "+ID_Name_Map.size());
            actStatus.setText("Disconnected");
            finalizeDisconnection();
        }


    }

    /**
     * A host method. Advertises a connection to possible peers after checking it checks if the device has a WIFI-connection.
     */
    private void advertise() {
        //debugging("advertise");
        if (!isConnectedToNetwork()) {
            //debugging("not connected to wifi");
            actStatus.setText("Please connect the device to WiFi!");
            disconnectButton.setAlpha(0.5f);
            disconnectButton.setClickable(false);
            connectionButton.setAlpha(1f);
            connectionButton.setClickable(true);
            return;
        }

        ID_Name_Map.put(Nearby.Connections.getLocalDeviceId(apiClient), username);
        String name = "Nearby Advertising";


        Nearby.Connections.startAdvertising(apiClient, name, null, CONNECTION_TIME_OUT, this).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    actStatus.setText("Advertising");
                    ID_Name_Map.put(Nearby.Connections.getLocalDeviceId(apiClient), username);


                    //participants++;
                    //debugging("Starting to advertise NR: "+participants);
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
        //debugging("discover");
        if (!isConnectedToNetwork())
            return;
        String serviceID = getString(R.string.service_id);
        Nearby.Connections.startDiscovery(apiClient, serviceID, 10000L, this).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    actStatus.setText("Discovering");
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        //debugging("onConnected");
    }


    // WICHTIG IST DIE DEVICEID


    @Override
    public void onConnectionSuspended(int i) {
        apiClient.reconnect();
    }

    /**
     * The onConnectionRequest method gets fired each time there is one, but it is only really handled by the host of the connection.
     * The method tries to automatically accept the connection with the other device and subsequently inserts the new peer into the peerlist.
     *
     * @param remoteEndpointId   deviceID + number
     * @param remoteDeviceId     deviceID
     * @param remoteEndpointName deviceID of communication partner
     * @param payload
     */
    @Override
    public void onConnectionRequest(final String remoteEndpointId, final String remoteDeviceId, final String remoteEndpointName, final byte[] payload) {
        //debugging("onConnectionRequest");
        if (isHost) {
            //debugging("host trying to accept request");
            Nearby.Connections.acceptConnectionRequest(apiClient, remoteEndpointId, payload, this).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        if (!remotePeerEndpoints.contains(remoteEndpointId)) {
                            remotePeerEndpoints.add(remoteEndpointId);
                        }
                        String user = new String(payload, StandardCharsets.UTF_8);
                        //debugging("user: "+user);
                        debugging("name of partner" + remoteEndpointId);
                        ID_Name_Map.put(remoteDeviceId, user);
                        //debugging("INFORMATION: "+ remoteEndpointId + ", "+ remoteDeviceId+", "+ remoteEndpointName);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        sendMessage("NEWPLAYER-" + Nearby.Connections.getLocalDeviceId(apiClient) + "-" + username);
                        messageAdapter.notifyDataSetChanged();
                        //sendMessage(username + " connected!");
                        //participants++;
                        //debugging("Request accepted NR: "+ ID_Name_Map.size());
                        checkStartGame();

                    }
                }
            });
        } else {
            //debugging("no host - not accepting");
            Nearby.Connections.rejectConnectionRequest(apiClient, remoteEndpointId);
        }
    }

    private void checkStartGame() {
        //debugging("found that!");
        setupParticipantList();
        if (isHost) {
            if (ID_Name_Map.size() == 2 || ID_Name_Map.size() == 4) {
                startButton.setVisibility(View.VISIBLE);
                startButton.setClickable(true);
                startButton.setBackgroundColor(Color.RED);
            } else {
                startButton.setVisibility(View.INVISIBLE);
                startButton.setClickable(false);
            }
        }
    }

    private void setupParticipantList() {
        String partString = listCurrentParticipants();
        messageAdapter.clear();
        messageAdapter.add(partString);
        messageAdapter.notifyDataSetChanged();
    }

    private void finalizeConnection() {
        if (isConnected == false) {
            isConnected = true;
            //debugging("Device "+Nearby.Connections.getLocalDeviceId(apiClient)+ " connected");
        } else {
            //debugging("finalize connect falsch aufgerufen");
        }

        String partString = listCurrentParticipants();
        sendMessage(partString);
        checkStartGame();

    }

    private void finalizeDisconnection() {
        if (isConnected == true) {
            isConnected = false;
            //debugging("Device "+Nearby.Connections.getLocalDeviceId(apiClient)+ " disconnected");
        } else {
            //debugging("finalize disconnect falsch aufgerufen");
        }
        String partString = listCurrentParticipants();
        sendMessage(partString);
        checkStartGame();

    }

    private String listCurrentParticipants() {
        String particip = "";
        for (Map.Entry<String, String> entry : ID_Name_Map.entrySet()) {
            particip += entry.getValue() + " (" + entry.getKey() + ")\n";
        }
        sendMessage(particip);
        return particip;
    }

    /**
     * Method to send a message from to all other participants.
     *
     * @param message text to be sent
     */
    private void sendMessage(String message) {
        //debugging("sendMessage");
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

    /**
     * Automatically sends a connectionRequest to a host-device in case an existing session is found (serviceID has to be the same).
     * This connectionRequest is also automatically accepted by the host.
     *
     * @param endpointId   Id of the endpoint
     * @param deviceId     deviceId of the endpoint
     * @param serviceId    serviceId of the endpoint
     * @param endpointName name of the endpoint
     */
    @Override
    public void onEndpointFound(String endpointId, final String deviceId, final String serviceId, String endpointName) {
        //debugging("onEndpointFound");

        byte[] payload = username.getBytes(StandardCharsets.UTF_8);
        //byte[] payload = null;

        //debugging("i found something");
        Nearby.Connections.sendConnectionRequest(apiClient, deviceId, endpointId, payload, new Connections.ConnectionResponseCallback() {
            @Override
            public void onConnectionResponse(String s, Status status, byte[] bytes) {
                if (status.isSuccess()) {
                    ID_Name_Map.put(Nearby.Connections.getLocalDeviceId(apiClient), username);
                    actStatus.setText("Connected to: " + hostName);
                    Nearby.Connections.stopDiscovery(apiClient, serviceId);
                    remoteHostEndpoint = s;
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    finalizeConnection();

                } else {
                    actStatus.setText("Connection to " + hostName + " failed");
                }
            }
        }, this);
    }

    /**
     * Sets the isConnected field false in case the endpoint (host) is lost.
     *
     * @param s String parameter
     */
    @Override
    public void onEndpointLost(String s) {
        debugging("onEndpointLost");
        if (!isHost) {
            finalizeDisconnection();
        }
    }

    /**
     * Normalerweise nur niedrige Zahlen + Koordinaten (max 20) im Array und durch fehlende Konvertierung (zB von String auf byte[] bleibt das auch so
     *
     * @param b
     * @return
     */
    private boolean onlyNum(byte[] b) {

        boolean isStone = true;

        for (int i = 0; i < b.length; i++) {
            if (b[i] > 20) {
                isStone = false;
                return isStone;
            }

        }

        return isStone;
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
        //debugging("onMessageReceived");
        if (onlyNum(payload)) {
            debugging("setStone", 1);
            FullscreenActivity full = Connection.getInstance().getFullscreenActivity();
            full.onMessageReceived(endpointId, payload, isReliable);

        } else {
            String message = new String(payload);
            debugging("sender " + endpointId + ", message " + message);
            if (message.startsWith("NEWPLAYER-")) {
                debugging("try to add new player to player list");
                String[] messArray = message.split("-");
                if (messArray.length == 3) {
                    String playerID = messArray[1];
                    String playerName = messArray[2];
                    ID_Name_Map.put(playerID, playerName);
                } else {
                    debugging("message array has wrong format");
                }
                if (isHost) {
                    sendMessage(message);
                    debugging("send new player");
                }
            } else if (message.startsWith("REMOVE-")) {
                debugging("try remove player to player list");
                String[] messArray = message.split("-");
                if (messArray.length == 2) {
                    String playerID = messArray[1];
                    ID_Name_Map.remove(playerID);
                } else {
                    debugging("message array has wrong format");
                }
                if (isHost) {
                    sendMessage(message);
                    debugging("remove player");
                }
            } else if (message.startsWith("COLOROK-")) {
                debugging("color is here", 1);
                ColorScreen cs = Connection.getInstance().getColorScreen();
                cs.onMessageReceived(endpointId, payload, isReliable);
            } else if (message.startsWith("START")) {
                debugging("try to start game");
                startButton.performClick();
            /*String[] messArray = message.split("-");
            if(messArray.length == 2) {
                String playerID = messArray[1];
                ID_Name_Map.remove(playerID);
            }
            else {
                debugging("message array has wrong format");
            }
            if( isHost ) {
                sendMessage(message);
                debugging("remove player");
            }*/
            }
            else if (message.startsWith("FULLSCREEN")) {
                debugging("start it!"+message);
                ColorScreen cs = Connection.getInstance().getColorScreen();
                cs.onMessageReceived(endpointId, payload, isReliable);
            }
            else {
                messageAdapter.add(message);
                messageAdapter.notifyDataSetChanged();

                if (isHost) {
                    sendMessage(message);
                }
            }
        }
    }

    /**
     * Gets triggered at the endpoint after a device is disconnected.
     *
     * @param s complete ID from disconnected partner
     */
    @Override
    public void onDisconnected(String s) {
        if (!isHost && isConnected) {
            disconnectButton.performClick();
        } else if (isHost && isConnected) {
            //debugging("... "+s);
            String id = "";
            if (s.contains(":")) {
                String[] idArr = s.split(":");
                id = idArr[0];
                ID_Name_Map.remove(id);
            }
            sendMessage("REMOVE-" + id);
            checkStartGame();
        }
        /*if( !isHost ) {
            finalizeDisconnection();
        }
        startButton.setVisibility(View.INVISIBLE);
        debugging("Disconnected NR: "+participants);*/
    }

    /**
     * Method to handle connection failures.
     *
     * @param connectionResult Parameter containing the error message
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //debugging("no connection possible"+connectionResult);
        if (!isHost) {
            finalizeDisconnection();
        }
    }

    /**
     * onClick method for the different buttons.
     * button-connection-click:    evaluates if the current device is hosting or joining - and then decides
     * if it should advertise or discover a connection.
     * <p/>
     * button-disconnection-click: connection is shut down and if it is a host, the whole connection gets dissolved.
     * <p/>
     * button-send-click:          sends a message to all other participants.
     *
     * @param v View which is clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_connection:
                disconnectButton.setAlpha(1f);
                disconnectButton.setClickable(true);

                connectionButton.setAlpha(0.5f);
                connectionButton.setClickable(false);
                if (doHosting) {
                    isHost = true;
                    advertise();
                    actStatus.setText("Advertising .. " + username);
                } else {
                    isHost = false;
                    discover();
                    actStatus.setText("Searching .. " + username);
                }
                break;

            case R.id.button_disconnection:
                if (isConnected) {
                    debugging("buttondisconnection");
                    disconnect();
                }
                disconnectButton.setAlpha(0.5f);
                disconnectButton.setClickable(false);
                connectionButton.setAlpha(1f);
                connectionButton.setClickable(true);
                actStatus.setText("Disconnected ");
                break;



            case R.id.button_start:
                if (isHost) {
                    sendMessage("START");
                }
                //set the api in the Singleton
                Connection.getInstance().setApiClient(apiClient);
                Connection.getInstance().setRemotePeerEndpoints(remotePeerEndpoints);
                //chooseStoneColor();
                final Intent intent = new Intent("at.aau.se2.test.COLORSCREEN");
                intent.putExtra("map", ID_Name_Map);
                intent.putExtra("host", isHost);
                intent.putExtra("hostEnd", remoteHostEndpoint);
                startActivity(intent);
        }
    }


    /**
     * Helper method for debugging
     *
     * @param debMessage debug-message
     */
    private void debugging(String debMessage) {
        Log.d("tobiasho", debMessage);
    }

    private void debugging(String debMessage, int i) {
        Log.d("tobiasho", debMessage);
    }

    private String output() {
        String output = "";
        for (String key : ID_Name_Map.keySet()) {
            output += (key + " " + ID_Name_Map.get(key)) + " ";
        }
        output += " - ANZ:" + ID_Name_Map.size();
        return output;
    }

}