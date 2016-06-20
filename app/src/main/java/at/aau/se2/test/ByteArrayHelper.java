package at.aau.se2.test;

/**
 * Helper-Class to handle the transformation of a two-dimensional byte array (stone) + additional information
 * for the message exchange. Also has the methods to fetch the information back from the transformed
 * one dimensional byte array.
 */
public class ByteArrayHelper {

    private int color;
    private int idx;
    private int idy;
    private byte[][] byteStone;

    /**
     * Creates a new byte array with each one row and column in addition.
     * This is for filling the other information (see parameters 2-4) and always be able to fetch it with the same algorithm.
     *
     * @param b stone bytearray
     * @param coordx x-coordinate of the stone
     * @param coordy y-coordinate of the stone
     * @param id id (=Color) of the stone
     * @return one-dimensional bytearray for sending messages
     */
    public byte[] createNewByteArray(byte[][] b, int coordx, int coordy, byte id) {
        byte[][] newByte = new byte[b.length + 1][b.length + 1];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b.length; j++) {
                newByte[i][j] = b[i][j];
            }
        }

        newByte[b.length][b.length - 2] = id;
        newByte[b.length][b.length - 1] = (byte) coordx;
        newByte[b.length][b.length] = (byte) coordy;

        return convertToOneDimension(newByte);
    }

    /**
     * Converting a two-dimensional bytearray into one dimension.
     * Used in the createNewByteArray - method.
     *
     * @param b two dimensional bytearray
     * @return
     */
    public byte[] convertToOneDimension(byte[][] b) {
        int count = 0;
        byte[] oneDim = new byte[b.length * b.length];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b.length; j++) {
                oneDim[count] = b[i][j];
                count++;
            }
        }
        return oneDim;
    }

    /**
     * Fetch the information from a one-dimensional bytearray received through a message.
     * bytearray "lays" in the first b.length-1 rows and columns, whereas the other information
     * is found in the last 3 fields of the array in the last row.
     *
     * @param b bytearray
     */
    public void fetchInformationFromByteArray (byte[] b) {
        int stoneDim = (int) Math.sqrt(b.length) - 1;
        int dim = 0;
        byte[][] stone = new byte[stoneDim][stoneDim];
        int counter = 0;
        for (int i = 0; i < b.length - stoneDim - 1; i++) {

            if (dim < stoneDim) {
                stone[counter][dim] = b[i];
                dim++;
            } else {
                dim = 0;
                counter++;

            }
        }

        this.byteStone = stone;
        this.color = b[b.length - 3];
        this.idy = b[b.length - 2];
        this.idx = b[b.length - 1];
    }

    public int getColor(){
        return this.color;
    }

    public int getIdx(){
        return this.idx;
    }

    public int getIdy(){
        return this.idy;
    }

    public byte[][] getByteStone(){
        return this.byteStone;
    }

}
