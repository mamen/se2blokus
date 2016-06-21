package at.aau.se2.test;


import android.content.Context;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

public class TestGameLogicNewFunctions {

    static final byte playerId = 2;
    static Player player = new Player(playerId);
    static Context context;
    static GameLogic gl = GameLogic.getInstance(player, context);


    static byte[][] someStone;
    static byte[][] someChangedStone;

    @BeforeClass
    public static void initialise() {
        gl.resetInstance();
        gl = GameLogic.getInstance(player, context);
        someStone = new byte[][]{{playerId, playerId, playerId, playerId}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        someChangedStone = new byte[][]{{playerId, 0, 0, 0},{playerId, 0, 0, 0},{playerId, 0, 0, 0},{playerId, 0, 0, 0}};
    }

    @Test
    public void testRotateAndShiftLeft() {
        Assert.assertTrue(Arrays.deepEquals(someChangedStone, gl.deformStone(gl.deformStone(gl.deformStone(gl.rotate(someStone), 1), 1), 1)));
    }

    @Test
    public void testWithFailure() {
        Assert.assertFalse(Arrays.deepEquals(someChangedStone, gl.deformStone(someStone, 1)));
    }

    @Test
    public void testComplexStuff() {
        Assert.assertTrue(Arrays.deepEquals(gl.deformStone(gl.rotate(gl.rotate(gl.rotate(someChangedStone))), 2), gl.deformStone(gl.rotate(gl.rotate(someStone)), 2)));
    }

    @Test
    public void testMoreStuff() {
        Assert.assertTrue(Arrays.deepEquals(gl.deformStone(gl.deformStone(gl.deformStone(gl.rotate(gl.rotate(gl.rotate(someChangedStone))), 2), 2), 2), someStone));
    }


}
