package gui;

import Interface.IGame;
import Interface.ITeam;
import Classes.Game;
import Sport.CalendarFootball;
import io.SaveGame;
import io.SaveManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class GUIFixture {

    private Stage primaryStage;
    private ITeam playerTeam;
    private CalendarFootball calendar;

    public GUIFixture(Stage primaryStage, ITeam playerTeam, CalendarFootball calendar) {
        this.primaryStage = primaryStage;
        this.playerTeam = playerTeam;
        this.calendar = calendar;
        show();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1b1b2f;");

        // Üst ve Sol Menüleri Ekle
        root.setTop(createTopBar());
        root.setLeft(createSidebar());

        // İçerik Alanı
        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 40, 20, 40));

        // Üst Başlık Alanı
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("FİKSTÜR VE MAÇ TAKVİMİ");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label("Sezon Boyunca Oynanacak Tüm Karşılaşmalar");
        subtitle.setTextFill(Color.web("#a5a5b0"));
        subtitle.setFont(Font.font("Segoe UI", 14));

        header.getChildren().addAll(title, subtitle);

        // Maç Listesi (Kaydırılabilir)
        VBox fixtureList = new VBox(30);
        fixtureList.setPadding(new Insets(10, 0, 20, 0));
        fixtureList.setStyle("-fx-background-color: transparent;");

        // Calendar nesnesinden haftaları çekiyoruz
        Map<Integer, List<Game>> schedule = calendar.getSchedule();

        for (Integer week : schedule.keySet()) {
            VBox weekBox = createWeekSection(week, schedule.get(week));
            fixtureList.getChildren().add(weekBox);
        }

        ScrollPane scrollPane = new ScrollPane(fixtureList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        content.getChildren().addAll(header, scrollPane);
        root.setCenter(content);

        primaryStage.setTitle("Fikstür - " + playerTeam.getName());
        
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(root);
        }
    }

    private VBox createWeekSection(int weekNum, List<Game> games) {
        VBox weekContainer = new VBox(10);
        
        // Hafta Başlığı
        Label weekLabel = new Label(weekNum + ". HAFTA");
        weekLabel.setTextFill(Color.web("#e43f5a"));
        weekLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        weekLabel.setPadding(new Insets(0, 0, 5, 5));

        VBox matchesBox = new VBox(10);
        for (Game game : games) {
            matchesBox.getChildren().add(createMatchRow(game));
        }

        weekContainer.getChildren().addAll(weekLabel, matchesBox);
        return weekContainer;
    }

  private HBox createMatchRow(IGame game) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(15, 25, 15, 25));
        row.setStyle("-fx-background-color: #162447; -fx-background-radius: 8; -fx-border-color: #1f4068; -fx-border-width: 1;");

        // Ev Sahibi
        Label homeTeam = new Label(game.getHomeTeam().getName());
        homeTeam.setTextFill(Color.WHITE);
        homeTeam.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
        homeTeam.setPrefWidth(250);
        homeTeam.setAlignment(Pos.CENTER_RIGHT);

        // Skor veya "VS"
        Label vsLabel = new Label(" VS ");
        if (game.isCompleted()) {
            int hScore = game.getHomeScore();
            int aScore = game.getAwayScore();
            vsLabel.setText(hScore + " - " + aScore);
            
            // Varsayılan renk (Kullanıcının takımı oynamıyorsa)
            String scoreColor = "#1f4068"; 

            // Kullanıcının takımına göre Galibiyet(Yeşil), Beraberlik(Gri), Mağlubiyet(Kırmızı) kontrolü
            if (game.getHomeTeam().equals(playerTeam)) {
                if (hScore > aScore) scoreColor = "#4CAF50"; // Galibiyet - Yeşil
                else if (hScore == aScore) scoreColor = "#9E9E9E"; // Beraberlik - Gri
                else scoreColor = "#F44336"; // Mağlubiyet - Kırmızı
            } else if (game.getAwayTeam().equals(playerTeam)) {
                if (aScore > hScore) scoreColor = "#4CAF50"; // Galibiyet - Yeşil
                else if (aScore == hScore) scoreColor = "#9E9E9E"; // Beraberlik - Gri
                else scoreColor = "#F44336"; // Mağlubiyet - Kırmızı
            }

            vsLabel.setStyle("-fx-background-color: " + scoreColor + "; -fx-padding: 5 15; -fx-background-radius: 15;");
        } else {
            // Oynanmamış maçlar
            vsLabel.setStyle("-fx-background-color: #e43f5a; -fx-padding: 5 15; -fx-background-radius: 5;");
        }
        
        vsLabel.setTextFill(Color.WHITE);
        vsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        // Deplasman
        Label awayTeam = new Label(game.getAwayTeam().getName());
        awayTeam.setTextFill(Color.WHITE);
        awayTeam.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
        awayTeam.setPrefWidth(250);
        awayTeam.setAlignment(Pos.CENTER_LEFT);

        // Kullanıcının takımıysa satırın dış kenarlığını vurgula
        if (game.getHomeTeam().equals(playerTeam) || game.getAwayTeam().equals(playerTeam)) {
            row.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 8; -fx-border-color: #e43f5a; -fx-border-width: 1.5;");
        }

        row.getChildren().addAll(homeTeam, vsLabel, awayTeam);

        // Hover Efekti
        row.setOnMouseEntered(e -> row.setOpacity(0.85));
        row.setOnMouseExited(e -> row.setOpacity(1.0));

        return row;
    }

    // --- GUIMAIN / GUITACTIC BİREBİR ÜST BAR ---
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: #162447; -fx-border-color: #d82bbc; -fx-border-width: 0 0 2 0;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);
        Label teamLabel = new Label(playerTeam != null ? playerTeam.getName() : "Takım Seçilmedi");
        teamLabel.setTextFill(Color.WHITE);
        teamLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label managerLabel = new Label("Menajer: Abdullah");
        managerLabel.setTextFill(Color.web("#a5a5b0"));
        managerLabel.setFont(Font.font("Segoe UI", 14));
        infoBox.getChildren().addAll(teamLabel, managerLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String weekText = calendar != null ? "Hafta " + (calendar.getCurrentWeek() + 1) : "";
        Label dateLabel = new Label(weekText + (GUIMain.isMatchDay ? " - Maç Günü" : " - Antrenman Haftası"));
        dateLabel.setTextFill(Color.WHITE);
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Button menuButton = new Button("Menü ⚙");
        menuButton.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;");
        menuButton.setOnMouseEntered(e -> menuButton.setStyle("-fx-background-color: #ffb732; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        menuButton.setOnMouseExited(e -> menuButton.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        menuButton.setOnAction(e -> GUIMenu.show(primaryStage));

        Button continueButton = new Button(GUIMain.isMatchDay ? "Maça Çık ⚽" : "Devam Et ▶");
        continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;");
        continueButton.setOnMouseEntered(e -> continueButton.setStyle("-fx-background-color: #ff5773; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        continueButton.setOnMouseExited(e -> continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        
        continueButton.setOnAction(e -> GUIMain.handleContinueAction(primaryStage));

        topBar.getChildren().addAll(infoBox, spacer, dateLabel, menuButton, continueButton);
        return topBar;
    }

    // --- GUIMAIN / GUITACTIC BİREBİR SOL MENÜ ---
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #1f4068;");
        sidebar.setPrefWidth(220);

        String[] menuItems = {"Ana Sayfa", "Taktikler", "Antrenman", "Fikstür", "Lig Tablosu"};
        
        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40);
            
            if (item.equals("Fikstür")) {
                btn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15; -fx-background-radius: 5;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15; -fx-background-radius: 5;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;"));
            }
            
            btn.setOnAction(e -> {
                if (item.equals("Ana Sayfa")) {
                    new GUIMain(primaryStage); 
                } else if (item.equals("Taktikler")) {
                    if (playerTeam != null) {
                        new GUITactic(primaryStage, playerTeam);
                    }
                } else if (item.equals("Lig Tablosu")) {
                    if (GUIMain.activeLeague != null && playerTeam != null) {
                        new GUILeagueRanking(primaryStage, playerTeam, GUIMain.activeLeague);
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
}