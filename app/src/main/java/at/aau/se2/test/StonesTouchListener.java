package at.aau.se2.test;

import android.content.ClipData;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Markus on 22.04.2016.
 */
public final class StonesTouchListener implements View.OnTouchListener {
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            //ClipData data = ClipData.newPlainText("", "");
            //View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            Toast.makeText(view.getContext(), "Drag", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            Toast.makeText(view.getContext(), "Drop", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
