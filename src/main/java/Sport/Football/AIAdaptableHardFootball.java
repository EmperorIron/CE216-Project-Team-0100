package Sport.Football;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.*;
import java.util.stream.Collectors;

public class AIAdaptableHardFootball extends AIFootball {

    public AIAdaptableHardFootball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        List<String> candidateFormations = Arrays.asList(getCoachPreferredFormation());

        return findBestTactic(candidateFormations, getAvailablePlayers());
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        adaptTactics(game, currentTactic);
        makeSubstitutions(game, currentTactic);
    }

    @Override
    protected void makeSubstitutions(IGame game, ITactic currentTactic) {
        boolean isHome = game.getHomeTeam().equals(this.team);
        int subsLeft = isHome ? game.getHomeSubsLeft() : game.getAwaySubsLeft();
        if (subsLeft <= 0) return;

        int myScore = isHome ? game.getHomeScore() : game.getAwayScore();
        int oppScore = isHome ? game.getAwayScore() : game.getHomeScore();
        
        List<IPlayer> substitutes = new ArrayList<>(currentTactic.getSubstitutes());
        if (substitutes.isEmpty()) return;

        if (myScore < oppScore) { // Losing, need to attack
            IPlayer playerOut = findWorstPerformer(currentTactic.getStartingLineup(), "DEFENDER");
            if (playerOut == null) playerOut = findWorstPerformer(currentTactic.getStartingLineup(), "MIDFIELDER");
            IPlayer playerIn = findBestAvailable(substitutes, "FORWARD");
            if (playerOut != null && playerIn != null) {
                performSubstitution(currentTactic, playerOut, playerIn, game, "AI seeking a goal");
                if (isHome) game.setHomeSubsLeft(subsLeft - 1); else game.setAwaySubsLeft(subsLeft - 1);
            }
        } else if (myScore > oppScore) { // Winning, need to defend
            IPlayer playerOut = findWorstPerformer(currentTactic.getStartingLineup(), "FORWARD");
            IPlayer playerIn = findBestAvailable(substitutes, "DEFENDER");
            if (playerOut != null && playerIn != null) {
                performSubstitution(currentTactic, playerOut, playerIn, game, "AI protecting the lead");
                if (isHome) game.setHomeSubsLeft(subsLeft - 1); else game.setAwaySubsLeft(subsLeft - 1);
            }
        }
    }

    private void performSubstitution(ITactic tactic, IPlayer out, IPlayer in, IGame game, String reason) {
        tactic.getStartingLineup().remove(out);
        tactic.getSubstitutes().remove(in);
        tactic.getStartingLineup().add(in);
        tactic.getSubstitutes().add(out);
        game.addLogEntry("AI Tactic (" + team.getName() + "): " + reason + ". In: " + in.getFullName() + ", Out: " + out.getFullName());
    }

    private IPlayer findWorstPerformer(List<IPlayer> players, String role) {
        return players.stream()
                .filter(p -> getRole(p).equals(role))
                .min(Comparator.comparingDouble(IPlayer::calculateOverallRating))
                .orElse(null);
    }

    private IPlayer findBestAvailable(List<IPlayer> players, String role) {
        return players.stream()
                .filter(p -> getRole(p).equals(role) && !p.isInjured())
                .max(Comparator.comparingDouble(IPlayer::calculateOverallRating))
                .orElse(null);
    }

    @Override
    protected void adaptTactics(IGame game, ITactic currentTactic) {
        boolean isHome = game.getHomeTeam().equals(this.team);
        int myScore = isHome ? game.getHomeScore() : game.getAwayScore();
        int oppScore = isHome ? game.getAwayScore() : game.getHomeScore();

        if (currentTactic instanceof TacticFootball tacticFootball) {
            if (myScore < oppScore) {
                String styleName = TacticFootball.AVAILABLE_STYLES.get(1).name();
                tacticFootball.applyTacticStyle(styleName);
                game.addLogEntry("AI Tactic (" + team.getName() + "): Switched to '" + styleName + "' tactic due to falling behind.");
            } else if (myScore > oppScore) {
                String styleName = TacticFootball.AVAILABLE_STYLES.get(2).name();
                tacticFootball.applyTacticStyle(styleName);
                game.addLogEntry("AI Tactic (" + team.getName() + "): Switched to '" + styleName + "' defensive tactic to protect the lead.");
            } else {
                tacticFootball.applyTacticStyle(TacticFootball.AVAILABLE_STYLES.get(0).name());
            }
        }
    }

    @Override
    protected double evaluateTactic(ITactic candidateTactic) {
        return candidateTactic.getTotalXGMultiplier() - candidateTactic.getTotalXGAMultiplier(); // Balanced difference
    }
}