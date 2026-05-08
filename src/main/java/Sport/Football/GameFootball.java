package Sport.Football;

import Classes.AI;
import Classes.Game;
import Classes.GameRules;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GameFootball extends Game {

    private static final double YELLOW_CARD_CHANCE = 0.009;
    private static final double RED_CARD_CHANCE = 0.0002;
    private static final double INJURY_CHANCE = 0.5; // Temporarily increased for testing injuries
    private static final double BASE_GOAL_CHANCE = 0.035;

    private float homeOffense, homeDefense, awayOffense, awayDefense;    
    private Map<IPlayer, Integer> playerYellowCards = new HashMap<>();

    public GameFootball(ITeam homeTeam, ITeam awayTeam, GameRules rules, ITactic homeTactic, ITactic awayTactic) {
        super(homeTeam, awayTeam, rules, homeTactic, awayTactic);
    }

    @Override
    protected void preMatchSetup() {
        PositionsFootball.resolvePositionCollisions(homeTactic);
        PositionsFootball.resolvePositionCollisions(awayTactic);

        recalculateTeamStrengths();
    }

    private void recalculateTeamStrengths() {
        float homeRatio = Math.max(0.1f, homeTactic.getStartingLineup().size() / 11.0f);
        float awayRatio = Math.max(0.1f, awayTactic.getStartingLineup().size() / 11.0f);
        
        homeOffense = homeTeam.getTotalOffensiveRating() * homeTactic.getTotalXGMultiplier() * homeRatio;
        homeDefense = homeTeam.getTotalDefensiveRating() * homeTactic.getTotalXGAMultiplier() * (1.0f / homeRatio);
        awayOffense = awayTeam.getTotalOffensiveRating() * awayTactic.getTotalXGMultiplier() * awayRatio;
        awayDefense = awayTeam.getTotalDefensiveRating() * awayTactic.getTotalXGAMultiplier() * (1.0f / awayRatio);
    }

    private void performSubstitution(ITeam team, ITactic tactic, int minute, String reason) {
        List<IPlayer> onField = tactic.getStartingLineup();
        List<IPlayer> bench = tactic.getSubstitutes();
        
        if (onField.isEmpty() || bench.isEmpty()) return;

        int outIndex = getRandom().nextInt(onField.size());
        int inIndex = getRandom().nextInt(bench.size());

        IPlayer playerOut = onField.remove(outIndex);
        IPlayer playerIn = bench.remove(inIndex);
        
        if (playerOut instanceof Classes.Player) {
            if (reason.contains("Injury")) {
                double randChance = getRandom().nextDouble();
                int duration = 3;
                if (randChance < 0.05) duration = 1;
                else if (randChance < 0.10) duration = 2;
                ((Classes.Player) playerOut).setInjuryDuration(duration);
            }
        }

        if (playerOut instanceof Classes.Player && playerIn instanceof Classes.Player) {
            ((Classes.Player) playerIn).setCurrentPositionId(((Classes.Player) playerOut).getCurrentPositionId());
        }
        
        onField.add(playerIn);
        bench.add(playerOut);
        
        addLogEntry(minute + "'. " + reason + " (" + team.getName() + ") -> Out: " + playerOut.getFullName() + " | In: " + playerIn.getFullName());
        
        PositionsFootball.resolvePositionCollisions(tactic);
    }

    @Override
    protected void simulatePeriod(int periodNumber) {
        int duration = rules.getPeriodDuration();
        int startMinute = ((periodNumber - 1) * duration) + 1;
        int endMinute = periodNumber * duration;

        for (int minute = startMinute; minute <= endMinute; minute++) {

            if (getRandom().nextDouble() < INJURY_CHANCE) {
                if (getRandom().nextBoolean()) {
                    if (homeTeam.isManagerAI()) {
                        if (homeSubsLeft > 0) {
                            performSubstitution(homeTeam, homeTactic, minute, "FORCED SUB (Injury)");
                            homeSubsLeft--;
                        } else {
                            List<IPlayer> onField = homeTactic.getStartingLineup();
                            if (!onField.isEmpty()) {
                                IPlayer injured = onField.get(getRandom().nextInt(onField.size()));
                                if (injured instanceof Classes.Player) ((Classes.Player) injured).setInjuryDuration(2);
                                addLogEntry(minute + "'. INJURY! (" + homeTeam.getName() + ") -> " + injured.getFullName() + " is injured but must play through the pain!");
                                recalculateTeamStrengths();
                            }
                        }
                    } else {
                        handlePlayerInjury(homeTeam, homeTactic, minute);
                    }
                } else {
                    if (awayTeam.isManagerAI()) {
                        if (awaySubsLeft > 0) {
                            performSubstitution(awayTeam, awayTactic, minute, "FORCED SUB (Injury)");
                            awaySubsLeft--;
                        } else {
                            List<IPlayer> onField = awayTactic.getStartingLineup();
                            if (!onField.isEmpty()) {
                                IPlayer injured = onField.get(getRandom().nextInt(onField.size()));
                                if (injured instanceof Classes.Player) ((Classes.Player) injured).setInjuryDuration(2);
                                addLogEntry(minute + "'. INJURY! (" + awayTeam.getName() + ") -> " + injured.getFullName() + " is injured but must play through the pain!");
                                recalculateTeamStrengths();
                            }
                        }
                    } else {
                        handlePlayerInjury(awayTeam, awayTactic, minute);
                    }
                }
            }

            // Card check for the home team
            if (getRandom().nextDouble() < YELLOW_CARD_CHANCE) {
                handleYellowCard(homeTeam, homeTactic, minute);
            } else if (getRandom().nextDouble() < RED_CARD_CHANCE) {
                handleRedCard(homeTeam, homeTactic, minute);
            }

            // Card check for the away team
            if (getRandom().nextDouble() < YELLOW_CARD_CHANCE) {
                handleYellowCard(awayTeam, awayTactic, minute);
            } else if (getRandom().nextDouble() < RED_CARD_CHANCE) {
                handleRedCard(awayTeam, awayTactic, minute);
            }

            double homeGoalChance = (homeOffense / (homeOffense + awayDefense)) * BASE_GOAL_CHANCE;
            if (getRandom().nextDouble() < homeGoalChance) { 
                homeScore++;
                addLogEntry(minute + "'. GOOOAALLL! " + homeTeam.getName() + " finds the net! Score: " + homeScore + "-" + awayScore);
            }

            double awayGoalChance = (awayOffense / (awayOffense + homeDefense)) * BASE_GOAL_CHANCE;
            if (getRandom().nextDouble() < awayGoalChance) {
                awayScore++;
                addLogEntry(minute + "'. GOOOAALLL! " + awayTeam.getName() + " scores! Score: " + homeScore + "-" + awayScore);
            }
        }
    }

    private void handleRedCard(ITeam team, ITactic tactic, int minute) {
        List<IPlayer> onField = tactic.getStartingLineup();
        if (onField.isEmpty()) return;
        int outIndex = getRandom().nextInt(onField.size());
        IPlayer redCarded = onField.remove(outIndex);
        addLogEntry(minute + "'. RED CARD! (" + team.getName() + ") -> Sent Off: " + redCarded.getFullName());
        PositionsFootball.resolvePositionCollisions(tactic);
        recalculateTeamStrengths();
    }

    private void handleYellowCard(ITeam team, ITactic tactic, int minute) {
     List<IPlayer> onField = tactic.getStartingLineup();
     if (onField.isEmpty()) return;

     int playerIndex = getRandom().nextInt(onField.size());
     IPlayer bookedPlayer = onField.get(playerIndex);

     // Get the player's current yellow card count and increase by 1
     int currentYellows = playerYellowCards.getOrDefault(bookedPlayer, 0) + 1;
     playerYellowCards.put(bookedPlayer, currentYellows);

     addLogEntry(minute + "'. YELLOW CARD! (" + team.getName() + ") -> Player: " + bookedPlayer.getFullName());

     // If reached 2 yellow cards (from GameRulesFootball), send off
     if (currentYellows >= rules.getYellowCardsForRed()) {
         addLogEntry(minute + "'. RED CARD FROM SECOND YELLOW! (" + team.getName() + ") -> Sent Off: " + bookedPlayer.getFullName());
         onField.remove(playerIndex);
         PositionsFootball.resolvePositionCollisions(tactic);
         recalculateTeamStrengths();
     }
 }

    private void handlePlayerInjury(ITeam team, ITactic tactic, int minute) {
        List<IPlayer> onField = tactic.getStartingLineup();
        if (onField.isEmpty()) return;
        
        int injuredIndex = getRandom().nextInt(onField.size());
        IPlayer injured = onField.get(injuredIndex);
        
        if (injured instanceof Classes.Player) {
            double randChance = getRandom().nextDouble();
            int duration = 3;
            if (randChance < 0.05) duration = 1;
            else if (randChance < 0.10) duration = 2;
            ((Classes.Player) injured).setInjuryDuration(duration);
        }
        
        addLogEntry(minute + "'. INJURY! (" + team.getName() + ") -> " + injured.getFullName() + " is injured but must play through the pain!");
        PositionsFootball.resolvePositionCollisions(tactic);
        recalculateTeamStrengths();
    }

    @Override
    protected void handlePeriodBreak(int periodNumber) {
        addLogEntry("--- REF BLOWS WHISTLE FOR END OF PERIOD " + periodNumber + ". SCORE: " + homeScore + " - " + awayScore + " ---");

        if (homeManager != null) {
            if (homeTeam.isManagerAI()) addLogEntry("Home team (" + homeTeam.getName() + ") makes a half-time tactical change...");
            homeManager.handlePeriodBreak(this, homeTactic, periodNumber);
            PositionsFootball.resolvePositionCollisions(homeTactic);
        }

        if (awayManager != null) {
            if (awayTeam.isManagerAI()) addLogEntry("Away team (" + awayTeam.getName() + ") makes a half-time tactical change...");
            awayManager.handlePeriodBreak(this, awayTactic, periodNumber);
            PositionsFootball.resolvePositionCollisions(awayTactic);
        }

        recalculateTeamStrengths();
    }

    public double getHomeXG() {
        double hOff = homeTeam.getTotalOffensiveRating();
        double aDef = awayTeam.getTotalDefensiveRating();
        double hXG = (hOff / (hOff + aDef)) * 2.25;
        return Double.isNaN(hXG) || Double.isInfinite(hXG) ? 1.0 : hXG;
    }

    public double getAwayXG() {
        double aOff = awayTeam.getTotalOffensiveRating();
        double hDef = homeTeam.getTotalDefensiveRating();
        double aXG = (aOff / (aOff + hDef)) * 2.25;
        return Double.isNaN(aXG) || Double.isInfinite(aXG) ? 1.0 : aXG;
    }

    public int getHomePossession() {
        double hOff = homeTeam.getTotalOffensiveRating();
        double aOff = awayTeam.getTotalOffensiveRating();
        int possH = (int) Math.round((hOff / (hOff + aOff)) * 100);
        if (possH < 10) return 50;
        if (possH > 90) return 90;
        return possH;
    }

    public double getHomeShotChance(int homePossession) {
        return (homePossession / 100.0) * 0.15;
    }

    public double getAwayShotChance(int homePossession) {
        return ((100 - homePossession) / 100.0) * 0.15;
    }

    @Override
    public String getEventType(String log) {
        if (log.contains("GOOOAALLL")) return "GOAL";
        if (log.contains("YELLOW CARD!") && !log.contains("RED CARD!")) return "YELLOW";
        if (log.contains("RED CARD!")) return "RED";
        if (log.contains("SUB") || log.contains("Sub")) return "SUB";
        if (log.contains("Injury") || log.contains("FORCED") || log.contains("INJURY!")) return "INJURY";
        return "INFO";
    }

    @Override
    protected void postMatchCleanup() {
        addLogEntry("--- MATCH ENDED! SCORE: " + homeScore + " - " + awayScore + " ---");
        
        homeTeam.setGoalsScored(homeTeam.getGoalsScored() + homeScore);
        homeTeam.setGoalsConceded(homeTeam.getGoalsConceded() + awayScore);
        awayTeam.setGoalsScored(awayTeam.getGoalsScored() + awayScore);
        awayTeam.setGoalsConceded(awayTeam.getGoalsConceded() + homeScore);

        if (homeScore > awayScore) {
            homeTeam.setWins(homeTeam.getWins() + 1);
            homeTeam.setPoints(homeTeam.getPoints() + rules.getVictoryPoints());
            awayTeam.setLosses(awayTeam.getLosses() + 1);
            awayTeam.setPoints(awayTeam.getPoints() + rules.getDefeatPoints());
        } else if (awayScore > homeScore) {
            awayTeam.setWins(awayTeam.getWins() + 1);
            awayTeam.setPoints(awayTeam.getPoints() + rules.getVictoryPoints());
            homeTeam.setLosses(homeTeam.getLosses() + 1);
            homeTeam.setPoints(homeTeam.getPoints() + rules.getDefeatPoints());
        } else {
            homeTeam.setDraws(homeTeam.getDraws() + 1);
            homeTeam.setPoints(homeTeam.getPoints() + rules.getDrawPoints());
            awayTeam.setDraws(awayTeam.getDraws() + 1);
            awayTeam.setPoints(awayTeam.getPoints() + rules.getDrawPoints());
        }
    }

    @Override
    protected void undoPostMatchCleanup() {
        homeTeam.setGoalsScored(homeTeam.getGoalsScored() - homeScore);
        homeTeam.setGoalsConceded(homeTeam.getGoalsConceded() - awayScore);
        awayTeam.setGoalsScored(awayTeam.getGoalsScored() - awayScore);
        awayTeam.setGoalsConceded(awayTeam.getGoalsConceded() - homeScore);

        if (homeScore > awayScore) {
            homeTeam.setWins(homeTeam.getWins() - 1);
            homeTeam.setPoints(homeTeam.getPoints() - rules.getVictoryPoints());
            awayTeam.setLosses(awayTeam.getLosses() - 1);
            awayTeam.setPoints(awayTeam.getPoints() - rules.getDefeatPoints());
        } else if (awayScore > homeScore) {
            awayTeam.setWins(awayTeam.getWins() - 1);
            awayTeam.setPoints(awayTeam.getPoints() - rules.getVictoryPoints());
            homeTeam.setLosses(homeTeam.getLosses() - 1);
            homeTeam.setPoints(homeTeam.getPoints() - rules.getDefeatPoints());
        } else {
            homeTeam.setDraws(homeTeam.getDraws() - 1);
            homeTeam.setPoints(homeTeam.getPoints() - rules.getDrawPoints());
            awayTeam.setDraws(awayTeam.getDraws() - 1);
            awayTeam.setPoints(awayTeam.getPoints() - rules.getDrawPoints());
        }
    }
}