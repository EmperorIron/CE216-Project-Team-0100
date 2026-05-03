package Sport.Football;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AIAdaptableEasyFootball extends AIFootball {

    public AIAdaptableEasyFootball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        String formation = getCoachPreferredFormation();
        ITactic tactic = new TacticFootball(formation);
        
        List<IPlayer> players = new ArrayList<>(getAvailablePlayers());
        players.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());

        if (players.size() >= 11) {
            tactic.setStartingLineup(new ArrayList<>(players.subList(0, 11)));
            tactic.setSubstitutes(new ArrayList<>(players.subList(11, players.size())));
        } else {
            tactic.setStartingLineup(new ArrayList<>(players));
        }
        
        return tactic;
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        adaptTactics(game, currentTactic);
    }

    @Override
    protected void makeSubstitutions(IGame game, ITactic currentTactic) {
        // Easy AI does not make tactical substitutions.
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
}