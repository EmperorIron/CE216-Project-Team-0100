package gui;

import Classes.TeamNameImport;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GUITeamNameImport {
    private Runnable onBack;

    public GUITeamNameImport(Runnable onBack) {
        this.onBack = onBack;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-darker");

        Label lblTitle = new Label("IMPORT CUSTOM TEAM NAMES");
        lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-letter-spacing: 2px;");
        HBox header = new HBox(lblTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 0, 20, 0));
        root.setTop(header);

        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20, 50, 20, 50));

        Label lblInstructions = new Label("Enter custom team names below (one per line).\nThese will be used instead of randomly generated names when starting a new game.");
        lblInstructions.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 16px;");

        CheckBox chkUseCustomNames = new CheckBox("Use Custom Team Names");
        chkUseCustomNames.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        chkUseCustomNames.setSelected(TeamNameImport.isUseCustomNames());
        chkUseCustomNames.setOnAction(e -> TeamNameImport.setUseCustomNames(chkUseCustomNames.isSelected()));

        TextArea textArea = new TextArea();
        textArea.setPromptText("Team A\nTeam B\nTeam C...");
        if (!TeamNameImport.getCustomTeamNames().isEmpty()) {
            textArea.setText(String.join("\n", TeamNameImport.getCustomTeamNames()));
        }
        textArea.setPrefHeight(400);
        textArea.getStyleClass().add("text-area-dark");

        centerBox.getChildren().addAll(lblInstructions, chkUseCustomNames, textArea);
        root.setCenter(centerBox);

        Button btnSave = new Button("SAVE NAMES");
        
        btnSave.setOnAction(e -> {
            String text = textArea.getText();
            if (!text.trim().isEmpty()) {
                List<String> names = Arrays.stream(text.split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                TeamNameImport.setCustomNames(names);
                TeamNameImport.setUseCustomNames(chkUseCustomNames.isSelected());
                TeamNameImport.saveToDisk();
                if (TeamNameImport.isUseCustomNames()) {
                    GUIPopup.showMessage("Success", null, "Successfully imported " + names.size() + " team names! Custom names are enabled.");
                } else {
                    GUIPopup.showMessage("Success", null, "Successfully imported " + names.size() + " team names! However, custom names are disabled.");
                }
            } else {
                TeamNameImport.setCustomNames(null);
                chkUseCustomNames.setSelected(false);
                TeamNameImport.setUseCustomNames(false);
                TeamNameImport.saveToDisk();
                GUIPopup.showMessage("Cleared", null, "Custom team names cleared. The game will generate random names.");
            }
        });

        Button btnLoadFile = new Button("LOAD FROM TXT");
        btnLoadFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Team Names TXT File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
            if (file != null) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    textArea.setText(String.join("\n", lines));
                } catch (Exception ex) {
                    GUIPopup.showMessage("Error", null, "Failed to read file: " + ex.getMessage());
                }
            }
        });

        Button btnBack = new Button("BACK");
        btnBack.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        btnSave.getStyleClass().add("btn-outline");
        btnLoadFile.getStyleClass().add("btn-outline");
        btnBack.getStyleClass().add("btn-outline");

        HBox footer = new HBox(20, btnLoadFile, btnSave, btnBack);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 30, 0));
        root.setBottom(footer);

        SceneManager.changeScene(root, "Sports Manager - Import Team Names");
    }
}