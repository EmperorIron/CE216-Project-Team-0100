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
import Sport.CalendarFootball;
import Sport.GameRulesFootball;
import Sport.GameFootball;
import Sport.LeagueFootball;
import io.SaveGame;
import io.SaveManager;

public class GUIMain {

    private Stage primaryStage;
    private BorderPane mainLayout;
    public static ITeam playerTeam;
    public static LeagueFootball activeLeague;
    public static CalendarFootball activeCalendar;
    public static boolean isMatchDay = false;
    public static boolean tacticConfirmedForMatch = false;
    
    public GUIMain(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initialize();
    }

    public static void startNewGame(String sport, Stage stage) {
        if ("FOOTBALL".equals(sport)) {
            GameRulesFootball rules = new GameRulesFootball();
            activeLeague = new LeagueFootball("Süper Lig", "Türkiye", 10, rules); 
            activeCalendar = new CalendarFootball(rules);
            activeCalendar.generateFixtures(activeLeague.getTeamRanking());
            
            System.out.println("Football game initialized! Transitioning to Team Selection...");
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
        gui.GUITactic.loadTacticData(saveGame.getPitchPlayers(), saveGame.getPlayersOnPitchQueue(), saveGame.getReservePlayersQueue(), playerTeam, saveGame.getTacticStyle());
        
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
                // Maç günüyse ve taktik onaylanmadıysa direkt taktik ekranına gönder
                new GUITactic(primaryStage, playerTeam);
                return;
            }

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
                            if (!g.isCompleted()) g.play(); // Diğer maçları anında simüle et
                        }
                    }
                    
                    if (playerGame != null && !playerGame.isCompleted()) {
                        // Ekstra Güvenlik: Kadro tam mı kontrolü
                        Sport.GameRulesFootball rules = new Sport.GameRulesFootball();
                        if (gui.GUITactic.getPlayersOnPitchQueue().size() != (rules.getFieldPlayerCount() - gui.GUITactic.redCardedPlayers.size())) {
                            
                            tacticConfirmedForMatch = false;
                            gui.GUIPopup.showMessage(primaryStage, "Eksik Kadro", "Kadro Tamamlanmadı!", "Maça çıkabilmek için İlk 11'in tam olması gerekmektedir! Lütfen kadronuzu kurun.");
                            
                            new gui.GUITactic(primaryStage, playerTeam);
                            return;
                        }
                        
                        boolean hasInjuredStarter = false;
                        for (Interface.IPlayer p : gui.GUITactic.getPlayersOnPitchQueue()) {
                            if (p.isInjured()) {
                                hasInjuredStarter = true;
                                break;
                            }
                        }
                        if (hasInjuredStarter) {
                            tacticConfirmedForMatch = false;
                            gui.GUIPopup.showMessage(primaryStage, "Sakat Oyuncu", "İlk 11'de Sakat Oyuncu Var!", "Maça çıkabilmek için sahada sakat oyuncu bulunmamalıdır. Lütfen sakat oyuncuyu değiştirin.");
                            
                            new gui.GUITactic(primaryStage, playerTeam);
                            return;
                        }
                        
                        final Classes.Game finalPlayerGame = playerGame;
                        if (gui.GUITactic.getReservePlayersQueue().size() != rules.getReservePlayerCount()) {
                            gui.GUIPopup.showConfirmation(primaryStage, "Eksik Yedekler", "Yedek Kulübesi Tam Değil!", 
                                "Yedek kulübesinde " + rules.getReservePlayerCount() + " oyuncu yok. Yine de maça çıkmak istiyor musunuz?", 
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
        mainLayout.setLeft(GUILeftandTopBarHelper.createSidebar(primaryStage, "Ana Sayfa"));
        mainLayout.setCenter(createDashboard());

        primaryStage.setTitle("Spor Menajerlik - Ana Ekran");
        
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

        Label welcomeLabel = new Label("Yönetim Özeti");
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
        VBox widget = createBaseWidget("Sıradaki Maç", "#e43f5a");
        widget.setPrefWidth(725); // Makes it span the width of the two bottom widgets
        
        HBox matchLayout = new HBox(60);
        matchLayout.setAlignment(Pos.CENTER);
        VBox.setVgrow(matchLayout, Priority.ALWAYS);
        
        if (activeCalendar != null && playerTeam != null) {
            int week = activeCalendar.getCurrentWeek() + 1;
            java.util.Map<Integer, java.util.List<Classes.Game>> schedule = activeCalendar.getSchedule();
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
                    Label weekLabel = new Label("Hafta " + week);
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
            } else if (week > activeCalendar.getSchedule().size()) {
                Label contentLabel = new Label("Sezon Sona Erdi.");
                contentLabel.setTextFill(Color.web("#e0e0e0"));
                contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                matchLayout.getChildren().add(contentLabel);
                widget.getChildren().add(matchLayout);
                return widget;
            }
        }
        
        Label contentLabel = new Label("Maç bulunamadı.");
        contentLabel.setTextFill(Color.web("#e0e0e0"));
        contentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        matchLayout.getChildren().add(contentLabel);
        widget.getChildren().add(matchLayout);
        return widget;
    }

    private VBox createTrainingPerformanceWidget() {
        VBox widget = createBaseWidget("En İyi Gelişenler (Haftalık)", "#4CAF50");
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
                Label l = new Label("Henüz antrenman verisi yok veya\ngelişen oyuncu bulunmuyor.");
                l.setTextFill(Color.web("#a5a5b0"));
                l.setFont(Font.font("Segoe UI", 14));
                list.getChildren().add(l);
            }
        }
        widget.getChildren().add(list);
        return widget;
    }

    private VBox createInjuryWidget() {
        VBox widget = createBaseWidget("Sakat Oyuncular", "#f0a500");
        VBox list = new VBox(8);
        
        if (playerTeam != null && playerTeam.getPlayers() != null) {
            int count = 0;
            for (Interface.IPlayer p : playerTeam.getPlayers()) {
                if (p.isInjured()) {
                    Label l = new Label("🚑 " + p.getFullName() + " (" + p.getInjuryDuration() + " Hafta)");
                    l.setTextFill(Color.web("#e43f5a"));
                    l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    list.getChildren().add(l);
                    count++;
                }
            }
            if (count == 0) {
                Label l = new Label("Takımda sakat oyuncu bulunmuyor.");
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