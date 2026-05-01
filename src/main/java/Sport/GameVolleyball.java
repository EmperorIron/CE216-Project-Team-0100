package Sport;

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

        addLogEntry("--- MAÇ BAŞLADI: " + homeTeam.getName() + " vs " + awayTeam.getName() + " ---");
        addLogEntry("Ev Sahibi Sistem: " + homeTactic.getFormation()
                  + " | Deplasman Sistem: " + awayTactic.getFormation());

        PositionsVolleyball.resolvePositionCollisions(homeTactic);
        PositionsVolleyball.resolvePositionCollisions(awayTactic);

        recalculateStrengths();

        homeSubsLeft = rules.getSubstitutionCount();
        awaySubsLeft = rules.getSubstitutionCount();
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
        addLogEntry("=== SET " + periodNumber + " BAŞLIYOR ===");

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
                addLogEntry("SET " + periodNumber + " → " + homeTeam.getName() + " kazandı! "
                          + homeSetPoints + "-" + awaySetPoints);
                break;
            }
            if (awaySetPoints >= targetPoints && awaySetPoints - homeSetPoints >= GameRulesVolleyball.WIN_BY) {
                awaySetsWon++;
                addLogEntry("SET " + periodNumber + " → " + awayTeam.getName() + " kazandı! "
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
        addLogEntry("--- SET " + periodNumber + " BİTTİ. SET SKORU: "
                  + homeSetsWon + " - " + awaySetsWon + " ---");

        if (homeSetsWon >= 3 || awaySetsWon >= 3) return;

        homeSubsLeft = rules.getSubstitutionCount();
        awaySubsLeft = rules.getSubstitutionCount();

        if (homeManager != null) {
            homeManager.handlePeriodBreak(this, homeTactic, periodNumber);
            PositionsVolleyball.resolvePositionCollisions(homeTactic);
        }
        if (awayManager != null) {
            awayManager.handlePeriodBreak(this, awayTactic, periodNumber);
            PositionsVolleyball.resolvePositionCollisions(awayTactic);
        }

        recalculateStrengths();
    }



    @Override
    protected void postMatchCleanup() {
        addLogEntry("");
        addLogEntry("--- MAÇ SONA ERDİ! SET SKORU: " + homeSetsWon + " - " + awaySetsWon + " ---");

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
                addLogEntry(awayTeam.getName() + " 3-2 mağlubiyetle 1 puan kazandı.");
            } else {
                awayTeam.setPoints(awayTeam.getPoints() + rules.getDefeatPoints());
            }
        } else {
            awayTeam.setWins(awayTeam.getWins() + 1);
            awayTeam.setPoints(awayTeam.getPoints() + rules.getVictoryPoints());
            homeTeam.setLosses(homeTeam.getLosses() + 1);
            if (homeSetsWon >= 2) {
                homeTeam.setPoints(homeTeam.getPoints() + 1);
                addLogEntry(homeTeam.getName() + " 3-2 mağlubiyetle 1 puan kazandı.");
            } else {
                homeTeam.setPoints(homeTeam.getPoints() + rules.getDefeatPoints());
            }
        }
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

        addLogEntry("SET " + setNumber + " - SAKATLIK! (" + team.getName()
                  + ") → " + injured.getFullName() + " oyundan çıktı.");
        PositionsVolleyball.resolvePositionCollisions(tactic);
    }



    public int getHomeSetsWon() { return homeSetsWon; }
    public int getAwaySetsWon() { return awaySetsWon; }

    public String getEventType(String log) {
        if (log.contains("SET") && log.contains("kazandı")) return "GOAL";   // set win = "goal" in GUI
        if (log.contains("SAKATLIK")) return "INJURY";
        if (log.contains("Değişiklik")) return "SUB";
        return "INFO";
    }
}
