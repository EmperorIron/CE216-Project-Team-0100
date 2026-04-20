package gui;

import Interface.IGame;
import Interface.ITeam;
import Classes.Game;
import Sport.CalendarFootball;
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

        // Üst Başlık Alanı
        VBox header = new VBox(10);
        header.setPadding(new Insets(25, 40, 10, 40));
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("FİKSTÜR VE MAÇ TAKVİMİ");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label("Sezon Boyunca Oynanacak Tüm Karşılaşmalar");
        subtitle.setTextFill(Color.web("#a5a5b0"));
        subtitle.setFont(Font.font("Segoe UI", 14));

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        // Maç Listesi (Kaydırılabilir)
        VBox fixtureList = new VBox(30);
        fixtureList.setPadding(new Insets(20, 40, 20, 40));
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
        
        root.setCenter(scrollPane);

        // Alt Bar (Geri Dön Butonu)
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(20, 40, 20, 40));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        Button backBtn = new Button("Ana Panele Dön");
        backBtn.setStyle("-fx-background-color: #4e4e6a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> new GUIMain(primaryStage)); // Ana ekrana dönüş
        
        bottomBar.getChildren().add(backBtn);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setTitle("Fikstür - " + playerTeam.getName());
        primaryStage.setScene(scene);
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
}