package Sport;

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
                tacticFootball.applyTacticStyle(TacticFootball.ALL_OUT_ATTACK);
                game.addLogEntry("AI Taktik (" + team.getName() + "): Geriye düşüldüğü için 'All Out Attack' taktiğine geçildi.");
            } else if (myScore > oppScore) {
                tacticFootball.applyTacticStyle(TacticFootball.PARK_THE_BUS);
                game.addLogEntry("AI Taktik (" + team.getName() + "): Skoru korumak için 'Park the Bus' savunma taktiğine geçildi.");
            } else {
                tacticFootball.applyTacticStyle(TacticFootball.BALANCED);
            }
        }
    }
}