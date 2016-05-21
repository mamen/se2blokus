package at.aau.se2.test;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

public class GameLogic {

    private static int SIZE = 20;
    private static byte[][] gameBoard;

    private static GameLogic instance;

    private Context context;

    private Player player;

    private GameLogic (Player player, Context context) {
        this.player = player;
        this.context = context;
        setupNewGameBoard();
    }

    public static GameLogic getInstance (Player player, Context context) {
        if (GameLogic.instance == null) {
            GameLogic.instance = new GameLogic(player, context);
        } else {
            GameLogic.instance.player = player;
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

    public byte[][] getGameBoard(){
        return gameBoard;
    }

    public void setSingleStone(byte val, int x, int y){
        gameBoard[x][y] = val;
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
    public boolean placeOverEdge(byte[][] b, int i, int j) {
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
    public boolean hitSomeStones(byte[][] b, int i, int j) {
        byte[][] board = getGameBoard();
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
    public boolean hitTheCorner(byte[][] b, int i, int j) {
        if (!placeOverEdge(b, i, j) && !hitSomeStones(b, i, j)) {
            byte[][] board = getGameBoard();
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
            Toast.makeText(context, ("Why does this fail?"), Toast.LENGTH_SHORT).show();
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

    public boolean checkTheRules(byte[][] b, int i, int j) {
        byte[][] board = getGameBoard();
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
                                Toast.makeText(context, ("Bottom Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row - 1], board[col + 1][row - 1])) {
                                corner++;
                            }
                            //Right seems fine
                        } else if ((col + 1) >= SIZE && (((row + 1) < SIZE) && (row - 1) >= 0)) {
                            if (checkSurroundings(board[col][row - 1], board[col - 1][row], board[col][row + 1])) {
                                Toast.makeText(context, ("Right Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row - 1], board[col - 1][row + 1])) {
                                corner++;
                            }
                            //Top seems fine
                        } else if ((row - 1) < 0 && ((col - 1) >= 0 && (col + 1) < SIZE)) {
                            if (checkSurroundings(board[col - 1][row], board[col][row + 1], board[col + 1][row])) {
                                Toast.makeText(context, ("Top Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row + 1], board[col + 1][row + 1])) {
                                corner++;
                            }
                            //Left seems fine
                        } else if ((col - 1) < 0 && ((row - 1) >= 0 && (row + 1) < SIZE)) {
                            if (checkSurroundings(board[col][row - 1], board[col + 1][row], board[col][row + 1])) {
                                Toast.makeText(context, ("Left Wall test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col + 1][row - 1], board[col + 1][row + 1])) {
                                corner++;
                            }
                            //Right down seems fine
                        } else if ((row + 1) >= SIZE && (col + 1) >= SIZE) {
                            if (checkSurroundings(board[col - 1][row], board[col][row - 1])) {
                                Toast.makeText(context, ("Right down corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row - 1])) {
                                corner++;
                            }
                            //Right upper seems fine
                        } else if ((row - 1) < 0 && (col + 1) >= SIZE) {
                            if (checkSurroundings(board[col - 1][row], board[col][row + 1])) {
                                Toast.makeText(context, ("Right upper corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col - 1][row + 1])) {
                                corner++;
                            }
                            //Left down seems fine
                        } else if ((row + 1) >= SIZE && (col - 1) < 0) {
                            if (checkSurroundings(board[col + 1][row], board[col][row - 1])) {
                                Toast.makeText(context, ("Left down corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col + 1][row - 1])) {
                                corner++;
                            }
                            //Left upper seems fine
                        } else if ((row - 1) < 0 && (col - 1) < 0) {
                            if (checkSurroundings(board[col + 1][row], board[col][row + 1])) {
                                Toast.makeText(context, ("Left upper corner test"), Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (checkSurroundings(board[col + 1][row + 1])) {
                                corner++;
                            }
                        } else {
                            //Inside field seems fine
                            if (checkSurroundings(board[col + 1][row], board[col][row + 1], board[col - 1][row], board[col][row - 1])) {
                                vibrate();
                                Toast.makeText(context, ("That's invalid, dude!"), Toast.LENGTH_SHORT).show();
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
    public void placeStone(byte[][] b, int i, int j) {
        for (int x = 0; x < b.length; x++) {
            for (int y = 0; y < b[x].length; y++) {
                if (b[x][y] != 0) {
                    setSingleStone(b[x][y], i + y, j + x);
                }
            }
        }
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
            if (check == player.getPlayerId()) return true;
        }
        return false;
    }


    private void vibrate() {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }
}
