package at.aau.se2.test;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

        playerID = (byte)(1+Math.random()*3);

        blocks = new Blocks(playerID);
        //1. Statusbar verstecken
        hideStatusBar();
        //2. Spielbrett erzeugen
        updateGameBoard();
        //3. BlockDrawer erzeugen
        initializeBlockDrawer();
    }

    @Override
    protected void onResume(){
        super.onResume();
        hideStatusBar();
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus){
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
        for(int i = 1; i < 22; i++){
            final ImageView oImageView = new ImageView(this);

            String color = "";
            switch(playerID){
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

            oImageView.setImageResource(getResources().getIdentifier(color+"_"+i, "drawable", getPackageName()));
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
                    for(ImageView bdc : blockDrawer_children){
                        if(bdc.equals(v)){
                            selectedBlockID = (int)bdc.getTag()-1; //Gewählter Spielstein
                            //Toast.makeText(getApplicationContext(), "ID: " + selectedBlockID, Toast.LENGTH_SHORT).show();
                            bdc.setBackgroundColor(Color.LTGRAY);
                        }else{
                            bdc.setBackgroundColor(Color.TRANSPARENT); //Highlight löschen
                        }
                    }
                    return false;
                }
            });
        }

    }

    //Bilschirmbreite
    private int getScreenWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    //Bilschirmhöhe
    private int getScreenHeight(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    private void updateGameBoard(){
        byte[][] board = gl.getGameBoard();

        gameBoardLayout = (GridLayout)findViewById(R.id.gameBoard);

        //Sicherheitshalber alle vorherigen Elemente auf dem gameBoard löschen
        gameBoardLayout.removeAllViews();

        gameBoardLayout.setColumnCount(SIZE);
        gameBoardLayout.setRowCount(SIZE);

        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                ImageView oImageView = new ImageView(this);
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                int size = getScreenWidth() / SIZE;
                switch(board[i][j]){
                    case 0:
                        param.height = size;
                        param.width = size;
                        param.setGravity(Gravity.CENTER);
                        param.columnSpec = GridLayout.spec(i);
                        param.rowSpec = GridLayout.spec(j);
                        oImageView.setImageResource(R.drawable.gameboard_empty);
                        oImageView.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (selectedBlockID >= 0) {
                                    for (int i = 0; i < gameBoardLayout.getColumnCount(); i++) {
                                        for (int j = 0; j < gameBoardLayout.getRowCount(); j++) {
                                            if (gameBoardLayout.getChildAt((i * 20) + j).equals(v)) {
                                                byte[][] b = blocks.getStone(selectedBlockID);
                                                for (int x = 0; x < b.length; x++) {
                                                    for (int y = 0; y < b.length; y++) {
                                                        //TODO:
                                                        //Array-out-of-bounds-exception
                                                        //Steine nicht außerhalb des Spielfeldes setzen
                                                        if (b[x][y] != 0) {
                                                            gl.setSingleStone(b[x][y], i + y, j + x);
                                                        }
                                                    }
                                                }
                                                int count = 0;
                                                if(!removed_blockDrawer_children.isEmpty()) {
                                                    for (ImageView t : removed_blockDrawer_children) {
                                                        if ((int) t.getTag() < selectedBlockID) {
                                                            count++;
                                                        }
                                                    }
                                                }

                                                int rm_index = Math.max(0,(selectedBlockID)-count);
                                                ImageView rm = (ImageView) blockDrawer.getChildAt(rm_index);
                                                //Toast.makeText(getApplicationContext(), (selectedBlockID)+" / "+rm_index + " / " + (22-blockDrawer.getChildCount()), Toast.LENGTH_SHORT).show();
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
                        });
                        break;
                    case 1:
                        param.height = size;
                        param.width = size;
                        param.setGravity(Gravity.CENTER);
                        param.columnSpec = GridLayout.spec(i);
                        param.rowSpec = GridLayout.spec(j);
                        oImageView.setImageResource(R.drawable.green_s_1);
                        break;
                    case 2:
                        param.height = size;
                        param.width = size;
                        param.setGravity(Gravity.CENTER);
                        param.columnSpec = GridLayout.spec(i);
                        param.rowSpec = GridLayout.spec(j);
                        oImageView.setImageResource(R.drawable.red_s_1);
                        break;
                    case 3:
                        param.height = size;
                        param.width = size;
                        param.setGravity(Gravity.CENTER);
                        param.columnSpec = GridLayout.spec(i);
                        param.rowSpec = GridLayout.spec(j);
                        oImageView.setImageResource(R.drawable.blue_s_1);
                        break;
                    case 4:
                        param.height = size;
                        param.width = size;
                        param.setGravity(Gravity.CENTER);
                        param.columnSpec = GridLayout.spec(i);
                        param.rowSpec = GridLayout.spec(j);
                        oImageView.setImageResource(R.drawable.yellow_s_1);
                        break;
                }
                oImageView.setLayoutParams(param);
                gameBoardLayout.addView(oImageView);
            }
        }
    }

    private void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }
}
