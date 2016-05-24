package at.aau.se2.test;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FullscreenActivity extends Activity {

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
    private View testView;
    private byte[][] rememberField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        //Initialisierung div. Variablen.
        fullscreenLayout = (RelativeLayout) findViewById(R.id.contentPanel);

        byte playerID = -1;
        selectedBlockID = -1;
        rememberField = new byte[3][3];

        String color;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
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
            }
        }

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
            ImageView accept;
            ImageView cancel;
            ImageView draggedImage;

            @Override
            public boolean onDrag(View v, DragEvent event) {

                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        //Drop nur auf das Spielfeld möglich
                        if (v instanceof GridLayout) {
                            Toast.makeText(getApplicationContext(),"DROP",Toast.LENGTH_SHORT);
                            draggedImage = (ImageView) event.getLocalState();

                            // Indexberechnung, wo der Stein platziert werden soll // v.getWidth/getHeight liefert bei jedem Stein 480 zurück
                            // Indexmanipulation, abhängig vom gewählten Stein (TODO Bei Drehung ziemlich sicher anzupassen!!)
                            index_i = (byte) (Math.floor(event.getX() / Math.floor(v.getWidth() / 20)) - manipulateX(selectedBlockID - 1));
                            index_j = (byte) (Math.floor(event.getY() / Math.floor(v.getHeight() / 20)) - manipulateY(selectedBlockID - 1));

                            //außerhalb des gültigen bereichs platziert
                            if(index_i < 0 || index_i > 19){
                                if(index_i < 0){
                                    index_i = 0;
                                }else{
                                    index_i = 19;
                                }
                            }

                            if(index_j < 0 || index_j > 19){
                                if(index_j < 0){
                                    index_j = 0;
                                }else{
                                    index_j = 19;
                                }
                            }
                            /*if (event.getX() > v.getWidth() || event.getY() > v.getHeight()
                                    || event.getX() < 0 || event.getY() < 0) {
                                if (event.getX() > v.getWidth()) {
                                    index_i = 19;
                                }
                                if (event.getY() > v.getHeight()) {
                                    index_j = 19;
                                }
                                if (event.getX() < 0) {
                                    index_i = 0;
                                }
                                if (event.getY() < 0) {
                                    index_j = 0;
                                }
                            }*/

                            //Preview erfolgreich gezeichnet?
                            final boolean drawn = drawStone(index_i, index_j);

                            // Accept-Button
                            if (drawn) {
                                RelativeLayout.LayoutParams params_accept = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                accept = new ImageView(getApplicationContext());
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
//                                            testView.setVisibility(View.INVISIBLE); //Müsste unnötig sein
                                            placeIt(player.getStone(selectedBlockID - 1), index_i, index_j); //Wirkliches Plazieren vom Stein
//                                            boardToLog();
                                        }
                                        fullscreenLayout.removeView(accept);
                                        fullscreenLayout.removeView(cancel);
//                                        boardToLog();
                                    }
                                });


                                // Cancel-Button
                                RelativeLayout.LayoutParams params_cancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                cancel = new ImageView(getApplicationContext());
                                cancel.setImageResource(R.drawable.cancel);
                                params_cancel.setMargins(Math.round(v.getWidth() / 2), v.getHeight() + cancel.getHeight(), 0, 0);
                                cancel.setLayoutParams(params_cancel);

                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fullscreenLayout.removeView(accept);
                                        fullscreenLayout.removeView(cancel);
                                        restore(index_i, index_j);
//                                        boardToLog();
                                    }
                                });

                                // Buttons zum View hinzufügen
                                if (isYourPlacementValid(index_i, index_j)) { //Ungültiger Zug, braucht nur den Cancel Button
                                    fullscreenLayout.addView(accept);
                                }
                                fullscreenLayout.addView(cancel);
                            } else {
                                testView.setVisibility(View.VISIBLE);
                            }
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
        for (int i = 0; i < 21; i++) {
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
                    vibrate(100);
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDrag(data, shadowBuilder, v, 0);


                    for (ImageView bdc : blockDrawer_children) {
                        if (bdc.equals(v)) {
                            selectedBlockID = (int) bdc.getTag(); //Gewählter Spielstein
                        }
                    }
                    testView = v;
                    testView.setVisibility(View.INVISIBLE);
                    return true;
                }
            });
        }

    }

    /**
     * Checks, if the preview would overwrite some stone
     *
     * @param i - the col where you want to place it
     * @param j - the row where you want to place it
     * @return false, if you would hit some stone
     * true, else
     */
    private boolean preValidation(int i, int j) {
        byte[][] b = player.getStone(selectedBlockID - 1);
        for (int x = 0; x < rememberField.length; x++) {
            for (int y = 0; y < rememberField[x].length; y++) {
                if (b[x][y] != 0 && rememberField[x][y] != 0) return false;
            }
        }
        return true;
    }


    private void updateGameBoard() {
        byte[][] board = gl.getGameBoard();

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
                    case 6:
                        oImageView.setImageResource(R.drawable.green_s_1);
                        break;
                    case 2:
                    case 7:
                        oImageView.setImageResource(R.drawable.red_s_1);
                        break;
                    case 3:
                    case 8:
                        oImageView.setImageResource(R.drawable.blue_s_1);
                        break;
                    case 4:
                    case 9:
                        oImageView.setImageResource(R.drawable.yellow_s_1);
                        break;
                }
                oImageView.setLayoutParams(param);

                gameBoardLayout.addView(oImageView);
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

            if (preValidation(x, y)) {
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
     * Tries to place a preview of your stone placement after letting go of the Drag
     *
     * @param x - the col where you want to place it
     * @param y - the row where you want to place it
     * @return false, if stone goes over edge (Should we handle this case better?)
     * true, if stone was drawn
     */
    private boolean drawStone(int x, int y) {
        if (selectedBlockID >= 0) {
            byte[][] b;
            b = player.getStone(selectedBlockID - 1);
            b = changeToPreview(b, true);

            if (gl.placeOverEdge(b, x, y)) {
                Toast.makeText(getApplicationContext(), "I'm sorry, but I can't draw this", Toast.LENGTH_SHORT).show();
                changeToPreview(b, false);
                return false;
            } else {
                rememberField = gl.rememberField(b, x, y);
                gl.placeStone(b, x, y);
                changeToPreview(b, false);
            }
        }
        updateGameBoard();
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
        testView.setVisibility(View.VISIBLE);
        gl.restoreField(rememberField, i, j);
        updateGameBoard();
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
            case 4:
            case 14:
            case 15:
            case 17:
                return 1;
            case 3:
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
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 12:
                return 0;
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
     * Stone placement, removal from BlockDrawer and Update
     * @param b - byte Array of your stone
     * @param i - the col where you want to restore it
     * @param j - the row where you want to restore it
     */
    private void placeIt(byte[][] b, int i, int j) {
        gl.placeStone(b, i, j);
        removeFromBlockDrawer();
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

}