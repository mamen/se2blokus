package at.aau.se2.test;

import android.graphics.Point;
import android.view.View;

public class OwnDragShadowBuilder extends View.DragShadowBuilder {

    int touchX, touchY;

    public OwnDragShadowBuilder(View v, int touchX, int touchY) {
        super(v);
        this.touchX = touchX;
        this.touchY = touchY;
    }

    @Override
    public void onProvideShadowMetrics (Point shadowSize, Point shadowTouchPoint) {
        super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
        shadowTouchPoint.set(touchX, touchY);
    }
}
