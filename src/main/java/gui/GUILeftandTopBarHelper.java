package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GUILeftandTopBarHelper {

    // Performansı artırmak için logoları bellekte saklayan Cache (Önbellek)
    private static final java.util.Map<String, javafx.scene.image.Image> emblemCache = new java.util.HashMap<>();

    public static HBox createTopBar(Stage primaryStage, Runnable onContinueOverride) {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: #162447; -fx-border-color: #d82bbc; -fx-border-width: 0 0 2 0;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.Node emblem = createEmblem(GUIMain.playerTeam, 40);
        Label teamLabel = new Label(GUIMain.playerTeam != null ? GUIMain.playerTeam.getName() : "Takım Seçilmedi");
        teamLabel.setTextFill(Color.WHITE);
        teamLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        infoBox.getChildren().addAll(emblem, teamLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String weekText = GUIMain.activeCalendar != null ? "Hafta " + (GUIMain.activeCalendar.getCurrentWeek() + 1) : "";
        Label dateLabel = new Label(weekText + (GUIMain.isMatchDay ? " - Maç Günü" : " - Antrenman Haftası"));
        dateLabel.setTextFill(Color.WHITE);
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Button menuButton = new Button("Menü ⚙");
        menuButton.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;");
        menuButton.setOnMouseEntered(e -> menuButton.setStyle("-fx-background-color: #ffb732; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        menuButton.setOnMouseExited(e -> menuButton.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        menuButton.setOnAction(e -> GUIMenu.show(primaryStage));

        String continueText = gui.GUITactic.isMidMatch ? "Maça Dön ⚽" : (GUIMain.isMatchDay ? "Maça Çık ⚽" : "Devam Et ▶");
        Button continueButton = new Button(continueText);
        continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;");
        continueButton.setOnMouseEntered(e -> continueButton.setStyle("-fx-background-color: #ff5773; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        continueButton.setOnMouseExited(e -> continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        
        continueButton.setOnAction(e -> {
            if (onContinueOverride != null) {
                onContinueOverride.run();
            } else {
                GUIMain.handleContinueAction(primaryStage);
            }
        });

        topBar.getChildren().addAll(infoBox, spacer, dateLabel, menuButton, continueButton);
        return topBar;
    }

    public static VBox createSidebar(Stage primaryStage, String activeTab) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #1f4068;");
        sidebar.setPrefWidth(220);

        String[] menuItems = {"Ana Sayfa", "Taktikler", "Antrenman", "Fikstür", "Lig Tablosu"};
        
        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40);
            btn.setDisable(gui.GUITactic.isMidMatch); 
            
            if (item.equals(activeTab)) {
                btn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15; -fx-background-radius: 5;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15; -fx-background-radius: 5; -fx-cursor: hand;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;"));
            }
            
            btn.setOnAction(e -> {
                if (item.equals(activeTab)) return; 

                if (item.equals("Ana Sayfa")) {
                    new GUIMain(primaryStage); 
                } else if (item.equals("Taktikler")) {
                    if (GUIMain.playerTeam != null) new GUITactic(primaryStage, GUIMain.playerTeam);
                } else if (item.equals("Fikstür")) {
                    if (GUIMain.activeCalendar != null && GUIMain.playerTeam != null) new GUIFixture(primaryStage, GUIMain.playerTeam, GUIMain.activeCalendar);
                } else if (item.equals("Lig Tablosu")) {
                    if (GUIMain.activeLeague != null && GUIMain.playerTeam != null) new GUILeagueRanking(primaryStage, GUIMain.playerTeam, GUIMain.activeLeague);
                } else if (item.equals("Antrenman")) {
                    if (GUIMain.playerTeam != null) new GUITraining(primaryStage, GUIMain.playerTeam);
                }
            });

            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }

    public static javafx.scene.Node createEmblem(Interface.ITeam team, double size) {
        Color teamColor = Color.web("#1f4068"); // Varsayılan renk
        if (team != null && team.getName() != null && !team.getName().isEmpty()) {
            int hash = Math.abs(team.getName().hashCode());
            teamColor = Color.hsb(hash % 360, 0.8, 0.9); // Takım ismine özel kalıcı bir renk üret
        }
        
        if (team instanceof Classes.Team) {
            String path = ((Classes.Team) team).getEmblemPath();
            if (path != null && !path.isEmpty()) {
                try {
                    javafx.scene.image.Image img = emblemCache.get(path);
                    if (img == null) {
                        java.io.InputStream stream = GUILeftandTopBarHelper.class.getResourceAsStream("/" + path);
                        if (stream != null) {
                            img = new javafx.scene.image.Image(stream);
                            emblemCache.put(path, img);
                        }
                    }
                    if (img != null) {
                        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                        iv.setFitWidth(size);
                        iv.setFitHeight(size);
                        iv.setPreserveRatio(true);
                        
                        // Kesilme (clip) sorununu çözmek için efektleri doğrudan tek bir ImageView üzerinde zincirliyoruz.
                        javafx.scene.effect.ColorInput colorInput = new javafx.scene.effect.ColorInput(0, 0, size * 2, size * 2, teamColor);
                        javafx.scene.effect.Blend diffBlend = new javafx.scene.effect.Blend(javafx.scene.effect.BlendMode.DIFFERENCE);
                        diffBlend.setTopInput(colorInput);
                        javafx.scene.effect.Blend maskBlend = new javafx.scene.effect.Blend(javafx.scene.effect.BlendMode.SRC_ATOP);
                        maskBlend.setTopInput(diffBlend);
                        iv.setEffect(maskBlend);
                        
                        return iv;
                    }
                } catch (Exception e) {}
            }
        }
        
        // Görsel yüklenemezse bile takıma özel renkte ve baş harfli bir daire oluştur
        javafx.scene.shape.Circle fallback = new javafx.scene.shape.Circle(size / 2, teamColor);
        fallback.setStroke(javafx.scene.paint.Color.WHITE);
        fallback.setStrokeWidth(2);
        
        Label initial = new Label(team != null && team.getName() != null && !team.getName().isEmpty() ? team.getName().substring(0, 1).toUpperCase() : "?");
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font("Segoe UI", FontWeight.BOLD, size / 2.5));
        
        return new StackPane(fallback, initial);
    }
}
