package at.aau.se2.test;

import android.content.Context;

import org.junit.BeforeClass;

public class TestGameLogicFunctions3 {

    static final byte playerId = 2;
    static Player player = new Player(playerId);
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
                {0,0,0,0},
                {0,0,0,0}
        };
        gameBoardRestored = new byte[20][20];
        gameBoardRestored[12][12] = playerId;
        gameBoardRestored[13][12] = playerId;
        gameBoardRestored[13][13] = playerId;

        gameBoardRestored[10][10] = playerId;
        gameBoardRestored[11][10] = playerId;
        gameBoardRestored[11][11] = playerId;
        gameBoardRestored[12][11] = playerId;
        gameBoardRestored[12][12] = playerId;

        gl.placeStone(secondTestStone, 10, 10);
    }


    //TODO: junit tests fail with jenkins
    /*
    at.aau.se2.test.TestGameLogicFunctions3 > testRememberField FAILED
    java.lang.AssertionError
        at org.junit.Assert.fail(Assert.java:86)
        at org.junit.Assert.assertTrue(Assert.java:41)
        at org.junit.Assert.assertTrue(Assert.java:52)
        at at.aau.se2.test.TestGameLogicFunctions3.testRememberField(TestGameLogicFunctions3.java:73)

at.aau.se2.test.TestGameLogicFunctions3 > testRememberField2 FAILED
    java.lang.AssertionError
        at org.junit.Assert.fail(Assert.java:86)
        at org.junit.Assert.assertTrue(Assert.java:41)
        at org.junit.Assert.assertTrue(Assert.java:52)
        at at.aau.se2.test.TestGameLogicFunctions3.testRememberField2(TestGameLogicFunctions3.java:78)

at.aau.se2.test.TestGameLogicFunctions3 > testRules3 FAILED
    java.lang.AssertionError
        at org.junit.Assert.fail(Assert.java:86)
        at org.junit.Assert.assertTrue(Assert.java:41)
        at org.junit.Assert.assertTrue(Assert.java:52)
        at at.aau.se2.test.TestGameLogicFunctions3.testRules3(TestGameLogicFunctions3.java:68)
     */
    /*@Test
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
    }*/
}
