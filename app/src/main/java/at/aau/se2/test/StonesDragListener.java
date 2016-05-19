package at.aau.se2.test;

import android.app.Activity;
import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Markus on 22.04.2016.
 */
public final class StonesDragListener  implements View.OnDragListener {
    private Activity usedActivity = null;

    public StonesDragListener(Activity activity){
        usedActivity = activity;
    }

    @Override
    public boolean onDrag(View droppedAtView, DragEvent event) {
        Toast.makeText(usedActivity.getApplicationContext(), ("DRAGG"), Toast.LENGTH_SHORT).show();
        ImageView draggedButton = (ImageView) event.getLocalState();
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                Toast.makeText(usedActivity.getApplicationContext(), ("Drag started"), Toast.LENGTH_SHORT).show();
                // do nothing - but feel free to implement something
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Toast.makeText(usedActivity.getApplicationContext(), ("Drag entered"), Toast.LENGTH_SHORT).show();
                // do nothing - but feel free to implement something
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                Toast.makeText(usedActivity.getApplicationContext(), ("Drag exited"), Toast.LENGTH_SHORT).show();
                // do nothing - but feel free to implement something
                break;
            case DragEvent.ACTION_DROP:
                Toast.makeText(usedActivity.getApplicationContext(), ("Drag drop"), Toast.LENGTH_SHORT).show();
                // Dropped - do whatever needs to be done
                draggedButton.setVisibility(View.VISIBLE);
                    /* Get parent group, if you want to switch elements between groups e.g.*/
                //ViewGroup owner = (ViewGroup) draggedButton.getParent();
                //owner.removeView(draggedButton);

                ImageView droppedAtButton = (ImageView) droppedAtView;
                int droppedID = droppedAtButton.getId();
                int draggedID = draggedButton.getId();

                /*String txtTrap = usedActivity.getBaseContext().getResources().getString(R.string.game_field_with_trap);
                String txtNoTrap = usedActivity.getBaseContext().getResources().getString(R.string.game_field_wout_trap);

                if (droppedAtButton.getText() == txtTrap
                        || droppedAtButton.getText() == "?" + txtTrap
                        || droppedAtButton.getText() == txtNoTrap
                        || droppedAtButton.getText() == "?" + txtNoTrap){
                    Toast.makeText(usedActivity, "Move already made for drop destination!"
                            , Toast.LENGTH_SHORT).show();
                }
                else {
                    draggedButton.setBackgroundColor(Color.GREEN);

                    Toast.makeText(usedActivity, "Drop@:" + droppedID + ", Drag@:" + draggedID
                            , Toast.LENGTH_SHORT).show();

                    droppedAtButton.setBackgroundColor(Color.RED);
                    droppedAtButton.setText("?" + txtTrap); // still need to implement that switching functionality
                    draggedButton.setText("?" + txtNoTrap); // still need to implement that switching functionality
                    // Remove listener, as cheating has been already performed and thus needs to be disabled
                    draggedButton.setOnTouchListener(null);

                        /*
                        * Get parent group, if you want to switch elements between groups e.g.
                        * but be careful in respect to the type of element needed to cast
                        * */
                    //LinearLayout container = (LinearLayout) droppedAtView.getParent();
                    //container.addView(draggedButton);
                //}
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Toast.makeText(usedActivity.getApplicationContext(), ("Drag ended"), Toast.LENGTH_SHORT).show();
                draggedButton.setVisibility(View.VISIBLE);
            default:
                break;
        }
        return true;
    }
}
