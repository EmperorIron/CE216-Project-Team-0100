package core;
import javafx.application.Application;
import javafx.stage.Stage;
import gui.GUITitlescreen;
import gui.*;
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Uygulamanın penceresini ekranı kaplayacak şekilde (maximized) ayarlar
        primaryStage.setMaximized(true);
        gui.SceneManager.setPrimaryStage(primaryStage);
        
        GUITitlescreen titleScreen = new GUITitlescreen();
        titleScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}