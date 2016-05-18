package at.aau.se2.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {

    private GridLayout gameBoardLayout;
    private LinearLayout blockDrawer;
    private GameLogic gl;
    private static final int SIZE = 20;
    private int selectedBlockID;
    private Blocks blocks;
    private List<ImageView> blockDrawer_children;
    private List<ImageView> removed_blockDrawer_children;
    private byte playerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedBlockID = -1;

        setContentView(R.layout.activity_fullscreen);

        gl = GameLogic.getInstance();

//        playerID = (byte)(1+Math.random()*3);

        String color = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            color = extras.getString("chosen_color");
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
            }
        }
        blocks = new Blocks(playerID);
        //1. Statusbar verstecken
        hideStatusBar();
        //2. Spielbrett erzeugen
        updateGameBoard();
        //3. BlockDrawer erzeugen
        initializeBlockDrawer();
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

    private void initializeBlockDrawer() {
        LinearLayout blockDrawer_parent = (LinearLayout) findViewById(R.id.blockDrawer_parent);
        ViewGroup.LayoutParams params = blockDrawer_parent.getLayoutParams();

        params.height = getScreenHeight() - (getScreenWidth());
        blockDrawer_parent.setLayoutParams(params);

        blockDrawer = (LinearLayout) findViewById(R.id.blockDrawer);

        blockDrawer_children = new ArrayList<ImageView>();
        removed_blockDrawer_children = new ArrayList<ImageView>();


        //Alle Spielsteine hinzufügen
        for (int i = 0; i < 21; i++) {
            final ImageView oImageView = new ImageView(this);

            String color = "";
            switch (playerID) {
                case 1:
                    color = "green";
                    break;
                case 2:
                    color = "red";
                    break;
                case 3:
                    color = "blue";
                    break;
                case 4:
                    color = "yellow";
                    break;
            }

            oImageView.setImageResource(getResources().getIdentifier(color + "_" + i, "drawable", getPackageName()));
            oImageView.setTag(i);
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.setGravity(Gravity.CENTER);
            oImageView.setLayoutParams(param);
            blockDrawer.addView(oImageView);
            blockDrawer_children.add(oImageView);

            //Touch-Eventhandler initialisieren
            oImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    for (ImageView bdc : blockDrawer_children) {
                        if (bdc.equals(v)) {
                            selectedBlockID = (int) bdc.getTag(); //Gewählter Spielstein
                            //Toast.makeText(getApplicationContext(), "ID: " + selectedBlockID, Toast.LENGTH_SHORT).show();
                            bdc.setBackgroundColor(Color.LTGRAY);
                        } else {
                            bdc.setBackgroundColor(Color.TRANSPARENT); //Highlight löschen
                        }
                    }
                    return false;
                }
            });
        }

    }

    //Bilschirmbreite
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

    private void updateGameBoard() {
        byte[][] board = gl.getGameBoard();

        gameBoardLayout = null;
        gameBoardLayout = (GridLayout) findViewById(R.id.gameBoard);

        //Sicherheitshalber alle vorherigen Elemente auf dem gameBoard löschen
        gameBoardLayout.removeAllViews();

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
                switch (board[i][j]) {
                    case 0:
                        oImageView.setImageResource(R.drawable.gameboard_empty);
                        oImageView.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                isYourPlacementValid(v);
                            }
                        });
                        break;
                    case 1:
                        oImageView.setImageResource(R.drawable.green_s_1);
                        break;
                    case 2:
                        oImageView.setImageResource(R.drawable.red_s_1);
                        break;
                    case 3:
                        oImageView.setImageResource(R.drawable.blue_s_1);
                        break;
                    case 4:
                        oImageView.setImageResource(R.drawable.yellow_s_1);
                        break;
                }
                oImageView.setLayoutParams(param);
                gameBoardLayout.addView(oImageView);
            }
        }
        if (removed_blockDrawer_children == null)
            Toast.makeText(getApplicationContext(), ("First goes in the corner"), Toast.LENGTH_SHORT).show();
    }


    private void isYourPlacementValid(View v) {
        if (selectedBlockID >= 0) {
            for (int i = 0; i < gameBoardLayout.getColumnCount(); i++) {
                for (int j = 0; j < gameBoardLayout.getRowCount(); j++) {
                    if (gameBoardLayout.getChildAt((i * SIZE) + j).equals(v)) {
                        byte[][] b;
                        b = blocks.getStone(selectedBlockID - 1);
                        if (!removed_blockDrawer_children.isEmpty()) {
                            if (checkTheRules(b, i, j)) {
                                placeStone(b, i, j);
                                removeFromBlockDrawer();
                            } else {
                                break;
                            }
                        } else {
                            if (hitTheCorner(b, i, j)) {
                                placeStone(b, i, j);
                                removeFromBlockDrawer();
                            }
                        }
                    }
                }
            }
            updateGameBoard();
        }

    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }


    /**
     * Checks if you place over the edges
     *
     * @param b - the byte array of your stone
     * @param i - the col where you want to place it
     * @param j - the row where you want to place it
     * @return true, if your stone reaches over the board
     * false, else
     */
    private boolean placeOverEdge(byte[][] b, int i, int j) {
        for (int col = i; col < i + b.length; col++) {
            for (int row = j; row < j + b.length; row++) {
                if (b[row - j][col - i] != 0 && (col >= SIZE || row >= SIZE)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks if you hit other stones with your placement
     *
     * @param b - the byte array of your stone
     * @param i - the col where you want to place it
     * @param j - the row where you want to place it
     * @return true, if you would hit other stones with this move
     * false, else
     */
    private boolean hitSomeStones(byte[][] b, int i, int j) {
        byte[][] board = gl.getGameBoard();
        for (int col = i; col < i + b.length; col++) {
            for (int row = j; row < j + b.length; row++) {
                if (b[row - j][col - i] != 0 && board[col][row] != 0) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Method for "First Stone to Corner"-Rule -- needs to be adapted to 1 vs. 1 because of two colors
     * Should be working for all stones now
     *
     * @param b - the byte array of your stone
     * @param i - the col where you want to place it
     * @param j - the row where you want to place it
     * @return true, if stone fits in the corner
     * false, if stone is not in the corner
     */
    private boolean hitTheCorner(byte[][] b, int i, int j) {
        if (!placeOverEdge(b, i, j) && !hitSomeStones(b, i, j)) {
            byte[][] board = gl.getGameBoard();
            for (int col = i; col < i + b.length; col++) {
                for (int row = j; row < j + b.length; row++) {
                    if (b[row - j][col - i] != 0) {
                        if ((col == 0 && row == 0) ||
                                (col == 0 && row == SIZE - 1) ||
                                (col == SIZE - 1 && row == 0) ||
                                (col == SIZE - 1 && row == SIZE - 1)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            Toast.makeText(getApplicationContext(), ("Why does this fail?"), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Checks the game rules, if your stone touches your color only by corner.
     * Also works at the edges, without Exceptions (as far as tested)
     * Different behaviour for the walls and the Corners, maybe something is extractable.
     * If-loops checks, if we touch one stone
     * Else-If checks then, if we touch at least one corner
     * Color sensitive
     *
     * @param b - the byte array of your stone
     * @param i - the col where you want to place it
     * @param j - the row where you want to place it
     * @return false, if stone Placement would be invalid
     * true, else
     */

    private boolean checkTheRules(byte[][] b, int i, int j) {
        byte[][] board = gl.getGameBoard();
        if (placeOverEdge(b, i, j)) {
            return false;
        } else if (hitSomeStones(b, i, j)) {
            return false;
        } else {
            int corner = 0; //We need at least one cornerContact to make it valid
            for (int col = i; col < i + b.length; col++) {
                for (int row = j; row < j + b.length; row++) {
                    if (b[row - j][col - i] != 0) {
                        //Let the cases begin
                            //Bottom seems fine
                        if ((row + 1) >= SIZE && (((col + 1) < SIZE) && (col - 1) >= 0)) {
                            if (checkSurroundings(board[col + 1][row], board[col - 1][row], board[col][row - 1])) {
                                Toast.makeText(getApplicationContext(), ("Bottom Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row - 1], board[col + 1][row - 1])) {
                                corner++;
                            }
                            //Right seems fine
                        } else if ((col + 1) >= SIZE && (((row + 1) < SIZE) && (row - 1) >= 0)) {
                            if (checkSurroundings(board[col][row - 1], board[col - 1][row], board[col][row + 1])) {
                                Toast.makeText(getApplicationContext(), ("Right Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row - 1], board[col - 1][row + 1])) {
                                corner++;
                            }
                            //Top seems fine
                        } else if ((row - 1) < 0 && ((col - 1) >= 0 && (col + 1) < SIZE)) {
                            if (checkSurroundings(board[col - 1][row], board[col][row + 1], board[col + 1][row])) {
                                Toast.makeText(getApplicationContext(), ("Top Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row + 1], board[col + 1][row + 1])) {
                                corner++;
                            }
                            //Left seems fine
                        } else if ((col - 1) < 0 && ((row - 1) >= 0 && (row + 1) < SIZE)) {
                            if (checkSurroundings(board[col][row - 1], board[col + 1][row], board[col][row + 1])) {
                                Toast.makeText(getApplicationContext(), ("Left Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col + 1][row - 1], board[col + 1][row + 1])) {
                                corner++;
                            }
                            //Right down seems fine
                        } else if ((row + 1) >= SIZE && (col + 1) >= SIZE) {
                            if (checkSurroundings(board[col - 1][row], board[col][row - 1])) {
                                Toast.makeText(getApplicationContext(), ("Right down corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row - 1])) {
                                corner++;
                            }
                            //Right upper seems fine
                        } else if ((row - 1) < 0 && (col + 1) >= SIZE) {
                            if (checkSurroundings(board[col - 1][row], board[col][row + 1])) {
                                Toast.makeText(getApplicationContext(), ("Right upper corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row + 1])) {
                                corner++;
                            }
                            //Left down seems fine
                        } else if ((row + 1) >= SIZE && (col - 1) < 0) {
                            if (checkSurroundings(board[col + 1][row], board[col][row - 1])) {
                                Toast.makeText(getApplicationContext(), ("Left down corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col + 1][row - 1])) {
                                corner++;
                            }
                            //Left upper seems fine
                        } else if ((row - 1) < 0 && (col - 1) < 0) {
                            if (checkSurroundings(board[col + 1][row], board[col][row + 1])) {
                                Toast.makeText(getApplicationContext(), ("Left upper corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col + 1][row + 1])) {
                                corner++;
                            }
                        } else {
                            //Inside field seems fine
                            if (checkSurroundings(board[col + 1][row], board[col][row + 1], board[col - 1][row], board[col][row - 1])) {
                                Toast.makeText(getApplicationContext(), ("That's invalid, dude!"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row - 1], board[col + 1][row - 1], board[col - 1][row + 1], board[col + 1][row + 1])) {
                                corner++;
                            }
                        }
                    }
                }
            }
            if (corner != 0)
                return true;
        }
        return false;
    }


    /**
     * places the stone, byte by byte
     *
     * @param b - the byte array of your stone
     * @param i - the col where you want to place it
     * @param j - the row where you want to place it
     */
    private void placeStone(byte[][] b, int i, int j) {
        for (int x = 0; x < b.length; x++) {
            for (int y = 0; y < b[x].length; y++) {
                if (b[x][y] != 0) {
                    gl.setSingleStone(b[x][y], i + y, j + x);
                }
            }
        }
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
     * Checks, if the surroundings are colored or not
     *
     * @param surroundings - bytes representing the color of the player
     * @return true, if one of the surroundings matches the current playerColor
     * false, else
     */
    public boolean checkSurroundings(byte... surroundings) {
        for (byte check : surroundings) {
            if (check == playerID) return true;
        }
        return false;
    }
}