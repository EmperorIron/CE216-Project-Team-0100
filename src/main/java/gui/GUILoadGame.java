package gui;
import io.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class GUILoadGame {

    // Callback to return to the main menu
    private final Runnable onBackToMenu;
    private final Consumer<SaveGame> onGameLoaded;

    public GUILoadGame(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
        this.onGameLoaded = null;
    }

    public GUILoadGame(Runnable onBackToMenu, Consumer<SaveGame> onGameLoaded) {
        this.onBackToMenu = onBackToMenu;
        this.onGameLoaded = onGameLoaded;
    }

    public void show() {
        // Using BorderPane as main layout (Top header, Center list, Bottom button)
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-darker");

        // --- TOP SECTION (HEADER) ---
        Label lblTitle = new Label("LOAD SAVED GAME");
        lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-letter-spacing: 2px;");
        HBox header = new HBox(lblTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 0, 20, 0));
        root.setTop(header);

        // --- CENTER SECTION (SCROLL LIST) ---
        VBox saveList = new VBox(15); // 15px spacing between cards
        saveList.setAlignment(Pos.TOP_CENTER);
        saveList.setPadding(new Insets(10, 50, 10, 50));
        saveList.setStyle("-fx-background-color: #050505;");

        // Save directory to read from
        File saveDir = new File(SaveManager.getSaveDirectory());
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        
        File[] files = saveDir.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (files != null && files.length > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            
            // 1. First, add Quick Save (Autosave) if it exists
            File autoSaveFile = new File(saveDir, "autosave.json");
            if (autoSaveFile.exists()) {
                String dateStr = sdf.format(new Date(autoSaveFile.lastModified()));
                String clubName = "";
                String gameTime = "";
                SaveGame tempSave = SaveManager.loadGame(autoSaveFile.getPath());
                if (tempSave != null) {
                    if (tempSave.getPlayerTeam() != null) clubName = tempSave.getPlayerTeam().getName();
                    if (tempSave.getCalendar() != null) gameTime = "Week " + (tempSave.getCalendar().getCurrentWeek() + 1);
                }
                VBox saveCard = createSaveCard("Quick Save (Autosave)", dateStr, clubName, gameTime, autoSaveFile.getPath());
                saveList.getChildren().add(saveCard);
            }
            
            // 2. Then add slot 1-10 in order
            for (int i = 1; i <= 10; i++) {
                File slotFile = new File(saveDir, "slot_" + i + ".json");
                if (slotFile.exists()) {
                    String dateStr = sdf.format(new Date(slotFile.lastModified()));
                    String clubName = "";
                    String gameTime = "";
                    SaveGame tempSave = SaveManager.loadGame(slotFile.getPath());
                    if (tempSave != null) {
                        if (tempSave.getPlayerTeam() != null) clubName = tempSave.getPlayerTeam().getName();
                        if (tempSave.getCalendar() != null) gameTime = "Week " + (tempSave.getCalendar().getCurrentWeek() + 1);
                    }
                    VBox saveCard = createSaveCard("Save Slot " + i, dateStr, clubName, gameTime, slotFile.getPath());
                    saveList.getChildren().add(saveCard);
                }
            }
            
            // 3. Add any other custom saves (legacy support)
            for (File file : files) {
                String fName = file.getName();
                if (!fName.equals("autosave.json") && !fName.matches("slot_([1-9]|10)\\.json")) {
                    String title = fName.replace(".json", "");
                    String dateStr = sdf.format(new Date(file.lastModified()));
                    String clubName = "";
                    String gameTime = "";
                    SaveGame tempSave = SaveManager.loadGame(file.getPath());
                    if (tempSave != null) {
                        if (tempSave.getPlayerTeam() != null) clubName = tempSave.getPlayerTeam().getName();
                        if (tempSave.getCalendar() != null) gameTime = "Week " + (tempSave.getCalendar().getCurrentWeek() + 1);
                    }
                    VBox saveCard = createSaveCard(title, dateStr, clubName, gameTime, file.getPath());
                    saveList.getChildren().add(saveCard);
                }
            }
        } else {
            Label emptyLabel = new Label("No save files found.");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
            saveList.getChildren().add(emptyLabel);
        }

        // ScrollPane (Infinite Scrolling Component)
        ScrollPane scrollPane = new ScrollPane(saveList);
        scrollPane.setFitToWidth(true); // Fit cards width to screen
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Show vertical scroll only if needed
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Completely hide horizontal scroll

        root.setCenter(scrollPane);

        // --- BOTTOM SECTION (BACK BUTTON) ---
        Button btnBack = new Button("BACK TO MAIN MENU");
        btnBack.setPrefWidth(300);
        btnBack.setPrefHeight(50);
        btnBack.getStyleClass().add("btn-outline");
        btnBack.setOnAction(e -> onBackToMenu.run()); // Return to main menu
        
        HBox footer = new HBox(btnBack);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 30, 0));
        root.setBottom(footer);

        SceneManager.changeScene(root, "Sports Manager - Load Game");
    }

    /**
     * Creates a visual box (card) for each save file.
     */
    private VBox createSaveCard(String title, String date, String clubName, String gameTime, String filePath) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setPrefHeight(80);
        card.setMaxWidth(600); // Prevent cards from stretching too much
        
        card.getStyleClass().add("save-card");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        String details = "";
        if (clubName != null && !clubName.isEmpty()) details += clubName + " | ";
        if (gameTime != null && !gameTime.isEmpty()) details += gameTime + " | ";
        details += "Last saved: " + date;
        Label lblDetails = new Label(details);
        lblDetails.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaaaaa;");

        card.getChildren().addAll(lblTitle, lblDetails);

        card.setOnMouseClicked(e -> {
            card.setDisable(true); // Disable card so user doesn't double-click
            
            Thread loadThread = new Thread(() -> {
                SaveGame loadedGame = SaveManager.loadGame(filePath);
                javafx.application.Platform.runLater(() -> {
                    if (loadedGame != null) {
                        if (onGameLoaded != null) {
                            onGameLoaded.accept(loadedGame);
                        }
                    } else {
                        card.setDisable(false); // Re-enable if load failed
                        GUIPopup.showMessage("Load Failed", "File Corrupted", 
                                "The selected save file could not be read properly. It may belong to an older version of the game.");
                    }
                });
            });
            loadThread.setDaemon(true);
            loadThread.start();
        });
        return card;
    }
}