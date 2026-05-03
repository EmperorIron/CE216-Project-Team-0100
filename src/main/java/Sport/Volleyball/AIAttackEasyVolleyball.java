package Sport.Volleyball;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AIAttackEasyVolleyball extends AIVolleyball {

    public AIAttackEasyVolleyball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        TacticVolleyball tactic = new TacticVolleyball("6-2");
        tactic.applyTacticStyle(TacticVolleyball.AVAILABLE_STYLES.get(1).name());

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
    protected void makeSubstitutions(IGame game, ITactic currentTactic) {
        // Easy AI does not substitute
    }

    @Override
    protected void adaptTactics(IGame game, ITactic currentTactic) {
        if (currentTactic instanceof TacticVolleyball tv) {
            String styleName = TacticVolleyball.AVAILABLE_STYLES.get(1).name();
            tv.applyTacticStyle(styleName);
            game.addLogEntry("AI Tactic (" + team.getName() + "): Continuing with '" + styleName + "'.");
        }
    }
}
