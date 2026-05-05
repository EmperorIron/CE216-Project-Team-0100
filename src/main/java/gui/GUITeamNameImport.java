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
    private Stage primaryStage;
    private Runnable onBack;

    public GUITeamNameImport(Stage primaryStage, Runnable onBack) {
        this.primaryStage = primaryStage;
        this.onBack = onBack;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #050505;");

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
        chkUseCustomNames.setSelected(TeamNameImport.useCustomNames);
        chkUseCustomNames.setOnAction(e -> TeamNameImport.useCustomNames = chkUseCustomNames.isSelected());

        TextArea textArea = new TextArea();
        textArea.setPromptText("Team A\nTeam B\nTeam C...");
        if (!TeamNameImport.customTeamNames.isEmpty()) {
            textArea.setText(String.join("\n", TeamNameImport.customTeamNames));
        }
        textArea.setPrefHeight(400);
        textArea.setStyle("-fx-control-inner-background: #162447; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

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
                TeamNameImport.useCustomNames = chkUseCustomNames.isSelected();
                if (TeamNameImport.useCustomNames) {
                    GUIPopup.showMessage(primaryStage, "Success", null, "Successfully imported " + names.size() + " team names! Custom names are enabled.");
                } else {
                    GUIPopup.showMessage(primaryStage, "Success", null, "Successfully imported " + names.size() + " team names! However, custom names are disabled.");
                }
            } else {
                TeamNameImport.setCustomNames(null);
                chkUseCustomNames.setSelected(false);
                TeamNameImport.useCustomNames = false;
                GUIPopup.showMessage(primaryStage, "Cleared", null, "Custom team names cleared. The game will generate random names.");
            }
        });

        Button btnLoadFile = new Button("LOAD FROM TXT");
        btnLoadFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Team Names TXT File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    textArea.setText(String.join("\n", lines));
                } catch (Exception ex) {
                    GUIPopup.showMessage(primaryStage, "Error", null, "Failed to read file: " + ex.getMessage());
                }
            }
        });

        Button btnBack = new Button("BACK");
        btnBack.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        // Add basic generic styling inline just for this menu
        String btnStyle = "-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 30; -fx-border-width: 2; -fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 200px; -fx-min-height: 50px;";
        btnSave.setStyle(btnStyle);
        btnLoadFile.setStyle(btnStyle);
        btnBack.setStyle(btnStyle);

        HBox footer = new HBox(20, btnLoadFile, btnSave, btnBack);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 30, 0));
        root.setBottom(footer);

        primaryStage.setTitle("Sports Manager - Import Team Names");
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(root);
        }
        primaryStage.show();
    }
}