package at.aau.se2.test;

import android.content.Context;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by KingSeybro on 11.06.2016.
 */
public class TestGameLogicFunctions3 {

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
    public void testRules3() {
        Assert.assertTrue(gl.checkTheRules(gl.rotate(firstTestStone), 10, 13));
    }

    @Test
    public void testRememberField() {
        Assert.assertTrue(Arrays.deepEquals(remember, gl.rememberField(secondTestStone, 12, 10)));
    }

    @Test
    public void testRememberField2() {
        Assert.assertTrue(Arrays.deepEquals(remember2, gl.rememberField(firstTestStone, 10, 11)));
    }
}
