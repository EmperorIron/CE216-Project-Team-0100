package Sport;

import Classes.Game;
import Classes.GameRules;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.List;

public class GameFootball extends Game {

    private float homeOffense, homeDefense, awayOffense, awayDefense;
    private int homeSubsLeft, awaySubsLeft;

    public GameFootball(ITeam homeTeam, ITeam awayTeam, GameRules rules, ITactic homeTactic, ITactic awayTactic) {
        super(homeTeam, awayTeam, rules, homeTactic, awayTactic);
    }

    @Override
    protected void preMatchSetup() {
        addLogEntry("--- MAÇ BAŞLADI: " + homeTeam.getName() + " vs " + awayTeam.getName() + " ---");
        addLogEntry("Ev Sahibi Taktik: " + homeTactic.getFormation() + " | Deplasman Taktik: " + awayTactic.getFormation());

        if (homeTactic.getStartingLineup().isEmpty()) {
            List<IPlayer> players = new ArrayList<>(homeTeam.getPlayers());
            homeTactic.setStartingLineup(new ArrayList<>(players.subList(0, 11)));
            homeTactic.setSubstitutes(new ArrayList<>(players.subList(11, players.size())));
        }
        if (awayTactic.getStartingLineup().isEmpty()) {
            List<IPlayer> players = new ArrayList<>(awayTeam.getPlayers());
            awayTactic.setStartingLineup(new ArrayList<>(players.subList(0, 11)));
            awayTactic.setSubstitutes(new ArrayList<>(players.subList(11, players.size())));
        }

        homeOffense = homeTeam.getTotalOffensiveRating() * homeTactic.getTotalXGMultiplier();
        homeDefense = homeTeam.getTotalDefensiveRating() * homeTactic.getTotalXGAMultiplier();
        awayOffense = awayTeam.getTotalOffensiveRating() * awayTactic.getTotalXGMultiplier();
        awayDefense = awayTeam.getTotalDefensiveRating() * awayTactic.getTotalXGAMultiplier();

        homeSubsLeft = rules.getSubstitutionCount();
        awaySubsLeft = rules.getSubstitutionCount();
    }

    private void performSubstitution(ITeam team, ITactic tactic, int minute, String reason) {
        List<IPlayer> onField = tactic.getStartingLineup();
        List<IPlayer> bench = tactic.getSubstitutes();
        
        if (onField.isEmpty() || bench.isEmpty()) return;

        int outIndex = random.nextInt(onField.size());
        int inIndex = random.nextInt(bench.size());

        IPlayer playerOut = onField.remove(outIndex);
        IPlayer playerIn = bench.remove(inIndex);
        onField.add(playerIn);
        
        addLogEntry(minute + "'. " + reason + " (" + team.getName() + ") -> Çıkan: " + playerOut.getFullName() + " | Giren: " + playerIn.getFullName());
    }

    @Override
    protected void simulatePeriod(int periodNumber) {
        int duration = rules.getPeriodDuration();
        int startMinute = ((periodNumber - 1) * duration) + 1;
        int endMinute = periodNumber * duration;

        for (int minute = startMinute; minute <= endMinute; minute++) {

            if (random.nextDouble() < 0.01) {
                if (random.nextBoolean() && homeSubsLeft > 0) {
                    performSubstitution(homeTeam, homeTactic, minute, "MECBURİ DEĞİŞİKLİK (Sakatlık)");
                    homeSubsLeft--;
                } else if (awaySubsLeft > 0) {
                    performSubstitution(awayTeam, awayTactic, minute, "MECBURİ DEĞİŞİKLİK (Sakatlık)");
                    awaySubsLeft--;
                }
            }

            double homeGoalChance = (homeOffense / (homeOffense + awayDefense)) * 0.025;
            if (random.nextDouble() < homeGoalChance) { 
                homeScore++;
                addLogEntry(minute + "'. GOOOAALLL! " + homeTeam.getName() + " ağları sarsıyor! Skor: " + homeScore + "-" + awayScore);
            }

            double awayGoalChance = (awayOffense / (awayOffense + homeDefense)) * 0.025;
            if (random.nextDouble() < awayGoalChance) {
                awayScore++;
                addLogEntry(minute + "'. GOOOAALLL! " + awayTeam.getName() + " golü buldu! Skor: " + homeScore + "-" + awayScore);
            }
        }
    }

    @Override
    protected void handlePeriodBreak(int periodNumber) {
        if (random.nextDouble() < 0.7 && homeSubsLeft > 0) {
            performSubstitution(homeTeam, homeTactic, (periodNumber * rules.getPeriodDuration()), "DEVRE ARASI TAKTİKSEL HAMLE");
            homeSubsLeft--;
        }
        if (random.nextDouble() < 0.7 && awaySubsLeft > 0) {
            performSubstitution(awayTeam, awayTactic, (periodNumber * rules.getPeriodDuration()), "DEVRE ARASI TAKTİKSEL HAMLE");
            awaySubsLeft--;
        }
        addLogEntry("--- HAKEM " + periodNumber + ". PERİYODU BİTİREN DÜDÜĞÜ ÇALDI. SKOR: " + homeScore + " - " + awayScore + " ---");
    }

    @Override
    protected void postMatchCleanup() {
        addLogEntry("--- MAÇ SONA ERDİ! SKOR: " + homeScore + " - " + awayScore + " ---");
        
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
}