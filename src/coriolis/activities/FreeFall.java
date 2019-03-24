package coriolis.activities;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import coriolis.views.ControlNode;
import coriolis.views.FallCanvas;
import coriolis.Activity;

/**
 * Created by maxvl on 26.05.2017.
 */
public class FreeFall implements Activity {
    private VBox canvasWrapper;
    private TilePane pane;
    private static FreeFall instance = new FreeFall();

    private final ControlNode heightControl;
    private final ControlNode latitudeControl;

    private final static double DEFAULT_LATITUDE = 0;
    private static final double DEFAULT_HEIGHT = 200;


    private FreeFall() {
        FallCanvas canvas = new FallCanvas();

        heightControl = new ControlNode("Высота башни, м", 5, 1000, DEFAULT_HEIGHT);
        canvas.towerHeightProperty().bind(heightControl.valueProperty());

        latitudeControl = new ControlNode("Широта, °", -90, 90, DEFAULT_LATITUDE);
        canvas.latitudeProperty().bind(latitudeControl.valueProperty());

        pane = new TilePane(Orientation.HORIZONTAL, heightControl, latitudeControl);
        pane.setTileAlignment(Pos.CENTER);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10));
        pane.setHgap(20);

        canvasWrapper = new VBox(canvas);
        canvas.widthProperty().bind(canvasWrapper.widthProperty());
        canvas.heightProperty().bind(canvasWrapper.heightProperty());
        canvasWrapper.setAlignment(Pos.CENTER);
    }

    public static FreeFall getInstance() {
        return instance;
    }

    @Override
    public Region getTop() {
        return canvasWrapper;
    }

    @Override
    public Region getBottom() {
        return pane;
    }

    @Override
    public String getLabel() {
        return "Свободное падение";
    }

    @Override
    public void reset() {
        heightControl.valueProperty().setValue(DEFAULT_HEIGHT);
        latitudeControl.valueProperty().setValue(DEFAULT_LATITUDE);
    }
}
