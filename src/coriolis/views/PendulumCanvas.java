package coriolis.views;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by maxvl on 19.05.2017.
 */
public class PendulumCanvas extends ResizableCanvas {

    public static final int TOOLTIP_GAP = 5;
    public static final int TOOLTIP_PADDING = 10;
    public static final double CHARGE_DEGREE = 1E9;
    private Thread thread;
    private final Runnable calcRunnable = () -> {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(0, 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            calculate();
        }
    };

    public double getConstantIntensity() {
        return constantIntensity.get();
    }

    public DoubleProperty constantIntensityProperty() {
        return constantIntensity;
    }

    public void setConstantIntensity(double constantIntensity) {
        this.constantIntensity.set(constantIntensity);
    }

    public double getConstantAngle() {
        return constantAngle.get();
    }

    public DoubleProperty constantAngleProperty() {
        return constantAngle;
    }

    public void setConstantAngle(double constantAngle) {
        this.constantAngle.set(constantAngle);
    }

    private static class Charge {
        final double charge;
        final double x;
        final double y;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Charge && ((Charge) obj).charge == charge && ((Charge) obj).x == x && ((Charge) obj).y == y;
        }

        public Charge(double charge, double x, double y) {
            this.charge = charge;
            this.x = x;
            this.y = y;
        }

        public double getCharge() {
            return charge;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public String toString() {
            return "X: " + scientificFormatNumber(x) + " м\nY: " + scientificFormatNumber(y)
                    + " м\nЗаряд: " + scientificFormatNumber(charge) + " Кл\n\nКликните, чтобы удалить.";
        }
    }


    private LinkedList<Pair<Double, Double>> dotList = new LinkedList<>();
    private double dipoleRotation = 0;
    private double dipoleRotationVelocity = 0;
    private Point2D.Double dipolePosition = new Point2D.Double(0.5, 0.5);
    private Point2D.Double dipoleVelocity = new Point2D.Double();
    private DoubleProperty mass = new SimpleDoubleProperty(0.0003);
    private double baseSize = 1;
    private DoubleProperty sizeX = new SimpleDoubleProperty(1);
    private DoubleProperty sizeY = new SimpleDoubleProperty(1);
    private DoubleProperty drawingAmplitude = new SimpleDoubleProperty();
    private DoubleProperty placingCharge = new SimpleDoubleProperty();
    private DoubleProperty dipoleCharge = new SimpleDoubleProperty();
    private DoubleProperty dipoleSeparation = new SimpleDoubleProperty();
    private DoubleProperty constantIntensity = new SimpleDoubleProperty();
    private DoubleProperty constantAngle = new SimpleDoubleProperty();
    private ArrayList<Charge> charges = new ArrayList<>();
    private long lastCalculated = System.currentTimeMillis();
    private double mouseX = 0;
    private double mouseY = 0;
    private boolean constant = false;


    public PendulumCanvas() {
        super();
        this.setOnMouseClicked(event -> {
            if (constant)
                return;

            AtomicReference<Charge> clickedCharge = new AtomicReference<>(null);
            charges.forEach(charge -> {
                double diameter = Math.sqrt(Math.abs(charge.charge * CHARGE_DEGREE)) * 8;
                if ((new Point2D.Double(charge.x * getWidth() / getSizeX(), charge.y * getHeight() / getSizeY()).distance(mouseX, mouseY) <= diameter / 2))
                    clickedCharge.set(charge);
            });
            if (clickedCharge.get() != null) {
                charges.remove(clickedCharge.get());
            } else {
                if (Math.abs(placingCharge.doubleValue()) > 1.6E-19 && Math.abs(placingCharge.doubleValue()) < 1E-6)
                    charges.add(new Charge(placingCharge.doubleValue(), event.getX() / getWidth() * getSizeX(), event.getY() / getHeight() * getSizeY()));
            }
        });
        thread = new Thread(calcRunnable);
        thread.setDaemon(true);
        setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
    }

    private double getSizeY() {
        return sizeY.doubleValue();
    }

    private double getSizeX() {
        return sizeX.doubleValue();
    }

    @Override
    void draw() {
        if (Double.isNaN(dipolePosition.x) || Double.isNaN(dipolePosition.y))
            dipolePosition.setLocation(sizeX.doubleValue() / 2, sizeY.doubleValue() / 2);

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setFont(Font.font("Roboto", 12));
        gc.setTextBaseline(VPos.TOP);
        gc.setFill(Color.rgb(0, 0, 0, .5));
        if (!constant)
            gc.fillText("Клик по пустому месту добавит заряд.", 2, getHeight() - 26);
        gc.fillText("Можно навести курсор на заряд или диполь.", 2, getHeight() - 14);
        if (!thread.isAlive()) {
            gc.setFill(Color.rgb(200, 0, 50, .7));
            gc.setFont(Font.font("Roboto", 14));
            gc.fillText("Произошло столкновение диполя с точечным зарядом. \nДвижение остановлено\nКнопка «Сбросить» запустит демонстрацию снова.", 2, 2);
        }
        AtomicReference<Charge> hoveredCharge = new AtomicReference<>(null);
        charges.forEach(charge -> {
            int blue = 128 + (int) (128 * charge.charge * CHARGE_DEGREE);
            if (blue > 255)
                blue = 255;
            if (blue < 0)
                blue = 0;
            gc.setFill(Color.rgb(80, blue, 180));
            double diameter = Math.sqrt(Math.abs(charge.charge * CHARGE_DEGREE)) * 8;
            if ((new Point2D.Double(charge.x * getWidth() / getSizeX(), charge.y * getHeight() / getSizeY()).distance(mouseX, mouseY) <= diameter / 2))
                hoveredCharge.set(charge);
            gc.fillOval(
                    charge.x * getWidth() / getSizeX() - diameter / 2, charge.y * getHeight() / getSizeY() - diameter / 2,
                    diameter, diameter);

        });


        double dipoleDiameter = Math.sqrt(dipoleCharge.doubleValue() * CHARGE_DEGREE) * 8;
        gc.setFill(Color.RED);
        gc.fillOval(dipolePosition.x * getWidth() / getSizeX() + getDipoleSeparation() * getWidth() / getSizeX() / 2 * Math.cos(dipoleRotation) - dipoleDiameter / 2,
                dipolePosition.y * getHeight() / getSizeY() - getDipoleSeparation() * getHeight() / getSizeY() / 2 * Math.sin(dipoleRotation) - dipoleDiameter / 2,
                dipoleDiameter, dipoleDiameter);
        gc.setFill(Color.BLUE);
        gc.fillOval(dipolePosition.x * getWidth() / getSizeX() - getDipoleSeparation() * getWidth() / getSizeX() / 2 * Math.cos(dipoleRotation) - dipoleDiameter / 2,
                dipolePosition.y * getHeight() / getSizeY() + getDipoleSeparation() * getHeight() / getSizeY() / 2 * Math.sin(dipoleRotation) - dipoleDiameter / 2,
                dipoleDiameter, dipoleDiameter);

        if (hoveredCharge.get() != null) {
            drawTooltip(gc, hoveredCharge.get().toString());
        } else if (dipolePosition.distance(mouseX * getSizeX() / getWidth(), mouseY * getSizeY() / getHeight()) <= dipoleSeparation.doubleValue()+0.02) {
            Point2D.Double forcePos = getForcePos();
            Point2D.Double forceNeg = getForceNeg();
            drawTooltip(gc, "X: " + scientificFormatNumber(dipolePosition.x)
                    + " м\nY: " + scientificFormatNumber(dipolePosition.y) + " м\n\nУгол поворота: " +
                    Math.round(Math.toDegrees(dipoleRotation)) + "°\n\nМасса: 0.3 г\nСила, действующая на\nположительный: "
                    + scientificFormatNumber(forcePos.distance(0, 0)) +
                    " Н\nотрицательный: "
                    + scientificFormatNumber(forceNeg.distance(0, 0)) + " Н");

            drawArrows(gc, forcePos, forceNeg);
        }

        if (constant)
            drawArrows(gc, getForcePos(), getForceNeg());
    }

    private void drawArrows(GraphicsContext gc, Point2D.Double forcePos, Point2D.Double forceNeg) {
        forcePos = new Point2D.Double((forcePos.x < 0 ? -1 : 1) * Math.sqrt(Math.abs(forcePos.x)), (forcePos.y < 0 ? -1 : 1) * Math.sqrt(Math.abs(forcePos.y)));
        forceNeg = new Point2D.Double((forceNeg.x < 0 ? -1 : 1) * Math.sqrt(Math.abs(forceNeg.x)), (forceNeg.y < 0 ? -1 : 1) * Math.sqrt(Math.abs(forceNeg.y)));
        gc.setStroke(Color.BLACK);
        double xpos = (dipolePosition.getX() + getDipoleSeparation() / 2 * Math.cos(dipoleRotation)) / getSizeX() * getWidth();
        double ypos = (dipolePosition.getY() - getDipoleSeparation() / 2 * Math.sin(dipoleRotation)) / getSizeY() * getHeight();
        int enlagrement = 10;
        drawArrow(gc, xpos, ypos,
                xpos + forcePos.x * getWidth() / getSizeX() * enlagrement, ypos + forcePos.y * getHeight() / getSizeY() * enlagrement);
        double xneg = (dipolePosition.getX() - getDipoleSeparation() / 2 * Math.cos(dipoleRotation)) / getSizeX() * getWidth();
        double yneg = (dipolePosition.getY() + getDipoleSeparation() / 2 * Math.sin(dipoleRotation)) / getSizeY() * getHeight();
        drawArrow(gc, xneg, yneg,
                xneg - forceNeg.x * getWidth() / getSizeX() * enlagrement, yneg - forceNeg.y * getHeight() / getSizeY() * enlagrement);
    }

    private void drawTooltip(GraphicsContext gc, String textToDisplay) {
        final Text text = new Text(textToDisplay);
        Font font = Font.font("Roboto", 14);
        text.setFont(font);

        double width = text.getLayoutBounds().getWidth();
        double height = text.getLayoutBounds().getHeight();
        gc.setFont(font);
        gc.setFill(Color.rgb(0, 0, 0, .5));
        gc.fillRect(
                mouseX + TOOLTIP_GAP + width + TOOLTIP_PADDING * 2 <= getWidth() ? mouseX + TOOLTIP_GAP
                        : mouseX - TOOLTIP_GAP - width - TOOLTIP_PADDING * 2,
                mouseY + TOOLTIP_GAP + height + TOOLTIP_PADDING * 2 <= getHeight() ?
                        mouseY + TOOLTIP_GAP :
                        mouseY - TOOLTIP_GAP - height - TOOLTIP_PADDING * 2, width + TOOLTIP_PADDING * 2, height + TOOLTIP_PADDING * 2);
        gc.setFill(Color.rgb(255, 255, 255));
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(textToDisplay, mouseX + TOOLTIP_GAP + width + TOOLTIP_PADDING * 2 <= getWidth() ?
                        mouseX + TOOLTIP_GAP + TOOLTIP_PADDING :
                        mouseX - TOOLTIP_GAP - width - TOOLTIP_PADDING,
                mouseY + TOOLTIP_GAP + height + TOOLTIP_PADDING * 2 <= getHeight() ?
                        mouseY + TOOLTIP_GAP + TOOLTIP_PADDING :
                        mouseY - TOOLTIP_GAP - height - TOOLTIP_PADDING);
    }

    private void calculate() {
        Point2D.Double forcePos = getForcePos();
        Point2D.Double forceNeg = getForceNeg();

        long currentTime = System.currentTimeMillis();
        double fps = 1000. / (currentTime - lastCalculated);
        lastCalculated = currentTime;
        dipoleVelocity.setLocation(dipoleVelocity.getX() + (forcePos.x - forceNeg.x) / mass.doubleValue() / fps,
                dipoleVelocity.getY() + (forcePos.y - forceNeg.y) / mass.doubleValue() / fps);
        dipolePosition.setLocation(dipolePosition.getX() + dipoleVelocity.getX() / fps,
                dipolePosition.getY() + dipoleVelocity.getY() / fps);

        charges.forEach(charge -> {
            if ((new Point2D.Double(charge.x, charge.y)).distance(dipolePosition.getX() + getDipoleSeparation() / 2 * Math.cos(dipoleRotation),
                    dipolePosition.getY() - getDipoleSeparation() / 2 * Math.sin(dipoleRotation)) < .01 ||
                    (new Point2D.Double(charge.x, charge.y)).distance(
                            dipolePosition.getX() - getDipoleSeparation() / 2 * Math.cos(dipoleRotation),
                            dipolePosition.getY() + getDipoleSeparation() / 2 * Math.sin(dipoleRotation)) < .01) {
                Thread.currentThread().interrupt();
            }
        });
        double forceMomentum = -getDipoleSeparation() / 2 * Math.cos(dipoleRotation) * (forcePos.y + forceNeg.y) -
                getDipoleSeparation() / 2 * Math.sin(dipoleRotation) * (forcePos.x + forceNeg.x);
        double rotationInertion = mass.doubleValue() * getDipoleSeparation() * getDipoleSeparation() / 4;

        dipoleRotationVelocity += forceMomentum / rotationInertion / fps;
        dipoleRotation += dipoleRotationVelocity / fps;
        dipoleRotation = ((dipoleRotation % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
    }

    public static String scientificFormatNumber(double number) {
        if (number == 0)
            return "0";
        if (number == 1)
            return "1";
        if (number == -1)
            return "-1";
        int exponent = 0;
        while (Math.abs(number) >= 10) {
            number /= 10;
            exponent++;
        }
        while (Math.abs(number) < 1) {
            number *= 10;
            exponent--;
        }

        if (exponent == 0)
            return new DecimalFormat("##.##").format(number);
        if (exponent == 1)
            return new DecimalFormat("##.##").format(number * 10);
        if (exponent == -1)
            return new DecimalFormat("##.##").format(number / 10);
        if (number == 1)
            return "10^" + exponent;
        if (number == -1)
            return "-10^" + exponent;
        return new DecimalFormat("##.##").format(number) + " * 10^" + exponent;
    }

    private Point2D.Double getForceNeg() {
        Point2D.Double intensityNeg = getIntensity(dipolePosition.getX() - getDipoleSeparation() / 2 * Math.cos(dipoleRotation),
                dipolePosition.getY() + getDipoleSeparation() / 2 * Math.sin(dipoleRotation));
        return new Point2D.Double(intensityNeg.x * dipoleCharge.doubleValue(), intensityNeg.y * dipoleCharge.doubleValue());
    }

    private Point2D.Double getForcePos() {
        Point2D.Double intensityPos = getIntensity(dipolePosition.getX() + getDipoleSeparation() / 2 * Math.cos(dipoleRotation),
                dipolePosition.getY() - getDipoleSeparation() / 2 * Math.sin(dipoleRotation));
        return new Point2D.Double(intensityPos.x * dipoleCharge.doubleValue(), intensityPos.y * dipoleCharge.doubleValue());
    }

    @Override
    public void setRedrawing(boolean redrawing) {
        super.setRedrawing(redrawing);
        if (redrawing) {
            drawingAmplitude.bind(Bindings.min(widthProperty(), heightProperty()).divide(2));
            sizeX.bind(widthProperty().divide(Bindings.min(widthProperty(), heightProperty())).multiply(baseSize));
            sizeY.bind(heightProperty().divide(Bindings.min(widthProperty(), heightProperty())).multiply(baseSize));
            dipolePosition.setLocation(sizeX.doubleValue() / 2, sizeY.doubleValue() / 2);
            if (!thread.isAlive()) {
                thread = new Thread(calcRunnable);
                thread.setDaemon(true);
                thread.start();
            }
        } else {
            drawingAmplitude.unbind();
            sizeX.unbind();
            sizeY.unbind();
            thread.interrupt();
        }
    }

    private Point2D.Double getIntensity(double x, double y) {
        if (constant) {
            return new Point2D.Double(getConstantIntensity() * Math.cos(Math.toRadians(getConstantAngle())),
                    -getConstantIntensity() * Math.sin(Math.toRadians(getConstantAngle())));
        }
        Point2D point = new Point2D.Double(x, y);
        Point2D.Double force = new Point2D.Double(0, 0);
        charges.forEach(charge -> {
            double forceN = 8.9875517873681764E9 * charge.charge / Math.pow(point.distance(charge.x, charge.y), 2);
            force.setLocation(force.getX() + forceN * (x - charge.x) / point.distance(charge.x, charge.y),
                    force.getY() + forceN * (y - charge.y) / point.distance(charge.x, charge.y));
        });
        return force;
    }

    private void drawArrow(GraphicsContext gc, double node1X, double node1Y, double node2X, double node2Y) {
        if (node1X==node2X && node1Y==node2Y)
            return;
        gc.strokeLine(node1X, node1Y, node2X, node2Y);
        double arrowAngle = Math.toRadians(45.0);
        double arrowLength = 5;
        double dx = node1X - node2X;
        double dy = node1Y - node2Y;
        double angle = Math.atan2(dy, dx);
        double x1 = Math.cos(angle + arrowAngle) * arrowLength + node2X;
        double y1 = Math.sin(angle + arrowAngle) * arrowLength + node2Y;

        double x2 = Math.cos(angle - arrowAngle) * arrowLength + node2X;
        double y2 = Math.sin(angle - arrowAngle) * arrowLength + node2Y;
        gc.strokeLine(node2X, node2Y, x1, y1);
        gc.strokeLine(node2X, node2Y, x2, y2);
    }

    public double getPlacingCharge() {
        return placingCharge.get();
    }

    public DoubleProperty placingChargeProperty() {
        return placingCharge;
    }

    public double getDipoleCharge() {
        return dipoleCharge.get();
    }

    public DoubleProperty dipoleChargeProperty() {
        return dipoleCharge;
    }

    public double getDipoleSeparation() {
        return dipoleSeparation.get();
    }

    public DoubleProperty dipoleSeparaionProperty() {
        return dipoleSeparation;
    }

    public void reset() {
        charges.clear();
        dipolePosition.setLocation(sizeX.doubleValue() / 2, sizeY.doubleValue() / 2);
        dipoleVelocity.setLocation(0, 0);
        dipoleRotation = 0;
        dipoleRotationVelocity = 0;
        setRedrawing(true);
    }

    public void setConstant(boolean value) {
        constant = value;
        if (value)
            charges.clear();
    }
}
