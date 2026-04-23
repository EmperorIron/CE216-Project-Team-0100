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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import Sport.GameFootball;
import Sport.GameRulesFootball;
import Sport.TacticFootball;
import Sport.AIAdaptableEasyFootball;
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
    
    // İstatistik Labelları (Sol Taraf)
    private Label homePossession, awayPossession;
    private Label homeShots, awayShots;
    private Label homeXG, awayXG;

    private GameFootball match;
    private List<String> matchLogs;
    private int currentLogIndex = 0;
    private int possH = 50;
    private Timeline matchTimeline;
    private BorderPane mainLayout;
    private boolean isMatchEnded = false;
    private int totalMatchMinutes = 90;
    private int periodDuration = 45;

    public GUIGame(Stage primaryStage, GameFootball match) {
        GUITactic.redCardedPlayers.clear();
        this.primaryStage = primaryStage;
        this.match = match;
        this.homeTeam = match.getHomeTeam();
        this.awayTeam = match.getAwayTeam();
        
        setupGameLogic();
        
        show();
        startMatchSimulation();
    }

    private void setupGameLogic() {
        GameRulesFootball rules = new GameRulesFootball();
        this.periodDuration = rules.getPeriodDuration();
        this.totalMatchMinutes = rules.getPeriodCount() * this.periodDuration;

        if (homeTeam.equals(GUIMain.playerTeam) && !GUITactic.getPlayersOnPitchQueue().isEmpty()) {
            match.setHomeManager(new Interface.IManager() {
                @Override public ITeam getTeam() { return homeTeam; }
                @Override public Interface.ITactic generateStartingTactic() {
                    TacticFootball t = new TacticFootball("1-4-4-2");
                    t.setStartingLineup(new ArrayList<>(GUITactic.getPlayersOnPitchQueue()));
                    t.setSubstitutes(new ArrayList<>(GUITactic.getReservePlayersQueue()));
                    t.applyTacticStyle(GUITactic.getCurrentTacticStyle());
                    return t;
                }
                @Override public void handlePeriodBreak(Interface.IGame g, Interface.ITactic t, int p) {}
            });
        } else {
            match.setHomeManager(new AIAdaptableEasyFootball(homeTeam));
        }

        if (awayTeam.equals(GUIMain.playerTeam) && !GUITactic.getPlayersOnPitchQueue().isEmpty()) {
            match.setAwayManager(new Interface.IManager() {
                @Override public ITeam getTeam() { return awayTeam; }
                @Override public Interface.ITactic generateStartingTactic() {
                    TacticFootball t = new TacticFootball("1-4-4-2");
                    t.setStartingLineup(new ArrayList<>(GUITactic.getPlayersOnPitchQueue()));
                    t.setSubstitutes(new ArrayList<>(GUITactic.getReservePlayersQueue()));
                    t.applyTacticStyle(GUITactic.getCurrentTacticStyle());
                    return t;
                }
                @Override public void handlePeriodBreak(Interface.IGame g, Interface.ITactic t, int p) {}
            });
        } else {
            match.setAwayManager(new AIAdaptableEasyFootball(awayTeam));
        }

        match.play();
        matchLogs = match.getGameLog();
    }

    public void show() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1b1b2f;");

        // Üst Bar ve Sol Menü Entegrasyonu
        mainLayout.setTop(createTopBar());
        mainLayout.setLeft(createSidebar());

        HBox matchContent = new HBox(30);
        matchContent.setPadding(new Insets(20));
        matchContent.setAlignment(Pos.CENTER);

        // --- SOL TARAF: SKORBOARD VE İSTATİSTİKLER (Görsele Uygun) ---
        VBox leftPanel = createLeftStatsPanel();
        
        // --- SAĞ TARAF: CANLI OLAYLAR (Gol, Kart, Değişiklik) ---
        VBox rightPanel = createRightEventPanel();

        matchContent.getChildren().addAll(leftPanel, rightPanel);
        mainLayout.setCenter(matchContent);

        primaryStage.setTitle("Canlı Maç - " + homeTeam.getName() + " vs " + awayTeam.getName());
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(mainLayout, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(mainLayout);
        }
    }

    private VBox createLeftStatsPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(550);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #162447; -fx-background-radius: 15; -fx-border-color: #1f4068; -fx-border-width: 2;");

        // Skorboard Alanı
        HBox scoreboard = new HBox(30);
        scoreboard.setAlignment(Pos.CENTER);
        scoreboard.setStyle("-fx-background-color: #1f4068; -fx-padding: 20; -fx-background-radius: 10;");

        VBox homeBox = createTeamHeader(homeTeam.getName(), "#e43f5a");
        scoreLabel = new Label("0 - 0");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        VBox awayBox = createTeamHeader(awayTeam.getName(), "#4CAF50");

        scoreboard.getChildren().addAll(homeBox, scoreLabel, awayBox);

        // Dakika ve Durum
        minuteLabel = new Label("00:00");
        minuteLabel.setTextFill(Color.web("#f0a500"));
        minuteLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        
        // İstatistikler Başlığı
        Label statsTitle = new Label("MAÇ İSTATİSTİKLERİ");
        statsTitle.setTextFill(Color.WHITE);
        statsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        VBox statsList = new VBox(15);
        homePossession = new Label("50%"); awayPossession = new Label("50%");
        homeShots = new Label("0"); awayShots = new Label("0");
        homeXG = new Label("0.00"); awayXG = new Label("0.00");

        statsList.getChildren().addAll(
            createStatRow("Topla Oynama", homePossession, awayPossession),
            createStatRow("Toplam Şut", homeShots, awayShots),
            createStatRow("Gol Beklentisi (xG)", homeXG, awayXG)
        );

        panel.getChildren().addAll(scoreboard, minuteLabel, statsTitle, statsList);
        panel.setAlignment(Pos.TOP_CENTER);
        return panel;
    }

    private VBox createRightEventPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(450);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #162447; -fx-background-radius: 15; -fx-border-color: #1f4068; -fx-border-width: 2;");

        Label title = new Label("CANLI ANLATIM & OLAYLAR");
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
        
        String color = "#1f4068";
        String iconText = "ℹ";
        
        switch (type) {
            case "GOAL": color = "#4CAF50"; iconText = "⚽"; break;
            case "YELLOW": color = "#f0a500"; iconText = "🟨"; break;
            case "RED": color = "#e43f5a"; iconText = "🟥"; break;
            case "SUB": color = "#4e4e6a"; iconText = "🔄"; break;
            case "INJURY": color = "#d82bbc"; iconText = "🚑"; break;
        }

        row.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");

        Label minLbl = new Label(minute + "'");
        minLbl.setTextFill(Color.WHITE);
        minLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Label descLbl = new Label(iconText + " " + description);
        descLbl.setTextFill(Color.WHITE);
        
        if (description.contains("+---") || description.contains("|")) {
            descLbl.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        } else {
            descLbl.setFont(Font.font("Segoe UI", 14));
            descLbl.setWrapText(true);
        }

        row.getChildren().addAll(minLbl, descLbl);
        eventLogContainer.getChildren().add(0, row); // Yeni olayları üste ekle
    }

    private void startMatchSimulation() {
        // Maç başı (Dakika 0) loglarını hemen yazdır
        while (currentLogIndex < matchLogs.size() && !matchLogs.get(currentLogIndex).matches("^\\d+'\\..*")) {
            addEvent("INFO", matchLogs.get(currentLogIndex));
            currentLogIndex++;
        }

        // Maç Başı Gerçekçi İstatistiklerin Hesaplanması
        double hOff = homeTeam.getTotalOffensiveRating();
        double aOff = awayTeam.getTotalOffensiveRating();
        double hDef = homeTeam.getTotalDefensiveRating();
        double aDef = awayTeam.getTotalDefensiveRating();

        double hXG = (hOff / (hOff + aDef)) * 2.25;
        double aXG = (aOff / (aOff + hDef)) * 2.25;
        if (Double.isNaN(hXG) || Double.isInfinite(hXG)) hXG = 1.0;
        if (Double.isNaN(aXG) || Double.isInfinite(aXG)) aXG = 1.0;

        homeXG.setText(String.format("%.2f", hXG));
        awayXG.setText(String.format("%.2f", aXG));

        possH = (int) Math.round((hOff / (hOff + aOff)) * 100);
        if (possH < 10) possH = 50;
        if (possH > 90) possH = 90;
        homePossession.setText(possH + "%");
        awayPossession.setText((100 - possH) + "%");

        matchTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            if (isMatchEnded) return;
            
            minute++;
            minuteLabel.setText(String.format("%02d:00", minute));
            
            Random rand = new Random();
            int shotsHome = Integer.parseInt(homeShots.getText());
            int shotsAway = Integer.parseInt(awayShots.getText());
            if (rand.nextDouble() < (possH / 100.0) * 0.15) shotsHome++;
            if (rand.nextDouble() < ((100 - possH) / 100.0) * 0.15) shotsAway++;
            homeShots.setText(String.valueOf(shotsHome));
            awayShots.setText(String.valueOf(shotsAway));

            boolean pauseForInjury = false;
            boolean pauseForHalfTime = false;
            
            if (minute % periodDuration == 0 && minute < totalMatchMinutes) {
                pauseForHalfTime = true;
            }

            // İlgili dakikadaki motor loglarını işle
            while (currentLogIndex < matchLogs.size()) {
                String log = matchLogs.get(currentLogIndex);
                if (log.startsWith(minute + "'.")) {
                    
                    String type = "INFO";
                    if (log.contains("GOOOAALLL")) {
                        type = "GOAL";
                        if (log.contains(homeTeam.getName())) homeScore++;
                        else awayScore++;
                        updateScore();
                    } else if (log.contains("SARI KART!") && !log.contains("KIRMIZI KART!")) { 
                        type = "YELLOW";
                        if (log.contains(GUIMain.playerTeam.getName())) {
                            for (IPlayer p : GUIMain.playerTeam.getPlayers()) {
                                if (log.contains(p.getFullName())) {
                                    if (!GUITactic.yellowCardedPlayers.contains(p)) {
                                        GUITactic.yellowCardedPlayers.add(p);
                                    }
                                }
                            }
                        }
                    } else if (log.contains("KIRMIZI KART!")) {
                        type = "RED";
                        if (log.contains(GUIMain.playerTeam.getName())) {
                            for (IPlayer p : GUIMain.playerTeam.getPlayers()) {
                                if (log.contains(p.getFullName())) {
                                    GUITactic.applyRedCard(p);
                                }
                            }
                        }
                    } else if (log.contains("Sakatlık") || log.contains("MECBURİ") || log.contains("SAKATLIK!")) {
                        type = "INJURY";
                        if (log.contains(GUIMain.playerTeam.getName())) {
                            pauseForInjury = true;
                        }
                    } else if (log.contains("Değişiklik")) {
                        type = "SUB";
                    }
                    
                    // Oyuncu değişikliği loglarını (Sakatlık dahil) GUITactic'e senkronize et
                    if (log.contains("Çıkan: ") && log.contains("Giren: ") && log.contains(GUIMain.playerTeam.getName())) {
                        IPlayer pOut = null;
                        IPlayer pIn = null;
                        for (IPlayer p : GUIMain.playerTeam.getPlayers()) {
                            if (log.contains("Çıkan: " + p.getFullName())) pOut = p;
                            if (log.contains("Giren: " + p.getFullName())) pIn = p;
                        }
                        if (pOut != null && pIn != null) {
                            if (type.equals("INJURY") && pOut instanceof Classes.Player) {
                                ((Classes.Player) pOut).setInjuryDuration(1); // Gerçekten sakatla (Örn: 1 haftalık)
                            }
                            GUITactic.performAutomaticSub(pOut, pIn);
                        }
                    } else if (log.contains("SAKATLIK!") && log.contains("Sakatlanan: ") && log.contains(GUIMain.playerTeam.getName())) {
                        IPlayer pInjured = null;
                        for (IPlayer p : GUIMain.playerTeam.getPlayers()) {
                            if (log.contains("Sakatlanan: " + p.getFullName())) pInjured = p;
                        }
                        if (pInjured != null) {
                            GUITactic.performAutomaticInjuryRemoval(pInjured);
                        }
                    }

                    addEvent(type, log);
                    currentLogIndex++;
                    
                } else if (log.matches("^\\d+'\\..*")) {
                    try {
                        int logMin = Integer.parseInt(log.substring(0, log.indexOf("'.")));
                        if (logMin > minute) break; 
                        else { addEvent("INFO", log); currentLogIndex++; }
                    } catch (Exception ex) {
                        addEvent("INFO", log); currentLogIndex++;
                    }
                } else {
                    addEvent("INFO", log);
                    currentLogIndex++;
                }
            }

            if (pauseForHalfTime || pauseForInjury) {
                matchTimeline.pause();
                
                String btnText = pauseForHalfTime ? "Devre Arası! Taktik Ekranına Git ⚙" : "Sakatlık Var! Değişiklik İçin Tıklayın ⚙";
                Button tacticBtn = new Button(btnText);
                tacticBtn.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
                tacticBtn.setOnAction(evt -> {
                    mainLayout.setBottom(null); 
                    GUITactic.isMidMatch = true;
                    GUITactic.onResumeMatch = () -> {
                        GUITactic.isMidMatch = false;
                        
                        Interface.ITactic playerTactic = homeTeam.equals(GUIMain.playerTeam) ? match.getHomeTactic() : match.getAwayTactic();
                        playerTactic.setStartingLineup(new ArrayList<>(GUITactic.getPlayersOnPitchQueue()));
                        playerTactic.setSubstitutes(new ArrayList<>(GUITactic.getReservePlayersQueue()));
                        if (playerTactic instanceof TacticFootball) {
                            ((TacticFootball) playerTactic).applyTacticStyle(GUITactic.getCurrentTacticStyle());
                        }

                        primaryStage.getScene().setRoot(mainLayout);
                        primaryStage.setTitle("Canlı Maç - " + homeTeam.getName() + " vs " + awayTeam.getName());
                        matchTimeline.play();
                    };
                    new GUITactic(primaryStage, GUIMain.playerTeam);
                });
                
                HBox btnContainer = new HBox(tacticBtn);
                btnContainer.setAlignment(Pos.CENTER);
                btnContainer.setPadding(new Insets(20, 0, 10, 0));
                mainLayout.setBottom(btnContainer);
            }

            if (minute >= totalMatchMinutes) {
                isMatchEnded = true;
                matchTimeline.stop();
                while (currentLogIndex < matchLogs.size()) {
                    addEvent("INFO", matchLogs.get(currentLogIndex));
                    currentLogIndex++;
                }
                
                Button endMatchBtn = new Button("Maçı Bitir ve Devam Et ▶");
                endMatchBtn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
                endMatchBtn.setOnAction(evt -> {
                    GUITactic.postMatchCleanup();
                    if (GUIMain.activeCalendar != null) GUIMain.activeCalendar.advanceToNextWeek();
                    GUIMain.isMatchDay = false;
                    new GUIMain(primaryStage);
                });
                HBox btnContainer = new HBox(endMatchBtn);
                btnContainer.setAlignment(Pos.CENTER);
                btnContainer.setPadding(new Insets(20, 0, 10, 0));
                mainLayout.setBottom(btnContainer);
            }
        }));
        
        matchTimeline.setCycleCount(Timeline.INDEFINITE);
        matchTimeline.play();
    }

    private void updateScore() {
        scoreLabel.setText(homeScore + " - " + awayScore);
    }

    // --- YARDIMCI GÖRSEL METODLAR ---

    private VBox createTeamHeader(String name, String color) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Circle logo = new Circle(30, Color.web(color));
        Label nameLbl = new Label(name);
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

    // GUIMain'den gelen standart metodlar
    private HBox createTopBar() { /* GUIMain'deki ile aynı */ return new HBox(); }
    private VBox createSidebar() { /* GUIMain'deki ile aynı */ return new VBox(); }
}