package at.aau.se2.test;

import android.content.Context;
import android.util.Log;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by KingSeybro on 09.06.2016.
 */
public class TestGameLogicFunctions2 {

    static final byte player_id = 2;
    static Player player = new Player(player_id);
    static Context context;
    static GameLogic gl = GameLogic.getInstance(player, context);
    static byte[][] firstTestStone;
    static byte[][] secondTestStone;
    static byte[][] remember;
    static byte[][] remember2;
    static byte[][] gameBoardRestored;

    byte[][] initialGameBoard = gl.getGameBoard();

    @BeforeClass
    public static void initialise() {
        firstTestStone = new byte[][]{
                {player_id, player_id, player_id, player_id},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        secondTestStone = new byte[][]{
                {player_id, player_id, 0},
                {0, player_id, player_id},
                {0, 0, player_id}
        };
        remember = new byte[][]{
                {0, 0, 0},
                {player_id, 0, 0},
                {player_id, 0, 0}
        };
        remember2 = new byte[][]{
                {0, player_id, player_id, 0},
                {0, 0, player_id, 0},
                {0,0,0,0},
                {0,0,0,0}
        };
        gameBoardRestored = new byte[20][20];
        gameBoardRestored[12][12] = player_id;
        gameBoardRestored[13][12] = player_id;
        gameBoardRestored[13][13] = player_id;

        gameBoardRestored[10][10] = player_id;
        gameBoardRestored[11][10] = player_id;
        gameBoardRestored[11][11] = player_id;
        gameBoardRestored[12][11] = player_id;
        gameBoardRestored[12][12] = player_id;

        gl.placeStone(secondTestStone, 10, 10);
    }

    @Test
    public void testEqualityOfFirstStone() { //player.getStone works
        Assert.assertTrue(Arrays.deepEquals(firstTestStone, player.getStone(6)));
    }

    @Test
    public void testEqualityOfSecondStone() { //player.getStone and gl.rotate works
        Assert.assertTrue(Arrays.deepEquals(secondTestStone, gl.rotate(player.getStone(16))));
    }

    @Test
    public void testHitSomeStone() {
        Assert.assertFalse(gl.hitSomeStones(firstTestStone, 4, 5));
    }

    @Test
    public void testHitTheCorner() {
        Assert.assertFalse(gl.hitTheCorner(firstTestStone, 2, 9));
    }

    @Test
    public void testHitTheCorner2() {
        Assert.assertTrue(gl.hitTheCorner(firstTestStone, 16, 19));
    }

    @Test
    public void testHitTheCorner3() {
        Assert.assertTrue(gl.hitTheCorner(gl.rotate(secondTestStone), 17, 0));
    }

    @Test
    public void testHitTheCorner4() {
        Assert.assertTrue(gl.hitTheCorner(gl.rotate(secondTestStone), 0, 17));
    }

    @Test
    public void testRules1() {
        Assert.assertFalse(gl.checkTheRules(firstTestStone, 10, 10));
    }

    @Test
    public void testRules2() {
        Assert.assertTrue(gl.checkTheRules(firstTestStone, 12, 9));
    }

    //TODO: junit test fails with jenkins
    /*@Test
    public void testRestoreFiel1() {
        gl.restoreField(remember2, 11, 12);
        Assert.assertTrue(Arrays.deepEquals(gameBoardRestored, gl.getGameBoard()));
    }*/

    @Test
    public void testCheckSurroundings1() {
        Assert.assertTrue(gl.checkSurroundings(gl.getGameBoard()[12][12]));
    }

    @Test
    public void testCheckSurroundings2() {
        Assert.assertFalse(gl.checkSurroundings(gl.getGameBoard()[2][5]));
    }

    public void ArrayToLog(byte[][] b) {
        String s = "";
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b.length; j++) {
                s += b[i][j] + ", ";
            }
            s+= '\n';
        }
        System.out.println(s);
    }
}
