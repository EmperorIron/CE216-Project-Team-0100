package gui;

import Classes.Positions;
import Classes.Trait;
import Interface.IPlayer;
import Interface.ITeam;
import Sport.GameRulesFootball;
import Sport.PositionsFootball;
import Sport.AIAdaptableEasyFootball;
import io.SaveGame;
import io.SaveManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GUITactic {

    private Stage primaryStage;
    private ITeam playerTeam;

    // --- STATİK (KALICI) VERİLER ---
    private static IPlayer[][] pitchPlayers = new IPlayer[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
    private static LinkedList<IPlayer> playersOnPitchQueue = new LinkedList<>();
    private static LinkedList<IPlayer> reservePlayersQueue = new LinkedList<>();
    private static List<IPlayer> subbedOutPlayers = new ArrayList<>();
    public static List<IPlayer> redCardedPlayers = new ArrayList<>();
    public static List<IPlayer> yellowCardedPlayers = new ArrayList<>();
    public static List<IPlayer> injuredInMatchPlayers = new ArrayList<>();
    private static String currentTacticStyle = "Balanced (xG: 1.00, xGA: 1.00)";
    private static ITeam currentTeam = null;
    public static boolean isMidMatch = false;
    public static Runnable onResumeMatch = null;

    // --- UI (GÖRSEL) BİLEŞENLER ---
    private StackPane[][] gridSlots = new StackPane[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
    private Region[][] highlightBoxes = new Region[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
    
    private VBox squadListContainer;
    private Label squadStatusLabel; 
    private Label teamStatsLabel;
    private ComboBox<String> tacticStyleComboBox;
    private List<String> traitNames = new ArrayList<>();
    
    private String activeSortColumn = "NO";
    private boolean sortAscending = true;

    private IPlayer selectedPlayerForPlacement = null;
    private HBox selectedPlayerRow = null;

    private int maxFieldPlayers;
    private int maxReservePlayers;

    public GUITactic(Stage primaryStage, ITeam playerTeam) {
        this.primaryStage = primaryStage;
        this.playerTeam = playerTeam;
        
        if (currentTeam != playerTeam) {
            pitchPlayers = new IPlayer[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
            playersOnPitchQueue.clear();
            reservePlayersQueue.clear();
            currentTeam = playerTeam;
        }

    if (!isMidMatch) {
        subbedOutPlayers.clear();
        redCardedPlayers.clear();
        yellowCardedPlayers.clear();
        injuredInMatchPlayers.clear();
    } else {
        // Sakat ve kırmızı kartlı oyunculara değişiklikle çıkarılmış oyuncu muamelesi yap
        for (IPlayer p : playerTeam.getPlayers()) {
            if (p.isInjured() || redCardedPlayers.contains(p)) {
                if (!subbedOutPlayers.contains(p)) subbedOutPlayers.add(p);
            }
        }
    }

        GameRulesFootball rules = new GameRulesFootball();
        this.maxFieldPlayers = rules.getFieldPlayerCount() - redCardedPlayers.size();
        this.maxReservePlayers = rules.getReservePlayerCount();
        
        if (!playerTeam.getPlayers().isEmpty()) {
            traitNames.addAll(playerTeam.getPlayers().get(0).getTraits().keySet());
        }
        show();
    }

    public static IPlayer[][] getPitchPlayers() { return pitchPlayers; }
    public static LinkedList<IPlayer> getPlayersOnPitchQueue() { return playersOnPitchQueue; }
    public static LinkedList<IPlayer> getReservePlayersQueue() { return reservePlayersQueue; }
    public static String getCurrentTacticStyle() { return currentTacticStyle; }

    public static void applyRedCard(IPlayer player) {
        if (!redCardedPlayers.contains(player)) {
            redCardedPlayers.add(player);
        }
        if (!subbedOutPlayers.contains(player)) {
            subbedOutPlayers.add(player); // Oyundan çıkarılmış muamelesi yap
        }
        playersOnPitchQueue.remove(player);
        if (!reservePlayersQueue.contains(player)) {
            reservePlayersQueue.add(player); // Maç kadrosundan (yedeklerden) silinmesini engelle
        }
        for (int i = 0; i < Positions.GRID_WIDTH; i++) {
            for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                if (pitchPlayers[i][j] != null && pitchPlayers[i][j].equals(player)) {
                    pitchPlayers[i][j] = null;
                }
            }
        }
    }

    public static void performAutomaticInjuryRemoval(IPlayer player) {
        if (!injuredInMatchPlayers.contains(player)) {
            injuredInMatchPlayers.add(player);
        }
        if (!subbedOutPlayers.contains(player)) {
            subbedOutPlayers.add(player); // Oyundan çıkarılmış muamelesi yap
        }
        playersOnPitchQueue.remove(player);
        if (!reservePlayersQueue.contains(player)) {
            reservePlayersQueue.add(player); // Maç kadrosundan (yedeklerden) silinmesini engelle
        }
        for (int i = 0; i < Positions.GRID_WIDTH; i++) {
            for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                if (pitchPlayers[i][j] != null && pitchPlayers[i][j].equals(player)) {
                    pitchPlayers[i][j] = null;
                }
            }
        }
    }

    public static void performAutomaticSub(IPlayer pOut, IPlayer pIn) {
        if (playersOnPitchQueue.contains(pOut) && reservePlayersQueue.contains(pIn)) {
            // Çıkan ve Giren oyuncuların yerlerini değiştir
            playersOnPitchQueue.remove(pOut);
            reservePlayersQueue.remove(pIn);
            
            if (!subbedOutPlayers.contains(pOut)) {
                subbedOutPlayers.add(pOut); // Çıkan yedek olarak işaretle ki bir daha giremesin
            }
            
            playersOnPitchQueue.add(pIn);
            reservePlayersQueue.add(pOut); // Çıkan oyuncuyu yedek listesine (kulübeye) geri koy
            
            // Matrix (Yeşil Saha) üzerinde yerlerini takas et
            for (int i = 0; i < Positions.GRID_WIDTH; i++) {
                for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                    if (pitchPlayers[i][j] != null && pitchPlayers[i][j].equals(pOut)) {
                        pitchPlayers[i][j] = pIn;
                        if (pIn instanceof Classes.Player) {
                            int posId = Classes.Positions.getPositionId(i, j);
                            ((Classes.Player) pIn).setCurrentPositionId(posId);
                            
                            // Yeni oyuncu için xG ve xGA değerlerini hesapla
                            PositionsFootball posFootball = new PositionsFootball();
                            float xgMult = posFootball.getXgMultipliers().getOrDefault(posId, 1.0f);
                            float xgaMult = posFootball.getXgaMultipliers().getOrDefault(posId, 1.0f);
                            double ovr = pIn.calculateOverallRating();
                            pIn.setxG((float) ((ovr / 100.0) * xgMult * 2.0)); 
                            pIn.setxGA((float) ((ovr / 100.0) * xgaMult * 2.0));
                        }
                        return;
                    }
                }
            }
        }
    }

    public static void loadTacticData(IPlayer[][] loadedPitch, LinkedList<IPlayer> loadedOnPitch, LinkedList<IPlayer> loadedReserves, ITeam team, String tacticStyle) {
        currentTeam = team;
        if (team == null || team.getPlayers() == null) return;
        
        currentTacticStyle = (tacticStyle != null) ? tacticStyle : "Balanced (xG: 1.00, xGA: 1.00)";
        
        Map<String, IPlayer> playerMap = new HashMap<>();
        for (IPlayer p : team.getPlayers()) {
            playerMap.put(p.getFullName(), p);
        }
        
        pitchPlayers = new IPlayer[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
        if (loadedPitch != null) {
            for (int i = 0; i < loadedPitch.length; i++) {
                for (int j = 0; j < loadedPitch[i].length; j++) {
                    if (loadedPitch[i][j] != null) {
                        IPlayer realP = playerMap.get(loadedPitch[i][j].getFullName());
                        pitchPlayers[i][j] = realP;
                        if (realP instanceof Classes.Player) {
                            int posId = Classes.Positions.getPositionId(i, j);
                            ((Classes.Player) realP).setCurrentPositionId(posId);
                            PositionsFootball posFootball = new PositionsFootball();
                            float xgMult = posFootball.getXgMultipliers().getOrDefault(posId, 1.0f);
                            float xgaMult = posFootball.getXgaMultipliers().getOrDefault(posId, 1.0f);
                            double ovr = realP.calculateOverallRating();
                            realP.setxG((float) ((ovr / 100.0) * xgMult * 2.0)); 
                            realP.setxGA((float) ((ovr / 100.0) * xgaMult * 2.0));
                        }
                    }
                }
            }
        }
        
        playersOnPitchQueue = new LinkedList<>();
        if (loadedOnPitch != null) {
            for (IPlayer p : loadedOnPitch) {
                IPlayer realP = playerMap.get(p.getFullName());
                if (realP != null) playersOnPitchQueue.add(realP);
            }
        }
        
        reservePlayersQueue = new LinkedList<>();
        if (loadedReserves != null) {
            for (IPlayer p : loadedReserves) {
                IPlayer realP = playerMap.get(p.getFullName());
                if (realP != null) reservePlayersQueue.add(realP);
            }
        }
    }

    public static void postMatchCleanup() {
        // Maç sonrası yedek listesinden sakat oyuncuları temizle
        if (reservePlayersQueue != null) {
            reservePlayersQueue.removeIf(IPlayer::isInjured);
        }
        
        // Maç içi durum değişkenlerini sıfırla
        isMidMatch = false;
        subbedOutPlayers.clear();
        redCardedPlayers.clear();
        yellowCardedPlayers.clear();
        injuredInMatchPlayers.clear();
        onResumeMatch = null;
    }

    public void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1b1b2f;");

        Runnable tacticContinueAction = () -> {
            if (GUIMain.isMatchDay || isMidMatch) {
                if (playersOnPitchQueue.size() != maxFieldPlayers) {
                    GUIPopup.showMessage(primaryStage, "Eksik Kadro", "Kadro Tamamlanmadı!", "İşleme devam edebilmek için İlk 11'in tam olması gerekmektedir!");
                    return;
                }
                
                boolean hasInjuredStarter = false;
                for (IPlayer p : playersOnPitchQueue) {
                    if (p.isInjured()) {
                        hasInjuredStarter = true;
                        break;
                    }
                }
                if (hasInjuredStarter) {
                    GUIPopup.showMessage(primaryStage, "Sakat Oyuncu", "İlk 11'de Sakat Oyuncu Var!", "İşleme devam edebilmek için sahada sakat oyuncu bulunmamalıdır. Lütfen sakat oyuncuyu yedeğe alın.");
                    return;
                }
                
                if (reservePlayersQueue.size() != maxReservePlayers) {
                    GUIPopup.showConfirmation(primaryStage, "Eksik Yedekler", "Yedek Kulübesi Tam Değil!", "Yedek kulübesinde " + maxReservePlayers + " oyuncu yok. Yine de devam etmek istiyor musunuz?", 
                        () -> {
                            if (isMidMatch && onResumeMatch != null) onResumeMatch.run();
                            else { GUIMain.tacticConfirmedForMatch = true; GUIMain.handleContinueAction(primaryStage); }
                        }, null);
                    return;
                }
            }
            if (isMidMatch && onResumeMatch != null) {
                onResumeMatch.run();
            } else {
                GUIMain.tacticConfirmedForMatch = true;
                GUIMain.handleContinueAction(primaryStage);
            }
        };

        mainLayout.setTop(GUILeftandTopBarHelper.createTopBar(primaryStage, tacticContinueAction));
        mainLayout.setLeft(GUILeftandTopBarHelper.createSidebar(primaryStage, "Taktikler"));

        HBox content = new HBox(40);
        content.setPadding(new Insets(30, 40, 30, 40));
        content.setAlignment(Pos.CENTER);

        StackPane pitchContainer = createPitchArea();
        VBox rightPanel = createRightPanel();

        content.getChildren().addAll(pitchContainer, rightPanel);
        mainLayout.setCenter(content);

        primaryStage.setTitle("Spor Menajerlik - Taktikler");
        
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(mainLayout, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(mainLayout);
        }
    }


    private int getInvertedY(int y) {
        return (Positions.GRID_HEIGHT - 1) - y;
    }

    private boolean isPositionValidForUI(int x, int y, IPlayer playerToPlace) {
        int posId = Classes.Positions.getPositionId(x, y);
        Sport.PositionsFootball posInfo = new Sport.PositionsFootball();
        if (!posInfo.getValidPositions().contains(posId)) return false;
        return true;
    }
   
    private StackPane createPitchArea() {
        StackPane container = new StackPane();
        container.setPrefSize(540, 680);
        container.setMaxSize(540, 680);
        container.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;");

        GridPane grid10x10 = new GridPane();
        grid10x10.setPrefSize(540, 680);

        for (int i = 0; i < Positions.GRID_WIDTH; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / Positions.GRID_WIDTH);
            grid10x10.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < Positions.GRID_HEIGHT; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / Positions.GRID_HEIGHT);
            grid10x10.getRowConstraints().add(rc);
        }

        for (int x = 0; x < Positions.GRID_WIDTH; x++) {
            for (int logicalY = 0; logicalY < Positions.GRID_HEIGHT; logicalY++) {
                
                int visualY = getInvertedY(logicalY);
                boolean isValid = isPositionValidForUI(x, logicalY, null);

                highlightBoxes[x][logicalY] = new Region();
                if (!isValid) {
                    highlightBoxes[x][logicalY].setStyle("-fx-background-color: #000000; -fx-opacity: 0.6; -fx-background-radius: 5;");
                } else {
                    highlightBoxes[x][logicalY].setStyle("-fx-background-color: transparent; -fx-background-radius: 5;");
                }
                GridPane.setMargin(highlightBoxes[x][logicalY], new Insets(1));
                grid10x10.add(highlightBoxes[x][logicalY], x, visualY);

                gridSlots[x][logicalY] = new StackPane();
                
                if (pitchPlayers[x][logicalY] != null) {
                    fillSlotWithPlayerUI(gridSlots[x][logicalY], pitchPlayers[x][logicalY], x, logicalY);
                } else if (isValid) {
                    resetSlot(x, logicalY);
                }
                
                grid10x10.add(gridSlots[x][logicalY], x, visualY);
                GridPane.setHalignment(gridSlots[x][logicalY], HPos.CENTER);
                GridPane.setValignment(gridSlots[x][logicalY], VPos.CENTER);
            }
        }

        container.getChildren().add(grid10x10);
        return container;
    }

    private void resetSlot(int x, int y) {
        StackPane slot = gridSlots[x][y];
        slot.getChildren().clear();
        slot.setPrefSize(54, 68); 
        slot.setMaxSize(54, 68);

        Circle dashedCircle = new Circle(15, Color.TRANSPARENT);
        dashedCircle.setStroke(Color.web("#FFFFFF60"));
        dashedCircle.setStrokeWidth(1.5);
        dashedCircle.getStrokeDashArray().addAll(4d, 4d);

        Label plusLabel = new Label("+");
        plusLabel.setTextFill(Color.web("#FFFFFF60"));
        plusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        slot.getChildren().addAll(dashedCircle, plusLabel);

        slot.setOnMouseEntered(e -> {
            dashedCircle.setStroke(Color.WHITE);
            plusLabel.setTextFill(Color.WHITE);
            slot.setStyle("-fx-cursor: hand;");
        });
        slot.setOnMouseExited(e -> {
            dashedCircle.setStroke(Color.web("#FFFFFF60"));
            plusLabel.setTextFill(Color.web("#FFFFFF60"));
        });

        slot.setOnMouseClicked(e -> {
            if (selectedPlayerForPlacement != null) {
                placePlayerOnPitch(selectedPlayerForPlacement, x, y);
                clearSelection(); 
            } else {
                openPlayerSelectionDialog(x, y);
            }
        });
    }

    private void openPlayerSelectionDialog(int x, int y) {
        int targetPosId = Positions.getPositionId(x, y);
        GameRulesFootball rules = new GameRulesFootball();

        List<IPlayer> availablePlayers = new ArrayList<>();
        for (IPlayer p : playerTeam.getPlayers()) {
            if (p.isInjured()) continue;
            if (isMidMatch && redCardedPlayers.contains(p)) continue;
            if (isMidMatch && !rules.isCanReEnter() && subbedOutPlayers.contains(p)) continue;
            availablePlayers.add(p);
        }

        availablePlayers.sort((p1, p2) -> Integer.compare(
                p2.getProficiencyAt(targetPosId),
                p1.getProficiencyAt(targetPosId)
        ));

        Map<String, IPlayer> playerMap = new HashMap<>();
        List<String> playerOptions = new ArrayList<>();

        for (IPlayer p : availablePlayers) {
            int proficiency = p.getProficiencyAt(targetPosId);
            String optionText = String.format("%s (Uyum: %d) - OVR: %.1f", 
                                p.getFullName(), proficiency, p.calculateOverallRating());
            
            if (playersOnPitchQueue.contains(p)) {
                optionText += " [Zaten Sahada]";
            } else if (reservePlayersQueue.contains(p)) {
                optionText += " [Yedekte]";
            }
            playerOptions.add(optionText);
            playerMap.put(optionText, p);
        }

        if (playerOptions.isEmpty()) return;

        GUIPopup.showChoiceDialog(primaryStage, "Oyuncu Seçimi", 
            String.format("Pozisyon: (X:%d, Y:%d)\n(Sahada en fazla %d oyuncu olabilir)", x, y, maxFieldPlayers),
            "Oyuncu Seçin:", playerOptions, selectedOption -> {
                IPlayer selectedPlayer = playerMap.get(selectedOption);
                placePlayerOnPitch(selectedPlayer, x, y);
                drawHeatMap(null);
            });
    }

    private void placePlayerOnPitch(IPlayer player, int destX, int destY) {
        GameRulesFootball rules = new GameRulesFootball();
        if (player.isInjured() || (isMidMatch && redCardedPlayers.contains(player)) || (isMidMatch && !rules.isCanReEnter() && subbedOutPlayers.contains(player))) {
            GUIPopup.showMessage(primaryStage, "Geçersiz Hamle", "Bu oyuncu sahaya sürülemez!", "Sakat, cezalı veya oyundan çıkmış oyuncular sadece yedek kulübesinde durabilir.");
            clearSelection();
            return;
        }

        int targetPosId = Positions.getPositionId(destX, destY);
        
      
        if (Sport.PositionsFootball.isGoalkeeperPosition(targetPosId)) {
            for (int i = 0; i < Positions.GRID_WIDTH; i++) {
                for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                    if (i == destX && j == destY) continue; 
                    IPlayer existingGK = pitchPlayers[i][j];
                    if (existingGK != null && Sport.PositionsFootball.isGoalkeeperPosition(Positions.getPositionId(i, j))) {
                        removePlayerFromSquad(existingGK);
                    }
                }
            }
        }

        IPlayer existingPlayer = pitchPlayers[destX][destY];
        if (existingPlayer != null && !existingPlayer.equals(player)) {
            playersOnPitchQueue.remove(existingPlayer);
            if (isMidMatch && !subbedOutPlayers.contains(existingPlayer)) {
                subbedOutPlayers.add(existingPlayer);
            }
            if (existingPlayer instanceof Classes.Player) {
                ((Classes.Player) existingPlayer).setCurrentPositionId(existingPlayer.getPrimaryPositionId());
                existingPlayer.setxG(0f);
                existingPlayer.setxGA(0f);
            }
            // Çıkan oyuncuyu yedek listesine geri ekle
            if (!reservePlayersQueue.contains(existingPlayer)) {
                reservePlayersQueue.add(existingPlayer);
            }
        }

        if (reservePlayersQueue.contains(player)) {
            reservePlayersQueue.remove(player);
        }

        if (playersOnPitchQueue.contains(player)) {
            playersOnPitchQueue.remove(player);
            removePlayerFromMatrix(player);
        }

        playersOnPitchQueue.add(player);

        if (playersOnPitchQueue.size() > maxFieldPlayers) {
            IPlayer oldestPlayer = playersOnPitchQueue.removeFirst();
            removePlayerFromMatrix(oldestPlayer);
            if (oldestPlayer instanceof Classes.Player) {
                ((Classes.Player) oldestPlayer).setCurrentPositionId(oldestPlayer.getPrimaryPositionId());
                oldestPlayer.setxG(0f);
                oldestPlayer.setxGA(0f);
            }
            // Kadro taştığı için sahadan alınan oyuncuyu yedek listesine geri ekle
            if (isMidMatch && !subbedOutPlayers.contains(oldestPlayer)) {
                subbedOutPlayers.add(oldestPlayer);
            }
            if (!reservePlayersQueue.contains(oldestPlayer)) {
                reservePlayersQueue.add(oldestPlayer);
            }
        }

        pitchPlayers[destX][destY] = player;
        
        // Yedek kapasitesinin aşılmasını önle (Eğer dışarıdan oyuncu girdiyse)
        while (reservePlayersQueue.size() > maxReservePlayers) {
            IPlayer dropped = reservePlayersQueue.removeFirst();
            if (dropped instanceof Classes.Player) {
                ((Classes.Player) dropped).setCurrentPositionId(dropped.getPrimaryPositionId());
                dropped.setxG(0f);
                dropped.setxGA(0f);
            }
        }
        
        if (player instanceof Classes.Player) {
            int posId = Positions.getPositionId(destX, destY);
            ((Classes.Player) player).setCurrentPositionId(posId);
            PositionsFootball posFootball = new PositionsFootball();
            float xgMult = posFootball.getXgMultipliers().getOrDefault(posId, 1.0f);
            float xgaMult = posFootball.getXgaMultipliers().getOrDefault(posId, 1.0f);
            double ovr = player.calculateOverallRating();
            player.setxG((float) ((ovr / 100.0) * xgMult * 2.0)); 
            player.setxGA((float) ((ovr / 100.0) * xgaMult * 2.0));
        }
        fillSlotWithPlayerUI(gridSlots[destX][destY], player, destX, destY);

        refreshSquadList();
        drawHeatMap(selectedPlayerForPlacement);
    }

    private void placePlayerOnBench(IPlayer player) {
        // Sadece sahanın dışındaki bir sakat oyuncuyu yedeğe almaya çalışırsa engelle (Sahadakini çıkarabilmeli)
        if (player.isInjured() && !playersOnPitchQueue.contains(player)) {
            GUIPopup.showMessage(primaryStage, "Geçersiz Hamle", "Sakat Oyuncu Yedeğe Alınamaz!", player.getFullName() + " sakatlığı nedeniyle yedek kulübesine alınamaz.");
            clearSelection();
            return;
        }

        if (playersOnPitchQueue.contains(player)) {
            playersOnPitchQueue.remove(player);
            if (isMidMatch && !subbedOutPlayers.contains(player)) {
                subbedOutPlayers.add(player);
            }
            removePlayerFromMatrix(player);
        }
        
        if (reservePlayersQueue.contains(player)) {
            reservePlayersQueue.remove(player);
        }

        reservePlayersQueue.add(player);

       
        if (reservePlayersQueue.size() > maxReservePlayers) {
            IPlayer dropped = reservePlayersQueue.removeFirst(); 
            if (dropped instanceof Classes.Player) {
                ((Classes.Player) dropped).setCurrentPositionId(dropped.getPrimaryPositionId());
                dropped.setxG(0f);
                dropped.setxGA(0f);
            }
        }

      
        if (player instanceof Classes.Player) {
            ((Classes.Player) player).setCurrentPositionId(player.getPrimaryPositionId());
            player.setxG(0f);
            player.setxGA(0f);
        }

        clearSelection();
        refreshSquadList();
    }

    private void removePlayerFromSquad(IPlayer player) {
        if (playersOnPitchQueue.contains(player)) {
            playersOnPitchQueue.remove(player);
            removePlayerFromMatrix(player);
        }
        if (reservePlayersQueue.contains(player)) {
            reservePlayersQueue.remove(player);
        }

        clearSelection();
        refreshSquadList();
    }

    private void removePlayerFromMatrix(IPlayer player) {
        for (int i = 0; i < Positions.GRID_WIDTH; i++) {
            for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                if (pitchPlayers[i][j] != null && pitchPlayers[i][j].equals(player)) {
                    pitchPlayers[i][j] = null; 
                    resetSlot(i, j); 
                }
            }
        }
    }

    private void autoFillSquad() {
        for (int i = 0; i < Positions.GRID_WIDTH; i++) {
            for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                if (pitchPlayers[i][j] != null) {
                    IPlayer p = pitchPlayers[i][j];
                    if (p instanceof Classes.Player) {
                        ((Classes.Player) p).setCurrentPositionId(p.getPrimaryPositionId());
                        p.setxG(0f);
                        p.setxGA(0f);
                    }
                    pitchPlayers[i][j] = null;
                    resetSlot(i, j);
                }
            }
        }
        playersOnPitchQueue.clear();
        reservePlayersQueue.clear();
        clearSelection();

        AIAdaptableEasyFootball ai = new AIAdaptableEasyFootball(playerTeam);
        Interface.ITactic aiTactic = ai.generateStartingTactic();
        
        for (IPlayer p : aiTactic.getStartingLineup()) {
            if (p instanceof Classes.Player) {
                ((Classes.Player) p).setCurrentPositionId(p.getPrimaryPositionId());
            }
        }

        PositionsFootball.resolvePositionCollisions(aiTactic);

        PositionsFootball posInfo = new PositionsFootball();

        for (IPlayer p : aiTactic.getStartingLineup()) {
            int posId = p.getPrimaryPositionId();
            if (p instanceof Classes.Player) {
                posId = ((Classes.Player) p).getCurrentPositionId();
            }
            int targetX = Classes.Positions.getX(posId);
            int targetY = Classes.Positions.getY(posId);
            
           
            IPlayer existing = pitchPlayers[targetX][targetY];
            if (existing != null) {
                playersOnPitchQueue.remove(existing);
            }

            pitchPlayers[targetX][targetY] = p;
            
            if (!playersOnPitchQueue.contains(p)) {
                playersOnPitchQueue.add(p);
            }
            
            if (p instanceof Classes.Player) {
                float xgMult = posInfo.getXgMultipliers().getOrDefault(posId, 1.0f);
                float xgaMult = posInfo.getXgaMultipliers().getOrDefault(posId, 1.0f);
                double ovr = p.calculateOverallRating();
                p.setxG((float) ((ovr / 100.0) * xgMult * 2.0)); 
                p.setxGA((float) ((ovr / 100.0) * xgaMult * 2.0));
            }
            fillSlotWithPlayerUI(gridSlots[targetX][targetY], p, targetX, targetY);
        }

        for (IPlayer p : aiTactic.getSubstitutes()) {
            if (reservePlayersQueue.size() < maxReservePlayers && !reservePlayersQueue.contains(p)) {
                reservePlayersQueue.add(p);
            }
        }
        
        // Kalan yedek kontenjanını sakat veya cezalı oyuncularla doldur (kadronun tam 9 yedeğe ulaşabilmesi için)
        for (IPlayer p : playerTeam.getPlayers()) {
            if (reservePlayersQueue.size() < maxReservePlayers && !playersOnPitchQueue.contains(p) && !reservePlayersQueue.contains(p)) {
                reservePlayersQueue.add(p);
            }
        }
        
        refreshSquadList();
        drawHeatMap(null);
    }

    private void fillSlotWithPlayerUI(StackPane slot, IPlayer player, int x, int y) {
        slot.getChildren().clear();
        slot.setOnMouseEntered(null);
        slot.setOnMouseExited(null);
        slot.setPrefSize(54, 68); 
        slot.setMaxSize(54, 68);

        VBox playerNode = new VBox(2);
        playerNode.setAlignment(Pos.CENTER);

        String kitNumber = y + "" + x;

        StackPane kitPane = new StackPane();
        Circle kit = new Circle(15, Color.web("#e43f5a"));
        kit.setStroke(Color.WHITE);
        kit.setStrokeWidth(1.5);
        
        Label numberLbl = new Label(kitNumber);
        numberLbl.setTextFill(Color.WHITE);
        numberLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

        kitPane.getChildren().addAll(kit, numberLbl);

        String shortName = formatPlayerName(player.getFullName());
        
        shortName = shortName.replace(" ", "\n");

        String bgColor = "rgba(0,0,0,0.6)";
        if (player.isInjured()) {
            shortName += " 🚑";
            bgColor = "rgba(216,43,188,0.8)";
        } else if (isMidMatch && redCardedPlayers.contains(player)) {
            shortName += " 🟥";
            bgColor = "rgba(228,63,90,0.8)";
        } else if (isMidMatch && yellowCardedPlayers.contains(player)) {
            shortName += " 🟨";
            bgColor = "rgba(240,165,0,0.8)";
        }

        Label nameLbl = new Label(shortName);
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9)); 
        nameLbl.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 0; -fx-background-radius: 4;");
        nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setMinHeight(0); 
        nameLbl.setMaxHeight(26);
        nameLbl.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        nameLbl.setMaxWidth(54); 
        nameLbl.setWrapText(true);
        nameLbl.setTooltip(new Tooltip(player.getFullName()));

        playerNode.getChildren().addAll(kitPane, nameLbl);

        playerNode.setOnMouseClicked(e -> {
            if (selectedPlayerForPlacement != null) {
                placePlayerOnPitch(selectedPlayerForPlacement, x, y);
                clearSelection();
            } else {
                openPlayerSelectionDialog(x, y);
            }
        });

        playerNode.setOnMouseEntered(e -> { kit.setFill(Color.web("#ff5773")); slot.setStyle("-fx-cursor: hand;"); });
        playerNode.setOnMouseExited(e -> kit.setFill(Color.web("#e43f5a")));

        slot.getChildren().add(playerNode);
        slot.setOnMouseClicked(null); 
    }

    private void handlePlayerRowClick(IPlayer player, HBox row) {
        if (selectedPlayerForPlacement == player) {
            clearSelection(); 
        } else {
            clearSelection(); 
            
            selectedPlayerForPlacement = player;
            selectedPlayerRow = row;
            
            row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5; -fx-border-color: #f0a500; -fx-border-width: 2;");
            
            drawHeatMap(player);
        }
    }

    private void clearSelection() {
        if (selectedPlayerRow != null) {
            selectedPlayerRow.setStyle("-fx-background-color: #1a294c; -fx-background-radius: 5; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;");
        }
        selectedPlayerForPlacement = null;
        selectedPlayerRow = null;
        drawHeatMap(null); 
    }

    private void drawHeatMap(IPlayer player) {
        if (player == null) {
            for (int x = 0; x < Positions.GRID_WIDTH; x++) {
                for (int logicalY = 0; logicalY < Positions.GRID_HEIGHT; logicalY++) {
                    if (!isPositionValidForUI(x, logicalY, null)) {
                        highlightBoxes[x][logicalY].setStyle("-fx-background-color: #000000; -fx-opacity: 0.6; -fx-background-radius: 5;");
                        if (pitchPlayers[x][logicalY] == null) {
                            gridSlots[x][logicalY].getChildren().clear();
                            gridSlots[x][logicalY].setOnMouseClicked(null);
                        }
                    } else {
                        highlightBoxes[x][logicalY].setStyle("-fx-background-color: transparent; -fx-background-radius: 5;");
                        if (pitchPlayers[x][logicalY] == null && gridSlots[x][logicalY].getChildren().isEmpty()) {
                            resetSlot(x, logicalY);
                        }
                    }
                }
            }
            return;
        }

        int max1 = -1;
        int max2 = -1;

        for (int x = 0; x < Positions.GRID_WIDTH; x++) {
            for (int logicalY = 0; logicalY < Positions.GRID_HEIGHT; logicalY++) {
                if (!isPositionValidForUI(x, logicalY, player)) continue;
                int posId = Positions.getPositionId(x, logicalY);
                int prof = player.getProficiencyAt(posId);
                if (prof > max1) {
                    max2 = max1;
                    max1 = prof;
                } else if (prof > max2 && prof < max1) {
                    max2 = prof;
                }
            }
        }

        boolean allMax1Full = true;
        for (int x = 0; x < Positions.GRID_WIDTH; x++) {
            for (int logicalY = 0; logicalY < Positions.GRID_HEIGHT; logicalY++) {
                if (!isPositionValidForUI(x, logicalY, player)) continue;
                int posId = Positions.getPositionId(x, logicalY);
                int prof = player.getProficiencyAt(posId);
                if (prof == max1) {
                    boolean isAvailable = (pitchPlayers[x][logicalY] == null || pitchPlayers[x][logicalY] == player);
                    if (isAvailable) {
                        allMax1Full = false;
                        break;
                    }
                }
            }
        }

        for (int x = 0; x < Positions.GRID_WIDTH; x++) {
            for (int logicalY = 0; logicalY < Positions.GRID_HEIGHT; logicalY++) {
                if (!isPositionValidForUI(x, logicalY, player)) {
                    highlightBoxes[x][logicalY].setStyle("-fx-background-color: #000000; -fx-opacity: 0.6; -fx-background-radius: 5;");
                    continue;
                }
                int posId = Positions.getPositionId(x, logicalY);
                int prof = player.getProficiencyAt(posId);
                
                String bgColor;
                String borderStyle = "";

                if (prof >= 80) bgColor = "rgba(76, 175, 80, 0.4)"; 
                else if (prof >= 60) bgColor = "rgba(240, 165, 0, 0.4)"; 
                else bgColor = "rgba(228, 63, 90, 0.4)"; 

                boolean isAvailable = (pitchPlayers[x][logicalY] == null || pitchPlayers[x][logicalY] == player);

                if (isAvailable) {
                    if (prof == max1) {
                        bgColor = "rgba(255, 255, 255, 0.7)"; 
                        borderStyle = " -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 5;";
                    } else if (prof == max2 && allMax1Full) {
                        bgColor = "rgba(0, 0, 0, 0.7)"; 
                        borderStyle = " -fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 5;";
                    }
                }

                String finalStyle = "-fx-background-color: " + bgColor + "; -fx-background-radius: 5;" + borderStyle;
                highlightBoxes[x][logicalY].setStyle(finalStyle);
            }
        }
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(650);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-color: #1f4068; -fx-border-width: 2;");

        HBox topControls = new HBox(20);
        topControls.setAlignment(Pos.CENTER_LEFT);
        
        squadStatusLabel = new Label();
        squadStatusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        tacticStyleComboBox = new ComboBox<>();
        tacticStyleComboBox.getItems().addAll(
            "Balanced (xG: 1.00, xGA: 1.00)",
            "All Out Attack (xG: 1.25, xGA: 1.15)",
            "Park the Bus (xG: 0.85, xGA: 0.75)"
        );
        tacticStyleComboBox.setValue(currentTacticStyle);
        tacticStyleComboBox.setStyle("-fx-background-color: #1f4068;");

        javafx.util.Callback<ListView<String>, ListCell<String>> cellFactory = lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #1f4068;");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: #1f4068; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");
                }
            }
        };
        tacticStyleComboBox.setCellFactory(cellFactory);
        tacticStyleComboBox.setButtonCell(cellFactory.call(null));
        
        tacticStyleComboBox.setOnAction(e -> {
            currentTacticStyle = tacticStyleComboBox.getValue();
            refreshSquadList();
        });
        
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        topControls.getChildren().addAll(squadStatusLabel, topSpacer, tacticStyleComboBox);

        HBox squadHeader = new HBox(10);
        squadHeader.setAlignment(Pos.CENTER_LEFT);
        
        teamStatsLabel = new Label("Takım xG: 0.00 | Takım xGA: 0.00");
        teamStatsLabel.setTextFill(Color.WHITE);
        teamStatsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button autoFillBtn = new Button("OTOMATİK KUR");
        autoFillBtn.setDisable(isMidMatch);
        autoFillBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        autoFillBtn.setOnAction(e -> autoFillSquad());

        Button benchBtn = new Button("YEDEĞE AL");
        benchBtn.setStyle("-fx-background-color: #f0a500; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        benchBtn.setOnAction(e -> {
            if (selectedPlayerForPlacement != null) {
                placePlayerOnBench(selectedPlayerForPlacement);
            }
        });

        Button removeBtn = new Button("ÇIKAR");
        removeBtn.setStyle("-fx-background-color: #4e4e6a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            if (selectedPlayerForPlacement != null) {
                removePlayerFromSquad(selectedPlayerForPlacement);
            }
        });

        squadHeader.getChildren().addAll(teamStatsLabel, headerSpacer, autoFillBtn, benchBtn, removeBtn);

        squadListContainer = new VBox(5);
        refreshSquadList(); 

        ScrollPane scrollPane = new ScrollPane(squadListContainer);
        scrollPane.setFitToWidth(false); 
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(480);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #162447; -fx-background-color: transparent;");

        panel.getChildren().addAll(topControls, squadHeader, scrollPane);
        return panel;
    }

    private String getPlayerKitNumber(IPlayer p) {
        for (int x = 0; x < Positions.GRID_WIDTH; x++) {
            for (int y = 0; y < Positions.GRID_HEIGHT; y++) {
                if (pitchPlayers[x][y] != null && pitchPlayers[x][y].equals(p)) {
                    return y + "" + x; 
                }
            }
        }
        if (reservePlayersQueue.contains(p)) {
            return "Y" + (reservePlayersQueue.indexOf(p) + 1); 
        }
        return "-"; 
    }

   
    private float[] getStyleMultipliers() {
        float styleXgMult = 1.0f;
        float styleXgaMult = 1.0f;
        if (currentTacticStyle != null) {
            if (currentTacticStyle.startsWith("All Out Attack")) {
                styleXgMult = 1.25f;
                styleXgaMult = 1.15f;
            } else if (currentTacticStyle.startsWith("Park the Bus")) {
                styleXgMult = 0.85f;
                styleXgaMult = 0.75f;
            }
        }
        return new float[]{styleXgMult, styleXgaMult};
    }

    private void refreshSquadList() {
        squadListContainer.getChildren().clear();

        List<IPlayer> allPlayers = playerTeam.getPlayers();
        List<IPlayer> onPitchPlayers = new ArrayList<>();
        List<IPlayer> reservePlayersList = new ArrayList<>();
        List<IPlayer> unselectedPlayers = new ArrayList<>();

        float totalXG = 0f;
        float totalXGA = 0f;

        for (IPlayer p : allPlayers) {
            String kit = getPlayerKitNumber(p);
            if (kit.startsWith("Y")) {
                reservePlayersList.add(p);
            } else if (!kit.equals("-")) {
                onPitchPlayers.add(p);
                totalXG += p.getxG();
                totalXGA += p.getxGA();
            } else {
                unselectedPlayers.add(p);
            }
        }

        squadStatusLabel.setText(String.format("SAHA: %d/%d | YEDEK: %d/%d", 
                playersOnPitchQueue.size(), maxFieldPlayers, 
                reservePlayersQueue.size(), maxReservePlayers));

     
        float[] mults = getStyleMultipliers();
        float styleXgMult = mults[0];
        float styleXgaMult = mults[1];

        if (teamStatsLabel != null) {
            teamStatsLabel.setText(String.format("Takım xG: %.2f  |  Takım xGA: %.2f", totalXG * styleXgMult, totalXGA * styleXgaMult));
        }
        
        if (playersOnPitchQueue.size() < maxFieldPlayers) {
            squadStatusLabel.setTextFill(Color.web("#e43f5a")); 
        } else {
            squadStatusLabel.setTextFill(Color.web("#4CAF50")); 
        }

        squadListContainer.getChildren().add(createHeaderRow());

        Comparator<IPlayer> comparator = (p1, p2) -> {
            int result = 0;
            switch (activeSortColumn) {
                case "NO":
                    String kit1 = getPlayerKitNumber(p1);
                    String kit2 = getPlayerKitNumber(p2);
                    
                    int no1 = kit1.equals("-") ? 9999 : (kit1.startsWith("Y") ? 1000 + Integer.parseInt(kit1.substring(1)) : Integer.parseInt(kit1));
                    int no2 = kit2.equals("-") ? 9999 : (kit2.startsWith("Y") ? 1000 + Integer.parseInt(kit2.substring(1)) : Integer.parseInt(kit2));
                    
                    result = Integer.compare(no1, no2);
                    break;
                case "OYUNCU ADI":
                    result = p1.getFullName().compareToIgnoreCase(p2.getFullName());
                    break;
                case "OVR":
                    result = Double.compare(p1.calculateOverallRating(), p2.calculateOverallRating());
                    break;
                case "XG":
                    result = Float.compare(p1.getxG(), p2.getxG());
                    break;
                case "XGA":
                    result = Float.compare(p1.getxGA(), p2.getxGA());
                    break;
                default: 
                    Trait t1 = p1.getTrait(activeSortColumn);
                    Trait t2 = p2.getTrait(activeSortColumn);
                    int v1 = t1 != null ? t1.getCurrentLevel() : 0;
                    int v2 = t2 != null ? t2.getCurrentLevel() : 0;
                    result = Integer.compare(v1, v2);
                    break;
            }
            return sortAscending ? result : -result;
        };

        onPitchPlayers.sort(comparator);
        reservePlayersList.sort(comparator);
        unselectedPlayers.sort(comparator);

        for (IPlayer p : onPitchPlayers) {
          
            HBox row = createPlayerRow(p, getPlayerKitNumber(p), styleXgMult, styleXgaMult);
            if (p == selectedPlayerForPlacement) {
                row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5; -fx-border-color: #f0a500; -fx-border-width: 2;");
                selectedPlayerRow = row;
            }
            squadListContainer.getChildren().add(row);
        }
        for (IPlayer p : reservePlayersList) {
            HBox row = createPlayerRow(p, getPlayerKitNumber(p), 1.0f, 1.0f); // Yedeklere çarpan etki etmez 
            if (p == selectedPlayerForPlacement) {
                row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5; -fx-border-color: #f0a500; -fx-border-width: 2;");
                selectedPlayerRow = row;
            }
            squadListContainer.getChildren().add(row);
        }
        for (IPlayer p : unselectedPlayers) {
            HBox row = createPlayerRow(p, "-", 1.0f, 1.0f);
            if (p == selectedPlayerForPlacement) {
                row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5; -fx-border-color: #f0a500; -fx-border-width: 2;");
                selectedPlayerRow = row;
            }
            // Sadece maç içindeyken (devre arası/sakatlık) kadro dışı oyuncuları karart ve tıklamayı engelle
            if (isMidMatch) {
                row.setOpacity(0.3);
                row.setDisable(true);
            }
            squadListContainer.getChildren().add(row);
        }
    }

    private HBox createHeaderRow() {
        HBox row = new HBox(8);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #1f4068; -fx-background-radius: 5;");
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().add(createHeaderButton("No", "NO", 40));
        row.getChildren().add(createHeaderButton("Oyuncu Adı", "OYUNCU ADI", 140));
        row.getChildren().add(createHeaderButton("OVR", "OVR", 50));
        
        row.getChildren().add(createHeaderButton("xG", "XG", 50));
        row.getChildren().add(createHeaderButton("xGA", "XGA", 50));

        for (String traitName : traitNames) {
            String shortName = traitName.length() > 5 ? traitName.substring(0, 5) + "." : traitName;
            row.getChildren().add(createHeaderButton(shortName, traitName, 55));
        }

        return row;
    }

    private Button createHeaderButton(String displayText, String sortKey, double width) {
        String finalTxt = displayText.toUpperCase();
        
        if (activeSortColumn.equals(sortKey)) {
            finalTxt += sortAscending ? " \u2191" : " \u2193";
        }

        Button btn = new Button(finalTxt);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e43f5a; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;");
        btn.setPrefWidth(width);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.setOnAction(e -> {
            if (activeSortColumn.equals(sortKey)) {
                sortAscending = !sortAscending; 
            } else {
                activeSortColumn = sortKey;
                sortAscending = sortKey.equals("NO") || sortKey.equals("OYUNCU ADI"); 
            }
            refreshSquadList();
        });

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e43f5a; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));

        return btn;
    }

    
    private HBox createPlayerRow(IPlayer player, String kitNumber, float xgMult, float xgaMult) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: #1a294c; -fx-background-radius: 5; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;");
        row.setAlignment(Pos.CENTER_LEFT);

        row.setOnMouseClicked(e -> handlePlayerRowClick(player, row));

        Label kitLbl = new Label(kitNumber);
        
        if (kitNumber.startsWith("Y")) kitLbl.setTextFill(Color.web("#f0a500"));
        else if (kitNumber.equals("-")) kitLbl.setTextFill(Color.web("#a5a5b0"));
        else kitLbl.setTextFill(Color.web("#e43f5a"));

        kitLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        kitLbl.setPrefWidth(40);
        kitLbl.setAlignment(Pos.CENTER_LEFT);

        String formattedName = formatPlayerName(player.getFullName());
        Label nameLbl = new Label(formattedName);
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nameLbl.setPrefWidth(140);
        nameLbl.setTooltip(new Tooltip(player.getFullName()));

        GameRulesFootball rules = new GameRulesFootball();
        if (player.isInjured()) {
            nameLbl.setText(formattedName + " 🚑");
            nameLbl.setTextFill(Color.web("#d82bbc"));
            row.setOpacity(0.6);
        } else if (isMidMatch && redCardedPlayers.contains(player)) {
            nameLbl.setText(formattedName + " 🟥");
            nameLbl.setTextFill(Color.RED);
            row.setOpacity(0.6);
        } else if (isMidMatch && yellowCardedPlayers.contains(player)) {
            nameLbl.setText(formattedName + " 🟨");
            nameLbl.setTextFill(Color.YELLOW);
        } else if (isMidMatch && !rules.isCanReEnter() && subbedOutPlayers.contains(player)) {
            nameLbl.setText(formattedName + " 🚫");
            nameLbl.setTextFill(Color.GRAY);
            row.setOpacity(0.6);
        } else {
            nameLbl.setTextFill(Color.WHITE);
        }

        int ovr = (int) Math.round(player.calculateOverallRating());
        StackPane ovrBox = createRatingBox(ovr);
        ovrBox.setPrefWidth(50);
        
   
        float displayXG = player.getxG();
        float displayXGA = player.getxGA();
        if (!kitNumber.equals("-") && !kitNumber.startsWith("Y")) {
            displayXG *= xgMult;
            displayXGA *= xgaMult;
        }

        Label xgLbl = new Label(String.format("%.2f", displayXG));
        xgLbl.setTextFill(Color.WHITE);
        xgLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        xgLbl.setPrefWidth(50);
        xgLbl.setAlignment(Pos.CENTER_LEFT);

        Label xgaLbl = new Label(String.format("%.2f", displayXGA));
        xgaLbl.setTextFill(Color.WHITE);
        xgaLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        xgaLbl.setPrefWidth(50);
        xgaLbl.setAlignment(Pos.CENTER_LEFT);
        
        row.getChildren().addAll(kitLbl, nameLbl, ovrBox, xgLbl, xgaLbl);

        for (String traitName : traitNames) {
            Trait t = player.getTrait(traitName);
            int traitValue = (t != null) ? t.getCurrentLevel() : 0;
            
            StackPane traitBox = createRatingBox(traitValue);
            traitBox.setPrefWidth(55); 
            
            row.getChildren().add(traitBox);
        }

        row.setOnMouseEntered(e -> {
            if (selectedPlayerRow != row) {
                row.setStyle("-fx-background-color: #213663; -fx-background-radius: 5;");
            }
        });
        row.setOnMouseExited(e -> {
            if (selectedPlayerRow != row) {
                row.setStyle("-fx-background-color: #1a294c; -fx-background-radius: 5; -fx-border-color: #1f4068; -fx-border-width: 0 0 1 0;");
            }
        });

        return row;
    }

    private String formatPlayerName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        return fullName;
    }

    private StackPane createRatingBox(int rating) {
        StackPane box = new StackPane();
        box.setPrefSize(45, 30); 
        
        String bgColor = "#e43f5a"; 
        if (rating >= 80) {
            bgColor = "#4CAF50"; 
        } else if (rating >= 60) {
            bgColor = "#f0a500"; 
        }

        box.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5;");

        Label ratingLbl = new Label(String.valueOf(rating));
        ratingLbl.setTextFill(Color.WHITE);
        ratingLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        box.getChildren().add(ratingLbl);
        return box;
    }
}