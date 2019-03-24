package coriolis.views;

import coriolis.ValuePrinterUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;


/**
 * Created by maxvl on 26.05.2017.
 */
public class CannonCanvas extends ResizableCanvas {

    public static final double G = 9.8;

    private static class Vector3d {
        public double x;
        public double y;
        public double z;

        public Vector3d(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        static Vector3d multiply(Vector3d v1, Vector3d v2) {
            return new Vector3d(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z,
                    v1.x * v2.y - v1.y * v2.x);
        }

        static Vector3d difference(Vector3d v1, Vector3d v2) {
            return new Vector3d(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
        }

        Vector3d sum(Vector3d v2) {
            return sum(this, v2);
        }

        Vector3d difference(Vector3d v2) {
            return difference(this, v2);
        }

        Vector3d multiply(Vector3d v2) {
            return multiply(this, v2);
        }

        Vector3d multiply(double num) {
            return multiply(this, num);
        }

        @Override
        public String toString() {
            return "{" + x + "; " + y + "; " + z + "}";
        }

        static Vector3d sum(Vector3d v1, Vector3d v2) {
            return new Vector3d(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
        }

        static Vector3d sum(Vector3d v1, double num) {
            return new Vector3d(v1.x + num, v1.y + num, v1.z + num);
        }

        static Vector3d multiply(Vector3d v, double num) {
            return new Vector3d(v.x * num, v.y * num, v.z * num);
        }

        double getLength() {
            return Math.sqrt(x * x + y * y + z * z);
        }
    }

    private final static double EARTH_ROTATION_SPEED = 7.2921158553E-5;

    private Image cannonImage;

    private DoubleProperty vx = new SimpleDoubleProperty();
    private DoubleProperty vy = new SimpleDoubleProperty();
    private DoubleProperty azimut = new SimpleDoubleProperty();
    private DoubleProperty latitude = new SimpleDoubleProperty();
    private DoubleProperty drawingDistance = new SimpleDoubleProperty();
    ChangeListener<Number> listener = (observable, oldValue, newValue) -> draw();

    public CannonCanvas() {
        super();
        drawingDistance.bind(Bindings.min(heightProperty(), widthProperty()).divide(2).subtract(25));

        cannonImage = new Image("coriolis/Cannon.png");
    }

    @Override
    public void setRedrawing(boolean redrawing) {
        if (redrawing) {
            vx.addListener(listener);
            vy.addListener(listener);
            azimut.addListener(listener);
            latitude.addListener(listener);
            heightProperty().addListener(listener);
            widthProperty().addListener(listener);
            draw();
        } else {
            vx.removeListener(listener);
            vy.removeListener(listener);
            azimut.removeListener(listener);
            latitude.removeListener(listener);
            heightProperty().removeListener(listener);
            widthProperty().removeListener(listener);
        }
    }

    @Override
    void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.setFill(Color.rgb(134, 181, 103));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setLineWidth(2);
        gc.setStroke(Color.rgb(138, 185, 211));
        gc.strokeLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        gc.strokeLine(getWidth() / 2, 0, getWidth() / 2, getHeight());

        Vector3d velocity = new Vector3d(vx.doubleValue() * Math.cos(azimut.doubleValue() / 180 * Math.PI),
                vx.doubleValue() * Math.sin(azimut.doubleValue() / 180 * Math.PI),
                vy.doubleValue());

        Vector3d omega = new Vector3d(EARTH_ROTATION_SPEED * Math.cos(latitude.doubleValue() / 180 * Math.PI),
                0,
                EARTH_ROTATION_SPEED * Math.sin(latitude.doubleValue() / 180 * Math.PI));

        double baseDistance = (vy.doubleValue() * 2 / G) * vx.doubleValue();
        Vector3d baseVector = new Vector3d(baseDistance * Math.cos(azimut.doubleValue() / 180 * Math.PI),
                baseDistance * Math.sin(azimut.doubleValue() / 180 * Math.PI), 0);

        double iterationStep = vy.doubleValue() * 2 / G / 2E4;

        Vector3d currentPosition = new Vector3d(0, 0, 0);
        Vector3d currentVelocity = new Vector3d(velocity.x, velocity.y, velocity.z);
        ArrayList<Vector3d> traectory = new ArrayList<>();
        Vector3d freeFallAcceleration = new Vector3d(0, 0, -G);
        if (iterationStep > 0)
            do {
                currentPosition = currentVelocity.multiply(iterationStep).sum(currentPosition);
                currentVelocity = omega.multiply(currentVelocity).multiply(2).sum(freeFallAcceleration)
                        .multiply(iterationStep).sum(currentVelocity);
                traectory.add(currentPosition);
            } while (currentPosition.z > 0);
        currentPosition.z = 0;

        gc.setFill(Color.rgb(245, 184, 149));
        for (int i = 0; i < traectory.size(); i++) {
            Vector3d vector3d = traectory.get(i);
            Vector3d part = baseVector.multiply(((double) i + 1) / traectory.size());
            vector3d = vector3d.difference(part).multiply(200).sum(part);
            fillOvalOnVector(vector3d, gc, baseVector, 2);
        }

        gc.setStroke(Color.rgb(175, 31, 36));
        gc.strokeLine(getWidth() / 2, getHeight() / 2,
                getWidth() / 2 + drawingDistance.doubleValue() * Math.sin(getAzimut() / 180 * Math.PI),
                getHeight() / 2 - drawingDistance.doubleValue() * Math.cos(getAzimut() / 180 * Math.PI));

        gc.setFill(gc.getStroke());
        gc.fillOval(getWidth() / 2 + drawingDistance.doubleValue() * Math.sin(getAzimut() / 180 * Math.PI) - 2,
                getHeight() / 2 - drawingDistance.doubleValue() * Math.cos(getAzimut() / 180 * Math.PI) - 2,
                4, 4);

        gc.setFill(Color.BLACK);
        fillOvalOnVector(currentPosition.difference(baseVector).multiply(200).sum(baseVector), gc, baseVector, 10);


        drawRotatedImage(gc, cannonImage, azimut.doubleValue(), (getWidth() - cannonImage.getWidth()) / 2,
                (getHeight() - cannonImage.getHeight()) / 2);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setFont(new Font("Roboto", 20));

        Vector3d deviation = Vector3d.difference(currentPosition, baseVector);


        gc.fillText("Перемещение: " + ValuePrinterUtil.printMetricValue(baseDistance),
                getWidth() / 2 - 15, getHeight());

        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Отклонение: " + ValuePrinterUtil.printMetricValue(deviation.getLength()),
                getWidth() / 2 + 15, getHeight());

    }

    private void fillOvalOnVector(Vector3d vector, GraphicsContext gc, Vector3d baseVector, double size) {

        double coefficient = drawingDistance.doubleValue() / baseVector.getLength();
        Vector3d corrected = new Vector3d(vector.x * coefficient,
                vector.y * coefficient, vector.z * coefficient);



        gc.fillOval(corrected.y + getWidth() / 2 - size / 2, -corrected.x + getHeight() / 2 - size / 2, size, size);
    }


    public double getVy() {
        return vy.get();
    }

    public DoubleProperty vyProperty() {
        return vy;
    }

    public double getAzimut() {
        return azimut.get();
    }

    public DoubleProperty azimutProperty() {
        return azimut;
    }

    public double getLatitude() {
        return latitude.get();
    }

    public DoubleProperty latitudeProperty() {
        return latitude;
    }

    private void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }


    private void drawRotatedImage(GraphicsContext gc, Image image, double angle, double tlpx, double tlpy) {
        gc.save(); // saves the current state on stack, including the current transform
        rotate(gc, angle, tlpx + image.getWidth() / 2, tlpy + image.getHeight() / 2);
        gc.drawImage(image, tlpx, tlpy);
        gc.restore(); // back to original state (before rotation)
    }

    public double getVx() {
        return vx.get();
    }

    public DoubleProperty vxProperty() {
        return vx;
    }
}
