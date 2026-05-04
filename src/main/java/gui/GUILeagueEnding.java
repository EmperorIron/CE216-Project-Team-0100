package gui;

import Interface.ITeam;
import Classes.League;
import Classes.Calendar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class GUILeagueEnding {
        
    private Stage primaryStage;
    private League activeLeague;
    private Calendar activeCalendar;

    public GUILeagueEnding(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.activeLeague = "VOLLEYBALL".equals(GUIMain.activeSport) ? GUIMain.activeVolleyballLeague : GUIMain.activeLeague;
        this.activeCalendar = "VOLLEYBALL".equals(GUIMain.activeSport) ? GUIMain.activeVolleyballCalendar : GUIMain.activeCalendar;
        show();
    }

    public void show() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b2f;");
        root.setPadding(new Insets(40));

        Label title = new Label("SEASON ENDED!");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));

        List<ITeam> ranking = activeLeague.getTeamRanking();
        ITeam champion = ranking.get(0); 

        VBox championBox = new VBox(15);
        championBox.setAlignment(Pos.CENTER);
        championBox.setStyle("-fx-background-color: #162447; -fx-background-radius: 15; -fx-border-color: #f0a500; -fx-border-width: 3; -fx-border-radius: 15;");
        championBox.setPadding(new Insets(30));
        championBox.setMaxWidth(500);

        Label champLabel = new Label("🏆 CHAMPIONS 🏆");
        champLabel.setTextFill(Color.web("#f0a500"));
        champLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        javafx.scene.Node emblem = GUILeftandTopBarHelper.createEmblem(champion, 100);

        Label champName = new Label(champion.getName());
        champName.setTextFill(Color.WHITE);
        champName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        String stats = champion.getPoints() + " PTS | " + champion.getWins() + " W - ";
        if (!"VOLLEYBALL".equals(GUIMain.activeSport)) {
            stats += champion.getDraws() + " D - ";
        }
        stats += champion.getLosses() + " L";

        Label champStats = new Label(stats);
        champStats.setTextFill(Color.web("#a5a5b0"));
        champStats.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        championBox.getChildren().addAll(champLabel, emblem, champName, champStats);

        HBox btnBox1 = new HBox(20);
        btnBox1.setAlignment(Pos.CENTER);
        Button btnTable = createStyledButton("Final League Table", "#4CAF50");
        btnTable.setOnAction(e -> new GUILeagueRanking(primaryStage, GUIMain.playerTeam, activeLeague));
        Button btnFixture = createStyledButton("Season Fixtures", "#1f4068");
        btnFixture.setOnAction(e -> new GUIFixture(primaryStage, GUIMain.playerTeam, activeCalendar));
        btnBox1.getChildren().addAll(btnTable, btnFixture);

        HBox btnBox2 = new HBox(20);
        btnBox2.setAlignment(Pos.CENTER);
        Button btnNextSeason = createStyledButton("Start Next Season", "#e43f5a");
        btnNextSeason.setOnAction(e -> startNewSeason());
        Button btnSave = createStyledButton("Save Game", "#f0a500");
        btnSave.setOnAction(e -> new GUISaveGame(() -> this.show()).show(primaryStage));
        btnBox2.getChildren().addAll(btnNextSeason, btnSave);

        Button btnTitle = createStyledButton("Exit to Main Menu", "#4e4e6a");
        btnTitle.setOnAction(e -> new GUITitlescreen().show(primaryStage));

        root.getChildren().addAll(title, championBox, btnBox1, btnBox2, btnTitle);

        primaryStage.setTitle("Sports Manager - Season Ended");
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(root);
        }
        primaryStage.show();
    }

    private void startNewSeason() {
        for (ITeam team : activeLeague.getTeamRanking()) {
            team.setWins(0);
            team.setLosses(0);
            team.setDraws(0);
            team.setGoalsScored(0);
            team.setGoalsConceded(0);
            team.setPoints(0);
        }
        
        activeCalendar.generateFixtures(activeLeague.getTeamRanking());
        
        GUIMain.isMatchDay = false;
        GUIMain.tacticConfirmedForMatch = false;
        
        new GUIMain(primaryStage);
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + color + ", 20%); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8; -fx-cursor: hand;"));
        return btn;
    }
}
