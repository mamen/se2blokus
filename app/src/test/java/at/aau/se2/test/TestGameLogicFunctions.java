package at.aau.se2.test;

import android.content.Context;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

public class TestGameLogicFunctions {

    static final byte playerId = 2;
    static Player player = new Player(playerId);
    static Context context;
    static GameLogic gl = GameLogic.getInstance(player, context);


    static byte[][] someStone;
    static byte[][] testGameBoard;

    @BeforeClass
    public static void initialise() {
        gl.resetInstance();
        gl = GameLogic.getInstance(player, context);
        someStone = new byte[][]{{playerId, playerId, playerId, playerId}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        testGameBoard = new byte[20][20];
        testGameBoard[16][0] = playerId;
        testGameBoard[17][0] = playerId;
        testGameBoard[18][0] = playerId;
        testGameBoard[19][0] = playerId;
        gl.placeStone(someStone, 16, 0);
    }

    @Test
    public void testPlacingAndGetGameBoard() {
        Assert.assertTrue(Arrays.deepEquals(testGameBoard, gl.getGameBoard()));
    }

    @Test
    public void testOverRightSide() {
        Assert.assertTrue(gl.placeOverEdge(someStone, 17, 3));
    }

    @Test
    public void testOverBottomSide() {
        Assert.assertTrue(gl.placeOverEdge(gl.rotate(someStone), 5, 17));
    }

    @Test
    public void testInsideField() {
        Assert.assertFalse(gl.placeOverEdge(someStone, 13, 7));
    }
}
