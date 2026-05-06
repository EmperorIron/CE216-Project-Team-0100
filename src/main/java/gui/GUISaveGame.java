package gui;

import Classes.GameContext;
import io.SaveGame;
import io.SaveManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

public class GUISaveGame {

    private final Runnable onBack;

    public GUISaveGame(Runnable onBack) {
        this.onBack = onBack;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #050505;");

        // --- HEADER ---
        Label lblTitle = new Label("SAVE GAME");
        lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-letter-spacing: 2px;");
        HBox header = new HBox(lblTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 0, 20, 0));
        root.setTop(header);

        // --- CENTER (SCROLL LIST FOR SLOTS) ---
        VBox saveList = new VBox(15);
        saveList.setAlignment(Pos.TOP_CENTER);
        saveList.setPadding(new Insets(10, 50, 10, 50));
        saveList.setStyle("-fx-background-color: #050505;");

        File saveDir = new File("saves/");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        // Generate 10 predefined save slots
        for (int i = 1; i <= 10; i++) {
            String fileName = "slot_" + i;
            File file = new File("saves/" + fileName + ".json");

            String dateStr = "Empty Slot";
            String clubName = "";
            String gameTime = "";
            if (file.exists()) {
                dateStr = sdf.format(new Date(file.lastModified()));
                SaveGame tempSave = SaveManager.loadGame(file.getPath());
                if (tempSave != null) {
                    if (tempSave.getPlayerTeam() != null) clubName = tempSave.getPlayerTeam().getName();
                    if (tempSave.getCalendar() != null) gameTime = "Week " + (tempSave.getCalendar().getCurrentWeek() + 1);
                }
            }

            VBox saveCard = createSaveCard("Save Slot " + i, dateStr, clubName, gameTime, fileName);
            saveList.getChildren().add(saveCard);
        }

        ScrollPane scrollPane = new ScrollPane(saveList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #050505; -fx-background-color: transparent; -fx-control-inner-background: #050505;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scrollPane);

        // --- FOOTER (BACK BUTTON) ---
        Button btnBack = new Button("BACK TO GAME");
        styleButton(btnBack);
        btnBack.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });
        
        HBox footer = new HBox(btnBack);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 30, 0));
        root.setBottom(footer);

        SceneManager.changeScene(root, "Sports Manager - Save Game");
    }

    private VBox createSaveCard(String title, String date, String clubName, String gameTime, String fileName) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setPrefHeight(80);
        card.setMaxWidth(600);
        
        String defaultStyle = "-fx-border-color: rgba(255, 255, 255, 0.3); -fx-border-radius: 10; -fx-border-width: 2; -fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-cursor: hand;";
        String hoverStyle = "-fx-border-color: #ffffff; -fx-border-radius: 10; -fx-border-width: 2; -fx-background-color: rgba(255, 255, 255, 0.15); -fx-background-radius: 10; -fx-cursor: hand;";
        
        card.setStyle(defaultStyle);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        String details;
        if (date.equals("Empty Slot")) {
            details = date;
        } else {
            details = "";
            if (clubName != null && !clubName.isEmpty()) details += clubName + " | ";
            if (gameTime != null && !gameTime.isEmpty()) details += gameTime + " | ";
            details += "Last saved: " + date;
        }
        Label lblDetails = new Label(details);
        lblDetails.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaaaaa;");

        card.getChildren().addAll(lblTitle, lblDetails);

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));
        
        card.setOnMouseClicked(e -> {
            card.setDisable(true); // Disable to prevent double-saving
            
            SaveGame saveData = new SaveGame(title, 
                GameContext.getInstance().getActiveLeague(), 
                GameContext.getInstance().getActiveCalendar(), 
                GameContext.getInstance().getPlayerTeam(),
                gui.GUISquadManager.getInstance().getPitchPlayers(), 
                gui.GUISquadManager.getInstance().getPlayersOnPitchQueue(), 
                gui.GUISquadManager.getInstance().getReservePlayersQueue(),
                gui.GUISquadManager.getInstance().getCurrentTacticStyle());
                
            Thread saveThread = new Thread(() -> {
                SaveManager.saveGame(saveData, fileName);
                
                javafx.application.Platform.runLater(() -> {
                    GUIPopup.showMessage("Save Successful", null, "Game successfully saved to " + title + ".");
                    show(); // Refresh UI
                });
            });
            saveThread.setDaemon(true);
            saveThread.start();
        });
        return card;
    }

    private void styleButton(Button button) {
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 30; -fx-border-width: 2; -fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 30; -fx-border-color: #ffffff; -fx-border-radius: 30; -fx-border-width: 2; -fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-cursor: hand;";
        
        button.setStyle(defaultStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));
    }
}
