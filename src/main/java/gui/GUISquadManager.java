package gui;

import Classes.Positions;
import Interface.IPlayer;
import Interface.ITeam;
import Sport.Football.GameRulesFootball;
import Sport.Volleyball.GameRulesVolleyball;
import Sport.Football.PositionsFootball;
import Sport.Volleyball.PositionsVolleyball;
import Sport.Football.TacticFootball;
import Sport.Volleyball.TacticVolleyball;
import Sport.Football.AIAdaptableEasyFootball;
import Sport.Volleyball.AIAdaptableEasyVolleyball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GUISquadManager {

    public static IPlayer[][] pitchPlayers = new IPlayer[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
    public static LinkedList<IPlayer> playersOnPitchQueue = new LinkedList<>();
    public static LinkedList<IPlayer> reservePlayersQueue = new LinkedList<>();
    public static List<IPlayer> subbedOutPlayers = new ArrayList<>();
    public static List<IPlayer> redCardedPlayers = new ArrayList<>();
    public static List<IPlayer> yellowCardedPlayers = new ArrayList<>();
    public static List<IPlayer> injuredInMatchPlayers = new ArrayList<>();
    public static String currentTacticStyle = null;
    public static ITeam currentTeam = null;
    public static boolean isMidMatch = false;
    public static Runnable onResumeMatch = null;

    public static IPlayer[][] getPitchPlayers() { return pitchPlayers; }
    public static LinkedList<IPlayer> getPlayersOnPitchQueue() { return playersOnPitchQueue; }
    public static LinkedList<IPlayer> getReservePlayersQueue() { return reservePlayersQueue; }

    public static void initSquad(ITeam playerTeam) {
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
            for (IPlayer p : playerTeam.getPlayers()) {
                if (p.isInjured() || redCardedPlayers.contains(p)) {
                    if (!subbedOutPlayers.contains(p)) subbedOutPlayers.add(p);
                }
            }
        }

        if (currentTacticStyle == null) {
            currentTacticStyle = getDefaultTacticStyle();
        }
    }

    public static String getDefaultTacticStyle() {
        if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
            Classes.Tactic.TacticStyle s = TacticVolleyball.AVAILABLE_STYLES.get(0);
            return String.format("%s (xG: %.2f, xGA: %.2f)", s.name(), s.xgMult(), s.xgaMult());
        }
        Classes.Tactic.TacticStyle s = TacticFootball.AVAILABLE_STYLES.get(0);
        return String.format("%s (xG: %.2f, xGA: %.2f)", s.name(), s.xgMult(), s.xgaMult());
    }

    public static String getCurrentTacticStyle() { 
        if (currentTacticStyle == null) return "VOLLEYBALL".equals(GUIMain.activeSport) ? TacticVolleyball.AVAILABLE_STYLES.get(0).name() : TacticFootball.AVAILABLE_STYLES.get(0).name();
        int idx = currentTacticStyle.indexOf(" (");
        if (idx != -1) {
            return currentTacticStyle.substring(0, idx);
        }
        return currentTacticStyle; 
    }

    public static float[] getStyleMultipliers() {
        float styleXgMult = 1.0f;
        float styleXgaMult = 1.0f;
        String cleanStyleName = getCurrentTacticStyle();
        
        if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
            for (Classes.Tactic.TacticStyle style : TacticVolleyball.AVAILABLE_STYLES) {
                if (style.name().equals(cleanStyleName)) {
                    styleXgMult = style.xgMult();
                    styleXgaMult = style.xgaMult();
                    break;
                }
            }
        } else {
            for (Classes.Tactic.TacticStyle style : TacticFootball.AVAILABLE_STYLES) {
                if (style.name().equals(cleanStyleName)) {
                    styleXgMult = style.xgMult();
                    styleXgaMult = style.xgaMult();
                    break;
                }
            }
        }
        return new float[]{styleXgMult, styleXgaMult};
    }

    public static String getPlayerKitNumber(IPlayer p) {
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

    public static void autoFillSquad(ITeam playerTeam, int maxReservePlayers) {
        // Clear current matrix
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
                }
            }
        }
        playersOnPitchQueue.clear();
        reservePlayersQueue.clear();

        Interface.ITactic aiTactic;
        Classes.Positions posInfo;

        if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
            AIAdaptableEasyVolleyball aiVb = new AIAdaptableEasyVolleyball(playerTeam);
            aiTactic = aiVb.generateStartingTactic();
            int[] vbSpots = {82, 85, 88, 22, 25, 28}; // Centers of LF, MF, RF, LB, MB, RB
            int idx = 0;
            for (IPlayer p : aiTactic.getStartingLineup()) {
                if (p instanceof Classes.Player) {
                    if (idx < 6) {
                        ((Classes.Player) p).setCurrentPositionId(vbSpots[idx]);
                        idx++;
                    } else {
                        ((Classes.Player) p).setCurrentPositionId(p.getPrimaryPositionId());
                    }
                }
            }
            posInfo = new PositionsVolleyball();
        } else {
            AIAdaptableEasyFootball ai = new AIAdaptableEasyFootball(playerTeam);
            aiTactic = ai.generateStartingTactic();
            for (IPlayer p : aiTactic.getStartingLineup()) {
                if (p instanceof Classes.Player) {
                    ((Classes.Player) p).setCurrentPositionId(p.getPrimaryPositionId());
                }
            }
            PositionsFootball.resolvePositionCollisions(aiTactic);
            posInfo = new PositionsFootball();
        }

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
            if (!playersOnPitchQueue.contains(p)) playersOnPitchQueue.add(p);
            
            if (p instanceof Classes.Player) {
                float xgMult = posInfo.getXgMultipliers().getOrDefault(posId, 1.0f);
                float xgaMult = posInfo.getXgaMultipliers().getOrDefault(posId, 1.0f);
                double ovr = p.calculateOverallRating();
                p.setxG((float) ((ovr / 100.0) * xgMult * 2.0)); 
                p.setxGA((float) ((ovr / 100.0) * xgaMult * 2.0));
            }
        }

        for (IPlayer p : aiTactic.getSubstitutes()) {
            if (reservePlayersQueue.size() < maxReservePlayers && !reservePlayersQueue.contains(p)) {
                reservePlayersQueue.add(p);
            }
        }
        
        for (IPlayer p : playerTeam.getPlayers()) {
            if (reservePlayersQueue.size() < maxReservePlayers && !playersOnPitchQueue.contains(p) && !reservePlayersQueue.contains(p)) {
                reservePlayersQueue.add(p);
            }
        }
    }

    public static String getFormationValidationMessage() {
        if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
            int lf = 0, mf = 0, rf = 0, lb = 0, mb = 0, rb = 0;
            for (Interface.IPlayer p : playersOnPitchQueue) {
                if (p instanceof Classes.Player) {
                    int posId = ((Classes.Player) p).getCurrentPositionId();
                    int x = Classes.Positions.getX(posId);
                    int y = Classes.Positions.getY(posId);
                    if (x >= 1 && x <= 3 && y >= 7 && y <= 9) lf++;
                    else if (x >= 4 && x <= 6 && y >= 7 && y <= 9) mf++;
                    else if (x >= 7 && x <= 9 && y >= 7 && y <= 9) rf++;
                    else if (x >= 1 && x <= 3 && y >= 1 && y <= 3) lb++;
                    else if (x >= 4 && x <= 6 && y >= 1 && y <= 3) mb++;
                    else if (x >= 7 && x <= 9 && y >= 1 && y <= 3) rb++;
                }
            }
            if (lf != 1 || mf != 1 || rf != 1 || lb != 1 || mb != 1 || rb != 1) {
                return "Volleyball requires exactly 1 player in each of the 6 court zones (Left/Mid/Right Front and Back).";
            }
        } else {
            int gkCount = 0;
            for (Interface.IPlayer p : playersOnPitchQueue) {
                if (p instanceof Classes.Player) {
                    int posId = ((Classes.Player) p).getCurrentPositionId();
                    if (PositionsFootball.isGoalkeeperPosition(posId)) {
                        gkCount++;
                    }
                }
            }
            if (gkCount != 1) {
                return "Football requires exactly 1 Goalkeeper on the pitch.";
            }
        }
        return null;
    }

    public static void removePlayerFromMatrix(IPlayer player) {
        for (int i = 0; i < Positions.GRID_WIDTH; i++) {
            for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                if (pitchPlayers[i][j] != null && pitchPlayers[i][j].equals(player)) {
                    pitchPlayers[i][j] = null;
                }
            }
        }
    }

    public static void removePlayerFromSquad(IPlayer player) {
        if (playersOnPitchQueue.contains(player)) {
            playersOnPitchQueue.remove(player);
            removePlayerFromMatrix(player);
        }
        if (reservePlayersQueue.contains(player)) {
            reservePlayersQueue.remove(player);
        }
    }

    public static String placePlayerOnBench(IPlayer player, int maxReservePlayers) {
        if (player.isInjured() && !playersOnPitchQueue.contains(player)) {
            return player.getFullName() + " cannot be placed on the bench due to injury.";
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

        return null;
    }

    public static String placePlayerOnPitch(IPlayer player, int destX, int destY, int maxFieldPlayers, int maxReservePlayers) {
        Classes.GameRules rules = "VOLLEYBALL".equals(GUIMain.activeSport) ? new Sport.Volleyball.GameRulesVolleyball() : new Sport.Football.GameRulesFootball();
        boolean canReEnter = rules.isCanReEnter();

        if (player.isInjured() || (isMidMatch && redCardedPlayers.contains(player)) || (isMidMatch && !canReEnter && subbedOutPlayers.contains(player))) {
            return "Injured, suspended or subbed out players can only stay on the bench.";
        }

        int targetPosId = Positions.getPositionId(destX, destY);
        
        if (!("VOLLEYBALL".equals(GUIMain.activeSport)) && PositionsFootball.isGoalkeeperPosition(targetPosId)) {
            for (int i = 0; i < Positions.GRID_WIDTH; i++) {
                for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                    if (i == destX && j == destY) continue;
                    IPlayer existingGK = pitchPlayers[i][j];
                    if (existingGK != null && PositionsFootball.isGoalkeeperPosition(Positions.getPositionId(i, j))) {
                        removePlayerFromSquad(existingGK);
                    }
                }
            }
        }

        if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
            int zoneMinX = -1, zoneMaxX = -1, zoneMinY = -1, zoneMaxY = -1;
            if (destX >= 1 && destX <= 3 && destY >= 7 && destY <= 9) { zoneMinX = 1; zoneMaxX = 3; zoneMinY = 7; zoneMaxY = 9; }
            else if (destX >= 4 && destX <= 6 && destY >= 7 && destY <= 9) { zoneMinX = 4; zoneMaxX = 6; zoneMinY = 7; zoneMaxY = 9; }
            else if (destX >= 7 && destX <= 9 && destY >= 7 && destY <= 9) { zoneMinX = 7; zoneMaxX = 9; zoneMinY = 7; zoneMaxY = 9; }
            else if (destX >= 1 && destX <= 3 && destY >= 1 && destY <= 3) { zoneMinX = 1; zoneMaxX = 3; zoneMinY = 1; zoneMaxY = 3; }
            else if (destX >= 4 && destX <= 6 && destY >= 1 && destY <= 3) { zoneMinX = 4; zoneMaxX = 6; zoneMinY = 1; zoneMaxY = 3; }
            else if (destX >= 7 && destX <= 9 && destY >= 1 && destY <= 3) { zoneMinX = 7; zoneMaxX = 9; zoneMinY = 1; zoneMaxY = 3; }

            if (zoneMinX != -1) {
                for (int i = zoneMinX; i <= zoneMaxX; i++) {
                    for (int j = zoneMinY; j <= zoneMaxY; j++) {
                        if (i == destX && j == destY) continue;
                        IPlayer existingInZone = pitchPlayers[i][j];
                        if (existingInZone != null) {
                            removePlayerFromSquad(existingInZone);
                            if (isMidMatch && !subbedOutPlayers.contains(existingInZone)) subbedOutPlayers.add(existingInZone);
                            if (existingInZone instanceof Classes.Player) {
                                ((Classes.Player) existingInZone).setCurrentPositionId(existingInZone.getPrimaryPositionId());
                                existingInZone.setxG(0f);
                                existingInZone.setxGA(0f);
                            }
                            if (!reservePlayersQueue.contains(existingInZone)) reservePlayersQueue.add(existingInZone);
                        }
                    }
                }
            }
        }

        IPlayer existingPlayer = pitchPlayers[destX][destY];
        if (existingPlayer != null && !existingPlayer.equals(player)) {
            removePlayerFromSquad(existingPlayer);
            if (isMidMatch && !subbedOutPlayers.contains(existingPlayer)) subbedOutPlayers.add(existingPlayer);
            if (existingPlayer instanceof Classes.Player) {
                ((Classes.Player) existingPlayer).setCurrentPositionId(existingPlayer.getPrimaryPositionId());
                existingPlayer.setxG(0f);
                existingPlayer.setxGA(0f);
            }
            if (!reservePlayersQueue.contains(existingPlayer)) reservePlayersQueue.add(existingPlayer);
        }

        if (reservePlayersQueue.contains(player)) reservePlayersQueue.remove(player);
        if (playersOnPitchQueue.contains(player)) {
            playersOnPitchQueue.remove(player);
            removePlayerFromMatrix(player);
        }

        playersOnPitchQueue.add(player);

        if (!"VOLLEYBALL".equals(GUIMain.activeSport) && playersOnPitchQueue.size() > maxFieldPlayers) {
            IPlayer oldestPlayer = playersOnPitchQueue.removeFirst();
            removePlayerFromMatrix(oldestPlayer);
            if (oldestPlayer instanceof Classes.Player) {
                ((Classes.Player) oldestPlayer).setCurrentPositionId(oldestPlayer.getPrimaryPositionId());
                oldestPlayer.setxG(0f);
                oldestPlayer.setxGA(0f);
            }
            if (isMidMatch && !subbedOutPlayers.contains(oldestPlayer)) subbedOutPlayers.add(oldestPlayer);
            if (!reservePlayersQueue.contains(oldestPlayer)) reservePlayersQueue.add(oldestPlayer);
        }

        pitchPlayers[destX][destY] = player;
        
        while (reservePlayersQueue.size() > maxReservePlayers) {
            IPlayer dropped = reservePlayersQueue.removeFirst();
            if (dropped instanceof Classes.Player) {
                ((Classes.Player) dropped).setCurrentPositionId(dropped.getPrimaryPositionId());
                dropped.setxG(0f);
                dropped.setxGA(0f);
            }
        }
        
        if (player instanceof Classes.Player) {
            ((Classes.Player) player).setCurrentPositionId(targetPosId);
            Classes.Positions posInfo = "VOLLEYBALL".equals(GUIMain.activeSport) ? new Sport.Volleyball.PositionsVolleyball() : new Sport.Football.PositionsFootball();
            float xgMult = posInfo.getXgMultipliers().getOrDefault(targetPosId, 1.0f);
            float xgaMult = posInfo.getXgaMultipliers().getOrDefault(targetPosId, 1.0f);
            double ovr = player.calculateOverallRating();
            player.setxG((float) ((ovr / 100.0) * xgMult * 2.0));
            player.setxGA((float) ((ovr / 100.0) * xgaMult * 2.0));
        }

        return null;
    }

    public static void applyRedCard(IPlayer player) {
        if (!redCardedPlayers.contains(player)) {
            redCardedPlayers.add(player);
        }
        if (!subbedOutPlayers.contains(player)) {
            subbedOutPlayers.add(player); 
        }
        playersOnPitchQueue.remove(player);
        if (!reservePlayersQueue.contains(player)) {
            reservePlayersQueue.add(player); 
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
            subbedOutPlayers.add(player); 
        }
        playersOnPitchQueue.remove(player);
        if (!reservePlayersQueue.contains(player)) {
            reservePlayersQueue.add(player); 
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
            playersOnPitchQueue.remove(pOut);
            reservePlayersQueue.remove(pIn);
            
            if (!subbedOutPlayers.contains(pOut)) {
                subbedOutPlayers.add(pOut); 
            }
            
            playersOnPitchQueue.add(pIn);
            reservePlayersQueue.add(pOut); 
            
            for (int i = 0; i < Positions.GRID_WIDTH; i++) {
                for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                    if (pitchPlayers[i][j] != null && pitchPlayers[i][j].equals(pOut)) {
                        pitchPlayers[i][j] = pIn;
                        if (pIn instanceof Classes.Player) {
                            int posId = Classes.Positions.getPositionId(i, j);
                            ((Classes.Player) pIn).setCurrentPositionId(posId);
                            
                            Classes.Positions posFootball = "VOLLEYBALL".equals(GUIMain.activeSport) ? new Sport.Volleyball.PositionsVolleyball() : new Sport.Football.PositionsFootball();
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
        
        currentTacticStyle = getDefaultTacticStyle();
        if (tacticStyle != null) {
            String rawName = tacticStyle;
            int parenIdx = tacticStyle.indexOf(" (");
            if (parenIdx != -1) rawName = tacticStyle.substring(0, parenIdx);

            if ("VOLLEYBALL".equals(GUIMain.activeSport)) {
                for (Classes.Tactic.TacticStyle s : TacticVolleyball.AVAILABLE_STYLES) {
                    if (s.name().equals(rawName)) {
                        currentTacticStyle = String.format("%s (xG: %.2f, xGA: %.2f)", s.name(), s.xgMult(), s.xgaMult());
                        break;
                    }
                }
            } else {
                for (Classes.Tactic.TacticStyle s : TacticFootball.AVAILABLE_STYLES) {
                    if (s.name().equals(rawName)) {
                        currentTacticStyle = String.format("%s (xG: %.2f, xGA: %.2f)", s.name(), s.xgMult(), s.xgaMult());
                        break;
                    }
                }
            }
        }
        
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
                            Classes.Positions posFootball = "VOLLEYBALL".equals(GUIMain.activeSport) ? new Sport.Volleyball.PositionsVolleyball() : new Sport.Football.PositionsFootball();
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
        if (reservePlayersQueue != null) {
            reservePlayersQueue.removeIf(IPlayer::isInjured);
        }
        isMidMatch = false;
        subbedOutPlayers.clear();
        redCardedPlayers.clear();
        yellowCardedPlayers.clear();
        injuredInMatchPlayers.clear();
        onResumeMatch = null;
    }
}