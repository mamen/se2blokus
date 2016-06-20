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
    public void testRotateAndPace() {
        byte[][] test = {{0, 0, 0}, {0, 0, 0}, {playerId, playerId, playerId}};
        byte[][] arr = new byte[20][20];
        arr[0][0] = playerId;
        arr[0][1] = playerId;
        arr[0][2] = playerId;
        gl.placeStone(gl.rotate(player.getStone(3)), -2, 0);
        Assert.assertTrue(Arrays.deepEquals(gl.getGameBoard(), arr));
    }

}
