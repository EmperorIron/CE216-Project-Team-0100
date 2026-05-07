package Sport.Volleyball;

import Classes.Game;
import Classes.GameRules;
import Interface.IPlayer;
import Interface.ITeam;
import Interface.ITactic;

import java.util.ArrayList;
import java.util.List;


public class GameVolleyball extends Game {

    private static final double INJURY_CHANCE = 0.5; // Temporarily increased for testing injuries

    private float homeAttack, homeDefense, awayAttack, awayDefense;

    private int homeSetsWon = 0;
    private int awaySetsWon = 0;

    public GameVolleyball(ITeam homeTeam, ITeam awayTeam, GameRules rules,
                          ITactic homeTactic, ITactic awayTactic) {
        super(homeTeam, awayTeam, rules, homeTactic, awayTactic);
    }


    @Override
    protected void preMatchSetup() {
        PositionsVolleyball.resolvePositionCollisions(homeTactic);
        PositionsVolleyball.resolvePositionCollisions(awayTactic);

        recalculateStrengths();
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
                addLogEntry("GOAL! " + homeTeam.getName() + " wins the rally! Score: " + homeSetPoints + "-" + awaySetPoints);
                if (getRandom().nextDouble() < INJURY_CHANCE) {
                    handleInjury(homeTeam, homeTactic, periodNumber, true);
                    recalculateStrengths();
                }
            } else {
                awaySetPoints++;
                addLogEntry("GOAL! " + awayTeam.getName() + " wins the rally! Score: " + homeSetPoints + "-" + awaySetPoints);
                if (getRandom().nextDouble() < INJURY_CHANCE) {
                    handleInjury(awayTeam, awayTactic, periodNumber, false);
                    recalculateStrengths();
                }
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
        }

        homeScore = homeSetsWon;
        awayScore = awaySetsWon;
        addLogEntry("--- SET " + periodNumber + " ENDED ---");
    }



    @Override
    protected void handlePeriodBreak(int periodNumber) {
        addLogEntry("--- SET " + periodNumber + " ENDED. SET SCORE: "
                  + homeSetsWon + " - " + awaySetsWon + " ---");

        if (homeSetsWon >= 3 || awaySetsWon >= 3) return;

        homeSubsLeft = rules.getSubstitutionCount();
        awaySubsLeft = rules.getSubstitutionCount();

        if (homeManager != null) {
            if (homeTeam.isManagerAI()) {
                addLogEntry("Home team (" + homeTeam.getName() + ") makes a set break tactical change...");
                fillMissingPlayers(homeTeam, homeTactic);
            }
            homeManager.handlePeriodBreak(this, homeTactic, periodNumber);
            PositionsVolleyball.resolvePositionCollisions(homeTactic);
        }
        if (awayManager != null) {
            if (awayTeam.isManagerAI()) {
                addLogEntry("Away team (" + awayTeam.getName() + ") makes a set break tactical change...");
                fillMissingPlayers(awayTeam, awayTactic);
            }
            awayManager.handlePeriodBreak(this, awayTactic, periodNumber);
            PositionsVolleyball.resolvePositionCollisions(awayTactic);
        }

        recalculateStrengths();
    }

    private void fillMissingPlayers(ITeam team, ITactic tactic) {
        int required = rules.getFieldPlayerCount();
        List<IPlayer> lineup = tactic.getStartingLineup();
        List<IPlayer> bench = tactic.getSubstitutes();
        
        while (lineup.size() < required && !bench.isEmpty()) {
            IPlayer bestSub = null;
            double bestOvr = -1;
            for (IPlayer p : bench) {
                if (!p.isInjured() && p.calculateOverallRating() > bestOvr) {
                    bestOvr = p.calculateOverallRating();
                    bestSub = p;
                }
            }
            if (bestSub != null) {
                bench.remove(bestSub);
                lineup.add(bestSub);
                if (bestSub instanceof Classes.Player pSub) pSub.setCurrentPositionId(pSub.getPrimaryPositionId());
                addLogEntry("AI Forced Sub (" + team.getName() + "): " + bestSub.getFullName() + " comes in to fill the empty position.");
            } else {
                break; // No healthy subs available
            }
        }
    }



    @Override
    protected void postMatchCleanup() {
        addLogEntry("");
        addLogEntry("--- MATCH ENDED! SET SCORE: " + homeSetsWon + " - " + awaySetsWon + " ---");

        homeTeam.setGoalsScored(homeTeam.getGoalsScored()    + homeSetsWon);
        homeTeam.setGoalsConceded(homeTeam.getGoalsConceded()+ awaySetsWon);
        awayTeam.setGoalsScored(awayTeam.getGoalsScored()    + awaySetsWon);
        awayTeam.setGoalsConceded(awayTeam.getGoalsConceded()+ homeSetsWon);

        if (homeSetsWon > awaySetsWon) {
            homeTeam.setWins(homeTeam.getWins() + 1);
            awayTeam.setLosses(awayTeam.getLosses() + 1);
            if (awaySetsWon >= 2) {
                homeTeam.setPoints(homeTeam.getPoints() + 2);
                awayTeam.setPoints(awayTeam.getPoints() + 1);
                addLogEntry(awayTeam.getName() + " earned 1 point with a 3-2 loss.");
                addLogEntry(homeTeam.getName() + " earned 2 points with a 3-2 win.");
            } else {
                homeTeam.setPoints(homeTeam.getPoints() + rules.getVictoryPoints());
                awayTeam.setPoints(awayTeam.getPoints() + rules.getDefeatPoints());
                addLogEntry(awayTeam.getName() + " earned " + rules.getDefeatPoints() + " points with a " + homeSetsWon + "-" + awaySetsWon + " loss.");
                addLogEntry(homeTeam.getName() + " earned " + rules.getVictoryPoints() + " points with a " + homeSetsWon + "-" + awaySetsWon + " win.");
            }
        } else {
            awayTeam.setWins(awayTeam.getWins() + 1);
            homeTeam.setLosses(homeTeam.getLosses() + 1);
            if (homeSetsWon >= 2) {
                awayTeam.setPoints(awayTeam.getPoints() + 2);
                homeTeam.setPoints(homeTeam.getPoints() + 1);
                addLogEntry(homeTeam.getName() + " earned 1 point with a 3-2 loss.");
                addLogEntry(awayTeam.getName() + " earned 2 points with a 3-2 win.");
            } else {
                awayTeam.setPoints(awayTeam.getPoints() + rules.getVictoryPoints());
                homeTeam.setPoints(homeTeam.getPoints() + rules.getDefeatPoints());
                addLogEntry(homeTeam.getName() + " earned " + rules.getDefeatPoints() + " points with a " + homeSetsWon + "-" + awaySetsWon + " loss.");
                addLogEntry(awayTeam.getName() + " earned " + rules.getVictoryPoints() + " points with a " + awaySetsWon + "-" + homeSetsWon + " win.");
            }
        }
    }

    private void handleInjury(ITeam team, ITactic tactic, int setNumber, boolean isHome) {
        List<IPlayer> onField = tactic.getStartingLineup();
        List<IPlayer> bench = tactic.getSubstitutes();
        if (onField.isEmpty()) return;

        int injuredIndex = getRandom().nextInt(onField.size());
        IPlayer injured = onField.get(injuredIndex);

        if (injured instanceof Classes.Player p) {
            int duration = 1 + getRandom().nextInt(3);
            p.setInjuryDuration(duration);
        }

        int subsLeft = isHome ? homeSubsLeft : awaySubsLeft;

        // AI Substitution Logic
        if (team.isManagerAI() && subsLeft > 0 && !bench.isEmpty()) {
            IPlayer bestSub = null;
            double bestOvr = -1;
            for (IPlayer p : bench) {
                if (!p.isInjured() && p.calculateOverallRating() > bestOvr) {
                    bestOvr = p.calculateOverallRating();
                    bestSub = p;
                }
            }
            if (bestSub != null) {
                onField.remove(injuredIndex);
                bench.remove(bestSub);
                onField.add(bestSub);
                bench.add(injured);
                if (bestSub instanceof Classes.Player pSub && injured instanceof Classes.Player pInj) {
                    pSub.setCurrentPositionId(pInj.getCurrentPositionId());
                }
                if (isHome) homeSubsLeft--; else awaySubsLeft--;
                addLogEntry("SET " + setNumber + " - FORCED SUB (Injury)! (" + team.getName() + ") -> Out: " + injured.getFullName() + " | In: " + bestSub.getFullName());
                PositionsVolleyball.resolvePositionCollisions(tactic);
                return;
            }
        }
        
        // If Human, or AI out of subs/bench (Play through the pain debuff)
        addLogEntry("SET " + setNumber + " - INJURY! (" + team.getName() + ") -> " + injured.getFullName() + " is injured but must play through the pain!");

        PositionsVolleyball.resolvePositionCollisions(tactic);
    }



    public int getHomeSetsWon() { return homeSetsWon; }
    public int getAwaySetsWon() { return awaySetsWon; }

    @Override
    public String getEventType(String log) {
        if (log.startsWith("GOAL!")) return "GOAL";
        if (log.contains("SUB") || log.contains("Sub") || log.contains("Substitution") || log.contains("Out:")) return "SUB";
        if (log.contains("INJURY") || log.contains("INJURED!")) return "INJURY";
        return "INFO";
    }
}
