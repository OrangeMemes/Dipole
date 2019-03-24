package coriolis;

import javafx.scene.layout.Region;

/**
 * Created by maxvl on 18.05.2017.
 */
public interface Activity {

    Region getTop();
    Region getBottom();
    String getLabel();
    void reset();
}
