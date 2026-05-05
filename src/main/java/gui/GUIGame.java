package gui;

import Classes.GameContext;
import Interface.ITeam;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import Sport.Football.GameFootball;
import Sport.Volleyball.GameVolleyball;

public class GUIGame {

    private ITeam homeTeam;
    private ITeam awayTeam;

    private Label scoreLabel;
    private Label minuteLabel;
    private VBox eventLogContainer;
    private ScrollPane eventScrollPane;

    private Label homePossession, awayPossession;
    private Label homeShots, awayShots;
    private Label homeXG, awayXG;
    private Slider speedSlider;

    private boolean isVolleyball = false;
    private Timeline matchTimeline;
    private BorderPane mainLayout;
    
    // MVC Controller
    private MatchController controller;

    // ─── Futbol constructor ───────────────────────────────────────────────────
    public GUIGame(GameFootball match) {
        GUISquadManager.getInstance().redCardedPlayers.clear();
        this.homeTeam = match.getHomeTeam();
        this.awayTeam = match.getAwayTeam();
        this.isVolleyball = false;
        this.controller = new MatchController(this, match);
        show();
    }

    // ─── Voleybol constructor ─────────────────────────────────────────────────
    public GUIGame(GameVolleyball match) {
        this.isVolleyball = true;
        this.homeTeam = match.getHomeTeam();
        this.awayTeam = match.getAwayTeam();
        this.controller = new MatchController(this, match);
        show();
    }

    // ─── UI ───────────────────────────────────────────────────────────────────
    public void show() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1b1b2f;");
        mainLayout.setTop(new HBox());
        mainLayout.setLeft(new VBox());

        HBox matchContent = new HBox(30);
        matchContent.setPadding(new Insets(20));
        matchContent.setAlignment(Pos.CENTER);
        matchContent.getChildren().addAll(createLeftStatsPanel(), createRightEventPanel());
        mainLayout.setCenter(matchContent);

        Button startBtn = new Button("Start Match ▶");
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        startBtn.setOnAction(e -> { mainLayout.setBottom(null); startMatchSimulation(); });

        HBox bottomBox = new HBox(startBtn);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(0, 0, 20, 0));
        mainLayout.setBottom(bottomBox);

        SceneManager.changeScene(mainLayout, "Live Match - " + homeTeam.getName() + " vs " + awayTeam.getName());
    }

    private VBox createLeftStatsPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(550);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #162447; -fx-background-radius: 15; -fx-border-color: #1f4068; -fx-border-width: 2;");

        HBox scoreboard = new HBox(30);
        scoreboard.setAlignment(Pos.CENTER);
        scoreboard.setStyle("-fx-background-color: #1f4068; -fx-padding: 20; -fx-background-radius: 10;");

        VBox homeBox = createTeamHeader(homeTeam, "#e43f5a");
        scoreLabel = new Label();
        scoreLabel.textProperty().bind(controller.scoreProperty());
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        VBox awayBox = createTeamHeader(awayTeam, "#4CAF50");
        scoreboard.getChildren().addAll(homeBox, scoreLabel, awayBox);

        minuteLabel = new Label();
        minuteLabel.textProperty().bind(controller.minuteProperty());
        minuteLabel.setTextFill(Color.web("#f0a500"));
        minuteLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        Label statsTitle = new Label("MATCH STATISTICS");
        statsTitle.setTextFill(Color.WHITE);
        statsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        homePossession = new Label(); awayPossession = new Label();
        homePossession.textProperty().bind(controller.homePossessionProperty());
        awayPossession.textProperty().bind(controller.awayPossessionProperty());
        homeShots = new Label();       awayShots = new Label();
        homeShots.textProperty().bind(controller.homeShotsProperty());
        awayShots.textProperty().bind(controller.awayShotsProperty());
        homeXG = new Label();
        awayXG = new Label();
        homeXG.textProperty().bind(controller.homeXGProperty());
        awayXG.textProperty().bind(controller.awayXGProperty());

        VBox statsList = new VBox(15);
        if (isVolleyball) {
            statsList.getChildren().addAll(
                createStatRow("Sets Won", homeShots, awayShots),
                createStatRow("Total Rallies", homeXG, awayXG)
            );
        } else {
            statsList.getChildren().addAll(
                createStatRow("Possession", homePossession, awayPossession),
                createStatRow("Total Shots", homeShots, awayShots),
                createStatRow("Expected Goals (xG)", homeXG, awayXG)
            );
        }

        Label speedLabel = new Label("Game Speed (1x - 100x)");
        speedLabel.setTextFill(Color.web("#a5a5b0"));
        speedLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        speedSlider = new Slider(1, 100, 100);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(25);
        speedSlider.valueProperty().addListener((obs, o, n) -> {
            if (matchTimeline != null) matchTimeline.setRate(n.doubleValue());
        });

        panel.getChildren().addAll(scoreboard, minuteLabel, statsTitle, statsList, new VBox(5, speedLabel, speedSlider));
        panel.setAlignment(Pos.TOP_CENTER);
        return panel;
    }

    private VBox createRightEventPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(450);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #162447; -fx-background-radius: 15; -fx-border-color: #1f4068; -fx-border-width: 2;");

        Label title = new Label("LIVE COMMENTARY & EVENTS");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        eventLogContainer = new VBox(10);
        eventLogContainer.setPadding(new Insets(10));
        eventScrollPane = new ScrollPane(eventLogContainer);
        eventScrollPane.setFitToWidth(true);
        eventScrollPane.setPrefHeight(500);
        eventScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panel.getChildren().addAll(title, eventScrollPane);
        return panel;
    }

    public void addEvent(String type, String timeText, String description) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER_LEFT);

        String color = "#1f4068", iconText = "ℹ";
        switch (type) {
            case "GOAL":   color = "#4CAF50"; iconText = isVolleyball ? "🏐" : "⚽"; break;
            case "YELLOW": color = "#f0a500"; iconText = "🟨"; break;
            case "RED":    color = "#e43f5a"; iconText = "🟥"; break;
            case "SUB":    color = "#4e4e6a"; iconText = "🔄"; break;
            case "INJURY": color = "#d82bbc"; iconText = "🚑"; break;
        }
        row.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");

        Label timeLbl = new Label(timeText);
        timeLbl.setTextFill(Color.WHITE);
        timeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Label descLbl = new Label(iconText + " " + description);
        descLbl.setTextFill(Color.WHITE);
        if (description.contains("+---") || description.contains("|"))
            descLbl.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        else {
            descLbl.setFont(Font.font("Segoe UI", 14));
            descLbl.setWrapText(true);
        }

        row.getChildren().addAll(timeLbl, descLbl);
        eventLogContainer.getChildren().add(0, row);
    }

    // ─── Ana Simülasyon ───────────────────────────────────────────────────────
    private void startMatchSimulation() {
        if (isVolleyball) {
            controller.initializeVolleyballStats();
            matchTimeline = new Timeline(new KeyFrame(Duration.seconds(0.3), e -> {
                controller.processTick();
            }));
        } else {
            controller.initializeFootballStats();
            matchTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
                controller.processTick();
            }));
        }
        matchTimeline.setCycleCount(Timeline.INDEFINITE);
        matchTimeline.setRate(speedSlider.getValue());
        matchTimeline.play();
    }

    // ─── Controller İçin Public UI Metotları ───────────────────────────────────
    
    public void pauseTimeline() {
        if (matchTimeline != null) matchTimeline.pause();
    }

    // ─── Ortak: Set/Devre arası butonu ───────────────────────────────────────
    public void showBreakButton(String btnText) {
        pauseTimeline();

        Button tacticBtn = new Button(btnText);
        tacticBtn.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        tacticBtn.setOnAction(evt -> {
            mainLayout.setBottom(null);
            GUISquadManager.getInstance().isMidMatch = true;
            GUISquadManager.getInstance().onResumeMatch = () -> {
                GUISquadManager.getInstance().isMidMatch = false;
                
                controller.applyTacticChanges();

                SceneManager.changeScene(mainLayout, "Live Match - " + homeTeam.getName() + " vs " + awayTeam.getName());
                matchTimeline.play();
            };
            new GUITactic(GameContext.getInstance().getPlayerTeam());
        });

        HBox btnContainer = new HBox(tacticBtn);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setPadding(new Insets(20, 0, 10, 0));
        mainLayout.setBottom(btnContainer);
    }

    // ─── Ortak: Maç sonu ─────────────────────────────────────────────────────
    public void endMatch() {
        Button endBtn = new Button("End Match and Continue ▶");
        endBtn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        endBtn.setOnAction(evt -> {
            if (!isVolleyball) GUISquadManager.getInstance().postMatchCleanup();
            if ("VOLLEYBALL".equals(GameContext.getInstance().getActiveSport()) && GameContext.getInstance().getActiveVolleyballCalendar() != null) {
                GameContext.getInstance().getActiveVolleyballCalendar().advanceToNextWeek();
                GUIMain.decrementAllInjuries();
            }
            else if (GameContext.getInstance().getActiveCalendar() != null) {
                GameContext.getInstance().getActiveCalendar().advanceToNextWeek();
                GUIMain.decrementAllInjuries();
            }
            GameContext.getInstance().setMatchDay(false);
            new GUIMain();
        });

        HBox btnContainer = new HBox(endBtn);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setPadding(new Insets(20, 0, 10, 0));
        mainLayout.setBottom(btnContainer);
    }

    // ─── Görsel yardımcılar ───────────────────────────────────────────────────
    private VBox createTeamHeader(ITeam team, String color) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        javafx.scene.Node logo = GUILeftandTopBarHelper.createEmblem(team, 60);
        Label nameLbl = new Label(team.getName());
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8));
        box.getChildren().addAll(logo, nameLbl);
        return box;
    }

    private HBox createStatRow(String title, Label homeVal, Label awayVal) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        homeVal.setTextFill(Color.WHITE);
        awayVal.setTextFill(Color.WHITE);
        Label titleLbl = new Label(title);
        titleLbl.setTextFill(Color.web("#a5a5b0"));
        titleLbl.setPrefWidth(150);
        titleLbl.setAlignment(Pos.CENTER);
        row.getChildren().addAll(homeVal, titleLbl, awayVal);
        return row;
    }
}
