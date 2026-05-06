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
import Interface.ISportFactory;
import Classes.GameContext;
import Sport.Football.FootballFactory;
import Sport.Volleyball.VolleyballFactory;
import io.SaveGame;
import io.SaveManager;

public class GUIMain {

    private BorderPane mainLayout;
    
    public GUIMain() {
        initialize();
    }

    public static void startNewGame(String sport) {
        GameContext ctx = GameContext.getInstance();
        ctx.setActiveSport(sport);
        gui.GUITraining.weeklySchedule.clear();
        
        Thread initThread = new Thread(() -> {
            if ("FOOTBALL".equals(sport)) {
                ctx.setSportFactory(new FootballFactory());
                Classes.GameRules rules = ctx.getSportFactory().createGameRules();
                ctx.setActiveLeague(ctx.getSportFactory().createLeague("Süper Lig", "Türkiye", 10, rules)); 
                ctx.setActiveCalendar(ctx.getSportFactory().createCalendar(rules));
                ctx.getActiveCalendar().generateFixtures(ctx.getActiveLeague().getTeamRanking());
                
                javafx.application.Platform.runLater(() -> {
                    new GUITeamSelection(); 
                });
            } else if ("VOLLEYBALL".equals(sport)) {
                ctx.setSportFactory(new VolleyballFactory());
                Classes.GameRules rules = ctx.getSportFactory().createGameRules();
                ctx.setActiveVolleyballLeague(ctx.getSportFactory().createLeague("Voleybol Ligi", "Türkiye", 10, rules));
                ctx.setActiveVolleyballCalendar(ctx.getSportFactory().createCalendar(rules));
                ctx.getActiveVolleyballCalendar().generateFixtures(ctx.getActiveVolleyballLeague().getTeamRanking());
    
                javafx.application.Platform.runLater(() -> {
                    new GUITeamSelection();
                });
            }
        });
        initThread.setDaemon(true);
        initThread.start();
    }

    public static void loadSavedGame(SaveGame saveGame) {
        GameContext ctx = GameContext.getInstance();
        ctx.setActiveLeague(saveGame.getCurrentLeague());
        ctx.setActiveCalendar(saveGame.getCalendar());
        ctx.setPlayerTeam(saveGame.getPlayerTeam());
        gui.GUITraining.weeklySchedule.clear();
        
        if (ctx.getActiveLeague() instanceof Sport.Volleyball.LeagueVolleyball) {
            ctx.setActiveSport("VOLLEYBALL");
            ctx.setSportFactory(new VolleyballFactory());
        } else {
            ctx.setActiveSport("FOOTBALL");
            ctx.setSportFactory(new FootballFactory());
        }
        
        gui.GUISquadManager.getInstance().loadTacticData(saveGame.getPitchPlayers(), saveGame.getPlayersOnPitchQueue(), saveGame.getReservePlayersQueue(), ctx.getPlayerTeam(), saveGame.getTacticStyle());
        
        new GUIMain();
    }

    public static void handleContinueAction() {
        GameContext ctx = GameContext.getInstance();
        if (!ctx.isMatchDay()) {
            Thread trainingThread = new Thread(() -> {
                if (ctx.getPlayerTeam() != null) GUITraining.applyWeeklyTrainingStatically(ctx.getPlayerTeam());
                javafx.application.Platform.runLater(() -> {
                    ctx.setMatchDay(true);
                    ctx.setTacticConfirmedForMatch(false);
                    new GUIMain();
                });
            });
            trainingThread.setDaemon(true);
            trainingThread.start();
        } else {
            if (!ctx.isTacticConfirmedForMatch()) {
                new GUITactic(ctx.getPlayerTeam());
                return;
            }

            Classes.Calendar cal = "VOLLEYBALL".equals(ctx.getActiveSport()) ? ctx.getActiveVolleyballCalendar() : ctx.getActiveCalendar();
            
            if (cal != null && ctx.getPlayerTeam() != null) {
                int currentWeek = cal.getCurrentWeek() + 1;
                java.util.Map<Integer, java.util.List<Classes.Game>> schedule = cal.getSchedule();

                if (schedule != null && schedule.containsKey(currentWeek)) {
                    java.util.List<Classes.Game> weekGames = schedule.get(currentWeek);
                    Classes.Game playerGame = null;

                    for (Classes.Game g : weekGames) {
                        if (g.getHomeTeam().equals(ctx.getPlayerTeam()) || g.getAwayTeam().equals(ctx.getPlayerTeam())) {
                            playerGame = g;
                        } else {
                            if (!g.isCompleted()) g.play();
                        }
                    }

                    if (playerGame != null && !playerGame.isCompleted()) {
                        Classes.GameRules rules = ctx.getSportFactory().createGameRules();
                        if (gui.GUISquadManager.getInstance().getPlayersOnPitchQueue().size() != (rules.getFieldPlayerCount() - gui.GUISquadManager.getInstance().redCardedPlayers.size())) {
                            ctx.setTacticConfirmedForMatch(false);
                            gui.GUIPopup.showMessage("Incomplete Squad", "Squad Incomplete!", "Starting " + rules.getFieldPlayerCount() + " must be full to play the match! Please set your squad.");
                            new gui.GUITactic(ctx.getPlayerTeam());
                            return;
                        }
                            
                        boolean hasInjuredStarter = false;
                        for (Interface.IPlayer p : gui.GUISquadManager.getInstance().getPlayersOnPitchQueue()) {
                            if (p.isInjured()) { hasInjuredStarter = true; break; }
                        }
                        if (hasInjuredStarter) {
                            ctx.setTacticConfirmedForMatch(false);
                            gui.GUIPopup.showMessage("Injured Player", "Injured Player in Starting " + rules.getFieldPlayerCount() + "!", "There must be no injured players on the pitch to play the match. Please substitute the injured player.");
                            new gui.GUITactic(ctx.getPlayerTeam());
                            return;
                        }
                            
                        String validationMsg = ctx.getSportFactory().validateFormation(new java.util.ArrayList<>(gui.GUISquadManager.getInstance().getPlayersOnPitchQueue()));
                        if (validationMsg != null) {
                            ctx.setTacticConfirmedForMatch(false);
                            gui.GUIPopup.showMessage("Invalid Formation", "Formation Rule Violated!", validationMsg);
                            new gui.GUITactic(ctx.getPlayerTeam());
                            return;
                        }
                            
                        final Classes.Game finalPlayerGame = playerGame;
                        if (gui.GUISquadManager.getInstance().getReservePlayersQueue().size() != rules.getReservePlayerCount()) {
                            gui.GUIPopup.showConfirmation("Incomplete Bench", "Bench is not full!", 
                                "There are not " + rules.getReservePlayerCount() + " players on the bench. Do you still want to play the match?", 
                                () -> ctx.getSportFactory().launchGame(finalPlayerGame),
                                () -> { ctx.setTacticConfirmedForMatch(false); new gui.GUITactic(ctx.getPlayerTeam()); }
                            );
                            return;
                        }
                        
                        // Inject the GUI choices into the domain manager BEFORE the game starts
                        if (finalPlayerGame.getHomeManager() instanceof Classes.HumanManager hm) {
                            hm.setPreMatchTactic(
                                new java.util.ArrayList<>(gui.GUISquadManager.getInstance().getPlayersOnPitchQueue()),
                                new java.util.ArrayList<>(gui.GUISquadManager.getInstance().getReservePlayersQueue()),
                                gui.GUISquadManager.getInstance().getCurrentTacticStyle());
                        } else if (finalPlayerGame.getAwayManager() instanceof Classes.HumanManager hm) {
                            hm.setPreMatchTactic(
                                new java.util.ArrayList<>(gui.GUISquadManager.getInstance().getPlayersOnPitchQueue()),
                                new java.util.ArrayList<>(gui.GUISquadManager.getInstance().getReservePlayersQueue()),
                                gui.GUISquadManager.getInstance().getCurrentTacticStyle());
                        }

                        ctx.getSportFactory().launchGame(playerGame);
                    } else {
                        Thread matchThread = new Thread(() -> {
                            cal.advanceToNextWeek();
                            decrementAllInjuries();
                            javafx.application.Platform.runLater(() -> {
                                ctx.setMatchDay(false);
                                ctx.setTacticConfirmedForMatch(false);
                                new GUIMain();
                            });
                        });
                        matchThread.setDaemon(true);
                        matchThread.start();
                    }
                }
            }
        }
    }

    private void initialize() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root-dark");

        // Adding Panels
        mainLayout.setTop(GUILeftandTopBarHelper.createTopBar(null));
        mainLayout.setLeft(GUILeftandTopBarHelper.createSidebar("Home"));
        mainLayout.setCenter(createDashboard());
        SceneManager.changeScene(mainLayout, "Sports Manager - Dashboard");
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

        GameContext ctx = GameContext.getInstance();
        // Pick the right calendar depending on active sport
        Classes.Calendar cal = "VOLLEYBALL".equals(ctx.getActiveSport()) ? ctx.getActiveVolleyballCalendar() : ctx.getActiveCalendar();

        if (cal != null && ctx.getPlayerTeam() != null) {
            int week = cal.getCurrentWeek() + 1;
            java.util.Map<Integer, java.util.List<Classes.Game>> schedule = cal.getSchedule();
            if (schedule != null && schedule.containsKey(week)) {
                Classes.Game nextGame = null;
                for (Classes.Game g : schedule.get(week)) {
                    if (g.getHomeTeam().equals(ctx.getPlayerTeam()) || g.getAwayTeam().equals(ctx.getPlayerTeam())) {
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
        GameContext ctx = GameContext.getInstance();
        VBox widget = createBaseWidget("Top Improvers (Weekly)", "#4CAF50");
        VBox list = new VBox(8);
        
        if (ctx.getPlayerTeam() != null && ctx.getPlayerTeam().getPlayers() != null) {
            java.util.List<Interface.IPlayer> players = new java.util.ArrayList<>(ctx.getPlayerTeam().getPlayers());
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
        GameContext ctx = GameContext.getInstance();
        VBox widget = createBaseWidget("Injured Players", "#f0a500");
        VBox list = new VBox(8);
        
        if (ctx.getPlayerTeam() != null && ctx.getPlayerTeam().getPlayers() != null) {
            int count = 0;
            for (Interface.IPlayer p : ctx.getPlayerTeam().getPlayers()) {
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
        widget.getStyleClass().add("widget-box");
        widget.setPrefWidth(350);
        widget.setPrefHeight(250);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(accentColor));
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        widget.getChildren().add(titleLabel);
        return widget;
    }

    public static void decrementAllInjuries() {
        GameContext ctx = GameContext.getInstance();
        if ("VOLLEYBALL".equals(ctx.getActiveSport()) && ctx.getActiveVolleyballLeague() != null) {
            for (ITeam team : ctx.getActiveVolleyballLeague().getTeamRanking()) {
                for (Interface.IPlayer p : team.getPlayers()) {
                    p.decrementInjury();
                }
            }
        } else if (ctx.getActiveLeague() != null) {
            for (ITeam team : ctx.getActiveLeague().getTeamRanking()) {
                for (Interface.IPlayer p : team.getPlayers()) {
                    p.decrementInjury();
                }
            }
        }
    }
}