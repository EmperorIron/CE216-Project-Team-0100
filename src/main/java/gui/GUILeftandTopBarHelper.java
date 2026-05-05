package gui;

import Classes.GameContext;
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

    public static HBox createTopBar(Runnable onContinueOverride) {
        GameContext ctx = GameContext.getInstance();
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.Node emblem = createEmblem(ctx.getPlayerTeam(), 40);
        Label teamLabel = new Label(ctx.getPlayerTeam() != null ? ctx.getPlayerTeam().getName() : "No Team Selected");
        teamLabel.setTextFill(Color.WHITE);
        teamLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        infoBox.getChildren().addAll(emblem, teamLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Classes.Calendar cal = "VOLLEYBALL".equals(ctx.getActiveSport()) ? ctx.getActiveVolleyballCalendar() : ctx.getActiveCalendar();
        String weekText = cal != null ? "Week " + (cal.getCurrentWeek() + 1) : "";
        Label dateLabel = new Label(weekText + (ctx.isMatchDay() ? " - Match Day" : " - Training Week"));
        dateLabel.setTextFill(Color.WHITE);
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        int errorCount = Classes.ErrorHandler.getErrors().size();
        String errorText = "Error" + String.format("%04d", Math.min(errorCount, 9999));
        Button errorButton = new Button(errorText);
        errorButton.getStyleClass().addAll("btn", errorCount > 0 ? "btn-primary" : "btn-secondary");
        errorButton.setOnAction(e -> GUIError.show());

        Button menuButton = new Button("Menu ⚙");
        menuButton.getStyleClass().addAll("btn", "btn-warning");
        menuButton.setOnAction(e -> GUIMenu.show());

        boolean seasonEnded = false;
        if ("VOLLEYBALL".equals(ctx.getActiveSport())) {
            if (ctx.getActiveVolleyballCalendar() != null && ctx.getActiveVolleyballCalendar().getSchedule() != null && ctx.getActiveVolleyballCalendar().getCurrentWeek() >= ctx.getActiveVolleyballCalendar().getSchedule().size()) {
                seasonEnded = true;
            }
        } else {
            if (ctx.getActiveCalendar() != null && ctx.getActiveCalendar().getSchedule() != null && ctx.getActiveCalendar().getCurrentWeek() >= ctx.getActiveCalendar().getSchedule().size()) {
                seasonEnded = true;
            }
        }
        final boolean isSeasonEnded = seasonEnded;

        String continueText = gui.GUISquadManager.getInstance().isMidMatch ? "Back to Match ⚽" : (isSeasonEnded ? "End of Season 🏆" : (ctx.isMatchDay() ? "Play Match ⚽" : "Continue ▶"));
        Button continueButton = new Button(continueText);
        continueButton.getStyleClass().addAll("btn", "btn-primary");
        
        continueButton.setOnAction(e -> {
            if (onContinueOverride != null) {
                onContinueOverride.run();
            } else if (isSeasonEnded) {
                new GUILeagueEnding();
            } else {
                GUIMain.handleContinueAction();
            }
        });

        topBar.getChildren().addAll(infoBox, spacer, dateLabel, errorButton, menuButton, continueButton);
        return topBar;
    }

    public static VBox createSidebar(String activeTab) {
        GameContext ctx = GameContext.getInstance();
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        String[] menuItems = {"Home", "Tactics", "Training", "Fixture", "League Table"};
        
        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40);
            btn.setDisable(gui.GUISquadManager.getInstance().isMidMatch); 
            
            if (item.equals(activeTab)) {
                btn.getStyleClass().add("sidebar-btn-active");
            } else {
                btn.getStyleClass().add("sidebar-btn");
            }
            
            btn.setOnAction(e -> {
                if (item.equals(activeTab)) return; 

                if (item.equals("Home")) {
                    new GUIMain(); 
                } else if (item.equals("Tactics")) {
                    if (ctx.getPlayerTeam() != null) new GUITactic(ctx.getPlayerTeam());
                } else if (item.equals("Fixture")) {
                    if ("VOLLEYBALL".equals(ctx.getActiveSport())) {
                        if (ctx.getActiveVolleyballCalendar() != null && ctx.getPlayerTeam() != null) new GUIFixture(ctx.getPlayerTeam(), ctx.getActiveVolleyballCalendar());
                    } else {
                        if (ctx.getActiveCalendar() != null && ctx.getPlayerTeam() != null) new GUIFixture(ctx.getPlayerTeam(), ctx.getActiveCalendar());
                    }
                } else if (item.equals("League Table")) {
                if ("VOLLEYBALL".equals(ctx.getActiveSport())) {
                    if (ctx.getActiveVolleyballLeague() != null && ctx.getPlayerTeam() != null) new GUILeagueRanking(ctx.getPlayerTeam(), ctx.getActiveVolleyballLeague());
                } else {
                    if (ctx.getActiveLeague() != null && ctx.getPlayerTeam() != null) new GUILeagueRanking(ctx.getPlayerTeam(), ctx.getActiveLeague());
                }
                } else if (item.equals("Training")) {
                    if (ctx.getPlayerTeam() != null) new GUITraining(ctx.getPlayerTeam());
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
