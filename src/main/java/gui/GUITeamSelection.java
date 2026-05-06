package gui;

import Classes.GameContext;
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

    public GUITeamSelection() {
        show();
    }

    public void show() {
        VBox layout = new VBox(30);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("root-dark");

        // Title
        Label title = new Label("SELECT THE TEAM YOU WILL MANAGE");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));

        // Fetch teams from the active league created in GUIMain
        List<ITeam> teams = "VOLLEYBALL".equals(GameContext.getInstance().getActiveSport())
                ? GameContext.getInstance().getActiveVolleyballLeague().getTeamRanking()
                : GameContext.getInstance().getActiveLeague().getTeamRanking();

        // Team List (Scrollable area)
        VBox teamList = new VBox(15);
        teamList.setAlignment(Pos.CENTER);
        
        for (ITeam team : teams) {
            teamList.getChildren().add(createTeamCard(team));
        }

        ScrollPane scrollPane = new ScrollPane(teamList);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        scrollPane.setPrefHeight(500);

        // Back Button
        Button backBtn = new Button("Back to Main Menu");
        backBtn.getStyleClass().addAll("btn", "btn-secondary");
        backBtn.setOnAction(e -> {
            GUITitlescreen titleScreen = new GUITitlescreen();
            titleScreen.show();
        });

        layout.getChildren().addAll(title, scrollPane, backBtn);

        SceneManager.changeScene(layout, "Sports Manager - Select Team");
    }

    private HBox createTeamCard(ITeam team) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15, 30, 15, 30));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(800);
        card.getStyleClass().add("card-box");

        // Team Name and Emblem
        HBox nameBox = new HBox(15);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setPrefWidth(350);
        javafx.scene.Node emblem = GUILeftandTopBarHelper.createEmblem(team, 60);
        Label nameLabel = new Label(team.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameBox.getChildren().addAll(emblem, nameLabel);

     
        VBox stats = new VBox(5);
        Label offLabel = new Label("Offense: " + String.format("%.1f", team.getTotalOffensiveRating()));
        offLabel.setTextFill(Color.web("#4CAF50"));
        Label defLabel = new Label("Defense: " + String.format("%.1f", team.getTotalDefensiveRating()));
        defLabel.setTextFill(Color.web("#e43f5a"));
        stats.getChildren().addAll(offLabel, defLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Select Button
        Button selectBtn = new Button("MANAGE TEAM");
        selectBtn.getStyleClass().addAll("btn", "btn-primary");
        
        selectBtn.setOnAction(e -> {
            team.setManagerAI(false); 
            GameContext.getInstance().setPlayerTeam(team); 
            
            // Ensure all scheduled games for this team have a HumanManager instead of the default AI manager
            Classes.Calendar cal = "VOLLEYBALL".equals(GameContext.getInstance().getActiveSport()) 
                    ? GameContext.getInstance().getActiveVolleyballCalendar() 
                    : GameContext.getInstance().getActiveCalendar();
            
            if (cal != null && cal.getSchedule() != null) {
                for (java.util.List<Classes.Game> games : cal.getSchedule().values()) {
                    for (Classes.Game game : games) {
                        if (game.getHomeTeam().equals(team)) {
                            game.setHomeManager("VOLLEYBALL".equals(GameContext.getInstance().getActiveSport()) 
                                    ? new Sport.Volleyball.HumanManagerVolleyball(team) 
                                    : new Sport.Football.HumanManagerFootball(team));
                        }
                        if (game.getAwayTeam().equals(team)) {
                            game.setAwayManager("VOLLEYBALL".equals(GameContext.getInstance().getActiveSport()) 
                                    ? new Sport.Volleyball.HumanManagerVolleyball(team) 
                                    : new Sport.Football.HumanManagerFootball(team));
                        }
                    }
                }
            }

            new GUIMain(); 
        });

        card.getChildren().addAll(nameBox, stats, spacer, selectBtn);
        return card;
    }
}