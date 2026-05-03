package gui;

import Interface.IPlayer;
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
import Sport.Football.GameRulesFootball;
import Sport.Volleyball.GameRulesVolleyball;
import Sport.Football.TacticFootball;
import Sport.Volleyball.TacticVolleyball;
import Sport.Football.AIAdaptableEasyFootball;
import Sport.Volleyball.AIAdaptableEasyVolleyball;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GUIGame {

    private Stage primaryStage;
    private ITeam homeTeam;
    private ITeam awayTeam;

    private int homeScore = 0;
    private int awayScore = 0;
    private int minute = 0;

    private Label scoreLabel;
    private Label minuteLabel;
    private VBox eventLogContainer;
    private ScrollPane eventScrollPane;

    private Label homePossession, awayPossession;
    private Label homeShots, awayShots;
    private Label homeXG, awayXG;
    private Slider speedSlider;

    private GameFootball match;
    private GameVolleyball volleyballMatch;
    private boolean isVolleyball = false;
    private List<String> matchLogs;

    // Voleybol set takibi
    private int currentSetNumber = 0;

    private int currentLogIndex = 0;
    private int possH = 50;
    private Timeline matchTimeline;
    private BorderPane mainLayout;
    private boolean isMatchEnded = false;
    private boolean isSetBreak = false;  // set arası bekleme flag'i
    private int totalMatchMinutes = 90;
    private int periodDuration = 45;

    // ─── Futbol constructor ───────────────────────────────────────────────────
    public GUIGame(Stage primaryStage, GameFootball match) {
        GUISquadManager.redCardedPlayers.clear();
        this.primaryStage = primaryStage;
        this.match = match;
        this.homeTeam = match.getHomeTeam();
        this.awayTeam = match.getAwayTeam();
        setupGameLogic();
        show();
    }

    // ─── Voleybol constructor ─────────────────────────────────────────────────
    public GUIGame(Stage primaryStage, GameVolleyball match) {
        this.primaryStage = primaryStage;
        this.volleyballMatch = match;
        this.isVolleyball = true;
        this.homeTeam = match.getHomeTeam();
        this.awayTeam = match.getAwayTeam();
        setupVolleyballLogic();
        show();
    }

    // ─── Setup: Football ──────────────────────────────────────────────────────
    private void setupGameLogic() {
        Classes.GameRules rules = match.getRules();
        this.periodDuration = rules.getPeriodDuration();
        this.totalMatchMinutes = rules.getPeriodCount() * this.periodDuration;

        match.play();
        matchLogs = match.getGameLog();
    }

    // ─── Setup: Volleyball ────────────────────────────────────────────────────
    private void setupVolleyballLogic() {
        Classes.GameRules rules = volleyballMatch.getRules();
        this.periodDuration = rules.getPeriodDuration();       // 30 rally ticks per set
        this.totalMatchMinutes = rules.getPeriodCount() * this.periodDuration; // 150

        volleyballMatch.play();
        matchLogs = volleyballMatch.getGameLog();
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

        primaryStage.setTitle("Live Match - " + homeTeam.getName() + " vs " + awayTeam.getName());
        if (primaryStage.getScene() == null) primaryStage.setScene(new Scene(mainLayout, 1280, 720));
        else primaryStage.getScene().setRoot(mainLayout);
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
        scoreLabel = new Label("0 - 0");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        VBox awayBox = createTeamHeader(awayTeam, "#4CAF50");
        scoreboard.getChildren().addAll(homeBox, scoreLabel, awayBox);

        minuteLabel = new Label(isVolleyball ? "SET 0" : "00:00");
        minuteLabel.setTextFill(Color.web("#f0a500"));
        minuteLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        Label statsTitle = new Label("MATCH STATISTICS");
        statsTitle.setTextFill(Color.WHITE);
        statsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        homePossession = new Label("50%"); awayPossession = new Label("50%");
        homeShots = new Label("0");       awayShots = new Label("0");
        homeXG = new Label("0.00");       awayXG = new Label("0.00");

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

    private void addEvent(String type, String description) {
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

        String timeText = isVolleyball ? ("S" + currentSetNumber) : (minute + "'");
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
            startVolleyballSimulation();
        } else {
            startFootballSimulation();
        }
    }

    // ═══ FUTBOL SİMÜLASYONU ══════════════════════════════════════════════════
    private void startFootballSimulation() {
        while (currentLogIndex < matchLogs.size() && !matchLogs.get(currentLogIndex).matches("^\\d+'\\...*"))
            addEvent("INFO", matchLogs.get(currentLogIndex++));

        homeXG.setText(String.format("%.2f", match.getHomeXG()));
        awayXG.setText(String.format("%.2f", match.getAwayXG()));
        possH = match.getHomePossession();
        homePossession.setText(possH + "%");
        awayPossession.setText((100 - possH) + "%");

        matchTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            if (isMatchEnded) return;

            minute++;
            minuteLabel.setText(String.format("%02d:00", minute));

            Random rand = new Random();
            int shotsHome = Integer.parseInt(homeShots.getText());
            int shotsAway = Integer.parseInt(awayShots.getText());
            if (rand.nextDouble() < match.getHomeShotChance(possH)) shotsHome++;
            if (rand.nextDouble() < match.getAwayShotChance(possH)) shotsAway++;
            homeShots.setText(String.valueOf(shotsHome));
            awayShots.setText(String.valueOf(shotsAway));

            // Log işleme
            while (currentLogIndex < matchLogs.size()) {
                String log = matchLogs.get(currentLogIndex);
                if (log.startsWith(minute + "'.")) {
                    String type = match.getEventType(log);
                    if (type.equals("GOAL")) {
                        if (log.contains(homeTeam.getName())) homeScore++;
                        else awayScore++;
                        updateScore();
                    } else if (type.equals("YELLOW")) {
                        if (log.contains(GUIMain.playerTeam.getName()) && log.contains("Oyuncu: ")) {
                            String pName = log.substring(log.indexOf("Oyuncu: ") + 8).trim();
                            for (IPlayer p : GUIMain.playerTeam.getPlayers())
                                if (p.getFullName().equals(pName)) { GUISquadManager.yellowCardedPlayers.add(p); break; }
                        }
                    } else if (type.equals("RED")) {
                        if (log.contains(GUIMain.playerTeam.getName()) && log.contains("Oyundan Atılan: ")) {
                            String pName = log.substring(log.indexOf("Oyundan Atılan: ") + 16).trim();
                            for (IPlayer p : GUIMain.playerTeam.getPlayers())
                                if (p.getFullName().equals(pName)) { GUISquadManager.applyRedCard(p); break; }
                        }
                    }
                    syncSubAndInjury(log);
                    addEvent(type, log);
                    currentLogIndex++;
                } else if (log.matches("^\\d+'\\..*.")) {
                    try {
                        int logMin = Integer.parseInt(log.substring(0, log.indexOf("'.")));
                        if (logMin > minute) break;
                        else { addEvent("INFO", log); currentLogIndex++; }
                    } catch (Exception ex) { addEvent("INFO", log); currentLogIndex++; }
                } else { addEvent("INFO", log); currentLogIndex++; }
            }

            // Devre arası
            if (minute % periodDuration == 0 && minute < totalMatchMinutes) {
                showBreakButton("Half Time! Go to Tactics ⚙");
            }

            // Maç sonu
            if (minute >= totalMatchMinutes) {
                endMatch();
            }
        }));

        matchTimeline.setCycleCount(Timeline.INDEFINITE);
        matchTimeline.setRate(speedSlider.getValue());
        matchTimeline.play();
    }

    // ═══ VOLEYBOL SİMÜLASYONU ════════════════════════════════════════════════
    private void startVolleyballSimulation() {
        // Başlangıç loglarını göster (SET 1 başlamadan önce)
        while (currentLogIndex < matchLogs.size()) {
            String log = matchLogs.get(currentLogIndex);
            if (log.startsWith("=== SET")) break;
            addEvent("INFO", log);
            currentLogIndex++;
        }

        minuteLabel.setText("SET 1 STARTING");

        matchTimeline = new Timeline(new KeyFrame(Duration.seconds(0.3), e -> {
            if (isMatchEnded || isSetBreak) return;  // set arası veya maç bitti ise işleme

            int processed = 0;
            while (currentLogIndex < matchLogs.size() && processed < 4) {
                String log = matchLogs.get(currentLogIndex);
                String type = volleyballMatch.getEventType(log);

                if (type.equals("GOAL")) {
                    if (log.contains(homeTeam.getName())) homeScore++;
                    else awayScore++;
                    updateScore();
                    homeShots.setText(String.valueOf(homeScore));
                    awayShots.setText(String.valueOf(awayScore));
                }

                if (log.startsWith("=== SET")) {
                    try {
                        String setStr = log.replace("=", "").replace("SET", "").replace("STARTING", "").trim();
                        currentSetNumber = Integer.parseInt(setStr);
                        minuteLabel.setText("SET " + currentSetNumber + " ⚡");
                    } catch (Exception ignored) {}
                }

                addEvent(type, log);
                currentLogIndex++;
                processed++;

                if (log.startsWith("--- SET ") && log.contains("ENDED")) {
                    try {
                        String numStr = log.substring(8, log.indexOf(" ENDED")).trim();
                        currentSetNumber = Integer.parseInt(numStr);
                        minuteLabel.setText("SET " + currentSetNumber + " ENDED");
                    } catch (Exception ignored) {}

                    if (homeScore < 3 && awayScore < 3) {
                        isSetBreak = true;  // flag — sonraki tick'leri engelle
                        showBreakButton("SET " + currentSetNumber + " ENDED  —  Set Break Tactics ⚙");
                    }
                    break;
                }
            }

            if (currentLogIndex >= matchLogs.size()) {
                endMatch();
            }
        }));

        matchTimeline.setCycleCount(Timeline.INDEFINITE);
        matchTimeline.setRate(speedSlider.getValue());
        matchTimeline.play();
    }

    // ─── Ortak: Set/Devre arası butonu ───────────────────────────────────────
    private void showBreakButton(String btnText) {
        matchTimeline.pause();

        Button tacticBtn = new Button(btnText);
        tacticBtn.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        tacticBtn.setOnAction(evt -> {
            mainLayout.setBottom(null);
            GUISquadManager.isMidMatch = true;
            GUISquadManager.onResumeMatch = () -> {
                GUISquadManager.isMidMatch = false;
                isSetBreak = false;  // set arası bitti, simülasyon devam edebilir

                // Oyuncunun seçtiği kadroyu/taktiği maça yansıt
                if (isVolleyball) {
                    Interface.ITactic playerTactic = homeTeam.equals(GUIMain.playerTeam)
                            ? volleyballMatch.getHomeTactic()
                            : volleyballMatch.getAwayTactic();
                    if (homeTeam.equals(GUIMain.playerTeam) && volleyballMatch.getHomeManager() instanceof Classes.HumanManager hm) {
                        hm.applyChangesFromGUI(playerTactic);
                    } else if (awayTeam.equals(GUIMain.playerTeam) && volleyballMatch.getAwayManager() instanceof Classes.HumanManager hm) {
                        hm.applyChangesFromGUI(playerTactic);
                    }
                } else {
                    Interface.ITactic playerTactic = homeTeam.equals(GUIMain.playerTeam)
                            ? match.getHomeTactic()
                            : match.getAwayTactic();
                    if (homeTeam.equals(GUIMain.playerTeam) && match.getHomeManager() instanceof Classes.HumanManager hm) {
                        hm.applyChangesFromGUI(playerTactic);
                    } else if (awayTeam.equals(GUIMain.playerTeam) && match.getAwayManager() instanceof Classes.HumanManager hm) {
                        hm.applyChangesFromGUI(playerTactic);
                    }
                }

                primaryStage.getScene().setRoot(mainLayout);
                primaryStage.setTitle("Live Match - " + homeTeam.getName() + " vs " + awayTeam.getName());
                matchTimeline.play();
            };
            new GUITactic(primaryStage, GUIMain.playerTeam);
        });

        HBox btnContainer = new HBox(tacticBtn);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setPadding(new Insets(20, 0, 10, 0));
        mainLayout.setBottom(btnContainer);
    }

    // ─── Ortak: Maç sonu ─────────────────────────────────────────────────────
    private void endMatch() {
        isMatchEnded = true;
        matchTimeline.stop();
        while (currentLogIndex < matchLogs.size())
            addEvent("INFO", matchLogs.get(currentLogIndex++));

        Button endBtn = new Button("End Match and Continue ▶");
        endBtn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        endBtn.setOnAction(evt -> {
            if (!isVolleyball) GUISquadManager.postMatchCleanup();
            if ("VOLLEYBALL".equals(GUIMain.activeSport) && GUIMain.activeVolleyballCalendar != null)
                GUIMain.activeVolleyballCalendar.advanceToNextWeek();
            else if (GUIMain.activeCalendar != null)
                GUIMain.activeCalendar.advanceToNextWeek();
            GUIMain.isMatchDay = false;
            new GUIMain(primaryStage);
        });

        HBox btnContainer = new HBox(endBtn);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setPadding(new Insets(20, 0, 10, 0));
        mainLayout.setBottom(btnContainer);
    }

    // ─── Yardımcı: Değişiklik/Sakatlık sync (futbol) ────────────────────────
    private void syncSubAndInjury(String log) {
        if (GUIMain.playerTeam == null) return;
        if (log.contains("Çıkan: ") && log.contains("Giren: ") && log.contains(GUIMain.playerTeam.getName())) {
            String outName = log.substring(log.indexOf("Çıkan: ") + 7, log.indexOf(" | Giren: ")).trim();
            String inName  = log.substring(log.indexOf("Giren: ") + 7).trim();
            IPlayer pOut = null, pIn = null;
            for (IPlayer p : GUIMain.playerTeam.getPlayers()) {
                if (p.getFullName().equals(outName)) pOut = p;
                if (p.getFullName().equals(inName))  pIn  = p;
            }
            if (pOut != null && pIn != null) GUISquadManager.performAutomaticSub(pOut, pIn);
        } else if (log.contains("SAKATLIK!") && log.contains("Sakatlanan: ") && log.contains(GUIMain.playerTeam.getName())) {
            String injName = log.substring(log.indexOf("Sakatlanan: ") + 12, log.indexOf(". Oyuncu tedavi")).trim();
            for (IPlayer p : GUIMain.playerTeam.getPlayers()) {
                if (p.getFullName().equals(injName)) { GUISquadManager.performAutomaticInjuryRemoval(p); break; }
            }
        }
    }

    private void updateScore() {
        scoreLabel.setText(homeScore + " - " + awayScore);
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
