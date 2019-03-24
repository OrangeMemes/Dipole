package coriolis;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {




    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("coriolis.fxml"));
        primaryStage.setTitle("Диполь");

        primaryStage.setScene(new Scene(root, 750, 500));
        primaryStage.setMinHeight(450);
        primaryStage.setMinWidth(700);
        primaryStage.getScene().getStylesheets().add("https://fonts.googleapis.com/css?family=Roboto");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
