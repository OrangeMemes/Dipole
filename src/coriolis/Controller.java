package coriolis;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXToolbar;
import coriolis.activities.FoucaultsPendulum;
import coriolis.activities.FreeFall;
import coriolis.activities.ShootingCannon;
import javafx.animation.FadeTransition;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    public GridPane pane;
    public JFXToolbar toolBar;
    public Text title;
    public JFXButton resetButton;
    private Activity activity;
    public JFXDrawer drawer;
    public JFXHamburger hamburger;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawer.close();
        turnOffPickOnBoundsFor(drawer);/*

        HamburgerBackArrowBasicTransition burgerAnimation = new HamburgerBackArrowBasicTransition(hamburger);

        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (drawer.isShown())
                drawer.close();
            else
                drawer.open();
        });

        resetButton.setOnMouseClicked(event -> reset());

        drawer.setOnDrawerClosing(event -> {
            burgerAnimation.setRate(-1);
            burgerAnimation.play();
        });


        drawer.setOnDrawerOpening(event -> {
            burgerAnimation.setRate(1);
            burgerAnimation.play();
        });


*/
        foucault(null);
    }

    private void placeNewNodes(Activity nodes) {
        nodes.getTop().setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        nodes.getBottom().setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        FadeTransition transition = new FadeTransition(Duration.millis(50), pane);
        transition.setFromValue(1.);
        transition.setToValue(0.);

        transition.setOnFinished(event -> {
            if (pane.getChildren() != null && activity != null) {
                pane.getChildren().clear();
            }
            pane.add(nodes.getTop(), 0, 0);
            pane.add(nodes.getBottom(), 1, 0);
            transition.setFromValue(0.);
            transition.setToValue(1.);
            transition.setOnFinished(null);
            transition.playFromStart();
        });
        transition.play();
//        title.setText(nodes.getLabel());
        activity = nodes;
    }

    private void reset() {
        FadeTransition transition = new FadeTransition(Duration.millis(400), pane);
        transition.setFromValue(1.);
        transition.setToValue(0.);
        transition.setOnFinished(event -> {
            activity.reset();
            transition.setFromValue(0.);
            transition.setToValue(1.);
            transition.setOnFinished(null);
            transition.playFromStart();
        });
        transition.play();
    }

    private void turnOffPickOnBoundsFor(Node n) {
        n.setPickOnBounds(false);
        if (n instanceof Parent) {
            for (Node c : ((Parent) n).getChildrenUnmodifiable()) {
                turnOffPickOnBoundsFor(c);
            }
        }
    }

    public void foucault(MouseEvent mouseEvent) {
        placeNewNodes(FoucaultsPendulum.getInstance());
        drawer.close();
    }

    public void fall(MouseEvent mouseEvent) {
        placeNewNodes(FreeFall.getInstance());
        drawer.close();
    }

    public void cannon(MouseEvent mouseEvent) {
        placeNewNodes(ShootingCannon.getInstance());
        drawer.close();
    }
}


