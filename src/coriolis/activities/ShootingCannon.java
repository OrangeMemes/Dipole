package coriolis.activities;

import javafx.geometry.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import coriolis.views.CannonCanvas;
import coriolis.views.ControlNode;
import coriolis.Activity;

/**
 * Created by maxvl on 26.05.2017.
 */
public class ShootingCannon implements Activity {
    private static final double DEFAULT_VX = 75;
    private static final double DEFAULT_VY = 30;
    private static final double DEFAULT_ANGLE = 25;
    private static final double DEFAULT_LATITUDE = 60;

    private static ShootingCannon instance = new ShootingCannon();

    private VBox canvasWrapper;
    private CannonCanvas canvas;
    private TilePane pane;
    private final ControlNode vxControl;
    private final ControlNode vyControl;
    private final ControlNode angleControl;
    private final ControlNode latitudeControl;

    public static ShootingCannon getInstance() {
        return instance;
    }

    private ShootingCannon(){
        canvas = new CannonCanvas();

        vxControl = new ControlNode("Скорость по OX, м/с", 10, 100, DEFAULT_VX);
        canvas.vxProperty().bind(vxControl.valueProperty());

        vyControl = new ControlNode("Скорость по OY, м/с", 1, 100, DEFAULT_VY);
        canvas.vyProperty().bind(vyControl.valueProperty());

        angleControl = new ControlNode("Азимут, °", 0, 360, DEFAULT_ANGLE);
        canvas.azimutProperty().bind(angleControl.valueProperty());

        latitudeControl = new ControlNode("Широта, °", -90, 90, DEFAULT_LATITUDE);
        canvas.latitudeProperty().bind(latitudeControl.valueProperty());


        pane = new TilePane(Orientation.HORIZONTAL, vxControl, vyControl, angleControl, latitudeControl);
        pane.setTileAlignment(Pos.CENTER);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new javafx.geometry.Insets(10));
        pane.setHgap(20);

        canvasWrapper = new VBox(canvas);
        canvas.widthProperty().bind(canvasWrapper.widthProperty());
        canvas.heightProperty().bind(canvasWrapper.heightProperty());
        canvasWrapper.setAlignment(Pos.CENTER);
        canvas.setRedrawing(true);
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
        return "Артиллерийская стрельба";
    }

    @Override
    public void reset() {
        vxControl.valueProperty().setValue(DEFAULT_VX);
        vyControl.valueProperty().setValue(DEFAULT_VY);
        angleControl.valueProperty().setValue(DEFAULT_ANGLE);
        latitudeControl.valueProperty().setValue(DEFAULT_LATITUDE);
    }
}
