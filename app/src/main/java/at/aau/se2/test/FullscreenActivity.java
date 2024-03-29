package at.aau.se2.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FullscreenActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.ConnectionRequestListener,
        Connections.MessageListener,
        Connections.EndpointDiscoveryListener,
        View.OnClickListener
{

    public RelativeLayout fullscreenLayout;
    public GridLayout gameBoardLayout;
    private LinearLayout blockDrawer;
    public GameLogic gl;
    private static final int SIZE = 20;
    public int selectedBlockID;
    private List<ImageView> blockDrawerChildren;
    private List<ImageView> removedBlockDrawerChildren;
    public Player player;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean doubleTappedToClaim = false;
    public static ImageView testView;
    private byte[][] rememberField;
    public boolean elementFinished;
    private View.DragShadowBuilder shadowBuilder;
    public int transposeCount; //Zähler wie oft der Stein gedreht wurde
    private boolean doSettings;


    //private Connection connection;
    private GoogleApiClient apiClient;
    private boolean isHost;
    //private boolean isConnected = true;
    private String remoteHostEndpoint;
    private List<String> remotePeerEndpoints = new ArrayList<>();
    private HashMap<String, String> idNameMap = new HashMap<>();
    private byte playerID;
    private ImageView imgView;
    private int myturn;
    private int actTurn;
    private String otherColors = "";
    public MediaPlayer placeSound;
    private int winCount = 0;
    private String winner = "";

    private static ArrayList<Player> players;
    private int countFinished = 0;

    private TextView pointsRed;
    private TextView pointsBlue;
    private TextView pointsGreen;
    private TextView pointsYellow;

    private int oldPointsRed;
    private int oldPointsBlue;
    private int oldPointsGreen;
    private int oldPointsYellow;
    private final int MAX_POINTS_REGULAR = 89;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        placeSound = MediaPlayer.create(getApplicationContext(), R.raw.click);

        //Initialisierung div. Variablen.
        fullscreenLayout = (RelativeLayout) findViewById(R.id.contentPanel);


        playerID = -1;
        selectedBlockID = -1;
        rememberField = new byte[5][5];
        elementFinished = true;

        Bundle extras = getIntent().getExtras();
        doSettings = extras.getBoolean("test");

        idNameMap = (HashMap<String, String>) extras.getSerializable("map");
        isHost = extras.getBoolean("host");
        remoteHostEndpoint = extras.getString("hostEnd");
        remotePeerEndpoints = Connection.getInstance().getRemotePeerEndpoints();
        Log.d("String.length()", "Length: " + otherColors.length());
        otherColors = extras.getString("setColors");
        //get the api from the Singleton
        apiClient = Connection.getInstance().getApiClient();
        Connection.getInstance().setFullscreenActivity(this);

        playerID = extras.getByte("color");
        if (doSettings) {
            if (idNameMap.size() == 2) {
                if (isHost) {
                    myturn = Integer.parseInt(String.format("%s", extras.getString("turn").charAt(0)));
                } else {
                    myturn = Integer.parseInt(String.format("%s", extras.getString("turn").charAt(1)));
                }
            } else if (idNameMap.size() == 3) {
                int other1 = Integer.parseInt(otherColors.charAt(0) + "");
                int other2 = Integer.parseInt(otherColors.charAt(1) + "");
                if (playerID < other1 && playerID < other2) {
                    myturn = Integer.parseInt(extras.getString("turn").charAt(0) + "");
                } else if (playerID > other1 && playerID > other2) {
                    myturn = Integer.parseInt(extras.getString("turn").charAt(1) + "");
                } else {
                    myturn = Integer.parseInt(extras.getString("turn").charAt(2) + "");
                }
            } else {
                myturn = Integer.parseInt(String.format("%s", extras.getString("turn").charAt(playerID - 1)));
            }
            //Log.d("--DEBUG--",extras.getString("turn"));
            //Log.d("--DEBUG--",Integer.toString(myturn));
        }






        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // lade Player
        player = new Player(playerID);
        // lade GameLogic
        gl = GameLogic.getInstance(player, this.getApplicationContext());
        // Statusbar verstecken
        hideStatusBar();

        // Spielbrett erzeugen
        updateGameBoard();

        // BlockDrawer erzeugen
        initializeBlockDrawer();


        // Draglistener erstellen
        gameBoardLayout.setOnDragListener(new DragListener(this));


        imgView = (ImageView) findViewById(R.id.img_stop);
        //if (doSettings) {
        imgView.setVisibility(View.GONE);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doubleTap();
            }
        });
        //}

        actTurn = 1;

        if (doSettings) {
            if (myturn == actTurn) {
                enableScreenInteraction();
            } else {
                disableScreenInteraction();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initializePoints();
    }

    /**
     * Initialises the points-counter
     * sets the layoutparams and font-styles
     */
    private void initializePoints() {
        Typeface font = Typeface.createFromAsset(getAssets(), "blocked.ttf");

        pointsRed = (TextView) findViewById(R.id.pointsRed);
        pointsBlue = (TextView) findViewById(R.id.pointsBlue);
        pointsGreen = (TextView) findViewById(R.id.pointsGreen);
        pointsYellow = (TextView) findViewById(R.id.pointsYellow);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (playerID){
            case 1:
                pointsGreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.setCancelable(false);
                        builder.setTitle("DO YOU WANT TO PASS TO THE NEXT PLAYER?");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendMessage("GOON");
                                myTurnAdd();
                                disableScreenInteraction();

                            }
                        });
                        builder.show();
                    }
                });
                break;
            case 2:
                pointsRed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.setCancelable(false);
                        builder.setTitle("DO YOU WANT TO PASS TO THE NEXT PLAYER?");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendMessage("GOON");
                                myTurnAdd();
                                disableScreenInteraction();

                            }
                        });
                        builder.show();
                    }
                });
                break;
            case 3:
                pointsBlue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.setCancelable(false);
                        builder.setTitle("DO YOU WANT TO PASS TO THE NEXT PLAYER?");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendMessage("GOON");
                                myTurnAdd();
                                disableScreenInteraction();

                            }
                        });
                        builder.show();
                    }
                });
                break;
            case 4:
                pointsYellow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.setCancelable(false);
                        builder.setTitle("DO YOU WANT TO PASS TO THE NEXT PLAYER?");
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendMessage("GOON");
                                myTurnAdd();
                                disableScreenInteraction();

                            }
                        });
                        builder.show();
                    }
                });
                break;

        }

        RelativeLayout.LayoutParams paramsPointsRed = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsPointsBlue = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsPointsGreen = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsPointsYellow = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        int quater = getScreenWidth() / 4;
        int top = getScreenHeight() - pointsRed.getPaddingTop() - pointsRed.getPaddingBottom() - pointsRed.getLineHeight() - 20;

        paramsPointsRed.setMargins(0, top, 0, 0);
        paramsPointsRed.width = quater;
        paramsPointsBlue.setMargins(quater, top, 0, 0);
        paramsPointsBlue.width = quater;
        paramsPointsGreen.setMargins(quater * 2, top, 0, 0);
        paramsPointsGreen.width = quater;
        paramsPointsYellow.setMargins(quater * 3, top, 0, 0);
        paramsPointsYellow.width = quater;

        pointsRed.setTypeface(font);
        pointsBlue.setTypeface(font);
        pointsGreen.setTypeface(font);
        pointsYellow.setTypeface(font);
        pointsRed.setTextSize(20);
        pointsBlue.setTextSize(20);
        pointsGreen.setTextSize(20);
        pointsYellow.setTextSize(20);


        pointsRed.setLayoutParams(paramsPointsRed);
        pointsBlue.setLayoutParams(paramsPointsBlue);
        pointsGreen.setLayoutParams(paramsPointsGreen);
        pointsYellow.setLayoutParams(paramsPointsYellow);

        pointsRed.setText("0");
        pointsBlue.setText("0");
        pointsGreen.setText("0");
        pointsYellow.setText("0");

        oldPointsRed = 0;
        oldPointsYellow = 0;
        oldPointsBlue = 0;
        oldPointsGreen = 0;
    }

    /**
     * updates the player-points
     */
    public void updatePoints() {
        byte[][] gameBoard = gl.getGameBoard();

        int curPointsRed = 0;
        int curPointsGreen = 0;
        int curPointsBlue = 0;
        int curPointsYellow = 0;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                switch (gameBoard[i][j]) {
                    case 1:
                        curPointsGreen++;
                        break;
                    case 2:
                        curPointsRed++;
                        break;
                    case 3:
                        curPointsBlue++;
                        break;
                    case 4:
                        curPointsYellow++;
                        break;

                }
            }
        }

        curPointsGreen += addExtraPoints(oldPointsGreen, curPointsGreen);
        curPointsRed += addExtraPoints(oldPointsRed, curPointsRed);
        curPointsBlue += addExtraPoints(oldPointsBlue, curPointsBlue);
        curPointsYellow += addExtraPoints(oldPointsYellow, curPointsYellow);

        oldPointsGreen = curPointsGreen;
        oldPointsRed = curPointsRed;
        oldPointsBlue = curPointsBlue;
        oldPointsYellow = curPointsYellow;

        pointsRed.setText(Integer.toString(curPointsRed));
        pointsGreen.setText(Integer.toString(curPointsGreen));
        pointsBlue.setText(Integer.toString(curPointsBlue));
        pointsYellow.setText(Integer.toString(curPointsYellow));

    }

    public void winAdd(String endpointId){
        dev(endpointId);
        String localEndpoint = Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0];
        dev(localEndpoint);
        if(!endpointId.equals(localEndpoint)) {
            winCount++;
            dev(winCount+"");
            if(winCount==idNameMap.size()-1){
                //PLAYER OFFICIALLY WINS!
                goOn();
            }
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                winCount = 0;
            }
        }, 5000);
    }

    public void claim(String s){
        final String win = s;
        dev(""+s);
        String localEndpoint = Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0];
        dev(""+localEndpoint);
        if(!s.equals(localEndpoint)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(idNameMap.get(s) + " CLAIMS TO BE THE WINNER.");
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("HE IS!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    winner = idNameMap.get(win);
                    sendMessage("WINNER-" + Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0]);
                    winCount++;

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            winCount = 0;
                        }
                    }, 5000);
                    if(winCount==idNameMap.size()-1){
                        //PLAYER OFFICIALLY WINS!
                        goOn();
                    }
                }
            });
            builder.show();
        }
    }



    /**
     * adds a bonus to the points
     * @param oldpoints
     * @param currentPoints
     * @return new points
     */
    private int addExtraPoints(int oldpoints, int currentPoints) {
        int retValue = 0;
        if (oldpoints == MAX_POINTS_REGULAR - 1 && currentPoints == MAX_POINTS_REGULAR) {
            retValue += 20;
        }
        if (currentPoints == MAX_POINTS_REGULAR) {
            retValue += 15;
        }

        return retValue;
    }

    /**
     * initialises the Blockdrawer
     * adds all the stones to the drawer
     * this method is only called at the beginning of the game
     */
    private void initializeBlockDrawer() {
        LinearLayout blockDrawerParent = (LinearLayout) findViewById(R.id.blockDrawer_parent);
        ViewGroup.LayoutParams params = blockDrawerParent.getLayoutParams();

        params.height = getScreenHeight() - (getScreenWidth());
        blockDrawerParent.setLayoutParams(params);

        blockDrawer = (LinearLayout) findViewById(R.id.blockDrawer);

        blockDrawerChildren = new ArrayList<>();
        removedBlockDrawerChildren = new ArrayList<>();

        String color = player.getPlayerColor();

        //Alle Spielsteine hinzufügen
        for (int i = 0; i < 22; i++) {
            final ImageView oImageView = new ImageView(this);

            oImageView.setImageResource(getResources().getIdentifier(color + "_" + i, "drawable", getPackageName()));
            oImageView.setTag(i);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.setGravity(Gravity.CENTER);
            oImageView.setLayoutParams(param);
            blockDrawer.addView(oImageView);
            blockDrawerChildren.add(oImageView);

            //Touch-Eventhandler initialisieren
            oImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (elementFinished) {
                        vibrate(100);
                        ClipData data = ClipData.newPlainText("", "");
                        //v.getHeight()/2 -> Mitte vom Stein; v.getHeight -> Unterm Stein
                        shadowBuilder = new OwnDragShadowBuilder(v, v.getWidth() / 2, v.getHeight() / 2);
                        v.startDrag(data, shadowBuilder, v, 0);

                        for (ImageView bdc : blockDrawerChildren) {
                            if (bdc.equals(v)) {
                                selectedBlockID = (int) bdc.getTag(); //Gewählter Spielstein
                            }
                        }
                        testView = (ImageView) v;
                        testView.setVisibility(View.INVISIBLE);
                        elementFinished = false;
                        return true;
                    }
                    return true;
                }
            });
        }

    }

    /**
     * creates the gameboard and fills the gameboardlayout
     * this method is only called at the beginning of the game
     */
    private void updateGameBoard() {
        gameBoardLayout = null;
        gameBoardLayout = (GridLayout) findViewById(R.id.gameBoard);

        //Sicherheitshalber alle vorherigen Elemente auf dem gameBoard löschen
        gameBoardLayout.removeAllViews();
        //Toast.makeText(getApplicationContext(),"GAMEBOARDID =  " + gameBoardLayout.getId(), Toast.LENGTH_LONG).show();

        gameBoardLayout.setColumnCount(SIZE);
        gameBoardLayout.setRowCount(SIZE);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {

                ImageView oImageView = new ImageView(this);
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                int size = getScreenWidth() / SIZE;
                param.height = size;
                param.width = size;

                param.setGravity(Gravity.CENTER);
                param.columnSpec = GridLayout.spec(i);
                param.rowSpec = GridLayout.spec(j);
                gameBoardLayout.addView(oImageView);
            }
        }

        updatePartOfGameBoard(0, 0, SIZE, SIZE);
    }

    /**
     * Updates a specific part of the GameBoard
     *
     * @param startX start-position (X) for the update
     * @param startY start-position (Y) for the update
     * @param endX   end-position (X) for the update
     * @param endY   end-position (Y) for the update
     */
    private void updatePartOfGameBoard(int startX, int startY, int endX, int endY) {
        byte[][] board = gl.getGameBoard();

        //Toast.makeText(this.getApplicationContext(),"updated from x: " + startX + " y: " + startY + " to x: " + endX + " y:"+endY, Toast.LENGTH_LONG).show();
        if (endX > 20) endX = 20;
        if (endY > 20) endY = 20;
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                ImageView oImageView = (ImageView) gameBoardLayout.getChildAt(20 * i + j);
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                int size = getScreenWidth() / SIZE;
                //param.stuff extracted because duplicate code
                param.height = size;
                param.width = size;

                param.setGravity(Gravity.CENTER);
                param.columnSpec = GridLayout.spec(i);
                param.rowSpec = GridLayout.spec(j);

                switch (board[i][j]) { //Ermöglicht Unterscheidung zwischen wirklichem Stein und Preview
                    case 0:
                    case 5:
                        oImageView.setImageResource(R.drawable.gameboard_empty);
                        break;
                    case 1:
                        oImageView.setImageResource(R.drawable.green_s_1);
                        break;
                    case 6:
                        oImageView.setImageResource(R.drawable.green_s_1_preview);
                        break;
                    case 2:
                        oImageView.setImageResource(R.drawable.red_s_1);
                        break;
                    case 7:
                        oImageView.setImageResource(R.drawable.red_s_1_preview);
                        break;
                    case 3:
                        oImageView.setImageResource(R.drawable.blue_s_1);
                        break;
                    case 8:
                        oImageView.setImageResource(R.drawable.blue_s_1_preview);
                        break;
                    case 4:
                        oImageView.setImageResource(R.drawable.yellow_s_1);
                        break;
                    case 9:
                        oImageView.setImageResource(R.drawable.yellow_s_1_preview);
                        break;
                    default:
                        throw new ExceptionInInitializerError("Draw color on GameBoard failed");
                }
                int index = i * 20 + j;
                oImageView.setLayoutParams(param);
                gameBoardLayout.removeViewAt(index);
                gameBoardLayout.addView(oImageView, index);
            }
        }
    }

    /**
     * Checks your placement:
     * First stone must "hitTheCorner"
     * Every other stone need to follow the gamerules
     *
     * @param x - the col where you want to place it
     * @param y - the row where you want to place it
     * @return true, if the placement is valid
     * false, else
     */
    public boolean isYourPlacementValid(int x, int y) {
        if (selectedBlockID >= 0) {
            byte[][] b = player.getStone(selectedBlockID - 1);
            for (int a = 0; a < transposeCount; a++) {
                b = gl.rotate(b);
            }
            // TODO Deform Array
            if (preValidation()) {
                if (!removedBlockDrawerChildren.isEmpty()) {
                    if (!gl.checkTheRules(b, x, y)) {
                        return false;
                    }
                } else {
                    if (!gl.hitTheCorner(b, x, y)) {
                        //vibrate(500);
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks, if the preview would overwrite some stone
     *
     * @return false, if you would hit some stone
     * true, else
     */
    private boolean preValidation() {
        byte[][] b = player.getStone(selectedBlockID - 1);
        for (int a = 0; a < transposeCount; a++) {
            b = gl.rotate(b);
        }
        for (int x = 0; x < rememberField.length; x++) {
            for (int y = 0; y < rememberField[x].length; y++) {
                if (b[x][y] != 0 && rememberField[x][y] != 0) return false;
            }
        }
        return true;
    }

    /**
     * Tries to place a preview of your stone placement after letting go of the Drag
     * Stone gets rotated as many times as the transpose-button was pressed
     *
     * @param x - the col where you want to place it
     * @param y - the row where you want to place it
     * @return false, if stone goes over edge (Should we handle this case better?)
     * true, if stone was drawn
     */
    public boolean drawStone(int x, int y) {
        if (selectedBlockID >= 0) {
            byte[][] b = player.getStone(selectedBlockID - 1);

            for (int a = 0; a < transposeCount; a++) {
                b = gl.rotate(b);
            }

            //TODO Deform Array

            b = changeToPreview(b, true);

            if (gl.placeOverEdge(b, x, y)) {
                changeToPreview(b, false);
                return false;
            } else {
                rememberField = gl.rememberField(b, x, y);
                gl.placeStone(b, x, y);
                changeToPreview(b, false);
            }
        }

        updatePartOfGameBoard(x, y, (x + 6 > 20) ? 20 : x + 6, (y + 6 > 20) ? 20 : y + 6);
        return true;
    }

    /**
     * Changes the color of the stone, to give it the same color (But recognizable as Preview)
     *
     * @param b        - Byte array of your stone
     * @param addOrSub - Add -> Get PreviewColor; Sub -> Get NormalColor
     * @return Stone Array with changed color
     */
    private byte[][] changeToPreview(byte[][] b, boolean addOrSub) {
        byte help = player.getPlayerId();
        if (addOrSub) {
            help += 5;
        } else {
            help -= 5;
        }
        byte[][] retArr = new byte[b.length][b.length];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[i].length; j++) {
                if (b[i][j] != 0) retArr[i][j] = help;
            }
        }
        return retArr;
    }

    /**
     * Restores the field, to the state before the preview was drawn
     *
     * @param i - the col where you want to restore it
     * @param j - the row where you want to restore it
     */
    public void restore(int i, int j) {
        gl.restoreField(rememberField, i, j);
        updatePartOfGameBoard(i, j, (i + 5 > 20) ? 20 : i + 5, (j + 5 > 20) ? 20 : j + 5);
    }

    public void endGame(){
        dev("endFULL");
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

    /**
     * Manipulates the XPlacement, so that you can place it more natural
     * Don't change the BlockOrder, or this won't work properly!!
     *
     * @param selectedBlock The tag of your stone
     * @return an Integer, to change the placement in X-direction
     */
    public static int manipulateX(int selectedBlock) {
        switch (selectedBlock) {
            case 0:
            case 1:
            case 2:
            case 14:
            case 15:
            case 17:
                return 0;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 18:
            case 19:
            case 20:
                return 1;
            default:
                throw new ExceptionInInitializerError("manipulateX failed");
        }
    }

    /**
     * Manipulates the YPlacement, so that you can place it more natural
     * Don't change the BlockOrder, or this won't work properly!!
     *
     * @param selectedBlock The tag of your stone
     * @return an Integer, to change the placement in Y-direction
     */
    public static int manipulateY(int selectedBlock) {
        switch (selectedBlock) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 12:
                return 0;
            case 4:
            case 10:
            case 11:
            case 13:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
                return 1;
            case 14:
                return 2;
            default:
                throw new ExceptionInInitializerError("manipulateY failed");
        }
    }

    /**
     * makes the phone go vrrr vrrr ;)
     * @param duration
     */
    private void vibrate(int duration) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(duration);
    }

    /**
     * Removes the used stone from the View
     */
    private void removeFromBlockDrawer() {
        int count = 0;
        if (!removedBlockDrawerChildren.isEmpty()) {
            for (ImageView t : removedBlockDrawerChildren) {
                if ((int) t.getTag() < selectedBlockID) {
                    count++;
                }
            }
        }

        int rmIndex = Math.max(0, (selectedBlockID) - count);
        ImageView rm = (ImageView) blockDrawer.getChildAt(rmIndex);
        blockDrawer.removeView(rm);
        blockDrawerChildren.remove(rm);
        removedBlockDrawerChildren.add(rm);
        selectedBlockID = -1;
    }

    /**
     * Stone placement, removal from BlockDrawer, Update and calculating new Score and looking for anymore turns
     *
     * @param b - byte Array of your stone
     * @param i - the col where you want to restore it
     * @param j - the row where you want to restore it
     */
    public void placeIt(byte[][] b, int i, int j) {
        gl.placeStone(b, i, j);

        if (player.getScore() == (player.MAX_STONES - 1)) {
            player.addToScore(20); //GameRule: if last stone placed == single stone
            player.calculateScore(b);
        } else {
            player.calculateScore(b);
        }
        if (player.getScore() >= player.MAX_STONES) {
            player.addToScore(15);
        }

        player.removeFromArray(selectedBlockID - 1);
        player.putToSaveIndices(b, i, j);

        removeFromBlockDrawer();
        updatePartOfGameBoard(i, j, (i + b.length), (j + b.length));

        //if there is another move to make doSomething; right now just for testing, can be used when needed
        /*if (areTurnsLeft()) {
            Toast.makeText(getApplicationContext(), "There are turns left, you go", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "You lost buddy", Toast.LENGTH_SHORT).show();
        }*/
        player.printSaveIndices();

        ByteArrayHelper ba = new ByteArrayHelper();
        byte[] byteArr = ba.createNewByteArray(b, i, j, playerID);
        if (doSettings) {
//            sendMessage(byteArr);
            //Log.d("--DEBUG--","isit?");
            isItMyTurn(true, byteArr);
        }

        //updateGameBoard();
    }

    /**
     * places the stone of an other player
     * @param b
     * @param i
     * @param j
     */
    private void placeStoneOfOtherPlayer(byte[][] b, int i, int j) {
        gl.placeStone(b, i, j);
        updatePartOfGameBoard(i, j, (i + b.length), (j + b.length));
        updatePoints();
    }

    /**
     * gets the screen width
     * @return the screen width
     */
    public int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * gets the screen height
     * @return the screen height
     */
    private int getScreenHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
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


    /**
     * Not excessively tested, but worked where I wanted it to work :)
     *
     * @return true, as soon as it sees another possible turn
     * false, else
     */
    public boolean areTurnsLeft() {

        int selectionRemember = selectedBlockID; // To restore, if there is another possible move
        int transposeRemember = transposeCount; // ---------||---------
        byte[] remainingStones = player.getRemainingStones(); // What stones do you still have (Saved as tags)
        byte[][] gameboard = gl.getGameBoard();
        boolean breakable = false;
        boolean oneMoreTurn = false;
        boolean fullTest = false;

        ArrayList<IndexTuple> savedTuples = player.getSaveIndices(); // Tuples with the Indices of your placed stones
        if (player.getScore() < player.MAX_STONES) { // Probably useless, because you should not be able to lay more than MAX_STONES
            for (IndexTuple tuple : savedTuples) { // Look at every IndexTuple (where your stones lay)
//                Log.d("Tuple Info Vorm Testen", tuple.toString());
                if (tuple.getHasTurns()) {
                    for (byte stone : remainingStones) { // Look at every stone you still have
                        breakable = false;
                        if (stone != -1) { // Already placed stone
                            int i = tuple.getIndex_j(); // Index_i, bit confusing with row and col..
                            int j = tuple.getIndex_i(); // Index_j
                            selectedBlockID = stone + 1; // isYourPlacementValid needs selectedBlockID
                            byte[][] actualStone = player.getStone(selectedBlockID - 1);
                            int help = actualStone.length;
                            //Log.d("DebugInfo", "i = " + i + ", j = " + j + ", selected = " + selectedBlockID + ", transpose = " + transposeCount);
                            int lu = 0, ll = 0, ru = 0, rl = 0;
                            for (int h1 = help; h1 > 0; h1--) {
                                for (int h2 = help; h2 > 0; h2--) {

                                    if ((i - h1) >= 0 - h1 && (j - h2) >= 0 - h2 && lu == 0) { //Left upper corner
                                        if (i - 1 >= 0 && j - 1 >= 0) {
                                            if (gameboard[i - 1][j - 1] == 0) { // If that corner is not free, you can stop...
                                                for (int tr = 0; tr < 4; tr++) { // Test all four transpositions
                                                    transposeCount = tr;
                                                    if (cornerTesting(j - h2, i - h1, selectionRemember, transposeRemember)) {
                                                        if (fullTest) {
                                                            breakable = true;
                                                            oneMoreTurn = true;
                                                            break;
                                                        } else {
                                                            return true;
                                                        }
                                                    }
                                                    if (breakable) break;
                                                }
                                                if (breakable) break;
                                            } else {
                                                lu++; // ...and set 1/4 int (needed later)
                                            }
                                        }
                                        if (breakable) break;
                                    }
                                    if (breakable) break;

                                    if ((i - h1) >= 0 - h1 && (j + h2) < SIZE && ll == 0) { //Right upper corner
                                        if (i - 1 >= 0) {
                                            if (gameboard[i - 1][j + 1] == 0) {
                                                for (int tr = 0; tr < 4; tr++) {
                                                    transposeCount = tr;
                                                    if (cornerTesting(j + h2, i - h1, selectionRemember, transposeRemember)) {
                                                        if (fullTest) {
                                                            breakable = true;
                                                            oneMoreTurn = true;
                                                            break;
                                                        } else {
                                                            return true;
                                                        }
                                                    }
                                                    if (breakable) break;
                                                }
                                                if (breakable) break;
                                            } else {
                                                ll++;
                                            }
                                        }
                                        if (breakable) break;
                                    }
                                    if (breakable) break;

                                    if ((i + h1) < SIZE && (j - h2) >= 0 - h2 && ru == 0) { //Left lower corner
                                        if (j - 1 >= 0) {
                                            if (gameboard[i + 1][j - 1] == 0) {
                                                for (int tr = 0; tr < 4; tr++) {
                                                    transposeCount = tr;
                                                    if (cornerTesting(j - h2, i + h1, selectionRemember, transposeRemember)) {
                                                        if (fullTest) {
                                                            breakable = true;
                                                            oneMoreTurn = true;
                                                            break;
                                                        } else {
                                                            return true;
                                                        }
                                                    }
                                                    if (breakable) break;
                                                }
                                                if (breakable) break;
                                            } else {
                                                ru++;
                                            }
                                        }
                                        if (breakable) break;
                                    }
                                    if (breakable) break;

                                    if ((i + h1) < SIZE && (j + h2) < SIZE && rl == 0) { //Right lower corner
                                        if (gameboard[i + 1][j + 1] == 0) {
                                            for (int tr = 0; tr < 4; tr++) {
                                                transposeCount = tr;
                                                if (cornerTesting(j + h2, i + h1, selectionRemember, transposeRemember)) {
                                                    if (fullTest) {
                                                        breakable = true;
                                                        oneMoreTurn = true;
                                                        break;
                                                    } else {
                                                        return true;
                                                    }
                                                }
                                                if (breakable) break;
                                            }
                                            if (breakable) break;
                                        } else {
                                            rl++;
                                        }
                                        if (breakable) break;
                                    }
                                    if (breakable) break;

                                    // If there is no free corner for this IndexTuple, it has no turns
                                    // May be boosting performance if breaking far enough
                                    if (lu != 0 && ll != 0 && ru != 0 && rl != 0) {
                                        tuple.setHasTurns(false);
                                        break;
                                    }
                                }
                                if (breakable) {
                                    break;
                                }
                            }
                        }
                    }
                    if (!breakable) {
                        tuple.setHasTurns(false);
                    }
//                    Log.d("Nach allen Steinen", tuple.toString());
                }
            }
        }
        return oneMoreTurn;
    }

    /**
     * Watch one corner around your stone, to see if there is at least one more turn
     *
     * @param i                 - the col to check the placement
     * @param j                 - the row to check the placement
     * @param selectionRemember - if one placement is valid, reset selectedBlockID
     * @param transposeRemember - if one placement is valid, reset transposeCount
     * @return true, if there is one more turn
     * false, else
     */
    public boolean cornerTesting(int i, int j, int selectionRemember, int transposeRemember) {
        if (drawStone(i, j)) { // Is needed, to make isYourPlacementValid work
            if (isYourPlacementValid(i, j)) {
                selectedBlockID = selectionRemember;
                transposeCount = transposeRemember;
                restore(i, j);
                return true;
            } else {
                restore(i, j);
            }
            restore(i, j);
        }
        return false;
    }


    public void setFinished(String id){
        if(!id.equals(playerID)){
            countFinished++;
            goOn();
        }
    }

    /**
     * converts an array to a string
     * @param arr
     * @return the converted string
     */
    public String arrToString(byte[] arr) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            s += arr[i] + ", ";
        }

        return s;
    }

    /**
     * this sends a string message
     * @param message
     */
    private void sendMessage( String message ) {
        Log.d("--DEBUG--","sendMessage");
        if(!remotePeerEndpoints.isEmpty()) {
            if (isHost) {
                Nearby.Connections.sendReliableMessage(apiClient, remotePeerEndpoints, (message).getBytes());
            }
        } else {
            if (remoteHostEndpoint != null) {
                Nearby.Connections.sendReliableMessage(apiClient, remoteHostEndpoint, (message).getBytes());
            }
        }
    }

    /**
     * this sends a byte-array message
     * @param mess
     */
    private void sendMessage(byte[] mess) {
        Log.d("--DEBUG--","sendMessage" + isHost + "..." + remotePeerEndpoints.toString() + "..." + remoteHostEndpoint);
        if (!remotePeerEndpoints.isEmpty()) {
            if (isHost) {
                Log.d("--DEBUG--",arrToString(mess));
                Nearby.Connections.sendReliableMessage(apiClient, remotePeerEndpoints, mess);
            }
        } else {
            if (remoteHostEndpoint != null) {
                Log.d("--DEBUG--",arrToString(mess));
                Nearby.Connections.sendReliableMessage(apiClient, remoteHostEndpoint, mess);
            }
        }
    }

    private void myTurnAdd(){
        if (idNameMap.size() == 2) {
            actTurn++;
            if (actTurn == 3) actTurn = 1;
        } else if (idNameMap.size() == 3) {
            actTurn++;
            if (actTurn == 4) actTurn = 1;
        } else {
            actTurn++;
            if (actTurn == 5) actTurn = 1;
        }
    }

    /**
     * is it my turn? i don't know
     * @param sending
     * @param payload
     */
    public void isItMyTurn(boolean sending, byte[] payload) {
            if (sending) {
                sendMessage(payload);
            }

        int help = player.getScore();

        myTurnAdd();

        if (actTurn == myturn) {
            if (help > 0) {
                //player.setHasTurns(areTurnsLeft(false));
            }
            updatePoints();
            if (player.getHasTurns()) { // Wenn ich noch Spielzüge habe, kann ich weiterspielen..
                enableScreenInteraction();
                Toast.makeText(getApplicationContext(), "There are turns left, you go", Toast.LENGTH_SHORT).show();
            } else { // TODO ... wenn nicht, was muss dann aufgerufen werden, dass der nächste Spieler dran kommt!? So geht's nicht ;)
                Toast.makeText(getApplicationContext(), "You lost buddy", Toast.LENGTH_SHORT).show();
                disableScreenInteraction();
                notifyFinished();

                if (sending) {
                    isItMyTurn(true, payload);
                }
            }
        } else {
            disableScreenInteraction();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (player.getScore() > 0) {
                        //player.setHasTurns(areTurnsLeft(true));
                    }
                }
            }, 200);

        }
        
        /*if (idNameMap.size() == 2) {
            Log.d("--DEBUG--","small");
            actTurn++;
            if (actTurn == 3) {
                actTurn = 1;
            }
        } else {
            Log.d("--DEBUG--","big");
            actTurn++;
            if (actTurn == 5) {
                actTurn = 1;
            }
        }
        Log.d("--DEBUG--","actturn" + actTurn + ", " + myturn);
        if (actTurn == myturn) {
            enableScreenInteraction();
        } else {
            disableScreenInteraction();
        }*/

    }

    /**
     * this disables the screen interaction
     */
    public void disableScreenInteraction() {
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/

        imgView.setVisibility(View.VISIBLE);
        imgView.setImageResource(R.drawable.wait);
        imgView.setAlpha(0.4f);
        for (ImageView view : blockDrawerChildren) {
            view.setEnabled(false);
        }
    }

    public void doubleTap(){
        if (doubleTappedToClaim) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("DO YOU WANT TO CLAIM YOUR WIN?");
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendMessage("CLAIM-"+Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0]);
                    winner = idNameMap.get(Nearby.Connections.getLocalDeviceId(apiClient).split(":")[0]);
                }
            });
            builder.show();
        }

        this.doubleTappedToClaim = true;
        Toast.makeText(this, "Double-Tap for claiming the win!", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleTappedToClaim = false;
            }
        }, 300);


    }

    /**
     * this enables the screen interaction
     */
    public void enableScreenInteraction() {
        for (ImageView view : blockDrawerChildren) {
            view.setEnabled(true);
        }
        Log.d("--DEBUG--","should disable and display pic");
        imgView.setVisibility(View.GONE);
    }

    private void notifyFinished() {
        countFinished++;
        goOn();
        sendMessage("FINISHED-"+playerID);
    }

    private void goOn() {
            final Intent intent = new Intent("at.aau.se2.test.ENDSCREEN");
            intent.putExtra("isHost", isHost);
            intent.putExtra("hostEnd", remoteHostEndpoint);
            intent.putExtra("winner", winner);
            startActivity(intent);
            gl.resetInstance();
    }

    /**
     * this hides the statusbar
     */
    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("--DEBUG--","onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("--DEBUG--","connSuspended");
    }

    @Override
    public void onConnectionRequest(String s, String s1, String s2, byte[] bytes) {
        Log.d("--DEBUG--","request");
    }

    @Override
    public void onEndpointFound(String s, String s1, String s2, String s3) {
        Log.d("--DEBUG--","endpoind found");
    }

    @Override
    public void onEndpointLost(String s) {
        Log.d("--DEBUG--","endpoint lost");
    }

    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {

        Log.d("--DEBUG--","action received");

        ByteArrayHelper b = new ByteArrayHelper();
        b.fetchInformationFromByteArray(payload);

        int color = b.getColor();
        int idx = b.getIdx();
        int idy = b.getIdy();
        byte[][] stone = b.getByteStone();


        if (color != playerID) {
            Log.d("--DEBUG--","" + Integer.toString(color) + "_" + Integer.toString(playerID));
            placeStoneOfOtherPlayer(stone, idy, idx);
            isItMyTurn(false, null);

        }

        if (isHost) {
            sendMessage(payload);
            Log.d("--DEBUG--","send new action");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePoints();
        hideStatusBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideStatusBar();
    }

    private void dev(String debMessage) {
        Log.d("asdfconn", debMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}
