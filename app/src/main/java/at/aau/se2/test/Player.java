package at.aau.se2.test;

public class Player {
    private byte p_id;
    private Blocks blocks;

    public Player(byte p_id){
        this.p_id = p_id;

        // create blocks-object for player
        this.blocks = new Blocks(p_id);
    }

    public byte getPlayerId(){
        return p_id;
    }

    public byte[][] getStone(int num){
        return blocks.getStone(num);
    }

    public String getPlayerColor(){
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

}
