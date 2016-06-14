package at.aau.se2.test;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private byte playerID;
    private Blocks blocks;

    private int score;
    private byte[] remainingStones;

    private ArrayList<IndexTuple> saveIndices;
    public static final int MAX_STONES = 89;

    public Player(byte id) {
        this.playerID = id;

        // create blocks-object for player
        this.blocks = new Blocks(id);
        // Player score and remaining Stones (By the tag)
        score = 0;

        saveIndices = new ArrayList<>(MAX_STONES);

        remainingStones = new byte[21];
        fillArray();
    }

    public byte getPlayerId() {
        return playerID;
    }

    public byte[][] getStone(int num) {
        return blocks.getStone(num);
    }

    public String getPlayerColor() {
        switch (this.playerID) {
            case 1:
                return "green";
            case 2:
                return "red";
            case 3:
                return "blue";
            case 4:
                return "yellow";
            default:
                throw new ExceptionInInitializerError("Failed to initialise player-color");
        }
    }

    /**
     * Add to the players score
     *
     * @param newScore - what to add
     */
    public void addToScore(int newScore) {
        score += newScore;
    }

    /**
     * @return players score
     */
    public int getScore() {
        return this.score;
    }

    /**
     * For each colored stone, add 1 to the players score
     *
     * @param b - byte array of your stone
     */
    public void calculateScore(byte[][] b) {
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[i].length; j++) {
                if (b[i][j] > 0 && b[i][j] < 5) {
                    addToScore(1);
                }
            }
        }
    }

    /**
     * Fills the remaining stones with the tags
     */
    private void fillArray() {
        for (int i = 0; i < 21; i++) {
            remainingStones[i] = (byte) i;
        }
    }

    /**
     * Removes one tag from the array
     *
     * @param stoneID - to be removed tag
     */
    public void removeFromArray(int stoneID) {
        for (int i = 0; i < remainingStones.length; i++) {
            if (remainingStones[i] == stoneID) {
                remainingStones[i] = -1;
            }
        }
    }

    /**
     * @return remainingStones Array
     */
    public byte[] getRemainingStones() {
        return remainingStones;
    }

    public ArrayList<IndexTuple> getSaveIndices() {
        return saveIndices;
    }

    public int getSaveIndicesSize() {
        return saveIndices.size();
    }

    public void putToSaveIndices(byte[][] bytes, int index_i, int index_j) {
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < bytes[i].length; j++) {
                if (bytes[i][j] == playerID && dontSaveRedundance(bytes, i, j)) {
                    saveIndices.add(0, new IndexTuple(index_i + j, index_j + i));
                }
            }
        }
    }

    public boolean dontSaveRedundance(byte[][] b, int i, int j) {
        if((i-1) >= 0 && (i+1) < b.length) {
            if(b[i-1][j] == playerID && b[i+1][j] == playerID) {
                return false;
            }
        }
        if((j-1) >= 0 && (j+1) < b.length) {
            if(b[i][j-1] == playerID && b[i][j+1] == playerID) {
                return false;
            }
        }
        return true;
    }

    /* ---DEBUGGING--- */

    public void stonesToLog() {
        String s = "";
        for (int i = 0; i < remainingStones.length; i++) {
            if (remainingStones[i] != -1) {
                s += remainingStones[i] + ", ";
            }
        }
        Log.d("Remaining Stones", s);
    }

    public void printSaveIndices() {
        for (int i = 0; i < saveIndices.size(); i++) {
            Log.d("Indices:", saveIndices.get(i).toString());
        }
    }
}
