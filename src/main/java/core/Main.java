package core;
import javafx.application.Application;
import javafx.stage.Stage;
import gui.GUITitlescreen;
import gui.*;
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Sets the application window to maximize to fill the screen
        primaryStage.setMaximized(true);
        gui.SceneManager.setPrimaryStage(primaryStage);
        
        Classes.TeamNameImport.loadFromDisk();

        GUITitlescreen titleScreen = new GUITitlescreen();
        titleScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}