package Sport;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AIAdaptableEasyVolleyball extends AIVolleyball {

    public AIAdaptableEasyVolleyball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        TacticVolleyball tactic = new TacticVolleyball(getCoachPreferredFormation());
        tactic.applyTacticStyle(TacticVolleyball.SERVE_AND_RECEIVE);

        List<IPlayer> players = new ArrayList<>(getAvailablePlayers());
        players.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());

        if (players.size() >= 6) {
            tactic.setStartingLineup(new ArrayList<>(players.subList(0, 6)));
            tactic.setSubstitutes(new ArrayList<>(players.subList(6, players.size())));
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
    protected void makeSubstitutions(IGame game, ITactic currentTactic) { }

    @Override
    protected void adaptTactics(IGame game, ITactic currentTactic) {
        if (!(currentTactic instanceof TacticVolleyball tv)) return;
        int homeScore = game.getHomeScore();
        int awayScore = game.getAwayScore();

        boolean isHome = team.equals(game.getHomeTeam());
        boolean winning = isHome ? homeScore > awayScore : awayScore > homeScore;

        if (winning) {
            tv.applyTacticStyle(TacticVolleyball.DEFENSIVE_WALL);
            game.addLogEntry("AI Taktik (" + team.getName() + "): Öndeyiz, 'Defensive Wall'a geçiyoruz.");
        } else {
            tv.applyTacticStyle(TacticVolleyball.POWER_ATTACK);
            game.addLogEntry("AI Taktik (" + team.getName() + "): Gerideyiz, 'Power Attack'a geçiyoruz.");
        }
    }
}
