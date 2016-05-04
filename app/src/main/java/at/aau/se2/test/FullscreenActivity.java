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
    private static boolean firstrm = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                                placeStone(v);
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
    }

    /*
    * Actual New Stone Placement:
    **  Check Stone Placement:
    ** - if(placerOverEdge) stone reaches over field
    ** - if(hitSomeStones) stone hits another block
    ** - check for gamerules here!? if(removed_blockDrawer_children.isEmpty())
    **   -> firstStone -> corner, etc.
    * */
    private void placeStone(View v) {
        if (selectedBlockID >= 0) {
            for (int i = 0; i < gameBoardLayout.getColumnCount(); i++) {
                for (int j = 0; j < gameBoardLayout.getRowCount(); j++) {
                    if (gameBoardLayout.getChildAt((i * SIZE) + j).equals(v)) {
                        byte[][] b;
                        b = blocks.getStone(selectedBlockID-1);
                        //If single Stone, just place it
                        if (selectedBlockID != 0) {
                            if (placeOverEdge(b, i, j)) {
                                Toast.makeText(getApplicationContext(), ("You can't place that here"), Toast.LENGTH_SHORT).show();
                                System.out.println("No placing stone here");
                                break;
                            } else if(hitSomeStones(b, i, j)) {
                                System.out.println("Don't hurt other stones!");
                                Toast.makeText(getApplicationContext(), ("You can't place that here"), Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                for (int x = 0; x < b.length; x++) {
                                    for (int y = 0; y < b[x].length; y++) {
                                        if (b[x][y] != 0) {
                                            gl.setSingleStone(b[x][y], i + y, j + x);
                                        }
                                    }
                                }
                            }
                        } else {
                            gl.setSingleStone(b[0][0], i, j);
                        }

                        // Counting goes right, selectedBlockID should be same than Tag.
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


    /*
    * Checks if you place over the edges
    * */
    private boolean placeOverEdge(byte[][] b, int i, int j) {
        for (int row = i; row < i+b.length; row++) {
            for (int col = j; col < j+b.length; col++) {
                if(b[col-j][row-i] != 0 && (row >= SIZE || col >= SIZE)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
    * Checks if you hit other stones with your placement
    * */
    private boolean hitSomeStones(byte[][] b, int i, int j) {
        byte[][] board = gl.getGameBoard();
        for (int row = i; row < i+b.length; row++) {
            for (int col = j; col < j+b.length; col++) {
                if(b[col-j][row-i] != 0 && board[row][col] != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}