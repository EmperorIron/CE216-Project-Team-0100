package Sport;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AIDefenseEasyFootball extends AIFootball {

    public AIDefenseEasyFootball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        ITactic tactic = new TacticFootball("1-5-3-2"); // Always use a defensive formation
        
        List<IPlayer> players = new ArrayList<>(getAvailablePlayers());
        players.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());

        if (players.size() >= 11) {
            tactic.setStartingLineup(new ArrayList<>(players.subList(0, 11)));
            tactic.setSubstitutes(new ArrayList<>(players.subList(11, players.size())));
        } else {
            tactic.setStartingLineup(new ArrayList<>(players));
        }
        
        if (tactic instanceof TacticFootball tacticFootball) {
            tacticFootball.applyTacticStyle(TacticFootball.PARK_THE_BUS);
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
        // Always defensive
        if (currentTactic instanceof TacticFootball tacticFootball) {
            tacticFootball.applyTacticStyle(TacticFootball.PARK_THE_BUS);
            game.addLogEntry("AI Taktik (" + team.getName() + "): 'Park the Bus' taktiği ile devam ediyor.");
        }
    }
}