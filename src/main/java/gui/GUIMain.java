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
                        if (gui.GUITactic.getPlayersOnPitchQueue().size() != (rules.getFieldPlayerCount() - gui.GUITactic.redCardedPlayers.size()) || 
                            gui.GUITactic.getReservePlayersQueue().size() != rules.getReservePlayerCount()) {
                            
                            tacticConfirmedForMatch = false;
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                            alert.setTitle("Eksik Kadro");
                            alert.setHeaderText("Kadro Tamamlanmadı!");
                            alert.setContentText("Maça çıkabilmek için İlk 11'in ve " + rules.getReservePlayerCount() + " yedeğin tam olması gerekmektedir! Lütfen kadronuzu kurun.");
                            alert.showAndWait();
                            
                            new gui.GUITactic(primaryStage, playerTeam);
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
        mainLayout.setTop(createTopBar());
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createDashboard());

        primaryStage.setTitle("Spor Menajerlik - Ana Ekran");
        
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(mainLayout, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(mainLayout);
        }
        
        primaryStage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: #162447; -fx-border-color: #d82bbc; -fx-border-width: 0 0 2 0;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Kulüp ve Menajer Bilgileri
        VBox infoBox = new VBox(5);
        Label teamLabel = new Label(playerTeam != null ? playerTeam.getName() : "Takım Seçilmedi");
        teamLabel.setTextFill(Color.WHITE);
        teamLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label managerLabel = new Label("Menajer: Abdullah");
        managerLabel.setTextFill(Color.web("#a5a5b0"));
        managerLabel.setFont(Font.font("Segoe UI", 14));
        infoBox.getChildren().addAll(teamLabel, managerLabel);

        // Arayı açmak için esnek boşluk
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Zaman Çizelgesi
        String weekText = activeCalendar != null ? "Hafta " + (activeCalendar.getCurrentWeek() + 1) : "";
        Label dateLabel = new Label(weekText + (isMatchDay ? " - Maç Günü" : " - Antrenman Haftası"));
        dateLabel.setTextFill(Color.WHITE);
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Button menuButton = new Button("Menü ⚙");
        menuButton.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;");
        menuButton.setOnMouseEntered(e -> menuButton.setStyle("-fx-background-color: #ffb732; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        menuButton.setOnMouseExited(e -> menuButton.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        menuButton.setOnAction(e -> GUIMenu.show(primaryStage));

        // İlerle Butonu
        Button continueButton = new Button(isMatchDay ? "Maça Çık ⚽" : "Devam Et ▶");
        continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;");
        continueButton.setOnMouseEntered(e -> continueButton.setStyle("-fx-background-color: #ff5773; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        continueButton.setOnMouseExited(e -> continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        
        continueButton.setOnAction(e -> handleContinueAction(primaryStage));

        topBar.getChildren().addAll(infoBox, spacer, dateLabel, menuButton, continueButton);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #1f4068;");
        sidebar.setPrefWidth(220);

        String[] menuItems = {"Ana Sayfa", "Kadro", "Taktikler", "Antrenman", "Fikstür", "Lig Tablosu"};
        
        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;");
            
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15; -fx-background-radius: 5;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;"));
            
          
            btn.setOnAction(e -> {
                System.out.println(item + " sekmesine tıklandı.");
                
                if (item.equals("Fikstür")) {
                    if (activeCalendar != null && playerTeam != null) {
                        new GUIFixture(primaryStage, playerTeam, activeCalendar);
                    } else {
                        System.out.println("Takvim veya takım yüklenmemiş!");
                    }
                } else if (item.equals("Ana Sayfa")) {
                    // Ana sayfa butonuna basınca Dashboard geri gelir
                    mainLayout.setCenter(createDashboard());
                } else if (item.equals("Taktikler")) {
                    if (playerTeam != null) {
                        new GUITactic(primaryStage, playerTeam);
                    } else {
                        System.out.println("Takım yüklenmemiş!");
                    }
                } else if (item.equals("Lig Tablosu")) {
                    if (activeLeague != null && playerTeam != null) {
                        new GUILeagueRanking(primaryStage, playerTeam, activeLeague);
                    }
                } else if (item.equals("Antrenman")) {
                    if (playerTeam != null) {
                        new GUITraining(primaryStage, playerTeam);
                    }
                }
                
            });

            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }

    private VBox createDashboard() {
        VBox dashboard = new VBox(25);
        dashboard.setPadding(new Insets(30));

        Label welcomeLabel = new Label("Yönetim Özeti");
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        HBox widgetsBox = new HBox(25);
        
        VBox nextMatchWidget = createWidget("Sıradaki Maç", "Rakip: Belirlenmedi\nDurum: Bekleniyor", "#e43f5a");
        VBox leagueWidget = createWidget("Lig Durumu", "Sıra: -\nPuan: 0", "#4CAF50");
        VBox teamWidget = createWidget("Takım Durumu", "Moral: Yüksek\nSakatlık: Yok", "#f0a500");

        widgetsBox.getChildren().addAll(nextMatchWidget, leagueWidget, teamWidget);
        dashboard.getChildren().addAll(welcomeLabel, widgetsBox);
        
        return dashboard;
    }

    private VBox createWidget(String title, String content, String accentColor) {
        VBox widget = new VBox(15);
        widget.setPadding(new Insets(20));
        widget.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
        widget.setPrefWidth(280);
        widget.setPrefHeight(180);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(accentColor));
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label contentLabel = new Label(content);
        contentLabel.setTextFill(Color.web("#e0e0e0"));
        contentLabel.setFont(Font.font("Segoe UI", 14));
        contentLabel.setWrapText(true);

        widget.getChildren().addAll(titleLabel, contentLabel);
        return widget;
    }
}