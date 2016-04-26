package at.aau.se2.test;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Markus on 22.04.2016.
 */
public class GameLogic {

    private static int SIZE = 20;
    private static byte[][] gameBoard;

    private static GameLogic instance;

    private GameLogic () {
        setupNewGameBoard();
    }

    public static GameLogic getInstance () {
        if (GameLogic.instance == null) {
            GameLogic.instance = new GameLogic();
        }
        return GameLogic.instance;
    }

    private void setupNewGameBoard() {
        gameBoard = new byte[SIZE][SIZE];
        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                gameBoard[i][j] = 0;
            }
        }
    }

    private boolean placeStone(byte playerID, int stone, int x, int y) {
        byte[][] oldGameBoard = new byte[SIZE][SIZE];

        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                oldGameBoard[i][j] = gameBoard[i][j];
            }
        }

        Stones st = new Stones(1);
        byte[][] block = st.getStone(stone);

        for(int i = 0; i < block.length; i++){
            for(int j = 0; j < block.length; j++){
                if(((i+y) >= gameBoard.length || (j+x) >= gameBoard.length) || gameBoard[i+y][j+x] != 0){
                    return false;
                }else{
                    if(block[i][j] != 0){
                        gameBoard[i+y][j+x] = playerID;
                    }
                }
            }
        }

        return true;

    }

    public byte[][] getGameBoard(){
        return gameBoard;
    }

    public void setSingleStone(byte val, int x, int y){
        gameBoard[x][y] = val;
    }

    public void printBoard(){
        for(int i = 0; i < SIZE; i++){
            Log.e("", Arrays.toString(gameBoard[i]));
        }
    }
}
