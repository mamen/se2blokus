package at.aau.se2.test;

/**
 * Created by Markus on 22.04.2016.
 */
public class Blocks {
    private byte playerID;
    private byte[][][] stones;

    public Blocks(byte playernum){
        this.playerID = playernum;
        generateStonesMatrix();
    }

    private void generateStonesMatrix(){
        stones = new byte[][][]{
                {
                        {this.playerID , 0 },
                        { 0, 0 }
                },
                {
                        { playerID, playerID },
                        { 0, 0 }
                },
                {
                        { playerID, playerID },
                        { 0, playerID }
                },
                {
                        { playerID, playerID, playerID },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                },
                {
                        { playerID, playerID },
                        { playerID, playerID }
                },
                {
                        { 0, playerID, 0 },
                        { playerID, playerID, playerID } ,
                        { 0, 0, 0 }
                },
                {
                        { playerID, playerID, playerID, playerID },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                },
                {
                        { 0, 0, playerID },
                        { playerID, playerID, playerID },
                        { 0, 0, 0 }
                },
                {
                        { 0, playerID, playerID },
                        { playerID, playerID, 0 },
                        { 0, 0, 0 }
                },
                {
                        { playerID, 0, 0, 0 },
                        { playerID, playerID, playerID, playerID },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                },
                {
                        { 0, playerID, 0 },
                        { 0, playerID, 0 },
                        { playerID, playerID, playerID }
                },
                {
                        { playerID, 0, 0 },
                        { playerID, 0, 0 },
                        { playerID, playerID, playerID }
                },
                {
                        { 0, playerID, playerID, playerID },
                        { playerID, playerID, 0, 0 },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                },
                {
                        { 0, 0, playerID },
                        { playerID, playerID, playerID },
                        { playerID, 0, 0 }
                },
                {
                        { playerID, 0, 0, 0, 0 },
                        { playerID, 0, 0, 0, 0 },
                        { playerID, 0, 0, 0, 0 },
                        { playerID, 0, 0, 0, 0 },
                        { playerID, 0, 0, 0, 0 }
                },
                {
                        { playerID, 0, 0 },
                        { playerID, playerID, 0 },
                        { playerID, playerID, 0 }
                },
                {
                        { 0, playerID, playerID },
                        { playerID, playerID, 0 },
                        { playerID, 0, 0 }
                },
                {
                        { playerID, playerID, 0 },
                        { playerID, 0, 0 },
                        { playerID, playerID, 0 }
                },
                {
                        { 0, playerID, playerID },
                        { playerID, playerID, 0 },
                        { 0, playerID, 0 }
                },
                {
                        { 0, playerID, 0 },
                        { playerID, playerID, playerID },
                        { 0, playerID, 0 }
                },
                {
                        { 0, playerID, 0, 0 },
                        { playerID, playerID, playerID, playerID },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                }
        };
    }

    public byte[][] getStone(int num){
        return stones[num];
    }
}
