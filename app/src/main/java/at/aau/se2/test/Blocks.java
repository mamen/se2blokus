package at.aau.se2.test;

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

    public byte[][] rotate(int deg, int stoneID){
        byte[][] stone = getStone((byte)stoneID);
        byte[][] newStone = new byte[stone.length][stone[0].length];

        for(int i = 0; i < stone.length; i++){
            for(int j = 0; j < stone[i].length; j++){
                switch(deg){
                    case 90:
                        newStone[j][stone.length-i-1] = stone[i][j];
                        break;
                    case 180:
                        newStone[stone.length-i-1][stone.length-j-1] = stone[i][j];
                        break;
                    case 270:
                        newStone[stone.length-j-1][i] = stone[i][j];
                        break;
                }
            }
        }
        return newStone;
    }
}
