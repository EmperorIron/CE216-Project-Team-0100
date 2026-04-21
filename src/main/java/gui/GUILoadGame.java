package gui;
import io.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class GUILoadGame {

    // Ana menüye dönmek için kullanacağımız callback
    private final Runnable onBackToMenu;
    private final Consumer<SaveGame> onGameLoaded;

    public GUILoadGame(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
        this.onGameLoaded = null;
    }

    public GUILoadGame(Runnable onBackToMenu, Consumer<SaveGame> onGameLoaded) {
        this.onBackToMenu = onBackToMenu;
        this.onGameLoaded = onGameLoaded;
    }

    public void show(Stage primaryStage) {
        // Ana düzen olarak BorderPane kullanıyoruz (Üst başlık, Orta liste, Alt buton)
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #050505;");

        // --- ÜST KISIM (BAŞLIK) ---
        Label lblTitle = new Label("LOAD SAVED GAME");
        lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-letter-spacing: 2px;");
        HBox header = new HBox(lblTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 0, 20, 0));
        root.setTop(header);

        // --- ORTA KISIM (SCROLL LISTESİ) ---
        VBox saveList = new VBox(15); // Kartlar arası 15px boşluk
        saveList.setAlignment(Pos.TOP_CENTER);
        saveList.setPadding(new Insets(10, 50, 10, 50));
        saveList.setStyle("-fx-background-color: #050505;"); // Arka plan uyumu

        // Okunacak kayıt dizini
        File saveDir = new File("saves/");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        
        File[] files = saveDir.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (files != null && files.length > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            for (File file : files) {
                String fileName = file.getName().replace(".json", "");
                String dateStr = sdf.format(new Date(file.lastModified()));
                VBox saveCard = createSaveCard(fileName, dateStr, "Click to load", file.getPath());
                saveList.getChildren().add(saveCard);
            }
        } else {
            Label emptyLabel = new Label("No save files found.");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
            saveList.getChildren().add(emptyLabel);
        }

        // ScrollPane (Sınırsız Kaydırma Bileşeni)
        ScrollPane scrollPane = new ScrollPane(saveList);
        scrollPane.setFitToWidth(true); // Kartların genişliği ekrana otursun
        scrollPane.setStyle("-fx-background: #050505; -fx-background-color: transparent; -fx-control-inner-background: #050505;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Sadece gerekirse dikey scroll çıkar
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Yatay scroll'u tamamen kapat

        root.setCenter(scrollPane);

        // --- ALT KISIM (GERİ BUTONU) ---
        Button btnBack = new Button("BACK TO MAIN MENU");
        styleButton(btnBack);
        btnBack.setOnAction(e -> onBackToMenu.run()); // Ana menüye dön
        
        HBox footer = new HBox(btnBack);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 30, 0));
        root.setBottom(footer);

        primaryStage.setTitle("Sports Manager - Load Game");
        
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(root);
        }
        
        primaryStage.show();
    }

    /**
     * Her bir kayıt dosyası için görsel bir kutu (kart) oluşturur.
     */
    private VBox createSaveCard(String title, String date, String detail, String filePath) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setPrefHeight(80);
        card.setMaxWidth(600); // Kartların çok fazla uzamasını engelle
        
        String defaultStyle = "-fx-border-color: rgba(255, 255, 255, 0.3); -fx-border-radius: 10; -fx-border-width: 2; -fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10;";
        String hoverStyle = "-fx-border-color: #ffffff; -fx-border-radius: 10; -fx-border-width: 2; -fx-background-color: rgba(255, 255, 255, 0.15); -fx-background-radius: 10;";
        
        card.setStyle(defaultStyle);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblDate = new Label(date + " | " + detail);
        lblDate.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaaaaa;");

        card.getChildren().addAll(lblTitle, lblDate);

        // Hover animasyonları ve tıklama
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));
        
        card.setOnMouseClicked(e -> {
            System.out.println("Yükleniyor: " + filePath);
            
            SaveGame loadedGame = SaveManager.loadGame(filePath);
            
            if (loadedGame != null) {
                System.out.println("Kaldığın lig: " + loadedGame.getCurrentLeague().getName());
                if (onGameLoaded != null) {
                    onGameLoaded.accept(loadedGame);
                }
            }
        });
        return card;
    }

    /**
     * Ana menüdeki buton stilini buraya da uyguluyoruz.
     */
    private void styleButton(Button button) {
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 30; -fx-border-width: 2; -fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial';";
        String hoverStyle = "-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 30; -fx-border-color: #ffffff; -fx-border-radius: 30; -fx-border-width: 2; -fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial';";
        
        button.setStyle(defaultStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));
    }
}