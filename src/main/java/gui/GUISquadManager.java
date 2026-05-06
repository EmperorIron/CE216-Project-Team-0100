package gui;

import Classes.Positions;
import Interface.IPlayer;
import Interface.ITeam;
import Classes.GameContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GUISquadManager {

    private static GUISquadManager instance;

    public IPlayer[][] pitchPlayers = new IPlayer[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
    public LinkedList<IPlayer> playersOnPitchQueue = new LinkedList<>();
    public LinkedList<IPlayer> reservePlayersQueue = new LinkedList<>();
    public List<IPlayer> subbedOutPlayers = new ArrayList<>();
    public List<IPlayer> redCardedPlayers = new ArrayList<>();
    public List<IPlayer> yellowCardedPlayers = new ArrayList<>();
    public List<IPlayer> injuredInMatchPlayers = new ArrayList<>();
    public String currentTacticStyle = null;
    public ITeam currentTeam = null;
    public boolean isMidMatch = false;
    public Runnable onResumeMatch = null;
    public int subsMadeThisMatch = 0;

    private GUISquadManager() {}

    public static GUISquadManager getInstance() {
        if (instance == null) {
            instance = new GUISquadManager();
        }
        return instance;
    }

    public IPlayer[][] getPitchPlayers() { return pitchPlayers; }
    public LinkedList<IPlayer> getPlayersOnPitchQueue() { return playersOnPitchQueue; }
    public LinkedList<IPlayer> getReservePlayersQueue() { return reservePlayersQueue; }

    public void initSquad(ITeam playerTeam) {
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
            subsMadeThisMatch = 0;
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

    public String getDefaultTacticStyle() {
        Classes.Tactic.TacticStyle s = GameContext.getInstance().getSportFactory().getAvailableTacticStyles().get(0);
        return String.format("%s (xG: %.2f, xGA: %.2f)", s.name(), s.xgMult(), s.xgaMult());
    }

    public String getCurrentTacticStyle() { 
        if (currentTacticStyle == null) return GameContext.getInstance().getSportFactory().getAvailableTacticStyles().get(0).name();
        int idx = currentTacticStyle.indexOf(" (");
        if (idx != -1) {
            return currentTacticStyle.substring(0, idx);
        }
        return currentTacticStyle; 
    }

    public float[] getStyleMultipliers() {
        float styleXgMult = 1.0f;
        float styleXgaMult = 1.0f;
        String cleanStyleName = getCurrentTacticStyle();
        
        for (Classes.Tactic.TacticStyle style : GameContext.getInstance().getSportFactory().getAvailableTacticStyles()) {
            if (style.name().equals(cleanStyleName)) {
                styleXgMult = style.xgMult();
                styleXgaMult = style.xgaMult();
                break;
            }
        }
        return new float[]{styleXgMult, styleXgaMult};
    }

    public String getPlayerKitNumber(IPlayer p) {
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

    public void autoFillSquad(ITeam playerTeam, int maxReservePlayers) {
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

        aiTactic = GameContext.getInstance().getSportFactory().createEasyAI(playerTeam).generateStartingTactic();
        GameContext.getInstance().getSportFactory().setupInitialPitchPositions(aiTactic);
        posInfo = GameContext.getInstance().getSportFactory().createPositions();

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
            if (!p.isInjured() && reservePlayersQueue.size() < maxReservePlayers && !playersOnPitchQueue.contains(p) && !reservePlayersQueue.contains(p)) {
                reservePlayersQueue.add(p);
            }
        }
    }

    public String getFormationValidationMessage() {
        return GameContext.getInstance().getSportFactory().validateFormation(new ArrayList<>(playersOnPitchQueue));
    }

    public void removePlayerFromMatrix(IPlayer player) {
        for (int i = 0; i < Positions.GRID_WIDTH; i++) {
            for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                if (pitchPlayers[i][j] != null && pitchPlayers[i][j].equals(player)) {
                    pitchPlayers[i][j] = null;
                }
            }
        }
    }

    public void removePlayerFromSquad(IPlayer player) {
        if (playersOnPitchQueue.contains(player)) {
            playersOnPitchQueue.remove(player);
            removePlayerFromMatrix(player);
            if (isMidMatch && !subbedOutPlayers.contains(player)) {
                subbedOutPlayers.add(player);
            }
        }
        if (reservePlayersQueue.contains(player)) {
            reservePlayersQueue.remove(player);
        }
    }

    public String placePlayerOnBench(IPlayer player, int maxReservePlayers) {
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

    public String placePlayerOnPitch(IPlayer player, int destX, int destY, int maxFieldPlayers, int maxReservePlayers) {
        Classes.GameRules rules = GameContext.getInstance().getSportFactory().createGameRules();
        boolean canReEnter = rules.isCanReEnter();

        if ((isMidMatch && redCardedPlayers.contains(player)) || (isMidMatch && !canReEnter && subbedOutPlayers.contains(player))) {
            return "Suspended or subbed out players can only stay on the bench.";
        }
        
        if (isMidMatch && player.isInjured() && !playersOnPitchQueue.contains(player)) {
            return "Injured players cannot be subbed into the match!";
        }
        
        boolean wasOnPitch = playersOnPitchQueue.contains(player);
        if (isMidMatch && !wasOnPitch && subsMadeThisMatch >= rules.getSubstitutionCount()) {
            return "No substitutions left! Maximum " + rules.getSubstitutionCount() + " allowed.";
        }

        IPlayer existingPlayer = pitchPlayers[destX][destY];
        List<IPlayer> sameZonePlayers = GameContext.getInstance().getSportFactory().getPlayersInSameZone(pitchPlayers, destX, destY);
        
        if (isMidMatch && !wasOnPitch && playersOnPitchQueue.size() >= maxFieldPlayers && existingPlayer == null && sameZonePlayers.isEmpty()) {
            return "Pitch is full! Remove a player to the bench first, or place the substitute directly over the player you want to replace.";
        }

        int targetPosId = Positions.getPositionId(destX, destY);
        
        for (IPlayer pToRemove : sameZonePlayers) {
            removePlayerFromSquad(pToRemove);
            if (isMidMatch && !subbedOutPlayers.contains(pToRemove)) subbedOutPlayers.add(pToRemove);
            if (pToRemove instanceof Classes.Player) {
                ((Classes.Player) pToRemove).setCurrentPositionId(pToRemove.getPrimaryPositionId());
                pToRemove.setxG(0f);
                pToRemove.setxGA(0f);
            }
            if (!reservePlayersQueue.contains(pToRemove)) reservePlayersQueue.add(pToRemove);
        }

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

        if (isMidMatch && !wasOnPitch) {
            subsMadeThisMatch++;
        }

        if (!isMidMatch && playersOnPitchQueue.size() > maxFieldPlayers) {
            IPlayer oldestPlayer = playersOnPitchQueue.removeFirst();
            removePlayerFromMatrix(oldestPlayer);
            if (oldestPlayer instanceof Classes.Player) {
                ((Classes.Player) oldestPlayer).setCurrentPositionId(oldestPlayer.getPrimaryPositionId());
                oldestPlayer.setxG(0f);
                oldestPlayer.setxGA(0f);
            }
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
            Classes.Positions posInfo = GameContext.getInstance().getSportFactory().createPositions();
            float xgMult = posInfo.getXgMultipliers().getOrDefault(targetPosId, 1.0f);
            float xgaMult = posInfo.getXgaMultipliers().getOrDefault(targetPosId, 1.0f);
            double ovr = player.calculateOverallRating();
            player.setxG((float) ((ovr / 100.0) * xgMult * 2.0));
            player.setxGA((float) ((ovr / 100.0) * xgaMult * 2.0));
        }

        return null;
    }

    public void applyRedCard(IPlayer player) {
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

    public void performAutomaticInjuryRemoval(IPlayer player) {
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

    public void performAutomaticSub(IPlayer pOut, IPlayer pIn) {
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
                            
                            Classes.Positions posInfo = GameContext.getInstance().getSportFactory().createPositions();
                            float xgMult = posInfo.getXgMultipliers().getOrDefault(posId, 1.0f);
                            float xgaMult = posInfo.getXgaMultipliers().getOrDefault(posId, 1.0f);
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

    public void loadTacticData(IPlayer[][] loadedPitch, LinkedList<IPlayer> loadedOnPitch, LinkedList<IPlayer> loadedReserves, ITeam team, String tacticStyle) {
        currentTeam = team;
        if (team == null || team.getPlayers() == null) return;
        
        currentTacticStyle = getDefaultTacticStyle();
        if (tacticStyle != null) {
            String rawName = tacticStyle;
            int parenIdx = tacticStyle.indexOf(" (");
            if (parenIdx != -1) rawName = tacticStyle.substring(0, parenIdx);

            for (Classes.Tactic.TacticStyle s : GameContext.getInstance().getSportFactory().getAvailableTacticStyles()) {
                if (s.name().equals(rawName)) {
                    currentTacticStyle = String.format("%s (xG: %.2f, xGA: %.2f)", s.name(), s.xgMult(), s.xgaMult());
                    break;
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
                            Classes.Positions posInfo = GameContext.getInstance().getSportFactory().createPositions();
                            float xgMult = posInfo.getXgMultipliers().getOrDefault(posId, 1.0f);
                            float xgaMult = posInfo.getXgaMultipliers().getOrDefault(posId, 1.0f);
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

    public void postMatchCleanup() {
        if (reservePlayersQueue != null) {
            reservePlayersQueue.removeIf(IPlayer::isInjured);
        }
        isMidMatch = false;
        subbedOutPlayers.clear();
        redCardedPlayers.clear();
        yellowCardedPlayers.clear();
        injuredInMatchPlayers.clear();
        subsMadeThisMatch = 0;
        onResumeMatch = null;
    }
}