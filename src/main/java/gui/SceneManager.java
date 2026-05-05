package gui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Centralized router for handling JavaFX scenes.
 * Prevents the "Pass-the-Stage" anti-pattern.
 */
public class SceneManager {
    private static Stage primaryStage;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void changeScene(Parent root, String title) {
        if (primaryStage == null) throw new IllegalStateException("Primary stage is not set in SceneManager.");
        if (title != null) primaryStage.setTitle(title);
        
        if (primaryStage.getScene() == null) {
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            try {
                scene.getStylesheets().add(SceneManager.class.getResource("/styles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Warning: styles.css not found in resources folder.");
            }
            primaryStage.setScene(scene);
        } else {
            primaryStage.getScene().setRoot(root);
        }
        primaryStage.show();
    }
}