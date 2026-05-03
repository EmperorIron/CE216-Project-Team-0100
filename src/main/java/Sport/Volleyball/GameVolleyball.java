package Sport.Volleyball;

import Classes.Game;
import Classes.GameRules;
import Interface.IPlayer;
import Interface.ITeam;
import Interface.ITactic;

import java.util.ArrayList;
import java.util.List;


public class GameVolleyball extends Game {

    private float homeAttack, homeDefense, awayAttack, awayDefense;

    private int homeSetsWon = 0;
    private int awaySetsWon = 0;

    public GameVolleyball(ITeam homeTeam, ITeam awayTeam, GameRules rules,
                          ITactic homeTactic, ITactic awayTactic) {
        super(homeTeam, awayTeam, rules, homeTactic, awayTactic);
    }


    @Override
    protected void preMatchSetup() {
        if (homeManager != null) this.homeTactic = homeManager.generateStartingTactic();
        if (awayManager != null) this.awayTactic = awayManager.generateStartingTactic();

        addLogEntry("--- MATCH STARTED: " + homeTeam.getName() + " vs " + awayTeam.getName() + " ---");
        addLogEntry("Home System: " + homeTactic.getFormation()
                  + " | Away System: " + awayTactic.getFormation());

        PositionsVolleyball.resolvePositionCollisions(homeTactic);
        PositionsVolleyball.resolvePositionCollisions(awayTactic);

        recalculateStrengths();

        homeSubsLeft = rules.getSubstitutionCount();
        awaySubsLeft = rules.getSubstitutionCount();
        
        logFormationGrid(homeTeam, homeTactic, "Match Start");
        logFormationGrid(awayTeam, awayTactic, "Match Start");
    }

    private void recalculateStrengths() {
        float homeRatio = Math.max(0.1f, homeTactic.getStartingLineup().size() / 6.0f);
        float awayRatio = Math.max(0.1f, awayTactic.getStartingLineup().size() / 6.0f);

        homeAttack  = homeTeam.getTotalOffensiveRating() * homeTactic.getTotalXGMultiplier()  * homeRatio;
        homeDefense = homeTeam.getTotalDefensiveRating() * homeTactic.getTotalXGAMultiplier() * (1.0f / homeRatio);
        awayAttack  = awayTeam.getTotalOffensiveRating() * awayTactic.getTotalXGMultiplier()  * awayRatio;
        awayDefense = awayTeam.getTotalDefensiveRating() * awayTactic.getTotalXGAMultiplier() * (1.0f / awayRatio);
    }



    @Override
    protected void simulatePeriod(int periodNumber) {
        if (homeSetsWon >= 3 || awaySetsWon >= 3) return;

        boolean isDecidingSet = (periodNumber == 5);
        int targetPoints = isDecidingSet
                ? GameRulesVolleyball.DECIDING_SET_POINTS
                : GameRulesVolleyball.REGULAR_SET_POINTS;

        int homeSetPoints = 0;
        int awaySetPoints = 0;

        addLogEntry("");
        addLogEntry("=== SET " + periodNumber + " STARTING ===");

        while (true) {
            double homeWinRally = homeAttack / (homeAttack + awayDefense);
            if (Double.isNaN(homeWinRally)) homeWinRally = 0.5;

            if (getRandom().nextDouble() < homeWinRally) {
                homeSetPoints++;
            } else {
                awaySetPoints++;
            }

            if (homeSetPoints >= targetPoints && homeSetPoints - awaySetPoints >= GameRulesVolleyball.WIN_BY) {
                homeSetsWon++;
                addLogEntry("SET " + periodNumber + " → " + homeTeam.getName() + " wins! "
                          + homeSetPoints + "-" + awaySetPoints);
                break;
            }
            if (awaySetPoints >= targetPoints && awaySetPoints - homeSetPoints >= GameRulesVolleyball.WIN_BY) {
                awaySetsWon++;
                addLogEntry("SET " + periodNumber + " → " + awayTeam.getName() + " wins! "
                          + homeSetPoints + "-" + awaySetPoints);
                break;
            }

            if (getRandom().nextDouble() < 0.002) {
                if (getRandom().nextBoolean()) {
                    handleInjury(homeTeam, homeTactic, periodNumber);
                } else {
                    handleInjury(awayTeam, awayTactic, periodNumber);
                }
                recalculateStrengths();
            }
        }

        homeScore = homeSetsWon;
        awayScore = awaySetsWon;
    }



    @Override
    protected void handlePeriodBreak(int periodNumber) {
        addLogEntry("--- SET " + periodNumber + " ENDED. SET SCORE: "
                  + homeSetsWon + " - " + awaySetsWon + " ---");

        if (homeSetsWon >= 3 || awaySetsWon >= 3) return;

        homeSubsLeft = rules.getSubstitutionCount();
        awaySubsLeft = rules.getSubstitutionCount();

        if (homeManager != null) {
            logFormationGrid(homeTeam, homeTactic, "Before Set Break Sub");
            if (homeTeam.isManagerAI()) addLogEntry("Home team (" + homeTeam.getName() + ") makes a set break tactical change...");
            homeManager.handlePeriodBreak(this, homeTactic, periodNumber);
            PositionsVolleyball.resolvePositionCollisions(homeTactic);
            logFormationGrid(homeTeam, homeTactic, "After Set Break Sub");
        }
        if (awayManager != null) {
            logFormationGrid(awayTeam, awayTactic, "Before Set Break Sub");
            if (awayTeam.isManagerAI()) addLogEntry("Away team (" + awayTeam.getName() + ") makes a set break tactical change...");
            awayManager.handlePeriodBreak(this, awayTactic, periodNumber);
            PositionsVolleyball.resolvePositionCollisions(awayTactic);
            logFormationGrid(awayTeam, awayTactic, "After Set Break Sub");
        }

        recalculateStrengths();
    }



    @Override
    protected void postMatchCleanup() {
        addLogEntry("");
        addLogEntry("--- MATCH ENDED! SET SCORE: " + homeSetsWon + " - " + awaySetsWon + " ---");

        logFormationGrid(homeTeam, homeTactic, "Match End");
        logFormationGrid(awayTeam, awayTactic, "Match End");

        homeTeam.setGoalsScored(homeTeam.getGoalsScored()    + homeSetsWon);
        homeTeam.setGoalsConceded(homeTeam.getGoalsConceded()+ awaySetsWon);
        awayTeam.setGoalsScored(awayTeam.getGoalsScored()    + awaySetsWon);
        awayTeam.setGoalsConceded(awayTeam.getGoalsConceded()+ homeSetsWon);

        if (homeSetsWon > awaySetsWon) {
            homeTeam.setWins(homeTeam.getWins() + 1);
            homeTeam.setPoints(homeTeam.getPoints() + rules.getVictoryPoints());
            awayTeam.setLosses(awayTeam.getLosses() + 1);
            if (awaySetsWon >= 2) {
                awayTeam.setPoints(awayTeam.getPoints() + 1);
                addLogEntry(awayTeam.getName() + " earned 1 point with a 3-2 loss.");
            } else {
                awayTeam.setPoints(awayTeam.getPoints() + rules.getDefeatPoints());
            }
        } else {
            awayTeam.setWins(awayTeam.getWins() + 1);
            awayTeam.setPoints(awayTeam.getPoints() + rules.getVictoryPoints());
            homeTeam.setLosses(homeTeam.getLosses() + 1);
            if (homeSetsWon >= 2) {
                homeTeam.setPoints(homeTeam.getPoints() + 1);
                addLogEntry(homeTeam.getName() + " earned 1 point with a 3-2 loss.");
            } else {
                homeTeam.setPoints(homeTeam.getPoints() + rules.getDefeatPoints());
            }
        }
    }

    private void logFormationGrid(ITeam team, ITactic tactic, String context) {
        String[][] grid = new String[10][10];
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                grid[y][x] = "";
            }
        }

        for (IPlayer p : tactic.getStartingLineup()) {
            int posId = p.getPrimaryPositionId();
            String label = "?";
            if (p instanceof Classes.Player) {
                posId = ((Classes.Player) p).getCurrentPositionId();
                label = String.valueOf(((Classes.Player) p).getJerseyNumber());
            }
            int x = posId % 10;
            int y = posId / 10;
            if (x >= 0 && x < 10 && y >= 0 && y < 10) {
                if (grid[y][x].isEmpty()) {
                    grid[y][x] = label;
                } else {
                    grid[y][x] += "," + label;
                }
            }
        }

        addLogEntry("");
        addLogEntry(context + " - " + team.getName() + " 10x10 Court Formation:");
        addLogEntry("+----------------------------------------+");
        for (int y = 9; y >= 0; y--) {
            StringBuilder row = new StringBuilder("|");
            for (int x = 0; x < 10; x++) {
                if (!grid[y][x].isEmpty()) {
                    row.append(String.format("%-4s", grid[y][x])); 
                } else {
                    row.append(".   "); 
                }
            }
            row.append("|");
            addLogEntry(row.toString());
        }
        addLogEntry("+----------------------------------------+");
        addLogEntry("");
    }

    private void handleInjury(ITeam team, ITactic tactic, int setNumber) {
        List<IPlayer> onField = tactic.getStartingLineup();
        if (onField.isEmpty()) return;

        int outIndex = getRandom().nextInt(onField.size());
        IPlayer injured = onField.remove(outIndex);

        if (injured instanceof Classes.Player p) {
            int duration = 1 + getRandom().nextInt(3);
            p.setInjuryDuration(duration);
        }

        addLogEntry("SET " + setNumber + " - INJURY! (" + team.getName()
                  + ") → " + injured.getFullName() + " subbed out.");
        PositionsVolleyball.resolvePositionCollisions(tactic);
    }



    public int getHomeSetsWon() { return homeSetsWon; }
    public int getAwaySetsWon() { return awaySetsWon; }

    public String getEventType(String log) {
        if (log.contains("SET") && log.contains("wins")) return "GOAL";   // set win = "goal" in GUI
        if (log.contains("INJURY")) return "INJURY";
        if (log.contains("Sub")) return "SUB";
        return "INFO";
    }
}
