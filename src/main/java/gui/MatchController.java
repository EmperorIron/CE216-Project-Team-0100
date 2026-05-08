package gui;

import Classes.GameContext;
import Interface.ITeam;
import Interface.IPlayer;
import Sport.Football.GameFootball;
import Sport.Volleyball.GameVolleyball;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Controller class for separating match simulation logic from the visual UI.
 */
public class MatchController {

    private GUIGame view;
    
    private GameFootball footballMatch;
    private GameVolleyball volleyballMatch;
    private boolean isVolleyball;
    private List<String> matchLogs;

    private int minute = 0;
    private int homeScore = 0;
    private int awayScore = 0;
    private int homeSetsWon = 0;
    private int awaySetsWon = 0;
    private int homeTotalRallies = 0;
    private int awayTotalRallies = 0;
    private int currentLogIndex = 0;
    private int currentSetNumber = 0;
    
    private int possH = 50;
    private int homeShots = 0;
    private int awayShots = 0;
    
    private int periodDuration;
    private int totalMatchMinutes;
    private MatchState currentState;
    
    private final StringProperty minuteProperty = new SimpleStringProperty("00:00");
    private final StringProperty scoreProperty = new SimpleStringProperty("0 - 0");
    private final StringProperty homePossessionProperty = new SimpleStringProperty("50%");
    private final StringProperty awayPossessionProperty = new SimpleStringProperty("50%");
    private final StringProperty homeShotsProperty = new SimpleStringProperty("0");
    private final StringProperty awayShotsProperty = new SimpleStringProperty("0");
    private final StringProperty homeXGProperty = new SimpleStringProperty("0.00");
    private final StringProperty awayXGProperty = new SimpleStringProperty("0.00");

    public StringProperty minuteProperty() { return minuteProperty; }
    public StringProperty scoreProperty() { return scoreProperty; }
    public StringProperty homePossessionProperty() { return homePossessionProperty; }
    public StringProperty awayPossessionProperty() { return awayPossessionProperty; }
    public StringProperty homeShotsProperty() { return homeShotsProperty; }
    public StringProperty awayShotsProperty() { return awayShotsProperty; }
    public StringProperty homeXGProperty() { return homeXGProperty; }
    public StringProperty awayXGProperty() { return awayXGProperty; }

    public MatchController(GUIGame view, GameFootball match) {
        this.view = view;
        this.footballMatch = match;
        this.isVolleyball = false;
        
        Classes.GameRules rules = match.getRules();
        this.periodDuration = rules.getPeriodDuration();
        this.totalMatchMinutes = rules.getPeriodCount() * this.periodDuration;
        
        this.currentState = new PlayingState();
        match.play();
        this.matchLogs = match.getGameLog();
    }

    public MatchController(GUIGame view, GameVolleyball match) {
        this.view = view;
        this.volleyballMatch = match;
        this.isVolleyball = true;
        
        scoreProperty.set("0 - 0 (SETS)");
        Classes.GameRules rules = match.getRules();
        this.periodDuration = rules.getPeriodDuration();
        this.totalMatchMinutes = rules.getPeriodCount() * this.periodDuration;
        
        this.currentState = new PlayingState();
        match.play();
        this.matchLogs = match.getGameLog();
    }

    public void initializeFootballStats() {
        while (currentLogIndex < matchLogs.size() && !matchLogs.get(currentLogIndex).matches("^\\d+'\\...*")) {
            view.addEvent("INFO", "", matchLogs.get(currentLogIndex++));
        }
        
        possH = footballMatch.getHomePossession();
        homePossessionProperty.set(possH + "%");
        awayPossessionProperty.set((100 - possH) + "%");
        homeShotsProperty.set("0");
        awayShotsProperty.set("0");
        homeXGProperty.set(String.format(Locale.US, "%.2f", footballMatch.getHomeXG()));
        awayXGProperty.set(String.format(Locale.US, "%.2f", footballMatch.getAwayXG()));
    }

    public void initializeVolleyballStats() {
        while (currentLogIndex < matchLogs.size()) {
            String log = matchLogs.get(currentLogIndex);
            if (log.startsWith("=== SET")) break;
            view.addEvent("INFO", "", log);
            currentLogIndex++;
        }
        minuteProperty.set("00:00");
    }

    public void processTick() {
        currentState.processTick();
    }

    public void applyTacticChanges() {
        currentState.applyTacticChanges();
    }

    private interface MatchState {
        void processTick();
        void applyTacticChanges();
    }

    private class PlayingState implements MatchState {
        @Override
        public void processTick() {
            if (isVolleyball) doVolleyballTick();
            else doFootballTick();
        }
        @Override
        public void applyTacticChanges() {} // Ignore
    }

    private class BreakState implements MatchState {
        @Override
        public void processTick() {} // Ignore ticks while paused
        @Override
        public void applyTacticChanges() {
            executeTacticChanges();
            currentState = new PlayingState(); // Resume playing
        }
    }

    private class EndedState implements MatchState {
        @Override
        public void processTick() {} // Ignore
        @Override
        public void applyTacticChanges() {} // Ignore
    }

    private void doFootballTick() {

        minute++;
        minuteProperty.set(String.format("%02d:00", minute));

        Random rand = new Random();
        if (rand.nextDouble() < footballMatch.getHomeShotChance(possH)) homeShots++;
        if (rand.nextDouble() < footballMatch.getAwayShotChance(possH)) awayShots++;
        
        homePossessionProperty.set(possH + "%");
        awayPossessionProperty.set((100 - possH) + "%");
        homeShotsProperty.set(String.valueOf(homeShots));
        awayShotsProperty.set(String.valueOf(awayShots));
        homeXGProperty.set(String.format(Locale.US, "%.2f", footballMatch.getHomeXG()));
        awayXGProperty.set(String.format(Locale.US, "%.2f", footballMatch.getAwayXG()));

        while (currentLogIndex < matchLogs.size()) {
            String log = matchLogs.get(currentLogIndex);
            if (log.startsWith(minute + "'.")) {
                String type = footballMatch.getEventType(log);
                if (type.equals("GOAL")) {
                    if (log.contains(footballMatch.getHomeTeam().getName())) homeScore++;
                    else awayScore++;
                    scoreProperty.set(homeScore + " - " + awayScore);
                } else if (type.equals("YELLOW")) {
                    processCard(log, "YELLOW");
                } else if (type.equals("RED")) {
                    processCard(log, "RED");
                }
                syncSubAndInjury(log);
                view.addEvent(type, minute + "'", log);
                currentLogIndex++;
            } else if (log.matches("^\\d+'\\..*.")) {
                try {
                    int logMin = Integer.parseInt(log.substring(0, log.indexOf("'.")));
                    if (logMin > minute) break;
                    else { view.addEvent("INFO", minute + "'", log); currentLogIndex++; }
                } catch (Exception ex) { view.addEvent("INFO", minute + "'", log); currentLogIndex++; }
            } else { view.addEvent("INFO", minute + "'", log); currentLogIndex++; }
        }

        if (minute % periodDuration == 0 && minute < totalMatchMinutes) {
            currentState = new BreakState();
            view.showBreakButton("Half Time! Go to Tactics ⚙");
        }

        if (minute >= totalMatchMinutes) {
            endMatchLogic();
        }
    }

    private void doVolleyballTick() {
        int processed = 0;
        while (currentLogIndex < matchLogs.size() && processed < 4) {
            String log = matchLogs.get(currentLogIndex);
            String type = volleyballMatch.getEventType(log);

            if (type.equals("GOAL")) {
                if (log.contains(volleyballMatch.getHomeTeam().getName())) {
                    homeScore++;
                    homeTotalRallies++;
                } else {
                    awayScore++;
                    awayTotalRallies++;
                }
                homeShotsProperty.set(String.valueOf(homeSetsWon));
                awayShotsProperty.set(String.valueOf(awaySetsWon));
                homeXGProperty.set(String.valueOf(homeTotalRallies));
                awayXGProperty.set(String.valueOf(awayTotalRallies));
                minuteProperty.set(String.format("%02d:%02d", homeScore, awayScore));
            }

            if (log.startsWith("=== SET")) {
                try {
                    String setStr = log.replace("=", "").replace("SET", "").replace("STARTING", "").trim();
                    currentSetNumber = Integer.parseInt(setStr);
                    minuteProperty.set(String.format("%02d:%02d", homeScore, awayScore));
                } catch (Exception ignored) {}
            }

            syncSubAndInjury(log);

            view.addEvent(type, "S" + currentSetNumber, log);
            currentLogIndex++;
            processed++;

            if (log.startsWith("--- SET ") && log.contains(" ENDED ---")) {
                if (homeScore > awayScore) homeSetsWon++;
                else if (awayScore > homeScore) awaySetsWon++;
                
                homeShotsProperty.set(String.valueOf(homeSetsWon));
                awayShotsProperty.set(String.valueOf(awaySetsWon));
                homeXGProperty.set(String.valueOf(homeTotalRallies));
                awayXGProperty.set(String.valueOf(awayTotalRallies));
                scoreProperty.set(homeSetsWon + " - " + awaySetsWon + " (SETS)");

                try {
                    String numStr = log.substring(8, log.indexOf(" ENDED")).trim();
                    currentSetNumber = Integer.parseInt(numStr);
                } catch (Exception ignored) {}

                if (homeSetsWon < 3 && awaySetsWon < 3) {
                    currentState = new BreakState();
                    view.showBreakButton("SET " + currentSetNumber + " ENDED  —  Set Break Tactics ⚙");
                } else {
                    scoreProperty.set(homeSetsWon + " - " + awaySetsWon + " (SETS)");
                }
                break;
            }
        }

        if (currentLogIndex >= matchLogs.size()) {
            endMatchLogic();
        }
    }

    private void processCard(String log, String cardType) {
        ITeam playerTeam = GameContext.getInstance().getPlayerTeam();
        if (playerTeam == null) return;
        
        String targetString = cardType.equals("YELLOW") ? "Player: " : "Sent Off: ";
        if (log.contains(playerTeam.getName()) && log.contains(targetString)) {
            String pName = log.substring(log.indexOf(targetString) + targetString.length()).trim();
            for (IPlayer p : playerTeam.getPlayers()) {
                if (p.getFullName().equals(pName)) {
                    if (cardType.equals("YELLOW")) {
                        GUISquadManager.getInstance().yellowCardedPlayers.add(p);
                    } else {
                        GUISquadManager.getInstance().applyRedCard(p);
                    }
                    break;
                }
            }
        }
    }

    private void syncSubAndInjury(String log) {
        ITeam playerTeam = GameContext.getInstance().getPlayerTeam();
        if (playerTeam == null) return;
        if (log.contains("Out: ") && log.contains(" | In: ") && log.contains("(" + playerTeam.getName() + ")")) {
            String outName = log.substring(log.indexOf("Out: ") + 5, log.indexOf(" | In: ")).trim();
            String inName  = log.substring(log.indexOf(" | In: ") + 7).trim();
            IPlayer pOut = null, pIn = null;
            for (IPlayer p : playerTeam.getPlayers()) {
                if (p.getFullName().equals(outName)) pOut = p;
                if (p.getFullName().equals(inName))  pIn  = p;
            }
            if (pOut != null && pIn != null) GUISquadManager.getInstance().performAutomaticSub(pOut, pIn);
        }
    }

    private void endMatchLogic() {
        currentState = new EndedState();
        view.pauseTimeline();
        while (currentLogIndex < matchLogs.size()) {
            view.addEvent("INFO", isVolleyball ? ("S" + currentSetNumber) : (minute + "'"), matchLogs.get(currentLogIndex++));
        }
        view.endMatch();
    }

    private void executeTacticChanges() {
        ITeam playerTeam = GameContext.getInstance().getPlayerTeam();
        
        int minRequired = isVolleyball ? 6 : 7;
        if (GUISquadManager.getInstance().getPlayersOnPitchQueue().size() < minRequired) {
            if (currentLogIndex < matchLogs.size()) {
                matchLogs.subList(currentLogIndex, matchLogs.size()).clear();
            }
            
            if (isVolleyball) {
                volleyballMatch.forfeit(playerTeam);
                homeScore = volleyballMatch.getHomeScore();
                awayScore = volleyballMatch.getAwayScore();
                homeSetsWon = volleyballMatch.getHomeSetsWon();
                awaySetsWon = volleyballMatch.getAwaySetsWon();
                scoreProperty.set(homeSetsWon + " - " + awaySetsWon + " (SETS)");
            } else {
                footballMatch.forfeit(playerTeam);
                homeScore = footballMatch.getHomeScore();
                awayScore = footballMatch.getAwayScore();
                scoreProperty.set(homeScore + " - " + awayScore);
            }
            endMatchLogic();
            return;
        }
        
        if (isVolleyball) {
            homeScore = 0;
            awayScore = 0;
            minuteProperty.set("00:00");
            Interface.ITactic playerTactic = volleyballMatch.getHomeTeam().equals(playerTeam)
                    ? volleyballMatch.getHomeTactic()
                    : volleyballMatch.getAwayTactic();
            if (volleyballMatch.getHomeTeam().equals(playerTeam) && volleyballMatch.getHomeManager() instanceof Classes.HumanManager hm) {
                hm.applyTacticalChanges(playerTactic, 
                    GUISquadManager.getInstance().getPlayersOnPitchQueue(), 
                    GUISquadManager.getInstance().getReservePlayersQueue(), 
                    GUISquadManager.getInstance().getCurrentTacticStyle());
            } else if (volleyballMatch.getAwayTeam().equals(playerTeam) && volleyballMatch.getAwayManager() instanceof Classes.HumanManager hm) {
                hm.applyTacticalChanges(playerTactic, 
                    GUISquadManager.getInstance().getPlayersOnPitchQueue(), 
                    GUISquadManager.getInstance().getReservePlayersQueue(), 
                    GUISquadManager.getInstance().getCurrentTacticStyle());
            }
        } else {
            Interface.ITactic playerTactic = footballMatch.getHomeTeam().equals(playerTeam)
                    ? footballMatch.getHomeTactic()
                    : footballMatch.getAwayTactic();
            if (footballMatch.getHomeTeam().equals(playerTeam) && footballMatch.getHomeManager() instanceof Classes.HumanManager hm) {
                hm.applyTacticalChanges(playerTactic, 
                    GUISquadManager.getInstance().getPlayersOnPitchQueue(), 
                    GUISquadManager.getInstance().getReservePlayersQueue(), 
                    GUISquadManager.getInstance().getCurrentTacticStyle());
            } else if (footballMatch.getAwayTeam().equals(playerTeam) && footballMatch.getAwayManager() instanceof Classes.HumanManager hm) {
                hm.applyTacticalChanges(playerTactic, 
                    GUISquadManager.getInstance().getPlayersOnPitchQueue(), 
                    GUISquadManager.getInstance().getReservePlayersQueue(), 
                    GUISquadManager.getInstance().getCurrentTacticStyle());
            }
        }
    }
}