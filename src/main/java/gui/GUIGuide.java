package gui;

import Interface.ITeam;
import Sport.Football.CalendarFootball;
import Sport.Football.LeagueFootball;
import io.SaveGame;
import io.SaveManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class GUIGuide {

    private ITeam playerTeam;
    private ImageView guideImageView;
    private Runnable onBack;

    public GUIGuide(ITeam playerTeam, Runnable onBack) {
        this.playerTeam = playerTeam;
        this.onBack = onBack;
        show();
    }

    public void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1b1b2f;");

        // Same top bar as GUIMain
        mainLayout.setTop(createTopBar());

        // Main Content Area
        HBox contentBox = new HBox(20);
        contentBox.setPadding(new Insets(30));
        contentBox.setAlignment(Pos.TOP_CENTER);

        // --- LEFT SIDE: SCROLLABLE BUTTON LIST ---
        VBox buttonList = new VBox(10);
        buttonList.setPadding(new Insets(10));
        buttonList.setStyle("-fx-background-color: #162447; -fx-background-radius: 10;");
        buttonList.setPrefWidth(250);

        // --- RIGHT SIDE: IMAGE ---
        guideImageView = new ImageView();
        guideImageView.setFitWidth(850);
        guideImageView.setFitHeight(600);
        guideImageView.setPreserveRatio(true);

        java.io.File folder = new java.io.File("src/main/resources/images/guideguide");
        if (folder.exists() && folder.isDirectory()) {
            java.io.File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null && files.length > 0) {
                java.util.Arrays.sort(files, (f1, f2) -> {
                    String num1 = f1.getName().replaceAll("\\D+", "");
                    String num2 = f2.getName().replaceAll("\\D+", "");
                    int n1 = num1.isEmpty() ? 0 : Integer.parseInt(num1);
                    int n2 = num2.isEmpty() ? 0 : Integer.parseInt(num2);
                    if (n1 == n2) {
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    }
                    return Integer.compare(n1, n2);
                });
                
                for (java.io.File file : files) {
                    String fileName = file.getName();
                    String guideTitle = fileName.substring(0, fileName.lastIndexOf('.'));
                    
                    Button guideBtn = new Button(guideTitle);
                    guideBtn.setMaxWidth(Double.MAX_VALUE);
                    guideBtn.setPrefHeight(45);
                    guideBtn.setStyle("-fx-background-color: #1f4068; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                    
                    guideBtn.setOnMouseEntered(e -> guideBtn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
                    guideBtn.setOnMouseExited(e -> guideBtn.setStyle("-fx-background-color: #1f4068; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
                    
                    guideBtn.setOnAction(e -> {
                        try {
                            Image img = new Image(file.toURI().toString());
                            guideImageView.setImage(img);
                        } catch (Exception ex) {
                            guideImageView.setImage(null);
                        }
                    });
                    buttonList.getChildren().add(guideBtn);
                }
                try {
                    Image firstImg = new Image(files[0].toURI().toString());
                    guideImageView.setImage(firstImg);
                } catch (Exception ex) { }
            } else {
                Label lbl = new Label("No PNG files found in:\nsrc/main/resources/images/guideguide/");
                lbl.setTextFill(Color.web("#e43f5a"));
                buttonList.getChildren().add(lbl);
            }
        } else {
            Label lbl = new Label("Folder not found:\nsrc/main/resources/images/guideguide/");
            lbl.setTextFill(Color.web("#e43f5a"));
            buttonList.getChildren().add(lbl);
        }

        ScrollPane scrollPane = new ScrollPane(buttonList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox rightSide = new VBox(15, guideImageView);
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-padding: 10;");
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        contentBox.getChildren().addAll(scrollPane, rightSide);
        mainLayout.setCenter(contentBox);

        SceneManager.changeScene(mainLayout, "Sports Manager - Guide");
    }

    // --- EXACTLY THE SAME METHODS AS GUIMAIN ---
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: #162447; -fx-border-color: #d82bbc; -fx-border-width: 0 0 2 0;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);
        Label teamLabel = new Label("User Guide");
        teamLabel.setTextFill(Color.WHITE);
        teamLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label managerLabel = new Label("In-Game Help and Rules");
        managerLabel.setTextFill(Color.web("#a5a5b0"));
        managerLabel.setFont(Font.font("Segoe UI", 14));
        infoBox.getChildren().addAll(teamLabel, managerLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("Go Back ↩");
        backBtn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        topBar.getChildren().addAll(infoBox, spacer, backBtn);
        return topBar;
    }
}