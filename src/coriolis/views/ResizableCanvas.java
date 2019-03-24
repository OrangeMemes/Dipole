package coriolis.views;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;


/**
 * Created by maxvl on 19.05.2017.
 */
public abstract class ResizableCanvas extends Canvas {
    AnimationTimer timer;

    public ResizableCanvas() {
        // Redraw canvas when size changes.
        setFocusTraversable(true);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };
    }

    abstract void draw();

    public void setRedrawing (boolean redrawing) {
        if (redrawing)
            timer.start();
        else
            timer.stop();
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double minWidth(double height) {
        return 0;
    }

    @Override
    public double minHeight(double width) {
        return 0;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

}
