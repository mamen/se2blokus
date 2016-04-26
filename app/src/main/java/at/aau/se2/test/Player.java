package at.aau.se2.test;

import android.graphics.Color;

/**
 * Created by Markus on 22.04.2016.
 */
public class Player {
    private Color p_color;
    private byte p_id;

    public Player(byte p_id, Color p_color){
        this.p_color = p_color;
        this.p_id = p_id;
    }

    public byte getPlayerId(){
        return p_id;
    }
    public Color getPlayerColor(){
        return p_color;
    }

}
