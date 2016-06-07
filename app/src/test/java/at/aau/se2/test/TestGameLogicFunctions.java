package at.aau.se2.test;

import android.content.Context;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by KingSeybro on 07.06.2016.
 */
public class TestGameLogicFunctions {

    static final byte player_id = 2;
    static Player player = new Player(player_id);
    static Context context;
    static GameLogic gl = GameLogic.getInstance(player, context);
    static byte[][] someStone;
    static byte[][] testGameBoard;
    byte[][] initialGameBoard = gl.getGameBoard();

    @BeforeClass
    public static void initialise() {
        someStone = new byte[][]{{player_id, player_id, player_id, player_id}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        testGameBoard = new byte[20][20];
    }

    /*@Test
    public void testSetupNewGameBoard() { //If run first, it works, because later gameboard is filled
        Assert.assertTrue(Arrays.deepEquals(new byte[20][20], gl.getGameBoard()));
    }*/

    @Test
    public void testPlacingAndGetGameBoard() {
        testGameBoard[16][0] = player_id;
        testGameBoard[17][0] = player_id;
        testGameBoard[18][0] = player_id;
        testGameBoard[19][0] = player_id;
        gl.placeStone(someStone, 16, 0);
        Assert.assertTrue(Arrays.deepEquals(testGameBoard, gl.getGameBoard()));
    }

    @Test
    public void testOverRightEdge() {
        Assert.assertTrue(gl.placeOverEdge(someStone, 17, 3));
    }

    @Test
    public void testOverBottomEdge() {
        Assert.assertTrue(gl.placeOverEdge(gl.rotate(someStone), 5, 17));
    }

    @Test
    public void testInsideField() {
        Assert.assertFalse(gl.placeOverEdge(someStone, 13, 7));
    }
}
