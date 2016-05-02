package at.aau.se2.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by KingSeybro on 27.04.2016.
 */
public class PeerScreen extends Activity implements View.OnClickListener {
    TextView textViewPeer;
    Button btnPeerPlay;
    Button btnPeerDont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peer_screen);
        initialise();
    }

    private void initialise() {
        textViewPeer = (TextView) findViewById(R.id.txtViewPeer);
        textViewPeer.setText("Hello Stranger");
        btnPeerPlay = (Button) findViewById(R.id.btnPlay);
        btnPeerDont = (Button) findViewById(R.id.btnDontPlay);
        btnPeerPlay.setOnClickListener(this);
        btnPeerDont.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlay:

                break;
            case R.id.btnDontPlay:

                break;
        }
    }
}
