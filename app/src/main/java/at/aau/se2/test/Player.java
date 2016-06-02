package at.aau.se2.test;

import android.util.Log;

import java.util.ArrayList;

public class Player {
    private byte p_id;
    private Blocks blocks;

    private int score;
    private byte[] remainingStones;

    private ArrayList<IndexTuple> saveIndices;
    public final int MAX_STONES = 89;

    public Player(byte p_id) {
        this.p_id = p_id;

        // create blocks-object for player
        this.blocks = new Blocks(p_id);
        // Player score and remaining Stones (By the tag)
        score = 0;

        saveIndices = new ArrayList<>(MAX_STONES);

        remainingStones = new byte[21];
        fillArray();
    }

    public byte getPlayerId() {
        return p_id;
    }

    public byte[][] getStone(int num) {
        return blocks.getStone(num);
    }

    public String getPlayerColor() {
        switch (this.p_id) {
            case 1:
                return "green";
            case 2:
                return "red";
            case 3:
                return "blue";
            case 4:
                return "yellow";
        }
        return null;
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

    public void stonesToLog() {
        String s = "";
        for (int i = 0; i < remainingStones.length; i++) {
            if (remainingStones[i] != -1) {
                s += remainingStones[i] + ", ";
            }
        }
        Log.d("Remaining Stones", s);
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
                if (bytes[i][j] == p_id) {
                    saveIndices.add(new IndexTuple(index_i + j, index_j + i));
                }
            }
        }
    }

    public void printSaveIndices() {
        for (int i = 0; i < saveIndices.size(); i++) {
            Log.d("Indices:", saveIndices.get(i).toString());
        }
    }
}
