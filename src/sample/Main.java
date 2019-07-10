package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        root.setId("pane");
        primaryStage.setTitle("CGVhelper");
        Scene scene=new Scene(root,600,500);
        primaryStage.setScene(scene);
        scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
        primaryStage.show();
    }
    public static void main(String[] args) {launch(args);}
}
