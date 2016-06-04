package at.aau.se2.test;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        View.OnClickListener{

    private RelativeLayout fullscreenLayout;
    private GridLayout gameBoardLayout;
    private LinearLayout blockDrawer;
    private GameLogic gl;
    private static final int SIZE = 20;
    private int selectedBlockID;
    private List<ImageView> blockDrawer_children;
    private List<ImageView> removed_blockDrawer_children;
    private Player player;
    private boolean doubleBackToExitPressedOnce = false;
    private ImageView testView;
    private byte[][] rememberField;
    private boolean elementFinished;
    private View.DragShadowBuilder shadowBuilder;
    private int transposeCount; //Zähler wie oft der Stein gedreht wurde
    private Connection connection;
    private GoogleApiClient apiClient;
    private boolean isHost;
    private boolean isConnected = true;
    private String remoteHostEndpoint;
    private List<String> remotePeerEndpoints = new ArrayList<>();
    private HashMap<String, String> ID_Name_Map = new HashMap<String, String>();
    private byte playerID;

    /*
    TODO:
        - blockrotation
        - dragged image above finger
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        //Initialisierung div. Variablen.
        fullscreenLayout = (RelativeLayout) findViewById(R.id.contentPanel);

        byte playerID = -1;
        selectedBlockID = -1;
        rememberField = new byte[5][5];
        elementFinished = true;

        String color;
        Bundle extras = getIntent().getExtras();
        /*if (extras != null) {
            color = extras.getString("chosen_color");
            if (color != null) {
                switch (color) {
                    case "green":
                        playerID = 1;
                        break;
                    case "red":
                        playerID = 2;
                        break;
                    case "blue":
                        playerID = 3;
                        break;
                    case "yellow":
                        playerID = 4;
                        break;
                    default:
                        break;
                }
            }*/
        ID_Name_Map = ((HashMap<String, String>)extras.getSerializable("map"));
        isHost = extras.getBoolean("host");
        remoteHostEndpoint = extras.getString("hostEnd");
        remotePeerEndpoints = Connection.getInstance().getRemotePeerEndpoints();
        //get the api from the Singleton
        apiClient = Connection.getInstance().getApiClient();
        playerID = extras.getByte("color");
        Connection.getInstance().setFullscreenActivity(this);

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
        gameBoardLayout.setOnDragListener(new View.OnDragListener() {

            byte index_i = -1;
            byte index_j = -1;
            ImageButton accept;
            ImageButton cancel;
            ImageButton transpose;
            ImageButton move_up;
            ImageButton move_right;
            ImageButton move_down;
            ImageButton move_left;
            ImageView draggedImage;
            boolean dragged = false;
            boolean drawn;

            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED: //LongClick startet den Drag, Bild unsichtbar machen
                        testView.setVisibility(View.INVISIBLE);
                        dragged = false;
                        Log.d("DragStart", "Started");
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED: //Im Spielfeld
                        dragged = true;
                        Log.d("DragEntered", "Entered");
                        break;
                    case DragEvent.ACTION_DRAG_EXITED: //Außerhalb vom Spielfeld
                        dragged = false;
                        Log.d("DragExited", "Exited");
                        break;
                    case DragEvent.ACTION_DRAG_ENDED: //Wird geworfen, egal wo der Drag beendet wird
                        if (dragged) {
                            testView.setVisibility(View.INVISIBLE);
                        } else {
                            testView.setVisibility(View.VISIBLE);
                            elementFinished = true;
                        }
                        Log.d("DragEnded", "Ended");
                        break;
                    case DragEvent.ACTION_DROP: //Drop wird nur geworfen, wenn man im Spielfeld dropped

                        transposeCount = 0; //Neuer Stein, Zähler zurücksetzen
                        draggedImage = (ImageView) event.getLocalState();

                        // Indexberechnung, wo der Stein platziert werden soll
                        // Indexmanipulation, abhängig vom gewählten Stein
                        index_i = (byte) (Math.floor(event.getX() / Math.floor(v.getWidth() / 20)) - manipulateX(selectedBlockID - 1));
                        index_j = (byte) (Math.floor(event.getY() / Math.floor(v.getHeight() / 20)) - manipulateY(selectedBlockID - 1));

                        //außerhalb des gültigen bereichs platziert
                        if (index_i < 0 || index_i > 19) {
                            if (index_i < 0) {
                                index_i = 0;
                            } else {
                                index_i = 19;
                            }
                        }

                        if (index_j < 0 || index_j > 19) {
                            if (index_j < 0) {
                                index_j = 0;
                            } else {
                                index_j = 19;
                            }
                        }

                        //Preview erfolgreich gezeichnet?
                        drawn = drawStone(index_i, index_j);

                        //Bei left und up Probleme, das Stein nur richtig gedreht nach oben kann (Nullzeilen und Nullspalten)

                        // Movement Buttons müssen try-catch, da nicht ersichtlich ist,
                        // ob bei neuem moven, der Accept Button noch da ist
                        // Wenn keine Preview gezeichnet wurde, muss der alte Zustand wiederhergestellt werden
                        move_up = new ImageButton(getApplicationContext());
                        move_up.setImageResource(R.drawable.move_up);

                        move_up.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                restore(index_i, index_j);
                                if (drawStone(index_i, (index_j - 1 < 0) ? 0 : --index_j)) {
                                    try {
                                        if (isYourPlacementValid(index_i, index_j)) {
                                            fullscreenLayout.addView(accept);
                                        } else {
                                            fullscreenLayout.removeView(accept);
                                        }
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    restore(index_i, index_j);
                                    cancel.performClick();
                                }
                            }
                        });

                        move_down = new ImageButton(getApplicationContext());
                        move_down.setImageResource(R.drawable.move_down);

                        move_down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                restore(index_i, index_j);
                                if (drawStone(index_i, (index_j + 1 > 20) ? 20 : ++index_j)) {
                                    try {
                                        if (isYourPlacementValid(index_i, index_j)) {
                                            fullscreenLayout.addView(accept);
                                        } else {
                                            fullscreenLayout.removeView(accept);
                                        }
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    restore(index_i, --index_j);
                                    drawStone(index_i, index_j);
                                }
                            }
                        });

                        move_left = new ImageButton(getApplicationContext());
                        move_left.setImageResource(R.drawable.move_left);

                        move_left.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                restore(index_i, index_j);
                                if (drawStone((index_i - 1 < 0) ? 0 : --index_i, index_j)) {
                                    try {
                                        if (isYourPlacementValid(index_i, index_j)) {
                                            fullscreenLayout.addView(accept);
                                        } else {
                                            fullscreenLayout.removeView(accept);
                                        }
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    restore(--index_i, index_j);
                                    drawStone(index_i, index_j);
//                                    cancel.performClick();
                                }
                            }
                        });

                        move_right = new ImageButton(getApplicationContext());
                        move_right.setImageResource(R.drawable.move_right);

                        move_right.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                restore(index_i, index_j);
                                if (drawStone((index_i + 1 > 20) ? 20 : ++index_i, index_j)) {
                                    try {
                                        if (isYourPlacementValid(index_i, index_j)) {
                                            fullscreenLayout.addView(accept);
                                        } else {
                                            fullscreenLayout.removeView(accept);
                                        }
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    restore(--index_i, index_j);
                                    drawStone(index_i, index_j);
                                }
                            }
                        });


                        RelativeLayout.LayoutParams params_move_up = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        RelativeLayout.LayoutParams params_move_right = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        RelativeLayout.LayoutParams params_move_down = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        RelativeLayout.LayoutParams params_move_left = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                        //TODO: Berechnungen falsch, momentan einfach wird einfach irgendetwas gerechnet ;)
                        params_move_up.setMargins((int) Math.floor(gameBoardLayout.getHeight() / 2), 0, 0, 0);
                        params_move_right.setMargins((int) Math.floor(gameBoardLayout.getHeight() / 4) * 3, (int) Math.floor(gameBoardLayout.getHeight() / 2), 0, 0);
                        params_move_down.setMargins((int) Math.floor(gameBoardLayout.getHeight() / 2), (int) Math.floor(gameBoardLayout.getHeight() / 4) * 3, 0, 0);
                        params_move_left.setMargins(0, (int) Math.floor(gameBoardLayout.getHeight() / 2), 0, 0);

                        move_up.setLayoutParams(params_move_up);
                        move_right.setLayoutParams(params_move_right);
                        move_down.setLayoutParams(params_move_down);
                        move_left.setLayoutParams(params_move_left);

                        // Accept-Button
                        if (drawn) {
                            RelativeLayout.LayoutParams params_accept = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            accept = new ImageButton(getApplicationContext());
                            accept.setImageResource(R.drawable.checkmark);
                            params_accept.setMargins(0, v.getHeight() + accept.getHeight(), 0, 0);
                            accept.setLayoutParams(params_accept);

                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Platzieren nicht möglich - Preview wieder löschen und Bild im BlockDrawer wieder anzeigen
                                    if (!isYourPlacementValid(index_i, index_j)) {
                                        vibrate(500);
                                        restore(index_i, index_j);
                                    } else {
                                        testView.setVisibility(View.INVISIBLE); //Müsste unnötig sein
                                        byte[][] b = player.getStone(selectedBlockID - 1);
                                        for (int a = 0; a < transposeCount; a++) { //Stein drehen, je nachdem wie oft der Button gedrückt wurde
                                            b = gl.rotate(b);
                                        }
                                        placeIt(b, index_i, index_j); //Wirkliches Plazieren vom Stein
                                    }
                                    gl.removeViews(fullscreenLayout, accept, cancel, transpose, move_up, move_right, move_down, move_left);
                                    elementFinished = true; //Nächster Stein kann geLongClicked werden
                                }
                            });


                            // Cancel-Button
                            RelativeLayout.LayoutParams params_cancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            cancel = new ImageButton(getApplicationContext());
                            cancel.setImageResource(R.drawable.cancel);
                            params_cancel.setMargins(Math.round(v.getWidth() / 3), v.getHeight() + cancel.getHeight(), 0, 0);
                            cancel.setLayoutParams(params_cancel);

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    gl.removeViews(fullscreenLayout, accept, cancel, transpose, move_up, move_right, move_down, move_left);
                                    testView.setVisibility(View.VISIBLE);
                                    elementFinished = true;
                                    restore(index_i, index_j);
//                                        boardToLog();
                                }
                            });

                            RelativeLayout.LayoutParams params_transpose = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            transpose = new ImageButton(getApplicationContext());
                            transpose.setImageResource(R.drawable.transpose);
                            params_transpose.setMargins(Math.round((2 * v.getWidth()) / 3), v.getHeight() + transpose.getHeight(), 0, 0);
                            transpose.setLayoutParams(params_transpose);

                            //Board wiederherstellen, Stein drehen und neue Preview zeichnen, Accept Button nur bei gültigem Zug anzeigen
                            //Try-Catch, da ich Accept nicht zweimal adden darf
                            transpose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    restore(index_i, index_j);
                                    transposeCount = (transposeCount + 1) % 4;
                                    drawn = drawStone(index_i, index_j);
                                    if (drawn) {
                                        if (!isYourPlacementValid(index_i, index_j)) {
                                            fullscreenLayout.removeView(accept);
                                        } else {
                                            try {
                                                fullscreenLayout.addView(accept);
                                            } catch (IllegalStateException e) {

                                            }
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "I'm sorry, but I can't draw this", Toast.LENGTH_SHORT).show();
                                        cancel.performClick();
                                    }
                                }
                            });


                            // Buttons zum View hinzufügen
                            if (isYourPlacementValid(index_i, index_j)) { //Ungültiger Zug, braucht Accept Button nicht
                                fullscreenLayout.addView(accept);
                            }
                            gl.addViews(fullscreenLayout, cancel, transpose, move_up, move_right, move_down, move_left);
                        } else {
                            //Preview wurde nicht gezeichnet
                            elementFinished = true;
                            dragged = false;
                            testView.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initializeBlockDrawer() {
        LinearLayout blockDrawer_parent = (LinearLayout) findViewById(R.id.blockDrawer_parent);
        ViewGroup.LayoutParams params = blockDrawer_parent.getLayoutParams();

        params.height = getScreenHeight() - (getScreenWidth());
        blockDrawer_parent.setLayoutParams(params);

        blockDrawer = (LinearLayout) findViewById(R.id.blockDrawer);

        blockDrawer_children = new ArrayList<>();
        removed_blockDrawer_children = new ArrayList<>();

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
            blockDrawer_children.add(oImageView);

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

                        for (ImageView bdc : blockDrawer_children) {
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
     * Updates the GameBoard
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
    private boolean isYourPlacementValid(int x, int y) {
        if (selectedBlockID >= 0) {
            byte[][] b = player.getStone(selectedBlockID - 1);
            for (int a = 0; a < transposeCount; a++) {
                b = gl.rotate(b);
            }
            if (preValidation()) {
                if (!removed_blockDrawer_children.isEmpty()) {
                    if (gl.checkTheRules(b, x, y)) {

                    } else {
                        return false;
                    }
                } else {
                    if (gl.hitTheCorner(b, x, y)) {

                    } else {
                        vibrate(500);
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
    private boolean drawStone(int x, int y) {
        if (selectedBlockID >= 0) {
            byte[][] b = player.getStone(selectedBlockID - 1);
            for (int a = 0; a < transposeCount; a++) {
                b = gl.rotate(b);
            }
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
    private void restore(int i, int j) {
        gl.restoreField(rememberField, i, j);
        updatePartOfGameBoard(i, j, (i + 5 > 20) ? 20 : i + 5, (j + 5 > 20) ? 20 : j + 5);
        //updateGameBoard();
    }

    /**
     * Manipulates the XPlacement, so that you can place it more natural
     * Don't change the BlockOrder, or this won't work properly!!
     *
     * @param selectedBlock The tag of your stone
     * @return an Integer, to change the placement in X-direction
     */
    private int manipulateX(int selectedBlock) {
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
                return 1;
        }
        return 0;
    }

    /**
     * Manipulates the YPlacement, so that you can place it more natural
     * Don't change the BlockOrder, or this won't work properly!!
     *
     * @param selectedBlock The tag of your stone
     * @return an Integer, to change the placement in Y-direction
     */
    private int manipulateY(int selectedBlock) {
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
                return 1;
            case 14:
                return 2;

        }
        return 0;
    }


    private void vibrate(int duration) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(duration);
    }

    /**
     * Removes the used stone from the View
     */
    private void removeFromBlockDrawer() {
        int count = 0;
        if (!removed_blockDrawer_children.isEmpty()) {
            for (ImageView t : removed_blockDrawer_children) {
                if ((int) t.getTag() < selectedBlockID) {
                    count++;
                }
            }
        }

        int rm_index = Math.max(0, (selectedBlockID) - count);
        ImageView rm = (ImageView) blockDrawer.getChildAt(rm_index);
        //Toast.makeText(getApplicationContext(), (selectedBlockID) + " / " + rm_index + " / " + (22 - blockDrawer.getChildCount()), Toast.LENGTH_SHORT).show();
        blockDrawer.removeView(rm);
        blockDrawer_children.remove(rm);
        removed_blockDrawer_children.add(rm);
        selectedBlockID = -1;
    }


    /**
     * Stone placement, removal from BlockDrawer, Update and calculating new Score and looking for anymore turns
     *
     * @param b - byte Array of your stone
     * @param i - the col where you want to restore it
     * @param j - the row where you want to restore it
     */
    private void placeIt(byte[][] b, int i, int j) {
        gl.placeStone(b, i, j);

        if (player.getScore() == (player.MAX_STONES - 1)) {
            player.addToScore(15); //GameRule: if last stone placed == single stone
            player.calculateScore(b);
        } else {
            player.calculateScore(b);
        }
        player.removeFromArray(selectedBlockID - 1);
//        player.stonesToLog();
        player.putToSaveIndices(b, i, j);
//        player.printSaveIndices();

        removeFromBlockDrawer();
        updatePartOfGameBoard(i, j, (i + b.length), (j + b.length));

//        Print score, just for testing
        Toast.makeText(getApplicationContext(), "Your score is " + player.getScore(), Toast.LENGTH_SHORT).show();

//        if there is another move to make doSomething; right now just for testing, can be used when needed
        if (areTurnsLeft()) {
            Toast.makeText(getApplicationContext(), "There are turns left, you go", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "You lost buddy", Toast.LENGTH_SHORT).show();
        }

        byte[] byteArr = createNewByteArray(b, i, j);
        sendMessage(byteArr);
        //updateGameBoard();
    }

    private void placeStoneOfOtherPlayer(byte[][] b, int i, int j) {
        gl.placeStone(b, i, j);
        updateGameBoard();
    }

    //Bildschirmbreite
    private int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    //Bilschirmhöhe
    private int getScreenHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideStatusBar();
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.finish();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void boardToLog() {
        byte[][] b = gl.getGameBoard();
        String s = "";
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[i].length; j++) {
                s += b[i][j] + ", ";
            }
            s += '\n';
        }
        s += '\n';
        Log.d("Board", s);
    }


    /**
     * Not excessively tested, but worked where I wanted it to work :)
     *
     * @return true, as soon as it sees another possible turn
     * false, else
     */
    public boolean areTurnsLeft() {
        int selectionRemember = selectedBlockID; // To restore if there is another possible move
        int transposeRemember = transposeCount; // ---------||---------
        byte[] remainingStones = player.getRemainingStones(); // What stones do you still have (Saved as tags)
        ArrayList<IndexTuple> savedTuples = player.getSaveIndices(); // Tuples with the Indices of your placed stones
        if (player.getSaveIndicesSize() < player.MAX_STONES) { // Probably useless, because you should not be able to lay more than MAX_STONES
            for (IndexTuple tuple : savedTuples) { // Look at every IndexTuple (where your stones lay)
                for (byte stone : remainingStones) { // Look at every stone you still have
                    if (stone != -1) { // Already placed stone
                        int i = tuple.getIndex_j(); // Index_i
                        int j = tuple.getIndex_i(); // Index_j
                        if (((i - 1) >= 0 && (j - 1) >= 0) || ((i + 1) < SIZE && (j - 1) >= 0) ||
                                ((i - 1) >= 0 && (j + 1) < SIZE) || ((i + 1) < SIZE && (j + 1) < SIZE)) { // Look only at the corners below your field[i][j]
                            selectedBlockID = stone + 1; // Test all remaining stones
                            for (int tr = 0; tr < 4; tr++) {
                                transposeCount = tr; //Test all four rotations
                            }
                            if (((i - 1) >= 0 && (j - 1) >= 0)) { // First corner
                                if (cornerTesting(i - 1, j - 1, selectionRemember, transposeRemember)) {
                                    return true;
                                }
                            } else if (((i + 1) < SIZE && (j - 1) >= 0)) { // Another corner
                                if (cornerTesting(i + 1, j - 1, selectionRemember, transposeRemember)) {
                                    return true;
                                }
                            } else if (((i - 1) >= 0 && (j + 1) < SIZE)) { // Another corner
                                if (cornerTesting(i - 1, j + 1, selectionRemember, transposeRemember)) {
                                    return true;
                                }
                            } else if (((i + 1) < SIZE && (j + 1) < SIZE)) { // Another corner
                                if (cornerTesting(i + 1, j + 1, selectionRemember, transposeRemember)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
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
        if (drawStone(i, j)) {
            restore(i, j);
            if (isYourPlacementValid(i, j)) {
                selectedBlockID = selectionRemember;
                transposeCount = transposeRemember;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        debugging("onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        debugging("connSuspended");
    }

    @Override
    public void onConnectionRequest(String s, String s1, String s2, byte[] bytes) {
        debugging("request");
    }

    @Override
    public void onEndpointFound(String s, String s1, String s2, String s3) {
        debugging("endpoind found");
    }

    @Override
    public void onEndpointLost(String s) {
        debugging("endpoint lost");
    }


    private byte[] createNewByteArray(byte[][] b, int coordy, int coordx){
        byte[][] newByte = new byte[b.length+1][b.length+1];
        for(int i = 0; i<b.length; i++){
            for(int j = 0; j<b.length; j++) {
                newByte[i][j] = b[i][j];
            }
        }

        newByte[b.length][b.length-2] = playerID;
        newByte[b.length][b.length-1] = (byte) coordx;
        newByte[b.length][b.length] = (byte) coordy;

        return convertToOneDimension(newByte);
    }

    private byte[] convertToOneDimension(byte[][] b){
        int count = 0;
        byte[] oneDim = new byte[b.length*b.length];
        for(int i = 0; i<b.length; i++){
            for(int j = 0; j<b.length; j++) {
                oneDim[count] = b[i][j];
                count++;
            }
        }
        return oneDim;
    }


    @Override
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable){

        debugging("action received");

        int stoneDim = (int) Math.sqrt(payload.length)-1;
        int dim = 0;
        byte[][] stone = new byte[stoneDim][stoneDim];
        int counter = 0;
        for(int i=0; i<payload.length-stoneDim-1; i++) {

            if (dim < stoneDim) {
                stone[counter][dim] = payload[i];
                dim++;
            } else {
                dim = 0;
                counter++;

            }
        }
        int color = payload[payload.length-3];
        int idx = payload[payload.length-2];
        int idy = payload[payload.length-1];

        if(color != playerID){
            placeStoneOfOtherPlayer(stone, idy, idx);
        }

        if( isHost ) {
            //sendMessage(message);
            debugging("send new player");
        }
    }

    public String arrToString(byte[] arr){
        String s = "";
        for(int i=0; i<arr.length; i++){
            s += arr[i]+", ";
        }

        return s;
    }



    private void sendMessage( byte[] mess ) {
        debugging("sendMessage"+isHost+"..."+remotePeerEndpoints.toString()+"..."+remoteHostEndpoint);
        if(!remotePeerEndpoints.isEmpty()) {
            if (isHost) {
                debugging(arrToString(mess));
                Nearby.Connections.sendReliableMessage(apiClient, remotePeerEndpoints, mess);
            }
        }
        else {
            if (remoteHostEndpoint != null) {
                debugging(arrToString(mess));
                Nearby.Connections.sendReliableMessage(apiClient, remoteHostEndpoint, mess);
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

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void debugging(String debMessage) {
        Log.d("tobiasho", debMessage);
    }
}
