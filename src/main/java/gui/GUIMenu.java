package gui;

import io.SaveGame;
import io.SaveManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GUIMenu {

    // Menüyü çağırmak için bu metodu kullanacaksın: GUIPauseMenu.show(primaryStage);
    public static void show(Stage ownerStage) {
        Stage popupStage = new Stage();
        popupStage.initOwner(ownerStage);
        
        // İŞTE SİHİRLİ KOD: İşletim sisteminin klasik pencere kenarlıklarını (Windows görünümünü) tamamen siler
        popupStage.initStyle(StageStyle.TRANSPARENT);
        
        // Bu menü açıkken arka plandaki oyuna tıklanmasını engeller
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Menü Kutusu (Lacivert arka plan, kırmızı ince kenarlık)
        VBox menuBox = new VBox(15);
        menuBox.setPadding(new Insets(40, 60, 40, 60));
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(350);
        menuBox.setStyle("-fx-background-color: #162447; -fx-background-radius: 15; -fx-border-color: #e43f5a; -fx-border-width: 2; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 20, 0, 0, 0);");

        Label title = new Label("OYUN MENÜSÜ");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        // Butonları oluştur (Tasarım temasına uygun renklerle)
        Button btnSave = createMenuButton("Kaydet (Save Game)", "#4CAF50");
        Button btnQuickSave = createMenuButton("Hızlı Kaydet (Quick Save)", "#66BB6A");
        Button btnLoad = createMenuButton("Oyunu Yükle (Load Game)", "#f0a500");
        Button btnGuide = createMenuButton("Rehber (Guide)", "#4e4e6a");
        Button btnMainMenu = createMenuButton("Ana Menüye Dön", "#e43f5a");
        Button btnExit = createMenuButton("Masaüstüne Çık", "#d82bbc");
        Button btnClose = createMenuButton("Oyuna Dön", "#1f4068");

        // --- BUTON İŞLEVLERİ ---
        btnClose.setOnAction(e -> popupStage.close());
        
        btnExit.setOnAction(e -> System.exit(0)); // Tüm programı kapatır
        
        btnMainMenu.setOnAction(e -> {
            popupStage.close();
            GUITitlescreen titleScreen = new GUITitlescreen();
            titleScreen.show(ownerStage);
        });
        
        btnGuide.setOnAction(e -> {
            popupStage.close();
            javafx.scene.Parent currentRoot = ownerStage.getScene().getRoot();
            String currentTitle = ownerStage.getTitle();
            new GUIGuide(ownerStage, GUIMain.playerTeam, () -> {
                ownerStage.getScene().setRoot(currentRoot);
                ownerStage.setTitle(currentTitle);
            });
        });

        btnQuickSave.setOnAction(e -> {
            SaveGame saveData = new SaveGame("autosave", GUIMain.activeLeague, GUIMain.activeCalendar, GUIMain.playerTeam,
                    gui.GUITactic.getPitchPlayers(), gui.GUITactic.getPlayersOnPitchQueue(), gui.GUITactic.getReservePlayersQueue(),
                    gui.GUITactic.getCurrentTacticStyle());
            SaveManager.saveGame(saveData, "autosave");
            System.out.println("Oyun hızlı kaydedildi: autosave.json");
            // İsteğe bağlı: Ekranda küçük bir onay mesajı gösterilebilir.
        });

        btnSave.setOnAction(e -> {
            System.out.println("Kayıt ekranı açılıyor...");
            // SaveManager.saveGame(..., ...);
        });

        // Butonları kutuya ekle
        menuBox.getChildren().addAll(title, btnSave, btnQuickSave, btnLoad, btnGuide, btnMainMenu, btnExit, new Region(), btnClose);

        // Kapatma butonundan önce biraz boşluk bırakmak için Region kullandık
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        // Tam ekran arka plan (Ana ekranı karartmak için)
        StackPane root = new StackPane(menuBox);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Arka planı %70 siyah yapar
        root.setPadding(new Insets(50));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT); // JavaFX penceresinin arka planını şeffaf yapar
        popupStage.setScene(scene);

        // Pop-up'ın boyutunu ve pozisyonunu ana pencere ile tam eşle
        popupStage.setWidth(ownerStage.getWidth());
        popupStage.setHeight(ownerStage.getHeight());
        popupStage.setX(ownerStage.getX());
        popupStage.setY(ownerStage.getY());

        // Menüyü göster
        popupStage.showAndWait();
    }

    // Özel tasarımlı buton üretici
    private static Button createMenuButton(String text, String baseColor) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;");
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: derive(" + baseColor + ", 30%); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5; -fx-cursor: hand;");
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;");
        });
        
        return btn;
    }
}