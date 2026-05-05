package Sport.Football;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.*;
import java.util.stream.Collectors;

public class AIDefenseHardFootball extends AIFootball {

    public AIDefenseHardFootball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        ITactic bestTactic = findBestTactic(Arrays.asList(getCoachPreferredFormation()), getAvailablePlayers());
        if (bestTactic instanceof TacticFootball tacticFootball) {
            tacticFootball.applyTacticStyle(TacticFootball.AVAILABLE_STYLES.get(2).name());
        }
        return bestTactic;
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        adaptTactics(game, currentTactic);
        makeSubstitutions(game, currentTactic);
    }

    @Override
    protected void makeSubstitutions(IGame game, ITactic currentTactic) {
        // Hard AI could sub tired defenders for fresh ones.
        boolean isHome = game.getHomeTeam().equals(this.team);
        int subsLeft = isHome ? game.getHomeSubsLeft() : game.getAwaySubsLeft();
        if (subsLeft <= 0) return;

        // Example: Sub a tired defender for a fresh one from the bench
    }

    @Override
    protected void adaptTactics(IGame game, ITactic currentTactic) {
        if (currentTactic instanceof TacticFootball tacticFootball) {
            String styleName = TacticFootball.AVAILABLE_STYLES.get(2).name();
            tacticFootball.applyTacticStyle(styleName);
            game.addLogEntry("AI Tactic (" + team.getName() + "): Continuing with '" + styleName + "' tactic.");
        }
    }

    @Override
    protected double evaluateTactic(ITactic candidateTactic) {
        return -candidateTactic.getTotalXGAMultiplier(); // Negative so that maxing it minimizes xGA
    }
}