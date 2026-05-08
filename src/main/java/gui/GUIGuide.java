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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class GUIGuide {

    private ITeam playerTeam;
    private TextArea guideContentArea;
    private Map<String, String> guideTexts = new HashMap<>();
    private Runnable onBack;

    public GUIGuide(ITeam playerTeam, Runnable onBack) {
        this.playerTeam = playerTeam;
        this.onBack = onBack;
        
        // Prepare guide contents
        for (int i = 1; i <= 10; i++) {
            guideTexts.put("Guide " + i, "Text " + i + "\n\nThis area is reserved for the " + i + "th guide content. You can add detailed explanations about the game here.");
        }
        
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
        buttonList.setStyle("-fx-background-color: #162447; -cite-background-radius: 10;");
        buttonList.setPrefWidth(250);

        for (int i = 1; i <= 10; i++) {
            String guideTitle = "Guide " + i;
            Button guideBtn = new Button(guideTitle);
            guideBtn.setMaxWidth(Double.MAX_VALUE);
            guideBtn.setPrefHeight(45);
            guideBtn.setStyle("-fx-background-color: #1f4068; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            
            guideBtn.setOnMouseEntered(e -> guideBtn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
            guideBtn.setOnMouseExited(e -> guideBtn.setStyle("-fx-background-color: #1f4068; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
            
            guideBtn.setOnAction(e -> guideContentArea.setText(guideTexts.get(guideTitle)));
            buttonList.getChildren().add(guideBtn);
        }

        ScrollPane scrollPane = new ScrollPane(buttonList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // --- RIGHT SIDE: HUGE TEXTBOX (Text Area) ---
        guideContentArea = new TextArea("Please select a topic you want to learn from the list on the left.");
        guideContentArea.setEditable(false);
        guideContentArea.setWrapText(true);
        guideContentArea.setPrefHeight(600);
        guideContentArea.setStyle("-fx-control-inner-background: #162447; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Segoe UI'; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;");
        
        HBox.setHgrow(guideContentArea, Priority.ALWAYS);

        contentBox.getChildren().addAll(scrollPane, guideContentArea);
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