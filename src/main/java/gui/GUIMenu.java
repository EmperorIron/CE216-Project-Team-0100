package gui;

import Classes.GameContext;
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
    public static void show() {
        Stage ownerStage = SceneManager.getPrimaryStage();
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
        menuBox.getStyleClass().add("menu-box");

        Label title = new Label("GAME MENU");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        // Butonları oluştur (Tasarım temasına uygun renklerle)
        Button btnSave = createMenuButton("Save Game", "btn-success");
        Button btnQuickSave = createMenuButton("Quick Save", "btn-success");
        Button btnLoad = createMenuButton("Load Game", "btn-warning");
        Button btnQuickLoad = createMenuButton("Quick Load", "btn-warning");
        Button btnGuide = createMenuButton("Guide", "btn-secondary");
        Button btnMainMenu = createMenuButton("Back to Main Menu", "btn-primary");
        Button btnExit = createMenuButton("Exit to Desktop", "btn-purple");
        Button btnClose = createMenuButton("Back to Game", "btn-info");

        // --- BUTON İŞLEVLERİ ---
        btnClose.setOnAction(e -> popupStage.close());
        
        btnExit.setOnAction(e -> System.exit(0)); // Tüm programı kapatır
        
        btnMainMenu.setOnAction(e -> {
            popupStage.close();
            GUITitlescreen titleScreen = new GUITitlescreen();
            titleScreen.show();
        });
        
        btnGuide.setOnAction(e -> {
            popupStage.close();
            javafx.scene.Parent currentRoot = ownerStage.getScene().getRoot();
            String currentTitle = ownerStage.getTitle();
            new GUIGuide(GameContext.getInstance().getPlayerTeam(), () -> {
                ownerStage.getScene().setRoot(currentRoot);
                ownerStage.setTitle(currentTitle);
            });
        });

        btnQuickSave.setOnAction(e -> {
            SaveGame saveData = new SaveGame("autosave", GameContext.getInstance().getActiveLeague(), GameContext.getInstance().getActiveCalendar(), GameContext.getInstance().getPlayerTeam(),
                    gui.GUISquadManager.getInstance().getPitchPlayers(), gui.GUISquadManager.getInstance().getPlayersOnPitchQueue(), gui.GUISquadManager.getInstance().getReservePlayersQueue(),
                    gui.GUISquadManager.getInstance().getCurrentTacticStyle());
            SaveManager.saveGame(saveData, "autosave");
            // İsteğe bağlı: Ekranda küçük bir onay mesajı gösterilebilir.
        });

        btnQuickLoad.setOnAction(e -> {
            java.io.File autoSaveFile = new java.io.File("saves/autosave.json");
            if (autoSaveFile.exists()) {
                SaveGame loadedGame = SaveManager.loadGame("saves/autosave.json");
                if (loadedGame != null) {
                    popupStage.close();
                    GUIMain.loadSavedGame(loadedGame);
                }
            } else {
                GUIPopup.showMessage("Warning", "Quick Save Not Found", "No quick save (autosave) has been created yet.");
            }
        });

        btnLoad.setOnAction(e -> {
            popupStage.close();
            javafx.scene.Parent currentRoot = ownerStage.getScene().getRoot();
            String currentTitle = ownerStage.getTitle();
            GUILoadGame loadGameMenu = new GUILoadGame(
                () -> {
                    ownerStage.getScene().setRoot(currentRoot);
                    ownerStage.setTitle(currentTitle);
                },
                (SaveGame loadedGame) -> GUIMain.loadSavedGame(loadedGame)
            );
            loadGameMenu.show();
        });

        btnSave.setOnAction(e -> {
            popupStage.close();
            javafx.scene.Parent currentRoot = ownerStage.getScene().getRoot();
            String currentTitle = ownerStage.getTitle();
            new GUISaveGame(() -> {
                ownerStage.getScene().setRoot(currentRoot);
                ownerStage.setTitle(currentTitle);
            }).show();
        });

        // Butonları kutuya ekle
        menuBox.getChildren().addAll(title, btnSave, btnQuickSave, btnLoad, btnQuickLoad, btnGuide, btnMainMenu, btnExit, new Region(), btnClose);

        // Kapatma butonundan önce biraz boşluk bırakmak için Region kullandık
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        // Tam ekran arka plan (Ana ekranı karartmak için)
        StackPane root = new StackPane(menuBox);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Arka planı %70 siyah yapar
        root.setPadding(new Insets(50));

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(SceneManager.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Warning: styles.css not found in resources folder.");
        }
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
    private static Button createMenuButton(String text, String cssClass) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.getStyleClass().addAll("btn", cssClass);
        
        return btn;
    }
}