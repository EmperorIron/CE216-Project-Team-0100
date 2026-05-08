package Classes;
import Interface.ITeam;
import java.util.HashMap;
import java.util.Map;
/**
 * Represents the set of rules for a specific game or sport.
 * This is an abstract class, intended to be extended by concrete rule sets
 * for different sports (e.g., FootballRules, BasketballRules).
 */
public abstract class GameRules {
    private int periodCount;  /** The number of periods in a game (e.g., 2 halves in football, 4 quarters in basketball). */
    private int periodDuration; /** The duration of each period in minutes. */
    private int playerCount; /** The total number of players on each team. */
    private int fieldPlayerCount;/** The number of players on the field for each team. */
    private int coachCount; /** The number of coaches allowed per team. */
    private int reservePlayerCount; /** The number of players on the bench (substitutes). */
    private int substitutionCount;  /** The maximum number of substitutions allowed per game. */
    private boolean canReEnter; /** Whether a substituted player can re-enter the game. */
    private int victoryPoints;  /** Points awarded for a victory. */
    private int drawPoints; /** Points awarded for a draw. */
    private int defeatPoints; /** Points awarded for a defeat. */
    private int yellowCardsForRed; /** The number of yellow cards a player can receive before getting a red card. */
    private boolean canReplaceRedCardedPlayer; /** Whether a player who received a red card can be replaced by a substitute. */
    private int teamYellowCardLimit; /** The limit of yellow cards for a team before a penalty is applied. (0 if no limit) */
    private float expToLevelUpBase; /** The base experience required for a trait to level up. */
    private float expGrowthRate; /** The multiplier for experience needed for subsequent levels. */
    private int weekCount; /** The number of weeks in a season, used for scheduling and progression. */
    private int matchbetweenTeams; /** The number of matches played between each pair of teams in the league. */
    private int matchesPerWeek; /** The number of matches played each week in the league. */
    private String trainingormatch; /** A 7-character string representing the week's schedule (Mon-Sun). '0' for training, '1' for a match. E.g., "0000011" for matches on Sat/Sun. */
    /**
     * Defines the point value of different types of scores and their probability.
     * Key: The point value of a score (e.g., 2 for a 2-pointer in basketball).
     * Value: The probability of this score type occurring (e.g., 0.8 for 80%).
     * For a simple sport like football, this could be {1: 1.0}.
     */
    private Map<Integer, Double> scoreProbabilities;

    protected GameRules(int periodCount, int periodDuration, int playerCount, int fieldPlayerCount, int coachCount, int reservePlayerCount,
                        int substitutionCount, boolean canReEnter, Map<Integer, Double> scoreProbabilities,
                        int victoryPoints, int drawPoints, int defeatPoints, int yellowCardsForRed,
                        boolean canReplaceRedCardedPlayer, int teamYellowCardLimit, float expToLevelUpBase, float expGrowthRate,
                        int weekCount, int matchbetweenTeams, int matchesPerWeek, String trainingormatch) {
        this.periodCount = periodCount;
        this.periodDuration = periodDuration;
        this.playerCount = playerCount;
        this.fieldPlayerCount = fieldPlayerCount;
        this.coachCount = coachCount;
        this.reservePlayerCount = reservePlayerCount;
        this.substitutionCount = substitutionCount;
        this.canReEnter = canReEnter;
        this.scoreProbabilities = scoreProbabilities;
        this.victoryPoints = victoryPoints;
        this.drawPoints = drawPoints;
        this.defeatPoints = defeatPoints;
        this.yellowCardsForRed = yellowCardsForRed;
        this.canReplaceRedCardedPlayer = canReplaceRedCardedPlayer;
        this.teamYellowCardLimit = teamYellowCardLimit;
        this.expToLevelUpBase = expToLevelUpBase;
        this.expGrowthRate = expGrowthRate;
        this.weekCount = weekCount;
        this.matchbetweenTeams = matchbetweenTeams;
        this.matchesPerWeek = matchesPerWeek;
        if (trainingormatch == null || !trainingormatch.matches("[01]{7}")) {
            ErrorHandler.logError("trainingormatch must be a 7-character string of '0's and '1's.");
            this.trainingormatch = "0000001";
        } else {
            this.trainingormatch = trainingormatch;
        }
    }
    // Setters and Getters
    public int getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(int periodCount) {
        this.periodCount = periodCount;
    }

    public int getPeriodDuration() {
        return periodDuration;
    }

    public void setPeriodDuration(int periodDuration) {
        this.periodDuration = periodDuration;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getFieldPlayerCount() {
        return fieldPlayerCount;
    }

    public int getCoachCount() {
        return coachCount;
    }

    public int getReservePlayerCount() {
        return reservePlayerCount;
    }

    public void setReservePlayerCount(int reservePlayerCount) {
        this.reservePlayerCount = reservePlayerCount;
    }

    public int getSubstitutionCount() {
        return substitutionCount;
    }

    public void setSubstitutionCount(int substitutionCount) {
        this.substitutionCount = substitutionCount;
    }

    public boolean isCanReEnter() {
        return canReEnter;
    }

    public void setCanReEnter(boolean canReEnter) {
        this.canReEnter = canReEnter;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public int getDrawPoints() {
        return drawPoints;
    }

    public void setDrawPoints(int drawPoints) {
        this.drawPoints = drawPoints;
    }

    public int getDefeatPoints() {
        return defeatPoints;
    }

    public void setDefeatPoints(int defeatPoints) {
        this.defeatPoints = defeatPoints;
    }

    public int getYellowCardsForRed() {
        return yellowCardsForRed;
    }

    public void setYellowCardsForRed(int yellowCardsForRed) {
        this.yellowCardsForRed = yellowCardsForRed;
    }

    public boolean isCanReplaceRedCardedPlayer() {
        return canReplaceRedCardedPlayer;
    }

    public void setCanReplaceRedCardedPlayer(boolean canReplaceRedCardedPlayer) {
        this.canReplaceRedCardedPlayer = canReplaceRedCardedPlayer;
    }

    public int getTeamYellowCardLimit() {
        return teamYellowCardLimit;
    }

    public void setTeamYellowCardLimit(int teamYellowCardLimit) {
        this.teamYellowCardLimit = teamYellowCardLimit;
    }

    public Map<Integer, Double> getScoreProbabilities() {
        return scoreProbabilities;
    }

    public void setScoreProbabilities(Map<Integer, Double> scoreProbabilities) {
        this.scoreProbabilities = scoreProbabilities;
    }

    public float getExpToLevelUpBase() {
        return expToLevelUpBase;
    }

    public void setExpToLevelUpBase(float expToLevelUpBase) {
        this.expToLevelUpBase = expToLevelUpBase;
    }

    public float getExpGrowthRate() {
        return expGrowthRate;
    }

    public void setExpGrowthRate(float expGrowthRate) {
        this.expGrowthRate = expGrowthRate;
    }

    public int getWeekCount() {
        return weekCount;
    }

    public void setWeekCount(int weekCount) {
        this.weekCount = weekCount;
    }

    public int getMatchbetweenTeams() {
        return matchbetweenTeams;
    }

    public void setMatchbetweenTeams(int matchbetweenTeams) {
        this.matchbetweenTeams = matchbetweenTeams;
    }

    public int getMatchesPerWeek() {
        return matchesPerWeek;
    }

    public void setMatchesPerWeek(int matchesPerWeek) {
        this.matchesPerWeek = matchesPerWeek;
    }

    public String getTrainingormatch() {
        return trainingormatch;
    }

    public void setTrainingormatch(String trainingormatch) {
        if (trainingormatch == null || !trainingormatch.matches("[01]{7}")) {
            ErrorHandler.logError("trainingormatch must be a 7-character string of '0's and '1's.");
            this.trainingormatch = "0000001";
        } else {
            this.trainingormatch = trainingormatch;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
} 
