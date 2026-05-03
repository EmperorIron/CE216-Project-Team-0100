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