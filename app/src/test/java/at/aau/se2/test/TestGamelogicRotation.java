package at.aau.se2.test;

import android.content.Context;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;


public class TestGamelogicRotation {

    static final byte player_id = 2;
    static Player player = new Player(player_id);
    static Context context;
    static GameLogic gl = GameLogic.getInstance(player, context);


    @Test
    public void testStoneZeroRotation() {
        byte[][] test = {{0, player_id}, {0, 0}};
        byte[][] rotated = gl.rotate(player.getStone(0));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneOneRotation() {
        byte[][] test = {{0, player_id}, {0, player_id}};
        byte[][] rotated = gl.rotate(player.getStone(1));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneTwoRotation() {
        byte[][] test = {{0, player_id}, {player_id, player_id}};
        byte[][] rotated = gl.rotate(player.getStone(2));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneThreeRotation() {
        byte[][] test = {{0, 0, player_id}, {0, 0, player_id}, {0, 0, player_id}};
        byte[][] rotated = gl.rotate(player.getStone(3));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }

    @Test
    public void testStoneFiveRotation() {
        byte[][] test = {{0, 0, 0}, {player_id, player_id, player_id}, {0, player_id, 0}};
        byte[][] rotated = gl.rotate(gl.rotate(player.getStone(5)));
        Assert.assertTrue(Arrays.deepEquals(test, rotated));
    }
}
