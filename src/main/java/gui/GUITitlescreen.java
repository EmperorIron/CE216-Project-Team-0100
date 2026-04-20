package gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import io.SaveGame;

public class GUITitlescreen {

    public void show(Stage primaryStage) {
        // Main layout container (Vertical Box)
       
        VBox root = new VBox(20); 
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #050505;"); // Very dark background matching the design

        // Create the buttons
        Button btnNewGame = createStyledButton("NEW GAME");
        Button btnLoadGame = createStyledButton("LOAD GAME");
        Button btnGuide = createStyledButton("GUIDE");
        
        
        btnNewGame.setOnAction(e -> {
            GUISportSelection sportSelection = new GUISportSelection(
                selectedSport -> GUIMain.startNewGame(selectedSport, primaryStage),
                () -> this.show(primaryStage) // On Back Button
            );
            sportSelection.show(primaryStage);
        });

        btnLoadGame.setOnAction(e -> {
            GUILoadGame loadGameMenu = new GUILoadGame(
                () -> this.show(primaryStage), // On Back Button
                (SaveGame loadedGame) -> GUIMain.loadSavedGame(loadedGame, primaryStage) // On Game Loaded successfully
            );
            loadGameMenu.show(primaryStage);
        });

        btnGuide.setOnAction(e -> System.out.println("Opening Guide..."));
       

        // Add all buttons to the layout
        root.getChildren().addAll(btnNewGame, btnLoadGame, btnGuide);

        // Create the scene and set it to the stage
        Scene scene = new Scene(root, 800, 700, Color.BLACK); // Slightly increased height to 700
        primaryStage.setTitle("Sports Manager - Main Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

   
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        
       
        button.setPrefWidth(350);
        button.setPrefHeight(60);
        
      
        String defaultStyle = "-fx-background-color: transparent; " +
                              "-fx-border-color: #ffffff; " +
                              "-fx-border-radius: 30; " + // Creates the pill shape
                              "-fx-border-width: 2; " +
                              "-fx-text-fill: #ffffff; " +
                              "-fx-font-size: 16px; " +
                              "-fx-font-weight: bold; " +
                              "-fx-font-family: 'Arial';";
                              
        // Hover CSS style: Adds a slight dark grey background when hovered
        String hoverStyle = "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                            "-fx-background-radius: 30; " +
                            "-fx-border-color: #ffffff; " +
                            "-fx-border-radius: 30; " +
                            "-fx-border-width: 2; " +
                            "-fx-text-fill: #ffffff; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-family: 'Arial';";

        button.setStyle(defaultStyle);

        // Add hover effect listeners
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));

        return button;
    }
}
