package gui;

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

import Interface.ITeam;
import Sport.Football.CalendarFootball;
import Sport.Volleyball.CalendarVolleyball;
import Sport.Football.GameRulesFootball;
import Sport.Volleyball.GameRulesVolleyball;
import Sport.Football.GameFootball;
import Sport.Volleyball.GameVolleyball;
import Sport.Football.LeagueFootball;
import Sport.Volleyball.LeagueVolleyball;
import io.SaveGame;
import io.SaveManager;

public class GUIMain {

    private Stage primaryStage;
    private BorderPane mainLayout;
    public static ITeam playerTeam;
    public static LeagueFootball activeLeague;
    public static CalendarFootball activeCalendar;
    // Volleyball counterparts
    public static LeagueVolleyball activeVolleyballLeague;
    public static CalendarVolleyball activeVolleyballCalendar;
    public static String activeSport = "FOOTBALL"; // tracks which sport is active
    public static boolean isMatchDay = false;
    public static boolean tacticConfirmedForMatch = false;
    
    public GUIMain(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initialize();
    }

    public static void startNewGame(String sport, Stage stage) {
        activeSport = sport;
        if ("FOOTBALL".equals(sport)) {
            GameRulesFootball rules = new GameRulesFootball();
            activeLeague = new LeagueFootball("Süper Lig", "Türkiye", 10, rules); 
            activeCalendar = new CalendarFootball(rules);
            activeCalendar.generateFixtures(activeLeague.getTeamRanking());
            
            System.out.println("Football game initialized! Transitioning to Team Selection...");
            new GUITeamSelection(stage); 
        } else if ("VOLLEYBALL".equals(sport)) {
            GameRulesVolleyball rules = new GameRulesVolleyball();
            activeVolleyballLeague = new LeagueVolleyball("Voleybol Ligi", "Türkiye", 10, rules);
            activeVolleyballCalendar = new CalendarVolleyball(rules);
            activeVolleyballCalendar.generateFixtures(activeVolleyballLeague.getTeamRanking());

            System.out.println("Volleyball game initialized! Transitioning to Team Selection...");
            new GUITeamSelection(stage);
        } else {
            System.out.println(sport + " is not fully implemented yet.");
        }
    }

    public static void loadSavedGame(SaveGame saveGame, Stage stage) {
        activeLeague = saveGame.getCurrentLeague();
        activeCalendar = saveGame.getCalendar();
        playerTeam = saveGame.getPlayerTeam();
        
        // Taktik verilerini yükle (Statik değişkenlere aktar)
        gui.GUISquadManager.loadTacticData(saveGame.getPitchPlayers(), saveGame.getPlayersOnPitchQueue(), saveGame.getReservePlayersQueue(), playerTeam, saveGame.getTacticStyle());
        
        System.out.println("Game Loaded successfully! Transitioning to Menu...");
        new GUIMain(stage);
    }

    public static void handleContinueAction(Stage primaryStage) {
        if (!isMatchDay) {
            if (playerTeam != null) GUITraining.applyWeeklyTrainingStatically(playerTeam);
            isMatchDay = true;
            tacticConfirmedForMatch = false;
            new GUIMain(primaryStage);
        } else {
            if (!tacticConfirmedForMatch) {
                new GUITactic(primaryStage, playerTeam);
                return;
            }

            // ── VOLLEYBALL branch ─────────────────────────────────────────────
            if ("VOLLEYBALL".equals(activeSport)) {
                if (activeVolleyballCalendar != null && playerTeam != null) {
                    int currentWeek = activeVolleyballCalendar.getCurrentWeek() + 1;
                    java.util.Map<Integer, java.util.List<Classes.Game>> schedule = activeVolleyballCalendar.getSchedule();

                    if (schedule != null && schedule.containsKey(currentWeek)) {
                        java.util.List<Classes.Game> weekGames = schedule.get(currentWeek);
                        Classes.Game playerGame = null;

                        for (Classes.Game g : weekGames) {
                            if (g.getHomeTeam().equals(playerTeam) || g.getAwayTeam().equals(playerTeam)) {
                                playerGame = g;
                            } else {
                                if (!g.isCompleted()) g.play();
                            }
                        }

                        if (playerGame != null && !playerGame.isCompleted()) {
                            Classes.GameRules rules = new Sport.Volleyball.GameRulesVolleyball();
                            if (gui.GUISquadManager.getPlayersOnPitchQueue().size() != rules.getFieldPlayerCount()) {
                                tacticConfirmedForMatch = false;
                                gui.GUIPopup.showMessage(primaryStage, "Incomplete Squad", "Squad Incomplete!", "Starting " + rules.getFieldPlayerCount() + " must be full to play the match! Please set your squad.");
                                new gui.GUITactic(primaryStage, playerTeam);
                                return;
                            }
                            
                            boolean hasInjuredStarter = false;
                            for (Interface.IPlayer p : gui.GUISquadManager.getPlayersOnPitchQueue()) {
                                if (p.isInjured()) { hasInjuredStarter = true; break; }
                            }
                            if (hasInjuredStarter) {
                                tacticConfirmedForMatch = false;
                                gui.GUIPopup.showMessage(primaryStage, "Injured Player", "Injured Player in Starting " + rules.getFieldPlayerCount() + "!", "There must be no injured players on the pitch to play the match. Please substitute the injured player.");
                                new gui.GUITactic(primaryStage, playerTeam);
                                return;
                            }
                            
                            String validationMsg = gui.GUISquadManager.getFormationValidationMessage();
                            if (validationMsg != null) {
                                tacticConfirmedForMatch = false;
                                gui.GUIPopup.showMessage(primaryStage, "Invalid Formation", "Formation Rule Violated!", validationMsg);
                                new gui.GUITactic(primaryStage, playerTeam);
                                return;
                            }
                            
                            final Classes.Game finalPlayerGame = playerGame;
                            if (gui.GUISquadManager.getReservePlayersQueue().size() != rules.getReservePlayerCount()) {
                                gui.GUIPopup.showConfirmation(primaryStage, "Incomplete Bench", "Bench is not full!", 
                                    "There are not " + rules.getReservePlayerCount() + " players on the bench. Do you still want to play the match?", 
                                    () -> new GUIGame(primaryStage, (GameVolleyball) finalPlayerGame),
                                    () -> { tacticConfirmedForMatch = false; new gui.GUITactic(primaryStage, playerTeam); }
                                );
                                return;
                            }
                            
                            new GUIGame(primaryStage, (GameVolleyball) playerGame);
                        } else {
                            activeVolleyballCalendar.advanceToNextWeek();
                            isMatchDay = false;
                            tacticConfirmedForMatch = false;
                            new GUIMain(primaryStage);
                        }
                    }
                }
                return;
            }

            // ── FOOTBALL branch (original logic) ─────────────────────────────
            if (activeCalendar != null && playerTeam != null) {
                int currentWeek = activeCalendar.getCurrentWeek() + 1;
                java.util.Map<Integer, java.util.List<Classes.Game>> schedule = activeCalendar.getSchedule();
                
                if (schedule != null && schedule.containsKey(currentWeek)) {
                    java.util.List<Classes.Game> weekGames = schedule.get(currentWeek);
                    Classes.Game playerGame = null;
                    
                    for (Classes.Game g : weekGames) {
                        if (g.getHomeTeam().equals(playerTeam) || g.getAwayTeam().equals(playerTeam)) {
                            playerGame = g;
                        } else {
                            if (!g.isCompleted()) g.play();
                        }
                    }
                    
                    if (playerGame != null && !playerGame.isCompleted()) {
                        Classes.GameRules rules = "VOLLEYBALL".equals(activeSport) ? new Sport.Volleyball.GameRulesVolleyball() : new Sport.Football.GameRulesFootball();
                        if (gui.GUISquadManager.getPlayersOnPitchQueue().size() != (rules.getFieldPlayerCount() - gui.GUISquadManager.redCardedPlayers.size())) {
                            tacticConfirmedForMatch = false;
                            gui.GUIPopup.showMessage(primaryStage, "Incomplete Squad", "Squad Incomplete!", "Starting " + rules.getFieldPlayerCount() + " must be full to play the match! Please set your squad.");
                            new gui.GUITactic(primaryStage, playerTeam);
                            return;
                        }
                        
                        boolean hasInjuredStarter = false;
                        for (Interface.IPlayer p : gui.GUISquadManager.getPlayersOnPitchQueue()) {
                            if (p.isInjured()) { hasInjuredStarter = true; break; }
                        }
                        if (hasInjuredStarter) {
                            tacticConfirmedForMatch = false;
                            gui.GUIPopup.showMessage(primaryStage, "Injured Player", "Injured Player in Starting " + rules.getFieldPlayerCount() + "!", "There must be no injured players on the pitch to play the match. Please substitute the injured player.");
                            new gui.GUITactic(primaryStage, playerTeam);
                            return;
                        }
                        
                        String validationMsg = gui.GUISquadManager.getFormationValidationMessage();
                        if (validationMsg != null) {
                            tacticConfirmedForMatch = false;
                            gui.GUIPopup.showMessage(primaryStage, "Invalid Formation", "Formation Rule Violated!", validationMsg);
                            new gui.GUITactic(primaryStage, playerTeam);
                            return;
                        }
                        
                        final Classes.Game finalPlayerGame = playerGame;
                        if (gui.GUISquadManager.getReservePlayersQueue().size() != rules.getReservePlayerCount()) {
                            gui.GUIPopup.showConfirmation(primaryStage, "Incomplete Bench", "Bench is not full!", 
                                "There are not " + rules.getReservePlayerCount() + " players on the bench. Do you still want to play the match?", 
                                () -> new GUIGame(primaryStage, (GameFootball) finalPlayerGame),
                                () -> { tacticConfirmedForMatch = false; new gui.GUITactic(primaryStage, playerTeam); }
                            );
                            return;
                        }
                        
                        new GUIGame(primaryStage, (GameFootball) playerGame);
                    } else {
                        activeCalendar.advanceToNextWeek();
                        isMatchDay = false;
                        tacticConfirmedForMatch = false;
                        new GUIMain(primaryStage);
                    }
                }
            }
        }
    }

    private void initialize() {
        mainLayout = new BorderPane();
        // Modern, koyu temalı arka plan
        mainLayout.setStyle("-fx-background-color: #1b1b2f;");

        // Panellerin Eklenmesi
        mainLayout.setTop(GUILeftandTopBarHelper.createTopBar(primaryStage, null));
        mainLayout.setLeft(GUILeftandTopBarHelper.createSidebar(primaryStage, "Home"));
        mainLayout.setCenter(createDashboard());

        primaryStage.setTitle("Sports Manager - Dashboard");
        
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(mainLayout, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(mainLayout);
        }
        
        primaryStage.show();
    }

    private VBox createDashboard() {
        VBox dashboard = new VBox(25);
        dashboard.setPadding(new Insets(30));

        Label welcomeLabel = new Label("Management Summary");
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        VBox nextMatchWidget = createNextMatchWidget();
        VBox trainingWidget = createTrainingPerformanceWidget();
        VBox injuryWidget = createInjuryWidget();

        HBox bottomWidgetsBox = new HBox(25);
        bottomWidgetsBox.getChildren().addAll(trainingWidget, injuryWidget);

        dashboard.getChildren().addAll(welcomeLabel, nextMatchWidget, bottomWidgetsBox);
        
        return dashboard;
    }

    private VBox createNextMatchWidget() {
        VBox widget = createBaseWidget("Next Match", "#e43f5a");
        widget.setPrefWidth(725);
        
        HBox matchLayout = new HBox(60);
        matchLayout.setAlignment(Pos.CENTER);
        VBox.setVgrow(matchLayout, Priority.ALWAYS);

        // Pick the right calendar depending on active sport
        Classes.Calendar cal = "VOLLEYBALL".equals(activeSport) ? activeVolleyballCalendar : activeCalendar;

        if (cal != null && playerTeam != null) {
            int week = cal.getCurrentWeek() + 1;
            java.util.Map<Integer, java.util.List<Classes.Game>> schedule = cal.getSchedule();
            if (schedule != null && schedule.containsKey(week)) {
                Classes.Game nextGame = null;
                for (Classes.Game g : schedule.get(week)) {
                    if (g.getHomeTeam().equals(playerTeam) || g.getAwayTeam().equals(playerTeam)) {
                        nextGame = g;
                        break;
                    }
                }
                
                if (nextGame != null) {
                    VBox homeBox = new VBox(15);
                    homeBox.setAlignment(Pos.CENTER);
                    javafx.scene.Node homeEmblem = GUILeftandTopBarHelper.createEmblem(nextGame.getHomeTeam(), 80);
                    Label homeName = new Label(nextGame.getHomeTeam().getName());
                    homeName.setTextFill(Color.WHITE);
                    homeName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                    homeBox.getChildren().addAll(homeEmblem, homeName);

                    VBox vsBox = new VBox(10);
                    vsBox.setAlignment(Pos.CENTER);
                    Label weekLabel = new Label("Week " + week);
                    weekLabel.setTextFill(Color.web("#a5a5b0"));
                    weekLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                    Label vsLabel = new Label("VS");
                    vsLabel.setTextFill(Color.web("#e43f5a"));
                    vsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
                    vsBox.getChildren().addAll(weekLabel, vsLabel);

                    VBox awayBox = new VBox(15);
                    awayBox.setAlignment(Pos.CENTER);
                    javafx.scene.Node awayEmblem = GUILeftandTopBarHelper.createEmblem(nextGame.getAwayTeam(), 80);
                    Label awayName = new Label(nextGame.getAwayTeam().getName());
                    awayName.setTextFill(Color.WHITE);
                    awayName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                    awayBox.getChildren().addAll(awayEmblem, awayName);

                    matchLayout.getChildren().addAll(homeBox, vsBox, awayBox);
                    widget.getChildren().add(matchLayout);
                    return widget;
                }
            } else if (week > cal.getSchedule().size()) {
                Label contentLabel = new Label("Season Ended.");
                contentLabel.setTextFill(Color.web("#e0e0e0"));
                contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                matchLayout.getChildren().add(contentLabel);
                widget.getChildren().add(matchLayout);
                return widget;
            }
        }
        
        Label contentLabel = new Label("No match found.");
        contentLabel.setTextFill(Color.web("#e0e0e0"));
        contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        matchLayout.getChildren().add(contentLabel);
        widget.getChildren().add(matchLayout);
        return widget;
    }

    private VBox createTrainingPerformanceWidget() {
        VBox widget = createBaseWidget("Top Improvers (Weekly)", "#4CAF50");
        VBox list = new VBox(8);
        
        if (playerTeam != null && playerTeam.getPlayers() != null) {
            java.util.List<Interface.IPlayer> players = new java.util.ArrayList<>(playerTeam.getPlayers());
            players.sort((p1, p2) -> {
                double gain1 = p1.calculateOverallRating() - gui.GUITraining.oldOvrMap.getOrDefault(p1, p1.calculateOverallRating());
                double gain2 = p2.calculateOverallRating() - gui.GUITraining.oldOvrMap.getOrDefault(p2, p2.calculateOverallRating());
                return Double.compare(gain2, gain1);
            });
            
            int count = 0;
            for (Interface.IPlayer p : players) {
                double gain = p.calculateOverallRating() - gui.GUITraining.oldOvrMap.getOrDefault(p, p.calculateOverallRating());
                if (gain > 0) {
                    Label l = new Label("↑ " + p.getFullName() + " (+ " + String.format(java.util.Locale.US, "%.1f", gain) + " OVR)");
                    l.setTextFill(Color.web("#4CAF50"));
                    l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    list.getChildren().add(l);
                    count++;
                }
                if (count >= 5) break;
            }
            if (count == 0) {
                Label l = new Label("No training data yet or\nno players improved.");
                l.setTextFill(Color.web("#a5a5b0"));
                l.setFont(Font.font("Segoe UI", 14));
                list.getChildren().add(l);
            }
        }
        widget.getChildren().add(list);
        return widget;
    }

    private VBox createInjuryWidget() {
        VBox widget = createBaseWidget("Injured Players", "#f0a500");
        VBox list = new VBox(8);
        
        if (playerTeam != null && playerTeam.getPlayers() != null) {
            int count = 0;
            for (Interface.IPlayer p : playerTeam.getPlayers()) {
                if (p.isInjured()) {
                    Label l = new Label("🚑 " + p.getFullName() + " (" + p.getInjuryDuration() + " Weeks)");
                    l.setTextFill(Color.web("#e43f5a"));
                    l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    list.getChildren().add(l);
                    count++;
                }
            }
            if (count == 0) {
                Label l = new Label("No injured players in the team.");
                l.setTextFill(Color.web("#4CAF50"));
                l.setFont(Font.font("Segoe UI", 14));
                list.getChildren().add(l);
            }
        }
        
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefHeight(180);
        
        widget.getChildren().add(scroll);
        return widget;
    }

    private VBox createBaseWidget(String title, String accentColor) {
        VBox widget = new VBox(15);
        widget.setPadding(new Insets(20));
        widget.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
        widget.setPrefWidth(350);
        widget.setPrefHeight(250);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(accentColor));
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        widget.getChildren().add(titleLabel);
        return widget;
    }
}