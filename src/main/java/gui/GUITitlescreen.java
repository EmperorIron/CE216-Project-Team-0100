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

    public void show() {
        // Main layout container (Vertical Box)
       
        VBox root = new VBox(20); 
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-darker");

        // Create the buttons
        Button btnNewGame = createStyledButton("NEW GAME");
        Button btnLoadGame = createStyledButton("LOAD GAME");
        Button btnGuide = createStyledButton("GUIDE");
        Button btnImportTeams = createStyledButton("IMPORT TEAM NAMES");
        
        
        btnNewGame.setOnAction(e -> {
            GUISportSelection sportSelection = new GUISportSelection(
                selectedSport -> GUIMain.startNewGame(selectedSport),
                () -> this.show() // On Back Button
            );
            sportSelection.show();
        });

        btnLoadGame.setOnAction(e -> {
            GUILoadGame loadGameMenu = new GUILoadGame(
                () -> this.show(), // On Back Button
                (SaveGame loadedGame) -> GUIMain.loadSavedGame(loadedGame) // On Game Loaded successfully
            );
            loadGameMenu.show();
        });

        btnGuide.setOnAction(e -> {
            new GUIGuide(null, () -> this.show());
        });

        btnImportTeams.setOnAction(e -> {
            new GUITeamNameImport(() -> this.show()).show();
        });
       

        // Add all buttons to the layout
        root.getChildren().addAll(btnNewGame, btnLoadGame, btnImportTeams, btnGuide);

        SceneManager.changeScene(root, "Sports Manager - Main Menu");
    }

   
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        
       
        button.setPrefWidth(350);
        button.setPrefHeight(60);
        
        button.getStyleClass().add("btn-outline");

        return button;
    }
}
