package core;
import javafx.application.Application;
import javafx.stage.Stage;
import gui.GUITitlescreen;
import gui.*;
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        GUITitlescreen titleScreen = new GUITitlescreen();
        titleScreen.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}