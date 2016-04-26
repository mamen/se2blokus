package at.aau.se2.test;

/**
 * Created by Markus on 22.04.2016.
 */
public class Stones {

    private byte x;

    private byte[][][] stones;

    public Stones(int playernum){
        this.x = (byte)playernum;
        generateStonesMatrix();
    }

    private void generateStonesMatrix(){
        stones = new byte[][][]{
                {
                        { x, 0 },
                        { 0, 0 }
                },
                {
                        { x, x },
                        { 0, 0 }
                },
                {
                        { x, x },
                        { 0, x }
                },
                {
                        { x, x, x },
                        { 0, 0, 0 },
                        { 0, 0, 0 }
                },
                {
                        { x, x },
                        { x, x }
                },
                {
                        { 0, x, 0 },
                        { x, x, x } ,
                        { 0, 0, 0 }
                },
                {
                        { x, x, x, x },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                },
                {
                        { 0, 0, x },
                        { x, x, x },
                        { 0, 0, 0 }
                },
                {
                        { 0, x, x },
                        { x, x, 0 },
                        { 0, 0, 0 }
                },
                {
                        { x, 0, 0, 0 },
                        { x, x, x, x },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                },
                {
                        { 0, x, 0 },
                        { 0, x, 0 },
                        { x, x, x }
                },
                {
                        { x, 0, 0 },
                        { x, 0, 0 },
                        { x, x, x }
                },
                {
                        { 0, x, x, x },
                        { x, x, 0, 0 },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                },
                {
                        { 0, 0, x },
                        { x, x, x },
                        { x, 0, 0 }
                },
                {
                        { x, 0, 0, 0, 0 },
                        { x, 0, 0, 0, 0 },
                        { x, 0, 0, 0, 0 },
                        { x, 0, 0, 0, 0 },
                        { x, 0, 0, 0, 0 }
                },
                {
                        { x, 0, 0 },
                        { x, x, 0 },
                        { x, x, 0 }
                },
                {
                        { 0, x, x },
                        { x, x, 0 },
                        { x, 0, 0 }
                },
                {
                        { x, x, 0 },
                        { x, 0, 0 },
                        { x, x, 0 }
                },
                {
                        { 0, x, x },
                        { x, x, 0 },
                        { 0, x, 0 }
                },
                {
                        { 0, x, 0 },
                        { x, x, x },
                        { 0, x, 0 }
                },
                {
                        { 0, x, 0, 0 },
                        { x, x, x, x },
                        { 0, 0, 0, 0 },
                        { 0, 0, 0, 0 }
                }
        };
    }

    public byte[][] getStone(int num){
        return stones[num];
    }
}
