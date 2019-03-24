package coriolis.activities;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import coriolis.Activity;
import coriolis.views.ControlNode;
import coriolis.views.PendulumCanvas;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * Created by maxvl on 19.05.2017.
 */
public class FoucaultsPendulum implements Activity {
    private static final double DEFAULT_DIPOLE_SEPARATION = 0.03;
    private static final double DEFAULT_DIPOLE_CHARGE = 5;
    private static final double DEFAULT_PLACING_CHARGE = 7;
    private static final double DEFAULT_INTENSITY = 0.2;
    private static final double DEFAULT_ANGLE = 0;

    private static FoucaultsPendulum instance = new FoucaultsPendulum();

    private VBox canvasWrapper;
    private PendulumCanvas canvas;
    private TilePane pane;
    private final ControlNode dipoleSeparation;
    private final ControlNode dipoleCharge;
    private final ControlNode placingCharge;
    private final ControlNode constantIntensity;
    private final ControlNode constantAngle;
    private final JFXToggleButton toggle;

    public static FoucaultsPendulum getInstance() {
        return instance;
    }

    private FoucaultsPendulum() {
        canvas = new PendulumCanvas();

        dipoleSeparation = new ControlNode("Плечо диполя, м", 0.001, 0.05, DEFAULT_DIPOLE_SEPARATION);
        canvas.dipoleSeparaionProperty().bind(dipoleSeparation.valueProperty());

        dipoleCharge = new ControlNode("Заряд диполя, нКл", 1, 10, DEFAULT_DIPOLE_CHARGE);
        canvas.dipoleChargeProperty().bind(dipoleCharge.valueProperty().divide(1E9));

        placingCharge = new ControlNode("Помещаемый заряд, нКл", -10, 10, DEFAULT_PLACING_CHARGE);
        canvas.placingChargeProperty().bind(placingCharge.valueProperty().divide(1E9));

        constantIntensity = new ControlNode("Модуль напряженности, МН/Кл", 0, .25, DEFAULT_INTENSITY);
        canvas.constantIntensityProperty().bind(constantIntensity.valueProperty().multiply(1E6));

        constantAngle = new ControlNode("Угол между E и OX, °", 0, 360, DEFAULT_ANGLE);
        canvas.constantAngleProperty().bind(constantAngle.valueProperty());

        toggle = new JFXToggleButton();
        Label text = new Label("Однородное поле");
        text.getStyleClass().addAll("material-label");
        FlowPane togglepane = new FlowPane(Orientation.HORIZONTAL, text, toggle);
        togglepane.setPadding(new Insets(5));
        togglepane.setPrefWidth(250);
        togglepane.setMaxWidth(250);

        JFXButton clearButton = new JFXButton("Сбросить");
        clearButton.getStyleClass().addAll("pane-btns");
        clearButton.setOnMouseClicked(event -> reset());
        pane = new TilePane(Orientation.VERTICAL,
                clearButton,
                dipoleSeparation,
                dipoleCharge,
                togglepane,
                placingCharge
        );
        pane.setPrefHeight(Double.POSITIVE_INFINITY);

        toggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                pane.getChildren().removeAll(constantIntensity, constantAngle);
                pane.getChildren().addAll(placingCharge);
            } else if (!oldValue && newValue) {
                pane.getChildren().removeAll(placingCharge);
                pane.getChildren().addAll(constantIntensity, constantAngle);
            }

            if (oldValue ^ newValue)
                canvas.setConstant(newValue);
        });

        pane.setTileAlignment(Pos.CENTER);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10));

        canvasWrapper = new VBox(canvas);
        canvas.widthProperty().bind(canvasWrapper.widthProperty());
        canvas.heightProperty().bind(canvasWrapper.heightProperty());
        canvasWrapper.setAlignment(Pos.CENTER);
        canvasWrapper.parentProperty().addListener((observable, oldValue, newValue) ->
                canvas.setRedrawing(newValue != null));
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
        return "Маятник Фуко";
    }

    @Override
    public void reset() {
        dipoleSeparation.valueProperty().setValue(DEFAULT_DIPOLE_SEPARATION);
        placingCharge.valueProperty().setValue(DEFAULT_PLACING_CHARGE);
        dipoleCharge.valueProperty().setValue(DEFAULT_DIPOLE_CHARGE);
        constantIntensity.valueProperty().setValue(DEFAULT_INTENSITY);
        constantAngle.valueProperty().setValue(DEFAULT_ANGLE);
        toggle.setSelected(false);
        canvas.reset();
    }

}
