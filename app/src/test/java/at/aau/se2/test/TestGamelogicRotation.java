package at.aau.se2.test;

import android.content.Context;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;


public class TestGamelogicRotation {

    static final byte playerId = 2;
    static Player player = new Player(playerId);
    static Context context;
    static GameLogic gl = GameLogic.getInstance(player, context);


    @BeforeClass
    public static void initialise() {
        gl.resetInstance();
        gl = GameLogic.getInstance(player, context);
    }

    @Test
    public void testStoneZeroRotation() {
        byte[][] test = {{0, playerId}, {0, 0}};
        byte[][] rotated = gl.rotate(player.getStone(0));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneOneRotation() {
        byte[][] test = {{0, playerId}, {0, playerId}};
        byte[][] rotated = gl.rotate(player.getStone(1));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneTwoRotation() {
        byte[][] test = {{0, playerId}, {playerId, playerId}};
        byte[][] rotated = gl.rotate(player.getStone(2));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneThreeRotation() {
        byte[][] test = {{0, 0, playerId}, {0, 0, playerId}, {0, 0, playerId}};
        byte[][] rotated = gl.rotate(player.getStone(3));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneFiveRotation() {
        byte[][] test = {{0, 0, 0}, {playerId, playerId, playerId}, {0, playerId, 0}};
        byte[][] rotated = gl.rotate(gl.rotate(player.getStone(5)));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testRotateAndPlaceLeft() {
        byte[][] test = {{0, 0, playerId}, {0, 0, playerId}, {0, 0, playerId}};
        Assert.assertFalse(gl.placeOverEdge(test, -2, 0));
    }


    @Test
    public void testGameRules() {
        byte[][] test = {{0, 0, playerId},
                {0, 0, playerId},
                {0, 0, playerId}
        };
        gl.placeStone(test, -2, 0);
        Assert.assertTrue(gl.checkTheRules(test, -1, 3));

    }

    @Test
    public void testhitthecorner() {
        byte[][] test = {{0, 0, playerId},
                {0, 0, playerId},
                {0, 0, playerId}
        };
        Assert.assertTrue(gl.hitTheCorner(test, -2, 0));
    }

    @Test
    public void testRememberField() {
        byte[][] test = {{0, 0, playerId},
                {0, 0, playerId},
                {0, 0, playerId}
        };
        byte[][] rememberMe = { {0, 0, 0},
                                {0, 0, 0},
                                {0, 0, 0}
        };
        Assert.assertTrue(Arrays.deepEquals(gl.rememberField(test, -2, 0), rememberMe));
    }

    @Test
    public void testRestore() {
        byte[][] rememberMe = { {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        gl.getGameBoard();
        gl.restoreField(rememberMe, -2, 0);
        Assert.assertTrue(Arrays.deepEquals(gl.getGameBoard(), new byte[20][20]));
    }
}
