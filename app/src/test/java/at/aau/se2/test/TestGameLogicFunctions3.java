package at.aau.se2.test;

import android.content.Context;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

public class TestGameLogicFunctions3 {


    static final byte playerId = 2;
    static Player player = new Player(playerId);
    static Context context;
    static GameLogic gl = GameLogic.getInstance(player, context);
    static byte[][] firstTestStone;
    static byte[][] secondTestStone;
    static byte[][] remember;
    static byte[][] remember2;

    byte[][] initialGameBoard = gl.getGameBoard();

    @BeforeClass
    public static void initialise() {
        gl.resetInstance();
        gl = GameLogic.getInstance(player, context);
        firstTestStone = new byte[][]{
                {playerId, playerId, playerId, playerId},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        secondTestStone = new byte[][]{
                {playerId, playerId, 0},
                {0, playerId, playerId},
                {0, 0, playerId}
        };
        remember = new byte[][]{
                {0, 0, 0},
                {playerId, 0, 0},
                {playerId, 0, 0}
        };
        remember2 = new byte[][]{
                {0, playerId, playerId, 0},
                {0, 0, playerId, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };

        gl.placeStone(secondTestStone, 10, 10);
    }


    @Test
    public void testRememberField() {
        Assert.assertTrue(Arrays.deepEquals(remember, gl.rememberField(secondTestStone, 12, 10)));
    }

    @Test
    public void testRememberField2() {
        Assert.assertTrue(Arrays.deepEquals(remember2, gl.rememberField(firstTestStone, 10, 11)));
    }

    @Test
    public void testRules3() {
        Assert.assertTrue(gl.checkTheRules(gl.rotate(firstTestStone), 10, 13));
    }
}
