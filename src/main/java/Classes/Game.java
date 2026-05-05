package Classes;

import Interface.IGame;
import Interface.ITactic;
import Interface.ITeam;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Game implements IGame {
    
    protected final ITeam homeTeam;
    protected final ITeam awayTeam;
    protected final GameRules rules;
    protected ITactic homeTactic;
    protected ITactic awayTactic;

    protected Interface.IManager homeManager;
    protected Interface.IManager awayManager;

    protected int homeScore;
    protected int awayScore;
    protected boolean isCompleted;
    protected final List<String> gameLog;
    protected transient Random random;
    protected int homeSubsLeft;
    protected int awaySubsLeft;

    public Game(ITeam homeTeam, ITeam awayTeam, GameRules rules, ITactic homeTactic, ITactic awayTactic) {
        if (homeTeam == null || awayTeam == null || rules == null || homeTactic == null || awayTactic == null) {
            throw new IllegalArgumentException("Teams, rules, and tactics cannot be null.");
        }
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.rules = rules;
        this.homeTactic = homeTactic;
        this.awayTactic = awayTactic;

        this.homeScore = 0;
        this.awayScore = 0;
        this.isCompleted = false;
        this.gameLog = new ArrayList<>();
    }

    // Setters and Getters 
    @Override public ITeam getHomeTeam() { return homeTeam; }
    @Override public ITeam getAwayTeam() { return awayTeam; }
    @Override public ITactic getHomeTactic() { return homeTactic; }
    @Override public ITactic getAwayTactic() { return awayTactic; }
    @Override public int getHomeScore() { return homeScore; }
    @Override public void setHomeScore(int homeScore) { this.homeScore = homeScore; }
    @Override public int getAwayScore() { return awayScore; }
    @Override public void setAwayScore(int awayScore) { this.awayScore = awayScore; }
    @Override public boolean isCompleted() { return isCompleted; }
    @Override public void setCompleted(boolean completed) { isCompleted = completed; }
    @Override public List<String> getGameLog() { return gameLog; }
    @Override public int getHomeSubsLeft() { return homeSubsLeft; }
    @Override public void setHomeSubsLeft(int homeSubsLeft) { this.homeSubsLeft = homeSubsLeft; }
    @Override public int getAwaySubsLeft() { return awaySubsLeft; }
    @Override public void setAwaySubsLeft(int awaySubsLeft) { this.awaySubsLeft = awaySubsLeft; }

    public GameRules getRules() {
        return rules;
    }

    public Interface.IManager getHomeManager() {
        return homeManager;
    }

    public Interface.IManager getAwayManager() {
        return awayManager;
    }

    public void setHomeManager(Interface.IManager homeManager) {
        this.homeManager = homeManager;
    }

    public void setAwayManager(Interface.IManager awayManager) {
        this.awayManager = awayManager;
    }

    protected Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }
    
    @Override
    public void addLogEntry(String entry) {
        if (entry != null) {
            this.gameLog.add(entry);
        }
    }

    @Override
    public ITeam getWinner() {
        if (!isCompleted) return null;
        if (homeScore > awayScore) return homeTeam;
        if (awayScore > homeScore) return awayTeam;
        return null; // draw
    }

    
    @Override
    public final void play() {
        if (isCompleted) {
            addLogEntry("This match has already been played!");
            return;
        }

        // 1. base match setup
        basePreMatchSetup();
        // 2. sport-specific match setup
        preMatchSetup();

        // 3. circular period simulation
        int periodCount = rules.getPeriodCount();
        for (int currentPeriod = 1; currentPeriod <= periodCount; currentPeriod++) {
            
            // play a period
            simulatePeriod(currentPeriod);
            
            // if its not the last period, handle the break (e.g., halftime)
            if (currentPeriod < periodCount) {
                handlePeriodBreak(currentPeriod);
            }
        }

        // 3. end of match cleanup and finalization
        postMatchCleanup();
        this.isCompleted = true;
    }

    protected void basePreMatchSetup() {
        if (homeManager != null) this.homeTactic = homeManager.generateStartingTactic();
        if (awayManager != null) this.awayTactic = awayManager.generateStartingTactic();

        addLogEntry("--- MATCH STARTED: " + homeTeam.getName() + " vs " + awayTeam.getName() + " ---");
        addLogEntry("Home Tactic: " + homeTactic.getFormation() + " | Away Tactic: " + awayTactic.getFormation());

        homeSubsLeft = rules.getSubstitutionCount();
        awaySubsLeft = rules.getSubstitutionCount();
    }

    public abstract String getEventType(String log);

    // Abstract methods to be implemented by specific game types (e.g., FootballGame, BasketballGame)
    protected abstract void preMatchSetup();
    protected abstract void simulatePeriod(int periodNumber);
    protected abstract void handlePeriodBreak(int periodNumber);
    protected abstract void postMatchCleanup();
}