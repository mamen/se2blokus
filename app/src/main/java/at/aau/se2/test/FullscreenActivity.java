package at.aau.se2.test;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        fullscreenLayout = (RelativeLayout) findViewById(R.id.contentPanel);

        byte playerID = -1;
        selectedBlockID = -1;

        String color;
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

        // create player-object
        player = new Player(playerID);

        gl = GameLogic.getInstance(player, this.getApplicationContext());

        //1. Statusbar verstecken
        hideStatusBar();
        //2. Spielbrett erzeugen
        updateGameBoard();

        gameBoardLayout.setOnDragListener(new View.OnDragListener() {

            byte index_i = -1;
            byte index_j = -1;
            ImageView accept;
            ImageView cancel;
            ImageView draggedImage;

            @Override
            public boolean onDrag(View v, DragEvent event) {
                //ImageView block;
                //GridLayout gameBoard;

                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        if(v instanceof GridLayout){ //only drop on GameBoard
                            Toast.makeText(getApplicationContext(),"i: " + index_i + " j: " + index_j, Toast.LENGTH_SHORT);
                            //gameBoard = (GridLayout)v;
                            //block = (ImageView) event.getLocalState();

                            draggedImage = (ImageView)event.getLocalState();

                            RelativeLayout.LayoutParams params_accept = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                            RelativeLayout.LayoutParams params_cancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                            index_i = (byte)Math.round(event.getX() / (v.getWidth() / 20));
                            index_j = (byte)Math.round(event.getY() / (v.getHeight() / 20));

                            //außerhalb des bildschirmes platziert
                            if(event.getX() > v.getWidth()){
                                index_i = 19;
                            }
                            if (event.getY() > v.getHeight()){
                                index_j = 19;
                            }
                            if(event.getX() < 0){
                                index_i = 0;
                            }
                            if (event.getY() < 0) {
                                index_j = 0;
                            }




                            accept = new ImageView(getApplicationContext());
                            accept.setImageResource(R.drawable.checkmark);

                            cancel = new ImageView(getApplicationContext());
                            cancel.setImageResource(R.drawable.cancel);

                            params_accept.setMargins(0,v.getHeight()+accept.getHeight(), 0,0);
                            params_cancel.setMargins(Math.round(v.getWidth()/2),v.getHeight()+accept.getHeight(), 0,0);

                            accept.setLayoutParams(params_accept);
                            cancel.setLayoutParams(params_cancel);



                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!isYourPlacementValid(index_i, index_j)){
                                        vibrate();
                                    }
                                    fullscreenLayout.removeView(accept);
                                    fullscreenLayout.removeView(cancel);
                                }
                            });

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    fullscreenLayout.removeView(accept);
                                    fullscreenLayout.removeView(cancel);
                                }
                            });


                            fullscreenLayout.addView(accept);
                            fullscreenLayout.addView(cancel);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //3. BlockDrawer erzeugen
        initializeBlockDrawer();
    }

    private void initializeBlockDrawer() {
        LinearLayout blockDrawer_parent = (LinearLayout) findViewById(R.id.blockDrawer_parent);
        ViewGroup.LayoutParams params = blockDrawer_parent.getLayoutParams();

        params.height = getScreenHeight() - (getScreenWidth());
        blockDrawer_parent.setLayoutParams(params);

        blockDrawer = (LinearLayout) findViewById(R.id.blockDrawer);

        blockDrawer_children = new ArrayList<ImageView>();
        removed_blockDrawer_children = new ArrayList<ImageView>();

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
            oImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ClipData data = ClipData.newPlainText("", "");
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                        v.startDrag(data, shadowBuilder, v, 0);


                        for (ImageView bdc : blockDrawer_children) {
                            if (bdc.equals(v)) {
                                selectedBlockID = (int) bdc.getTag(); //Gewählter Spielstein
                            }
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

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
                switch (board[i][j]) {
                    case 0:
                        oImageView.setImageResource(R.drawable.gameboard_empty);
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

    private boolean isYourPlacementValid(int x, int y) {
        if (selectedBlockID >= 0) {
                    byte[][] b;
                    b = player.getStone(selectedBlockID - 1);
                    if (!removed_blockDrawer_children.isEmpty()) {
                        if (gl.checkTheRules(b, x, y)) {
                            gl.placeStone(b, x, y);
                            removeFromBlockDrawer();
                        } else {
                            return false;
                        }
                    } else {
                        if (gl.hitTheCorner(b, x, y)) {
                            gl.placeStone(b, x, y);
                            removeFromBlockDrawer();
                        }else{
                            vibrate();
                        }
                    }
                }
        updateGameBoard();
        return true;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
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
     *
     * @return
     */
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
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}