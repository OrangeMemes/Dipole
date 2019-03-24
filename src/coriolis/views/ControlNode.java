package coriolis.views;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.util.Locale;


/**
 * Created by maxvl on 24.05.2017.
 */
public class ControlNode extends GridPane {
    private DoubleProperty value;


    public DoubleProperty valueProperty() {
        return value;
    }

    public ControlNode(String label, double sliderLeftBound, double sliderRightBound, double defaultValue) {
        super();

        this.setPrefWidth(250);
        value = new SimpleDoubleProperty(defaultValue);
        Label text = new Label(label);
        text.getStyleClass().addAll("material-label");
        JFXSlider slider = new JFXSlider(sliderLeftBound, sliderRightBound, defaultValue);
        slider.valueProperty().bindBidirectional(value);
        JFXTextField field = new JFXTextField(String.valueOf(defaultValue));
        field.textProperty().bindBidirectional(value, new NumberStringConverter(Locale.ENGLISH) {
            @Override
            public Number fromString(String value) {
                Number parsed;
                try {
                    Double.parseDouble(value);
                    parsed = super.fromString(value);
                } catch (Exception e) {
                    parsed = 0;
                }
                return Math.min(Math.max(parsed.doubleValue(), sliderLeftBound), sliderRightBound);
            }

            @Override
            public String toString(Number value) {
                return String.valueOf(value.doubleValue());
            }
        });
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            StringBuilder tmp = new StringBuilder(newValue);
            for (int i = 0; i < tmp.length(); i++) {
                char c = tmp.charAt(i);
                if (c == 'e')
                    tmp.setCharAt(i, 'E');
                else if (c != '-' && c != 'E' && (c < '0' || c > '9') && c != '.') {
                    tmp.setCharAt(i, '.');
                }
            }
            if (!newValue.equals(tmp.toString()))
                ((StringProperty) observable).setValue(tmp.toString());
        });
        slider.setFocusTraversable(false);

        add(text, 0, 0, 2, 1);
        add(slider, 0, 1);
        add(new VBox(field), 1, 1);

        getStyleClass().add("control-node");
        getChildren().forEach(node -> node.getStyleClass().add("control-node-child"));

        getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints());
        getColumnConstraints().get(0).setHgrow(Priority.ALWAYS);
        getColumnConstraints().get(1).setPrefWidth(55);
    }

}
