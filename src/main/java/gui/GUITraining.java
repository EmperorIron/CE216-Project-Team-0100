package gui;

import Classes.TrainingCategory;
import Interface.ITeam;
import Sport.TrainingDefensiveFootball;
import Sport.TrainingOffensiveFootball;
import Sport.TrainingPhysicalFootball;
import Sport.TrainingMentalFootball; // Eğer bu sınıfınız varsa, yoksa aşağıdan silin
import Sport.GameRulesFootball;
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

    private String activeCoachSortColumn = "AD";
    private boolean coachSortAscending = true;
    private VBox coachListContainer;

    private VBox reportTableContainer;
    public static Map<IPlayer, Double> oldOvrMap = new HashMap<>();
    public static Map<IPlayer, Map<String, Integer>> oldTraitLevelMap = new HashMap<>();
    public static int xpOffensive = 0, xpDefensive = 0, xpPhysical = 0, xpMental = 0;
    public static String lastTrainedCategory = "-";

    static {
        GameRulesFootball rules = new GameRulesFootball();
        String scheduleStr = rules.getTrainingormatch();
        String[] days = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"};
        
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

        show();
    }

    public void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1b1b2f;");

        mainLayout.setTop(createTopBar());
        mainLayout.setLeft(createSidebar());

        HBox content = new HBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        VBox leftPanel = new VBox(15);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPrefWidth(300);

        Label title = new Label("ANTRENMANLAR");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        
        Label subTitle = new Label("Haftalık odak türünü seçin.");
        subTitle.setTextFill(Color.web("#a5a5b0"));
        subTitle.setFont(Font.font("Segoe UI", 16));

        VBox scheduleContainer = createScheduleContainer();
        leftPanel.getChildren().addAll(title, subTitle, scheduleContainer);

        VBox middlePanel = createReportContainer();
        HBox.setHgrow(middlePanel, Priority.ALWAYS);

        VBox rightPanel = createCoachTableContainer();

        content.getChildren().addAll(leftPanel, middlePanel, rightPanel);
        mainLayout.setCenter(content);

        primaryStage.setTitle("Spor Menajerlik - Antrenman");
        
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

        String[] days = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"};
        GameRulesFootball rules = new GameRulesFootball();
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
                Label matchLabel = new Label("MAÇ GÜNÜ");
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

        Label title = new Label("ANTRENÖRLER VE ÖZELLİKLERİ");
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

        ICoach bestOff = getBestCoach("Offensive Coaching");
        ICoach bestDef = getBestCoach("Defensive Coaching");
        ICoach bestPhy = getBestCoach("Motivation");
        ICoach bestMen = getBestCoach("Player Management");

        Comparator<ICoach> comparator = (c1, c2) -> {
            int res = 0;
            switch (activeCoachSortColumn) {
                case "AD": res = c1.getName().compareToIgnoreCase(c2.getName()); break;
                case "HÜC": res = Integer.compare(getTraitVal(c1, "Offensive Coaching"), getTraitVal(c2, "Offensive Coaching")); break;
                case "SAV": res = Integer.compare(getTraitVal(c1, "Defensive Coaching"), getTraitVal(c2, "Defensive Coaching")); break;
                case "MOT": res = Integer.compare(getTraitVal(c1, "Motivation"), getTraitVal(c2, "Motivation")); break;
                case "YÖN": res = Integer.compare(getTraitVal(c1, "Player Management"), getTraitVal(c2, "Player Management")); break;
                case "ALTY": res = Integer.compare(getTraitVal(c1, "Youth Development"), getTraitVal(c2, "Youth Development")); break;
                default: break;
            }
            return coachSortAscending ? res : -res;
        };

        coaches.sort(comparator);

        for (ICoach c : coaches) {
            StringBuilder roleBuilder = new StringBuilder();
            if (c == bestOff) roleBuilder.append("Hücum\n");
            if (c == bestDef) roleBuilder.append("Savunma\n");
            if (c == bestPhy) roleBuilder.append("Fiziksel\n");
            if (c == bestMen) roleBuilder.append("Zihinsel\n");
            
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

    private HBox createCoachHeaderRow() {
        HBox row = new HBox(5);
        row.setPadding(new Insets(10, 5, 10, 5));
        row.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 5;");
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().add(createCoachHeaderBtn("Ad Soyad", "AD", 95));
        row.getChildren().add(createCoachHeaderBtn("Görev", "AD", 60));
        row.getChildren().add(createCoachHeaderBtn("HÜC", "HÜC", 35));
        row.getChildren().add(createCoachHeaderBtn("SAV", "SAV", 35));
        row.getChildren().add(createCoachHeaderBtn("MOT", "MOT", 35));
        row.getChildren().add(createCoachHeaderBtn("YÖN", "YÖN", 35));
        row.getChildren().add(createCoachHeaderBtn("ALTY", "ALTY", 35));

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
                coachSortAscending = sortKey.equals("AD"); 
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
        row.getChildren().add(createCoachStatBox(getTraitVal(c, "Offensive Coaching")));
        row.getChildren().add(createCoachStatBox(getTraitVal(c, "Defensive Coaching")));
        row.getChildren().add(createCoachStatBox(getTraitVal(c, "Motivation")));
        row.getChildren().add(createCoachStatBox(getTraitVal(c, "Player Management")));
        row.getChildren().add(createCoachStatBox(getTraitVal(c, "Youth Development")));

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

        Label title = new Label("OYUNCU GELİŞİM TABLOSU");
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

        header.getChildren().add(createReportLabel("OYUNCU", 130, true));
        header.getChildren().add(createReportLabel("OVR", 90, true));
        header.getChildren().add(createReportLabel("EĞİTİM", 80, true));
        
        String[] traitLabels = {"PAS", "ŞUT", "SAV", "HIZ", "FİZ", "ZİH"};
        for (String tLabel : traitLabels) {
            header.getChildren().add(createReportLabel(tLabel, 145, true));
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

            String[] traits = {"Passing", "Shooting", "Defense", "Speed", "Physical", "Mental"};
            for (String tName : traits) {
                Trait t = p.getTrait(tName);
                int level = t != null ? t.getCurrentLevel() : 0;
                int oldLevel = oldTraitLevelMap.containsKey(p) ? oldTraitLevelMap.get(p).getOrDefault(tName, level) : level;
                int levelDiff = level - oldLevel;
                
                int currentExp = t != null ? (int) t.getExp() : 0;
                int maxExp = t != null ? (int) t.getExpToLevelUp() : 0;
                
                int xpGainedForThisTrait = 0;
                if (tName.equals("Passing") || tName.equals("Shooting")) xpGainedForThisTrait = xpOffensive;
                if (tName.equals("Defense")) xpGainedForThisTrait = xpDefensive;
                if (tName.equals("Speed") || tName.equals("Physical")) xpGainedForThisTrait = xpPhysical;
                if (tName.equals("Mental")) xpGainedForThisTrait = xpMental;

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

        System.out.println("--- HAFTALIK ANTRENMAN BAŞLADI ---");

        oldOvrMap.clear();
        oldTraitLevelMap.clear();
        for (IPlayer p : team.getPlayers()) {
            oldOvrMap.put(p, p.calculateOverallRating());
            
            Map<String, Integer> tMap = new HashMap<>();
            String[] traitsToTrack = {"Passing", "Shooting", "Defense", "Speed", "Physical", "Mental"};
            for (String tName : traitsToTrack) {
                Trait t = p.getTrait(tName);
                tMap.put(tName, t != null ? t.getCurrentLevel() : 0);
            }
            oldTraitLevelMap.put(p, tMap);
        }

        GameRulesFootball rules = new GameRulesFootball();
        String scheduleStr = rules.getTrainingormatch();
        String[] days = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"};

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
                            new TrainingOffensiveFootball().apply(team);
                            xpOffensive += getExpectedXPStatically(team, "Offensive Coaching");
                            break;
                        case DEFENSIVE:
                            new TrainingDefensiveFootball().apply(team);
                            xpDefensive += getExpectedXPStatically(team, "Defensive Coaching");
                            break;
                        case PHYSICAL:
                            new TrainingPhysicalFootball().apply(team);
                            xpPhysical += getExpectedXPStatically(team, "Motivation");
                            break;
                        case MENTAL:
                            new TrainingMentalFootball().apply(team);
                            xpMental += getExpectedXPStatically(team, "Player Management");
                            break;
                    }
                } catch (Exception ex) {
                    System.out.println("HATA: " + ex.getMessage());
                }
            }
        }

        System.out.println("--- ANTRENMAN HAFTASI BİTTİ. OYUNCU GELİŞİMLERİ KAYDEDİLDİ. ---");

        StringBuilder cats = new StringBuilder();
        if (xpOffensive > 0) cats.append("HÜC ");
        if (xpDefensive > 0) cats.append("SAV ");
        if (xpPhysical > 0) cats.append("FİZ ");
        if (xpMental > 0) cats.append("ZİH ");
        lastTrainedCategory = cats.length() > 0 ? cats.toString().trim() : "-";
    }



    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle("-fx-background-color: #162447; -fx-border-color: #d82bbc; -fx-border-width: 0 0 2 0;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);
        Label teamLabel = new Label(playerTeam != null ? playerTeam.getName() : "Takım Seçilmedi");
        teamLabel.setTextFill(Color.WHITE);
        teamLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label managerLabel = new Label("Menajer: Abdullah");
        managerLabel.setTextFill(Color.web("#a5a5b0"));
        managerLabel.setFont(Font.font("Segoe UI", 14));
        infoBox.getChildren().addAll(teamLabel, managerLabel);

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

        Button continueButton = new Button(GUIMain.isMatchDay ? "Maça Çık ⚽" : "Devam Et ▶");
        continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;");
        continueButton.setOnMouseEntered(e -> continueButton.setStyle("-fx-background-color: #ff5773; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        continueButton.setOnMouseExited(e -> continueButton.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20; -fx-background-radius: 5;"));
        
        continueButton.setOnAction(e -> GUIMain.handleContinueAction(primaryStage));

        topBar.getChildren().addAll(infoBox, spacer, dateLabel, menuButton, continueButton);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #1f4068;");
        sidebar.setPrefWidth(220);

        String[] menuItems = {"Ana Sayfa", "Kadro", "Taktikler", "Antrenman", "Fikstür", "Lig Tablosu"};
        
        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40);
            
            if (item.equals("Antrenman")) {
                btn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15; -fx-background-radius: 5;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15; -fx-background-radius: 5; -fx-cursor: hand;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-font-size: 15px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 0 15;"));
            }
            
            btn.setOnAction(e -> {
                if (item.equals("Ana Sayfa")) {
                    new GUIMain(primaryStage); 
                } else if (item.equals("Taktikler")) {
                    if (playerTeam != null) {
                        new GUITactic(primaryStage, playerTeam);
                    }
                } else if (item.equals("Fikstür")) {
                    if (GUIMain.activeCalendar != null && playerTeam != null) {
                        new GUIFixture(primaryStage, playerTeam, GUIMain.activeCalendar);
                    }
                } else if (item.equals("Lig Tablosu")) {
                    if (GUIMain.activeLeague != null && playerTeam != null) {
                        new GUILeagueRanking(primaryStage, playerTeam, GUIMain.activeLeague);
                    }
                }
            });

            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }
}