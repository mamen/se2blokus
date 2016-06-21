package at.aau.se2.test;

public class Blocks {
    private byte playerID;
    private byte[][][] stones;

    public Blocks(byte playernum){
        this.playerID = playernum;
        generateStonesMatrix();
    }

    /**
     * generates the stones as a byte-matrix
     * the playerID defines the color
     */
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

    /**
     * returns the stone according to the given number
     * @param num
     * @return
     */
    public byte[][] getStone(int num){
        return stones[num];
    }

}
