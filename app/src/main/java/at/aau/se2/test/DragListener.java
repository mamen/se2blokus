package at.aau.se2.test;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DragListener implements View.OnDragListener {

    byte index_i = -1;
    byte index_j = -1;
    ImageButton accept;
    ImageButton cancel;
    ImageButton transpose;
    ImageButton move_up;
    ImageButton move_right;
    ImageButton move_down;
    ImageButton move_left;
    ImageView draggedImage;
    boolean dragged = false;
    boolean drawn;
    private FullscreenActivity context;

    public DragListener(FullscreenActivity c){
        this.context = c;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: //LongClick startet den Drag, Bild unsichtbar machen
                this.context.testView.setVisibility(View.INVISIBLE);
                dragged = false;
                Log.d("DragStart", "Started");
                break;
            case DragEvent.ACTION_DRAG_ENTERED: //Im Spielfeld
                dragged = true;
                Log.d("DragEntered", "Entered");
                break;
            case DragEvent.ACTION_DRAG_EXITED: //Außerhalb vom Spielfeld
                dragged = false;
                Log.d("DragExited", "Exited");
                break;
            case DragEvent.ACTION_DRAG_ENDED: //Wird geworfen, egal wo der Drag beendet wird
                if (dragged) {
                    this.context.testView.setVisibility(View.INVISIBLE);
                } else {
                    this.context.testView.setVisibility(View.VISIBLE);
                    this.context.elementFinished = true;
                }
                Log.d("DragEnded", "Ended");
                break;
            case DragEvent.ACTION_DROP: //Drop wird nur geworfen, wenn man im Spielfeld dropped

                this.context.transposeCount = 0; //Neuer Stein, Zähler zurücksetzen
                draggedImage = (ImageView) event.getLocalState();
                byte[][] stone = this.context.player.getStone(this.context.selectedBlockID - 1);
                final int stoneLength = stone.length;

                // Indexberechnung, wo der Stein platziert werden soll
                // Indexmanipulation, abhängig vom gewählten Stein
                index_i = (byte) (Math.floor(event.getX() / Math.floor(v.getWidth() / 20)) - this.context.manipulateX(this.context.selectedBlockID - 1));
                index_j = (byte) (Math.floor(event.getY() / Math.floor(v.getHeight() / 20)) - this.context.manipulateY(this.context.selectedBlockID - 1));

                //außerhalb des gültigen bereichs platziert
                if (index_i < 0 || index_i > 19) {
                    if (index_i < 0) {
                        index_i = 0;
                    } else {
                        index_i = 19;
                    }
                }

                if (index_j < 0 || index_j > 19) {
                    if (index_j < 0) {
                        index_j = 0;
                    } else {
                        index_j = 19;
                    }
                }

                //Preview erfolgreich gezeichnet?
                drawn = this.context.drawStone(index_i, index_j);

                //Bei left und up Probleme, das Stein nur richtig gedreht nach oben/links kann (Nullzeilen und Nullspalten)

                initializeMovementButtons(stoneLength);

                // Accept-Button
                if (drawn) {
                    initializeActionButtons(v);
                    // Buttons zum View hinzufügen
                    if (this.context.isYourPlacementValid(index_i, index_j)) { //Ungültiger Zug, braucht Accept Button nicht
                        this.context.fullscreenLayout.addView(accept);
                    }
                    this.context.gl.addViews(this.context.fullscreenLayout, cancel, transpose, move_up, move_right, move_down, move_left);
                } else {
                    //Preview wurde nicht gezeichnet
                    this.context.elementFinished = true;
                    dragged = false;
                    this.context.testView.setVisibility(View.VISIBLE);
                }

                break;
            default:
                break;
        }
        return true;
    }

    /**
     * initializes the movement buttons after the drag
     * @param stoneLength length of the stone
     */
    public void initializeMovementButtons(final int stoneLength){

        // Movement Buttons müssen try-catch, da nicht ersichtlich ist,
        // ob bei neuem moven, der Accept Button noch da ist
        // Wenn keine Preview gezeichnet wurde, muss der alte Zustand wiederhergestellt werden
        move_up = new ImageButton(this.context);
        move_up.setImageResource(R.drawable.move_up);

        move_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.restore(index_i, index_j);
                if (context.drawStone(index_i, (index_j - 1 < 0 - stoneLength) ? (0 - stoneLength) : --index_j)) {
                    try {
                        if (context.isYourPlacementValid(index_i, index_j)) {
                            context.fullscreenLayout.addView(accept);
                        } else {
                            context.fullscreenLayout.removeView(accept);
                        }
                    } catch (IllegalStateException e) {
                        Log.e("Error", e.getMessage());
                        //throw new IllegalStateException();
                    }
                } else {
                    context.restore(index_i, ++index_j);
                    context.drawStone(index_i, index_j);
                }
            }
        });

        move_down = new ImageButton(this.context);
        move_down.setImageResource(R.drawable.move_down);

        move_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.restore(index_i, index_j);
                if (context.drawStone(index_i, (index_j + 1 > 20) ? 20 : ++index_j)) {
                    try {
                        if (context.isYourPlacementValid(index_i, index_j)) {
                            context.fullscreenLayout.addView(accept);
                        } else {
                            context.fullscreenLayout.removeView(accept);
                        }
                    } catch (IllegalStateException e) {
                        Log.e("Error", e.getMessage());
                        //throw new IllegalStateException();
                    }
                } else {
                    context.restore(index_i, --index_j);
                    context.drawStone(index_i, index_j);
                }
            }
        });

        move_left = new ImageButton(this.context);
        move_left.setImageResource(R.drawable.move_left);

        move_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.restore(index_i, index_j);
                if (context.drawStone((index_i - 1 < 0 - stoneLength) ? (0 - stoneLength) : --index_i, index_j)) {
                    try {
                        if (context.isYourPlacementValid(index_i, index_j)) {
                            context.fullscreenLayout.addView(accept);
                        } else {
                            context.fullscreenLayout.removeView(accept);
                        }
                    } catch (IllegalStateException e) {
                        Log.e("Error", e.getMessage());
                    }
                } else {
                    context.restore(++index_i, index_j);
                    context.drawStone(index_i, index_j);
                    //cancel.performClick();
                }
            }
        });

        move_right = new ImageButton(this.context);
        move_right.setImageResource(R.drawable.move_right);

        move_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.restore(index_i, index_j);
                if (context.drawStone((index_i + 1 > 20) ? 20 : ++index_i, index_j)) {
                    try {
                        if (context.isYourPlacementValid(index_i, index_j)) {
                            context.fullscreenLayout.addView(accept);
                        } else {
                            context.fullscreenLayout.removeView(accept);
                        }
                    } catch (IllegalStateException e) {
                        Log.e("Error", e.getMessage());
                        //throw new IllegalStateException();
                    }
                } else {
                    context.restore(--index_i, index_j);
                    context.drawStone(index_i, index_j);
                }
            }
        });


        RelativeLayout.LayoutParams paramsMoveUp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsMoveRight = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsMoveDown = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams paramsMoveLeft = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        paramsMoveUp.setMargins((this.context.getScreenWidth() / 2) - ((move_up.getDrawable().getMinimumWidth() + move_up.getPaddingLeft() + move_up.getPaddingRight()) / 2), 0, 0, 0);

        paramsMoveRight.setMargins(this.context.gameBoardLayout.getWidth() - move_right.getPaddingLeft() - move_right.getPaddingRight() - move_right.getDrawable().getMinimumWidth(), (this.context.gameBoardLayout.getHeight() / 2 - move_right.getPaddingTop() - move_right.getDrawable().getMinimumHeight() / 2), 0, 0);

        paramsMoveDown.setMargins((this.context.getScreenWidth() / 2) - ((move_down.getDrawable().getMinimumWidth() + move_down.getPaddingLeft() + move_down.getPaddingRight()) / 2), (this.context.gameBoardLayout.getHeight() - move_down.getPaddingTop() - move_down.getPaddingBottom() - move_up.getDrawable().getMinimumHeight()), 0, 0);

        paramsMoveLeft.setMargins(0, (this.context.gameBoardLayout.getHeight() / 2 - move_left.getPaddingTop() - move_left.getDrawable().getMinimumHeight() / 2), 0, 0);

        move_up.setLayoutParams(paramsMoveUp);
        move_right.setLayoutParams(paramsMoveRight);
        move_down.setLayoutParams(paramsMoveDown);
        move_left.setLayoutParams(paramsMoveLeft);

        move_up.setAlpha(0.5f);
        move_right.setAlpha(0.5f);
        move_down.setAlpha(0.5f);
        move_left.setAlpha(0.5f);
    }

    /**
     * initializes the action buttons after the drag (place, cancel, rotate)
     * @param v the dragged view
     */
    public void initializeActionButtons(View v){
        RelativeLayout.LayoutParams paramsAccept = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        accept = new ImageButton(this.context);
        accept.setImageResource(R.drawable.checkmark);
        paramsAccept.setMargins(0, v.getHeight() + accept.getHeight(), 0, 0);
        accept.setLayoutParams(paramsAccept);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Platzieren nicht möglich - Preview wieder löschen und Bild im BlockDrawer wieder anzeigen
                if (!context.isYourPlacementValid(index_i, index_j)) {
                    //vibrate(500);
                    context.restore(index_i, index_j);
                } else {
                    context.placeSound.start(); //Sound abspielen
                    context.testView.setVisibility(View.INVISIBLE); //Müsste unnötig sein
                    byte[][] b = context.player.getStone(context.selectedBlockID - 1);
                    for (int a = 0; a < context.transposeCount; a++) { //Stein drehen, je nachdem wie oft der Button gedrückt wurde
                        b = context.gl.rotate(b);
                    }
                    context.placeIt(b, index_i, index_j); //Wirkliches Plazieren vom Stein
                    context.updatePoints();
                }
                context.gl.removeViews(context.fullscreenLayout, accept, cancel, transpose, move_up, move_right, move_down, move_left);
                context.elementFinished = true; //Nächster Stein kann geLongClicked werden
            }
        });


        // Cancel-Button
        RelativeLayout.LayoutParams paramsCancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cancel = new ImageButton(this.context);
        cancel.setImageResource(R.drawable.cancel);
        paramsCancel.setMargins(Math.round(v.getWidth() / 3), v.getHeight() + cancel.getHeight(), 0, 0);
        cancel.setLayoutParams(paramsCancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.gl.removeViews(context.fullscreenLayout, accept, cancel, transpose, move_up, move_right, move_down, move_left);
                context.testView.setVisibility(View.VISIBLE);
                context.elementFinished = true;
                context.restore(index_i, index_j);
                //boardToLog();
            }
        });

        RelativeLayout.LayoutParams paramsTranspose = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        transpose = new ImageButton(this.context);
        transpose.setImageResource(R.drawable.transpose);
        paramsTranspose.setMargins(Math.round((2 * v.getWidth()) / 3), v.getHeight() + transpose.getHeight(), 0, 0);
        transpose.setLayoutParams(paramsTranspose);

        //Board wiederherstellen, Stein drehen und neue Preview zeichnen, Accept Button nur bei gültigem Zug anzeigen
        //Try-Catch, da ich Accept nicht zweimal adden darf
        transpose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.restore(index_i, index_j);
                context.transposeCount = (context.transposeCount + 1) % 4;
                drawn = context.drawStone(index_i, index_j);
                if (drawn) {
                    if (!context.isYourPlacementValid(index_i, index_j)) {
                        context.fullscreenLayout.removeView(accept);
                    } else {
                        try {
                            context.fullscreenLayout.addView(accept);
                        } catch (IllegalStateException e) {
                            Log.e("Error", e.getMessage());
                            //throw new IllegalStateException();
                        }
                    }
                } else {
                    Toast.makeText(context, "I'm sorry, but I can't draw this", Toast.LENGTH_SHORT).show();
                    cancel.performClick();
                }
            }
        });
    }
}
