package gui;

import Interface.IGame;
import Interface.ITeam;
import Classes.Game;
import Classes.Calendar;
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

    private ITeam playerTeam;
    private Calendar calendar;

    public GUIFixture(ITeam playerTeam, Calendar calendar) {
        this.playerTeam = playerTeam;
        this.calendar = calendar;
        show();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1b1b2f;");

        // Üst ve Sol Menüleri Ekle
        root.setTop(GUILeftandTopBarHelper.createTopBar(null));
        root.setLeft(GUILeftandTopBarHelper.createSidebar("Fixture"));

        // İçerik Alanı
        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 40, 20, 40));

        // Üst Başlık Alanı
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("FIXTURE AND MATCH CALENDAR");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label("All matches to be played throughout the season");
        subtitle.setTextFill(Color.web("#a5a5b0"));
        subtitle.setFont(Font.font("Segoe UI", 14));

        // Hafta Seçici (Yatay Kaydırılabilir)
        HBox weekSelectorBox = new HBox(10);
        weekSelectorBox.setAlignment(Pos.CENTER_LEFT);
        weekSelectorBox.setPadding(new Insets(15, 0, 15, 0));

        ScrollPane weekScroll = new ScrollPane(weekSelectorBox);
        weekScroll.setFitToHeight(true);
        weekScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-control-inner-background: transparent;");
        weekScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        weekScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        header.getChildren().addAll(title, subtitle, weekScroll);

        // Maç Listesi (Kaydırılabilir)
        VBox fixtureList = new VBox(30);
        fixtureList.setPadding(new Insets(10, 0, 20, 0));
        fixtureList.setStyle("-fx-background-color: transparent;");

        // Calendar nesnesinden haftaları çekiyoruz
        Map<Integer, List<Game>> schedule = calendar.getSchedule();
        
        int actualCurrentWeek = calendar.getCurrentWeek() + 1;
        if (!schedule.containsKey(actualCurrentWeek)) actualCurrentWeek = 1;

        populateWeekSelector(weekSelectorBox, actualCurrentWeek, actualCurrentWeek, schedule, fixtureList);

        if (schedule.containsKey(actualCurrentWeek)) {
            fixtureList.getChildren().add(createWeekSection(actualCurrentWeek, schedule.get(actualCurrentWeek)));
        }

        // Seçili haftaya otomatik kaydır
        final int targetWeek = actualCurrentWeek;
        javafx.application.Platform.runLater(() -> {
            double scrollPosition = (double) (targetWeek - 1) / Math.max(1, schedule.size() - 1);
            weekScroll.setHvalue(scrollPosition);
        });

        ScrollPane scrollPane = new ScrollPane(fixtureList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        content.getChildren().addAll(header, scrollPane);
        root.setCenter(content);

        SceneManager.changeScene(root, "Fixture - " + playerTeam.getName());
    }

    private void populateWeekSelector(HBox weekSelectorBox, int actualCurrentWeek, int selectedWeek, Map<Integer, List<Game>> schedule, VBox fixtureList) {
        weekSelectorBox.getChildren().clear();
        
        for (Integer w : schedule.keySet()) {
            Button btn = new Button(String.valueOf(w));
            btn.setPrefWidth(45);
            btn.setPrefHeight(45);
            btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            btn.setCursor(javafx.scene.Cursor.HAND);

            if (w == selectedWeek) {
                // Seçili Hafta (Parlak Turuncu ve Beyaz Çerçeve)
                btn.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-background-radius: 25; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 25;");
            } else if (w < actualCurrentWeek) {
                // Geçmiş Haftalar (Soluk / Gri)
                btn.setStyle("-fx-background-color: #4e4e6a; -fx-text-fill: #a5a5b0; -fx-background-radius: 25;");
            } else if (w == actualCurrentWeek) {
                // İçinde Bulunulan Hafta (Kırmızı)
                btn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-background-radius: 25;");
            } else {
                // Gelecek Haftalar (Koyu Mavi)
                btn.setStyle("-fx-background-color: #1f4068; -fx-text-fill: white; -fx-background-radius: 25;");
            }

            btn.setOnAction(e -> {
                fixtureList.getChildren().clear();
                if (schedule.containsKey(w)) {
                    fixtureList.getChildren().add(createWeekSection(w, schedule.get(w)));
                }
                populateWeekSelector(weekSelectorBox, actualCurrentWeek, w, schedule, fixtureList);
            });

            weekSelectorBox.getChildren().add(btn);
        }
    }

    private VBox createWeekSection(int weekNum, List<Game> games) {
        VBox weekContainer = new VBox(10);
        
        // Hafta Başlığı
        Label weekLabel = new Label("WEEK " + weekNum);
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
        HBox homeBox = new HBox(10);
        homeBox.setAlignment(Pos.CENTER_RIGHT);
        homeBox.setPrefWidth(250);
        Label homeTeam = new Label(game.getHomeTeam().getName());
        homeTeam.setTextFill(Color.WHITE);
        homeTeam.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
        javafx.scene.Node homeEmblem = GUILeftandTopBarHelper.createEmblem(game.getHomeTeam(), 30);
        homeBox.getChildren().addAll(homeTeam, homeEmblem);

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
        HBox awayBox = new HBox(10);
        awayBox.setAlignment(Pos.CENTER_LEFT);
        awayBox.setPrefWidth(250);
        javafx.scene.Node awayEmblem = GUILeftandTopBarHelper.createEmblem(game.getAwayTeam(), 30);
        Label awayTeam = new Label(game.getAwayTeam().getName());
        awayTeam.setTextFill(Color.WHITE);
        awayTeam.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
        awayBox.getChildren().addAll(awayEmblem, awayTeam);

        // Kullanıcının takımıysa satırın dış kenarlığını vurgula
        if (game.getHomeTeam().equals(playerTeam) || game.getAwayTeam().equals(playerTeam)) {
            row.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 8; -fx-border-color: #e43f5a; -fx-border-width: 1.5;");
        }

        row.getChildren().addAll(homeBox, vsLabel, awayBox);

        // Hover Efekti
        row.setOnMouseEntered(e -> row.setOpacity(0.85));
        row.setOnMouseExited(e -> row.setOpacity(1.0));

        return row;
    }

}