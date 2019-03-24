package coriolis.views;

import coriolis.ValuePrinterUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Created by maxvl on 26.05.2017.
 */
public class FallCanvas extends ResizableCanvas {
    private final static double EARTH_ROTATION_SPEED = 7.2921158553E-5;

    public double getTowerHeight() {
        return towerHeight.get();
    }

    public DoubleProperty towerHeightProperty() {
        return towerHeight;
    }

    private DoubleProperty towerHeight = new SimpleDoubleProperty();

    public double getLatitude() {
        return latitude.get();
    }

    public DoubleProperty latitudeProperty() {
        return latitude;
    }

    private DoubleProperty latitude = new SimpleDoubleProperty();

    private Image towerWall;
    private Image towerTop;

    public FallCanvas() {
        super();
        ChangeListener<Number> listener = (observable, oldValue, newValue) -> draw();
        towerWall = new Image("coriolis/Tower_wall.png");
        towerTop = new Image("coriolis/Tower_top.png");
        towerHeight.addListener(listener);
        latitude.addListener(listener);
        heightProperty().addListener(listener);
        widthProperty().addListener(listener);
    }

    @Override
    void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setFill(Color.rgb(138, 185, 211));
        gc.fillRect(0, 0, getWidth(), getHeight() * 0.8);
        gc.setFill(Color.rgb(134, 181, 103));
        gc.fillRect(0, getHeight() * 0.8, getWidth(), getHeight() * 0.2);
        int floorsCapacity = (int) Math.round((getHeight() * 0.8 - towerTop.getHeight()) / towerWall.getHeight());
        int i;
        for (i = 0; i < floorsCapacity - 1; i++) {
            gc.drawImage(towerWall, (getWidth() - towerWall.getWidth()) / 2,
                    getHeight() * 0.8 - towerWall.getHeight() * (i + 1));
        }
        double startPositionY = getHeight() * 0.8 - towerWall.getHeight() * i;
        gc.drawImage(towerTop, (getWidth() - towerTop.getWidth()) / 2,
                startPositionY - towerTop.getHeight());
        startPositionY -= 18;

        gc.setFill(Color.rgb(245, 184, 149));
        for (double j = startPositionY; j <= getHeight() * 0.8 - 10; j += 7) {
            gc.fillOval(getWidth() / 2 + 50 * getDeviation(j - startPositionY)
                    * Math.sqrt(getTowerHeight()) - 1.5, j - 1.5, 3, 3);
        }

        gc.setFill(Color.BLACK);
        gc.fillOval(getWidth() / 2 + 50 * getDeviation(getHeight() * 0.8 - startPositionY - 10)
                        * Math.sqrt(getTowerHeight()) - 10,
                getHeight() * 0.8 - 20, 20, 20);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(new Font("Roboto", 20));

        String text = "Отклонение: " + ValuePrinterUtil.printMetricValue(getDeviation(getTowerHeight()));


        gc.fillText(text, getWidth()/2, getHeight()*0.8+20);
    }

    private double getDeviation(double height) {
        return 2. / 3 * EARTH_ROTATION_SPEED * Math.cos(latitude.doubleValue() / 180 * Math.PI)
                * Math.sqrt(2 * Math.pow(height, 3) / 9.8);
    }
}
