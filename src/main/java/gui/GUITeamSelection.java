package gui;

import Interface.ITeam;
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

public class GUITeamSelection {

    private Stage primaryStage;

    public GUITeamSelection(Stage primaryStage) {
        this.primaryStage = primaryStage;
        show();
    }

    public void show() {
        VBox layout = new VBox(30);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #1b1b2f;"); // Koyu tema

        // Başlık
        Label title = new Label("YÖNETECEĞİNİZ TAKIMI SEÇİN");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));

        // GUIMain'de oluşturulan aktif ligden takımları çekiyoruz
        List<ITeam> teams = GUIMain.activeLeague.getTeamRanking();

        // Takım Listesi (Kaydırılabilir alan)
        VBox teamList = new VBox(15);
        teamList.setAlignment(Pos.CENTER);
        
        for (ITeam team : teams) {
            teamList.getChildren().add(createTeamCard(team));
        }

        ScrollPane scrollPane = new ScrollPane(teamList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(500);

        // Geri Dön Butonu
        Button backBtn = new Button("Ana Menüye Dön");
        backBtn.setStyle("-fx-background-color: #4e4e6a; -fx-text-fill: white; -fx-font-size: 14px;");
        backBtn.setOnAction(e -> {
            GUITitlescreen titleScreen = new GUITitlescreen();
            titleScreen.show(primaryStage);
        });

        layout.getChildren().addAll(title, scrollPane, backBtn);

        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(layout, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(layout);
        }
    }

    private HBox createTeamCard(ITeam team) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15, 30, 15, 30));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(800);
        card.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;");

        // Takım İsmi
        Label nameLabel = new Label(team.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setPrefWidth(300);

     
        VBox stats = new VBox(5);
        Label offLabel = new Label("Hücum: " + String.format("%.1f", team.getTotalOffensiveRating()));
        offLabel.setTextFill(Color.web("#4CAF50"));
        Label defLabel = new Label("Savunma: " + String.format("%.1f", team.getTotalDefensiveRating()));
        defLabel.setTextFill(Color.web("#e43f5a"));
        stats.getChildren().addAll(offLabel, defLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Seç Butonu
        Button selectBtn = new Button("TAKIMI YÖNET");
        selectBtn.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        selectBtn.setOnAction(e -> {
            System.out.println(team.getName() + " seçildi. Oyun başlıyor...");
            team.setManagerAI(false); 
            GUIMain.playerTeam = team; 
            new GUIMain(primaryStage); 
        });

       
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 10; -fx-border-color: #e43f5a; -fx-border-width: 2;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;"));

        card.getChildren().addAll(nameLabel, stats, spacer, selectBtn);
        return card;
    }
}