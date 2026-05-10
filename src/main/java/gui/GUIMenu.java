package gui;

import Classes.GameContext;
import io.SaveGame;
import io.SaveManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GUIMenu {

    // You will use this method to call the menu: GUIPauseMenu.show(primaryStage);
    public static void show() {
        Stage ownerStage = SceneManager.getPrimaryStage();
        Stage popupStage = new Stage();
        popupStage.initOwner(ownerStage);
        
        // MAGIC CODE: Completely removes the OS's classic window borders (Windows look)
        popupStage.initStyle(StageStyle.TRANSPARENT);
        
        // Prevents clicking on the background game while this menu is open
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Menu Box (Navy blue background, red thin border)
        VBox menuBox = new VBox(15);
        menuBox.setPadding(new Insets(40, 60, 40, 60));
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(350);
        menuBox.getStyleClass().add("menu-box");

        Label title = new Label("GAME MENU");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        // Create buttons (With colors matching the design theme)
        Button btnSave = createMenuButton("Save Game", "btn-success");
        Button btnQuickSave = createMenuButton("Quick Save", "btn-success");
        Button btnLoad = createMenuButton("Load Game", "btn-warning");
        Button btnQuickLoad = createMenuButton("Quick Load", "btn-warning");
        Button btnGuide = createMenuButton("Guide", "btn-secondary");
        Button btnMainMenu = createMenuButton("Back to Main Menu", "btn-primary");
        Button btnExit = createMenuButton("Exit to Desktop", "btn-purple");
        Button btnClose = createMenuButton("Back to Game", "btn-info");

        // --- BUTTON FUNCTIONS ---
        btnClose.setOnAction(e -> popupStage.close());
        
        btnExit.setOnAction(e -> System.exit(0)); // Closes the entire program
        
        btnMainMenu.setOnAction(e -> {
            popupStage.close();
            GUITitlescreen titleScreen = new GUITitlescreen();
            titleScreen.show();
        });
        
        btnGuide.setOnAction(e -> {
            popupStage.close();
            javafx.scene.Parent currentRoot = ownerStage.getScene().getRoot();
            String currentTitle = ownerStage.getTitle();
            new GUIGuide(GameContext.getInstance().getPlayerTeam(), () -> {
                ownerStage.getScene().setRoot(currentRoot);
                ownerStage.setTitle(currentTitle);
            });
        });

        btnQuickSave.setOnAction(e -> {
            Classes.League leagueToSave = "VOLLEYBALL".equals(GameContext.getInstance().getActiveSport()) 
                ? GameContext.getInstance().getActiveVolleyballLeague() 
                : GameContext.getInstance().getActiveLeague();
            Classes.Calendar calendarToSave = "VOLLEYBALL".equals(GameContext.getInstance().getActiveSport()) 
                ? GameContext.getInstance().getActiveVolleyballCalendar() 
                : GameContext.getInstance().getActiveCalendar();

            if (leagueToSave == null || calendarToSave == null) {
                Classes.ErrorHandler.logError("Cannot quick save: Active league or calendar is null.");
                return;
            }

            SaveGame saveData = new SaveGame("Quick Save", leagueToSave, calendarToSave, GameContext.getInstance().getPlayerTeam(),
                    gui.GUISquadManager.getInstance().getPitchPlayers(), gui.GUISquadManager.getInstance().getPlayersOnPitchQueue(), gui.GUISquadManager.getInstance().getReservePlayersQueue(),
                    gui.GUISquadManager.getInstance().getCurrentTacticStyle());
            SaveManager.saveGame(saveData, "autosave");
            // Optional: A small confirmation message can be displayed on screen.
        });

        btnQuickLoad.setOnAction(e -> {
            String autoSavePath = SaveManager.getSaveDirectory() + "autosave.json";
            java.io.File autoSaveFile = new java.io.File(autoSavePath);
            if (autoSaveFile.exists()) {
                SaveGame loadedGame = SaveManager.loadGame(autoSavePath);
                if (loadedGame != null) {
                    popupStage.close();
                    GUIMain.loadSavedGame(loadedGame);
                }
            } else {
                GUIPopup.showMessage("Warning", "Quick Save Not Found", "No quick save has been created yet.");
            }
        });

        btnLoad.setOnAction(e -> {
            popupStage.close();
            javafx.scene.Parent currentRoot = ownerStage.getScene().getRoot();
            String currentTitle = ownerStage.getTitle();
            GUILoadGame loadGameMenu = new GUILoadGame(
                () -> {
                    ownerStage.getScene().setRoot(currentRoot);
                    ownerStage.setTitle(currentTitle);
                },
                (SaveGame loadedGame) -> GUIMain.loadSavedGame(loadedGame)
            );
            loadGameMenu.show();
        });

        btnSave.setOnAction(e -> {
            popupStage.close();
            javafx.scene.Parent currentRoot = ownerStage.getScene().getRoot();
            String currentTitle = ownerStage.getTitle();
            new GUISaveGame(() -> {
                ownerStage.getScene().setRoot(currentRoot);
                ownerStage.setTitle(currentTitle);
            }).show();
        });

        // Add buttons to the box
        menuBox.getChildren().addAll(title, btnSave, btnQuickSave, btnLoad, btnQuickLoad, btnGuide, btnMainMenu, btnExit, new Region(), btnClose);

        // Used Region to leave some space before the close button
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        // Full screen background (To darken the main screen)
        StackPane root = new StackPane(menuBox);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Makes background 70% black
        root.setPadding(new Insets(50));

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(SceneManager.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Warning: styles.css not found in resources folder.");
        }
        scene.setFill(Color.TRANSPARENT); // Makes JavaFX window background transparent
        popupStage.setScene(scene);

        // Perfectly match pop-up's size and position with the main window
        popupStage.setWidth(ownerStage.getWidth());
        popupStage.setHeight(ownerStage.getHeight());
        popupStage.setX(ownerStage.getX());
        popupStage.setY(ownerStage.getY());

        // Show menu
        popupStage.showAndWait();
    }

    // Custom designed button generator
    private static Button createMenuButton(String text, String cssClass) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.getStyleClass().addAll("btn", cssClass);
        
        return btn;
    }
}