package gui;

import Classes.GameContext;
import Interface.ITeam;
import Classes.League;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUILeagueRanking {

    private ITeam playerTeam;
    private League activeLeague;

    private VBox tableContainer;
    private String activeSortColumn = "P"; // Default Puan (Points)
    private boolean sortAscending = false;

    public GUILeagueRanking(ITeam playerTeam, League activeLeague) {
        this.playerTeam = playerTeam;
        this.activeLeague = activeLeague;
        show();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1b1b2f;");

        root.setTop(GUILeftandTopBarHelper.createTopBar(null));
        root.setLeft(GUILeftandTopBarHelper.createSidebar("League Table"));

        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 40, 20, 40));

        // Üst Başlık Alanı
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("LEAGUE TABLE");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label subtitle = new Label(activeLeague != null ? activeLeague.getName() + " Standings" : "Standings");
        subtitle.setTextFill(Color.web("#a5a5b0"));
        subtitle.setFont(Font.font("Segoe UI", 14));

        header.getChildren().addAll(title, subtitle);

        // Tablo Düzeni
        VBox tableLayout = new VBox(0);
        tableLayout.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;");
        tableLayout.setPadding(new Insets(10));

        tableContainer = new VBox(5);
        refreshTable();

        ScrollPane scrollPane = new ScrollPane(tableContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        tableLayout.getChildren().add(scrollPane);
        content.getChildren().addAll(header, tableLayout);
        root.setCenter(content);

        SceneManager.changeScene(root, "Sports Manager - League Table - " + playerTeam.getName());
    }

    private void refreshTable() {
        tableContainer.getChildren().clear();
        tableContainer.getChildren().add(createHeaderRow());

        if (activeLeague == null) return;

        List<ITeam> teams = new ArrayList<>(activeLeague.getTeamRanking());
        boolean isVolleyball = "VOLLEYBALL".equals(GameContext.getInstance().getActiveSport());

        Comparator<ITeam> comparator = (t1, t2) -> {
            int result = 0;
            switch (activeSortColumn) {
                case "TEAM": result = t1.getName().compareToIgnoreCase(t2.getName()); break;
                case "P": result = Integer.compare(
                        t1.getWins() + t1.getLosses() + (isVolleyball ? 0 : t1.getDraws()), 
                        t2.getWins() + t2.getLosses() + (isVolleyball ? 0 : t2.getDraws())); break;
                case "W": result = Integer.compare(t1.getWins(), t2.getWins()); break;
                case "D": result = Integer.compare(t1.getDraws(), t2.getDraws()); break;
                case "L": result = Integer.compare(t1.getLosses(), t2.getLosses()); break;
                case "GF": result = Integer.compare(t1.getGoalsScored(), t2.getGoalsScored()); break;
                case "GA": result = Integer.compare(t1.getGoalsConceded(), t2.getGoalsConceded()); break;
                case "GD": result = Integer.compare(t1.getGoalDifference(), t2.getGoalDifference()); break;
                case "PTS":
                default:
                    result = Integer.compare(t1.getPoints(), t2.getPoints());
                    if (result == 0) result = Integer.compare(t1.getGoalDifference(), t2.getGoalDifference());
                    if (result == 0) result = Integer.compare(t1.getGoalsScored(), t2.getGoalsScored());
                    break;
            }
            return sortAscending ? result : -result;
        };

        teams.sort(comparator);

        int pos = 1;
        for (ITeam team : teams) {
            tableContainer.getChildren().add(createTeamRow(pos++, team));
        }
    }

    private HBox createHeaderRow() {
        HBox row = new HBox(15);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 5;");
        row.setAlignment(Pos.CENTER_LEFT);

        Label posLbl = new Label("POS");
        posLbl.setPrefWidth(50);
        posLbl.setTextFill(Color.web("#a5a5b0"));
        posLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        posLbl.setAlignment(Pos.CENTER);
        row.getChildren().add(posLbl);

        boolean isVolleyball = "VOLLEYBALL".equals(GameContext.getInstance().getActiveSport());

        row.getChildren().add(createHeaderButton("TEAM", "TEAM", 250));
        row.getChildren().add(createHeaderButton("P", "P", 50));
        row.getChildren().add(createHeaderButton("W", "W", 50));
        if (!isVolleyball) row.getChildren().add(createHeaderButton("D", "D", 50));
        row.getChildren().add(createHeaderButton("L", "L", 50));
        row.getChildren().add(createHeaderButton(isVolleyball ? "SW" : "GF", "GF", 50));
        row.getChildren().add(createHeaderButton(isVolleyball ? "SL" : "GA", "GA", 50));
        row.getChildren().add(createHeaderButton(isVolleyball ? "SD" : "GD", "GD", 50));
        row.getChildren().add(createHeaderButton("PTS", "PTS", 50));

        return row;
    }

    private Button createHeaderButton(String displayText, String sortKey, double width) {
        String finalTxt = displayText;
        if (activeSortColumn.equals(sortKey)) {
            finalTxt += sortAscending ? " \u2191" : " \u2193";
        }

        Button btn = new Button(finalTxt);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e43f5a; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        btn.setPrefWidth(width);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.setOnAction(e -> {
            if (activeSortColumn.equals(sortKey)) {
                sortAscending = !sortAscending;
            } else {
                activeSortColumn = sortKey;
                sortAscending = sortKey.equals("TEAM"); 
            }
            refreshTable();
        });

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e43f5a; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;"));

        return btn;
    }

    private HBox createTeamRow(int position, ITeam team) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(12, 10, 12, 10));
        row.setAlignment(Pos.CENTER_LEFT);

        if (team.equals(playerTeam)) {
            row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5; -fx-border-color: #f0a500; -fx-border-width: 1.5;");
        } else {
            row.setStyle("-fx-background-color: #1a294c; -fx-background-radius: 5; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;");
        }

        Label posLbl = createLabel(String.valueOf(position), 50, true);
        posLbl.setAlignment(Pos.CENTER);
        if (position <= 3) posLbl.setTextFill(Color.web("#4CAF50")); // Şampiyonlar Ligi / Avrupa potası
        else if (position >= activeLeague.getTeamRanking().size() - 2) posLbl.setTextFill(Color.web("#e43f5a")); // Küme düşme potası
        
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setPrefWidth(250);
        javafx.scene.Node emblem = GUILeftandTopBarHelper.createEmblem(team, 25);
        Label nameLbl = createLabel(team.getName(), 200, true);
        nameBox.getChildren().addAll(emblem, nameLbl);
        
        boolean isVolleyball = "VOLLEYBALL".equals(GameContext.getInstance().getActiveSport());
        int played = team.getWins() + team.getLosses() + (isVolleyball ? 0 : team.getDraws());
        Label pLbl = createLabel(String.valueOf(played), 50, false);
        Label wLbl = createLabel(String.valueOf(team.getWins()), 50, false);
        Label dLbl = isVolleyball ? null : createLabel(String.valueOf(team.getDraws()), 50, false);
        Label lLbl = createLabel(String.valueOf(team.getLosses()), 50, false);
        Label gfLbl = createLabel(String.valueOf(team.getGoalsScored()), 50, false);
        Label gaLbl = createLabel(String.valueOf(team.getGoalsConceded()), 50, false);
        Label gdLbl = createLabel(String.valueOf(team.getGoalDifference()), 50, false);
        
        Label ptsLbl = createLabel(String.valueOf(team.getPoints()), 50, true);
        ptsLbl.setTextFill(Color.web("#f0a500"));

        row.getChildren().addAll(posLbl, nameBox, pLbl, wLbl);
        if (!isVolleyball) row.getChildren().add(dLbl);
        row.getChildren().addAll(lLbl, gfLbl, gaLbl, gdLbl, ptsLbl);

        if (!team.equals(playerTeam)) {
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #1a294c; -fx-background-radius: 5; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;"));
        }

        return row;
    }

    private Label createLabel(String text, double width, boolean isBold) {
        Label lbl = new Label(text);
        lbl.setPrefWidth(width);
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Segoe UI", isBold ? FontWeight.BOLD : FontWeight.NORMAL, 14));
        lbl.setAlignment(Pos.CENTER_LEFT);
        return lbl;
    }

}