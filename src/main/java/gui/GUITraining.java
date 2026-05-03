package gui;

import Classes.TrainingCategory;
import Interface.ITeam;
import Sport.Football.TrainingDefensiveFootball;
import Sport.Football.TrainingOffensiveFootball;
import Sport.Football.TrainingPhysicalFootball;
import Sport.Football.TrainingMentalFootball;
import Sport.Volleyball.TrainingDefensiveVolleyball;
import Sport.Volleyball.TrainingOffensiveVolleyball;
import Sport.Volleyball.TrainingPhysicalVolleyball;
import Sport.Volleyball.TrainingMentalVolleyball;
import Sport.Football.GameRulesFootball;
import Sport.Volleyball.GameRulesVolleyball;
import io.SaveGame;
import io.SaveManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import Interface.IPlayer;
import Interface.ICoach;
import Classes.Trait;

public class GUITraining {

    private Stage primaryStage;
    private ITeam playerTeam;
    
    public static Map<String, TrainingCategory> weeklySchedule = new HashMap<>();

    private String activeCoachSortColumn = "NAME";
    private boolean coachSortAscending = true;
    private VBox coachListContainer;

    private VBox reportTableContainer;
    private List<String> coachTraitNames = new ArrayList<>();
    private List<String> playerTraitNames = new ArrayList<>();
    public static Map<IPlayer, Double> oldOvrMap = new HashMap<>();
    public static Map<IPlayer, Map<String, Integer>> oldTraitLevelMap = new HashMap<>();
    public static int xpOffensive = 0, xpDefensive = 0, xpPhysical = 0, xpMental = 0;
    public static String lastTrainedCategory = "-";

    static {
        Classes.GameRules rules = "VOLLEYBALL".equals(GUIMain.activeSport) ? new GameRulesVolleyball() : new GameRulesFootball();
        String scheduleStr = rules.getTrainingormatch();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        
        for (int i = 0; i < 7; i++) {
            if (scheduleStr.charAt(i) == '0') {
                if (i % 2 == 0) weeklySchedule.put(days[i], TrainingCategory.PHYSICAL);
                else weeklySchedule.put(days[i], TrainingCategory.OFFENSIVE);
            }
        }
    }

    public GUITraining(Stage primaryStage, ITeam playerTeam) {
        this.primaryStage = primaryStage;
        this.playerTeam = playerTeam;
        
        if (playerTeam != null && playerTeam.getCoaches() != null && !playerTeam.getCoaches().isEmpty()) {
            coachTraitNames.addAll(playerTeam.getCoaches().get(0).getTraits().keySet());
        } else {
            if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
                coachTraitNames.addAll(java.util.Arrays.asList("Serve Coaching", "Attack Coaching", "Defense Coaching", "Player Management", "Motivation"));
            } else {
                coachTraitNames.addAll(java.util.Arrays.asList("Offensive Coaching", "Defensive Coaching", "Player Management", "Motivation", "Youth Development"));
            }
        }

        if (playerTeam != null && playerTeam.getPlayers() != null && !playerTeam.getPlayers().isEmpty()) {
            playerTraitNames.addAll(playerTeam.getPlayers().get(0).getTraits().keySet());
        } else {
            if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
                playerTraitNames.addAll(java.util.Arrays.asList("Serving", "Spiking", "Setting", "Blocking", "Digging", "Speed", "Physical", "Mental"));
            } else {
                playerTraitNames.addAll(java.util.Arrays.asList("Defense", "Passing", "Shooting", "Speed", "Physical", "Mental", "Goalkeeping"));
            }
        }

        show();
    }

    public void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1b1b2f;");

        mainLayout.setTop(GUILeftandTopBarHelper.createTopBar(primaryStage, null));
        mainLayout.setLeft(GUILeftandTopBarHelper.createSidebar(primaryStage, "Training"));

        HBox content = new HBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        VBox leftPanel = new VBox(15);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPrefWidth(300);

        Label title = new Label("TRAINING SESSIONS");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        
        Label subTitle = new Label("Select the weekly focus type.");
        subTitle.setTextFill(Color.web("#a5a5b0"));
        subTitle.setFont(Font.font("Segoe UI", 16));

        VBox scheduleContainer = createScheduleContainer();
        leftPanel.getChildren().addAll(title, subTitle, scheduleContainer);

        VBox middlePanel = createReportContainer();
        HBox.setHgrow(middlePanel, Priority.ALWAYS);

        VBox rightPanel = createCoachTableContainer();

        content.getChildren().addAll(leftPanel, middlePanel, rightPanel);
        mainLayout.setCenter(content);

        primaryStage.setTitle("Sports Manager - Training");
        
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(mainLayout, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(mainLayout);
        }
    }

    private VBox createScheduleContainer() {
        VBox container = new VBox(8);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(300);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        Classes.GameRules rules = "VOLLEYBALL".equals(GUIMain.activeSport) ? new GameRulesVolleyball() : new GameRulesFootball();
        String scheduleStr = rules.getTrainingormatch();

        for (int i = 0; i < days.length; i++) {
            String day = days[i];
            HBox dayRow = new HBox(10);
            dayRow.setPadding(new Insets(8, 10, 8, 10));
            dayRow.setStyle("-fx-background-color: #162447; -fx-background-radius: 8; -fx-border-color: #1f4068; -fx-border-width: 2;");
            dayRow.setAlignment(Pos.CENTER_LEFT);

            Label dayLabel = new Label(day.toUpperCase());
            dayLabel.setTextFill(Color.WHITE);
            dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            dayLabel.setPrefWidth(85);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            if (scheduleStr.charAt(i) == '1') {
                Label matchLabel = new Label("MATCH DAY");
                matchLabel.setTextFill(Color.web("#e43f5a"));
                matchLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                dayRow.getChildren().addAll(dayLabel, spacer, matchLabel);
                dayRow.setStyle("-fx-background-color: #1b1b2f; -fx-background-radius: 8; -fx-border-color: #e43f5a; -fx-border-width: 2;");
            } else {
                ComboBox<TrainingCategory> categoryBox = new ComboBox<>();
                categoryBox.getItems().addAll(TrainingCategory.OFFENSIVE, TrainingCategory.DEFENSIVE, TrainingCategory.PHYSICAL, TrainingCategory.MENTAL);
                categoryBox.setValue(weeklySchedule.get(day));
                categoryBox.setStyle("-fx-background-color: #1f4068; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                categoryBox.setPrefWidth(135);

                javafx.util.Callback<ListView<TrainingCategory>, ListCell<TrainingCategory>> cellFactory = lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(TrainingCategory item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("-fx-background-color: #1f4068;");
                        } else {
                            setText(item.toString());
                            setTextFill(Color.WHITE);
                            setStyle("-fx-background-color: #1f4068; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");
                        }
                    }
                };
                categoryBox.setCellFactory(cellFactory);
                categoryBox.setButtonCell(cellFactory.call(null));

                categoryBox.setOnAction(e -> weeklySchedule.put(day, categoryBox.getValue()));

                dayRow.getChildren().addAll(dayLabel, spacer, categoryBox);
            }

            container.getChildren().add(dayRow);
        }

        return container;
    }

    private VBox createCoachTableContainer() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(420);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;");

        Label title = new Label("COACHES AND TRAITS");
        title.setTextFill(Color.web("#f0a500"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        coachListContainer = new VBox(5);
        refreshCoachList();

        ScrollPane scroll = new ScrollPane(coachListContainer);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: #162447; -fx-background-color: transparent; -fx-control-inner-background: #162447;");
        scroll.setPrefHeight(500);

        panel.getChildren().addAll(title, scroll);
        return panel;
    }

    private void refreshCoachList() {
        coachListContainer.getChildren().clear();
        coachListContainer.getChildren().add(createCoachHeaderRow());

        if (playerTeam == null || playerTeam.getCoaches() == null) return;

        List<ICoach> coaches = new ArrayList<>(playerTeam.getCoaches());

        Map<String, ICoach> bestCoaches = new HashMap<>();
        for (String trait : coachTraitNames) {
            bestCoaches.put(trait, getBestCoach(trait));
        }

        Comparator<ICoach> comparator = (c1, c2) -> {
            int res = 0;
            if (activeCoachSortColumn.equals("NAME")) {
                res = c1.getName().compareToIgnoreCase(c2.getName());
            } else {
                res = Integer.compare(getTraitVal(c1, activeCoachSortColumn), getTraitVal(c2, activeCoachSortColumn));
            }
            return coachSortAscending ? res : -res;
        };

        coaches.sort(comparator);

        for (ICoach c : coaches) {
            StringBuilder roleBuilder = new StringBuilder();
            for (String trait : coachTraitNames) {
                if (c == bestCoaches.get(trait)) {
                    roleBuilder.append(getShortTraitName(trait)).append("\n");
                }
            }

            String role = roleBuilder.toString().trim();
            if (role.isEmpty()) role = "-";

            coachListContainer.getChildren().add(createCoachTableRow(c, role));
        }
    }

    private int getTraitVal(ICoach c, String tName) {
        Trait t = c.getTrait(tName);
        return t != null ? t.getCurrentLevel() : 0;
    }

    private ICoach getBestCoach(String traitName) {
        if (playerTeam == null || playerTeam.getCoaches() == null) return null;
        ICoach best = null;
        int max = -1;
        for (ICoach c : playerTeam.getCoaches()) {
            int val = getTraitVal(c, traitName);
            if (val > max) {
                max = val;
                best = c;
            }
        }
        return best;
    }

    private String getShortTraitName(String traitName) {
        if (traitName == null) return "";
        if (traitName.length() <= 4) return traitName.toUpperCase();
        String[] parts = traitName.split(" ");
        if (parts.length > 1) {
            return (parts[0].substring(0, Math.min(3, parts[0].length())) + " " + parts[1].substring(0, Math.min(3, parts[1].length()))).toUpperCase();
        }
        return traitName.substring(0, 4).toUpperCase();
    }

    private HBox createCoachHeaderRow() {
        HBox row = new HBox(5);
        row.setPadding(new Insets(10, 5, 10, 5));
        row.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 5;");
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().add(createCoachHeaderBtn("Full Name", "NAME", 95));
        row.getChildren().add(createCoachHeaderBtn("Role", "NAME", 60));
        
        for (String trait : coachTraitNames) {
            row.getChildren().add(createCoachHeaderBtn(getShortTraitName(trait), trait, 40));
        }

        return row;
    }

    private Button createCoachHeaderBtn(String text, String sortKey, double width) {
        String finalTxt = text;
        if (activeCoachSortColumn.equals(sortKey)) {
            finalTxt += coachSortAscending ? " \u2191" : " \u2193";
        }

        Button btn = new Button(finalTxt);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e43f5a; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;");
        btn.setPrefWidth(width);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.setOnAction(e -> {
            if (activeCoachSortColumn.equals(sortKey)) {
                coachSortAscending = !coachSortAscending;
            } else {
                activeCoachSortColumn = sortKey;
                coachSortAscending = sortKey.equals("NAME"); 
            }
            refreshCoachList();
        });

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e43f5a; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));

        return btn;
    }

    private HBox createCoachTableRow(ICoach c, String role) {
        HBox row = new HBox(5);
        row.setPadding(new Insets(8, 5, 8, 5));
        row.setStyle("-fx-background-color: #1a294c; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;");
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(c.getName() + "\n" + c.getSurname());
        nameLbl.setPrefWidth(95);
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Label roleLbl = new Label(role);
        roleLbl.setPrefWidth(60);
        roleLbl.setTextFill(Color.web("#f0a500"));
        roleLbl.setFont(Font.font("Segoe UI", 11));
        roleLbl.setWrapText(true);

        row.getChildren().addAll(nameLbl, roleLbl);
        
        for (String trait : coachTraitNames) {
            StackPane statBox = createCoachStatBox(getTraitVal(c, trait));
            statBox.setPrefWidth(40);
            row.getChildren().add(statBox);
        }

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #1a294c; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    private StackPane createCoachStatBox(int rating) {
        StackPane box = new StackPane();
        box.setPrefSize(35, 25);
        
        String bgColor = "#e43f5a"; 
        if (rating >= 80) bgColor = "#4CAF50"; 
        else if (rating >= 60) bgColor = "#f0a500"; 

        box.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 4;");

        Label ratingLbl = new Label(String.valueOf(rating));
        ratingLbl.setTextFill(Color.WHITE);
        ratingLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        box.getChildren().add(ratingLbl);
        return box;
    }

    private int getExpectedXP(String traitName) {
        return getExpectedXPStatically(playerTeam, traitName);
    }

    public static int getExpectedXPStatically(ITeam team, String traitName) {
        int maxSkill = 0;
        if (team != null && team.getCoaches() != null) {
            for (ICoach coach : team.getCoaches()) {
                Trait trait = coach.getTrait(traitName);
                if (trait != null && trait.getCurrentLevel() > maxSkill) {
                    maxSkill = trait.getCurrentLevel();
                }
            }
        }
        return 120 + (maxSkill * 100);
    }

    private VBox createReportContainer() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(450);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;");

        Label title = new Label("PLAYER DEVELOPMENT TABLE");
        title.setTextFill(Color.web("#f0a500"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        reportTableContainer = new VBox(5);
        refreshReportTable();

        ScrollPane scroll = new ScrollPane(reportTableContainer);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: #162447; -fx-background-color: transparent; -fx-control-inner-background: #162447;");
        scroll.setPrefHeight(500);

        panel.getChildren().addAll(title, scroll);
        return panel;
    }

    private void refreshReportTable() {
        if (reportTableContainer == null) return;
        reportTableContainer.getChildren().clear();

        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 5;");
        header.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().add(createReportLabel("PLAYER", 130, true));
        header.getChildren().add(createReportLabel("OVR", 90, true));
        header.getChildren().add(createReportLabel("TRAINING", 80, true));
        
        for (String tName : playerTraitNames) {
            header.getChildren().add(createReportLabel(getShortTraitName(tName), 145, true));
        }
        
        reportTableContainer.getChildren().add(header);

        if (playerTeam == null || playerTeam.getPlayers() == null) return;

        for (IPlayer p : playerTeam.getPlayers()) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #1a294c; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;");
            row.setAlignment(Pos.CENTER_LEFT);

            row.getChildren().add(createReportLabel(p.getFullName(), 130, true));

            double currentOvr = p.calculateOverallRating();
            double oldOvr = oldOvrMap.getOrDefault(p, currentOvr);
            double diff = currentOvr - oldOvr;

            Label lblOvr = createReportLabel(String.format("%.1f", currentOvr), 90, true);
            if (diff > 0.05) {
                lblOvr.setText(String.format("%.1f (+%.1f)", currentOvr, diff));
                lblOvr.setTextFill(Color.web("#4CAF50"));
            }
            row.getChildren().add(lblOvr);

            Label lblCat = createReportLabel(lastTrainedCategory, 80, false);
            lblCat.setTextFill(Color.web("#f0a500"));
            row.getChildren().add(lblCat);

            for (String tName : playerTraitNames) {
                Trait t = p.getTrait(tName);
                int level = t != null ? t.getCurrentLevel() : 0;
                int oldLevel = oldTraitLevelMap.containsKey(p) ? oldTraitLevelMap.get(p).getOrDefault(tName, level) : level;
                int levelDiff = level - oldLevel;
                
                int currentExp = t != null ? (int) t.getExp() : 0;
                int maxExp = t != null ? (int) t.getExpToLevelUp() : 0;
                
                int xpGainedForThisTrait = 0;
                if (t != null && t.getCategory() != null) {
                    switch (t.getCategory()) {
                        case OFFENSE: xpGainedForThisTrait = xpOffensive; break;
                        case DEFENSE: xpGainedForThisTrait = xpDefensive; break;
                        case PHYSICAL: xpGainedForThisTrait = xpPhysical; break;
                        case MENTAL: xpGainedForThisTrait = xpMental; break;
                    }
                }

                String levelStr = String.valueOf(level);
                if (levelDiff > 0) {
                    levelStr += " +" + levelDiff;
                }

                String text = levelStr + " {" + currentExp + "(+" + xpGainedForThisTrait + ")/" + maxExp + "}";
                Label lblT = createReportLabel(text, 145, false);
                
                if (levelDiff > 0) lblT.setTextFill(Color.web("#f0a500"));
                else if (xpGainedForThisTrait > 0) lblT.setTextFill(Color.web("#4CAF50"));
                else lblT.setTextFill(Color.web("#a5a5b0"));
                
                row.getChildren().add(lblT);
            }

            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #213663; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #1a294c; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;"));

            reportTableContainer.getChildren().add(row);
        }
    }

    private Label createReportLabel(String text, double width, boolean isBold) {
        Label lbl = new Label(text);
        lbl.setPrefWidth(width);
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Segoe UI", isBold ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        lbl.setTooltip(new Tooltip(text));
        return lbl;
    }

    // --- SEÇİLEN ANTRENMANLARI TAKIMA UYGULAMA ---
    private void applyWeeklyTraining() {
        applyWeeklyTrainingStatically(playerTeam);
        refreshReportTable();
    }

    public static void applyWeeklyTrainingStatically(ITeam team) {
        if (team == null) return;

        System.out.println("--- WEEKLY TRAINING STARTED ---");

        oldOvrMap.clear();
        oldTraitLevelMap.clear();
        for (IPlayer p : team.getPlayers()) {
            oldOvrMap.put(p, p.calculateOverallRating());
            
            Map<String, Integer> tMap = new HashMap<>();
            for (String tName : p.getTraits().keySet()) {
                Trait t = p.getTrait(tName);
                tMap.put(tName, t != null ? t.getCurrentLevel() : 0);
            }
            oldTraitLevelMap.put(p, tMap);
        }

        Classes.GameRules rules = "VOLLEYBALL".equals(GUIMain.activeSport) ? new GameRulesVolleyball() : new GameRulesFootball();
        String scheduleStr = rules.getTrainingormatch();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        xpOffensive = 0;
        xpDefensive = 0;
        xpPhysical = 0;
        xpMental = 0;

        for (int i = 0; i < 7; i++) {
            if (scheduleStr.charAt(i) == '0') {
                String day = days[i];
                TrainingCategory category = weeklySchedule.get(day);
                if (category == null) continue;
                
                try {
                    switch (category) {
                        case OFFENSIVE:
                            if ("VOLLEYBALL".equals(GUIMain.activeSport)) new TrainingOffensiveVolleyball().apply(team);
                            else new TrainingOffensiveFootball().apply(team);
                            String offTrait = "VOLLEYBALL".equals(GUIMain.activeSport) ? "Attack Coaching" : "Offensive Coaching";
                            xpOffensive += getExpectedXPStatically(team, offTrait);
                            break;
                        case DEFENSIVE:
                            if ("VOLLEYBALL".equals(GUIMain.activeSport)) new TrainingDefensiveVolleyball().apply(team);
                            else new TrainingDefensiveFootball().apply(team);
                            String defTrait = "VOLLEYBALL".equals(GUIMain.activeSport) ? "Defense Coaching" : "Defensive Coaching";
                            xpDefensive += getExpectedXPStatically(team, defTrait);
                            break;
                        case PHYSICAL:
                            if ("VOLLEYBALL".equals(GUIMain.activeSport)) new TrainingPhysicalVolleyball().apply(team);
                            else new TrainingPhysicalFootball().apply(team);
                            String phyTrait = "VOLLEYBALL".equals(GUIMain.activeSport) ? "Motivation" : "Motivation";
                            xpPhysical += getExpectedXPStatically(team, phyTrait);
                            break;
                        case MENTAL:
                            if ("VOLLEYBALL".equals(GUIMain.activeSport)) new TrainingMentalVolleyball().apply(team);
                            else new TrainingMentalFootball().apply(team);
                            xpMental += getExpectedXPStatically(team, "Player Management");
                            break;
                    }
                } catch (Exception ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                }
            }
        }

        System.out.println("--- TRAINING WEEK ENDED. PLAYER DEVELOPMENTS SAVED. ---");

        StringBuilder cats = new StringBuilder();
        if (xpOffensive > 0) cats.append("OFF ");
        if (xpDefensive > 0) cats.append("DEF ");
        if (xpPhysical > 0) cats.append("PHY ");
        if (xpMental > 0) cats.append("MEN ");
        lastTrainedCategory = cats.length() > 0 ? cats.toString().trim() : "-";
    }



}