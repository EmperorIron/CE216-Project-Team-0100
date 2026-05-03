package Sport.Volleyball;

import Classes.HumanManager;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HumanManagerVolleyball extends HumanManager {

    public HumanManagerVolleyball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        TacticVolleyball t = new TacticVolleyball("5-1");
        if (!gui.GUISquadManager.getPlayersOnPitchQueue().isEmpty()) {
            t.setStartingLineup(new ArrayList<>(gui.GUISquadManager.getPlayersOnPitchQueue()));
            t.setSubstitutes(new ArrayList<>(gui.GUISquadManager.getReservePlayersQueue()));
            t.applyTacticStyle(gui.GUISquadManager.getCurrentTacticStyle());
        } else {
            List<IPlayer> squad = team.getPlayers().stream()
                    .filter(p -> !p.isInjured())
                    .sorted((a, b) -> Double.compare(b.calculateOverallRating(), a.calculateOverallRating()))
                    .collect(Collectors.toList());
            t.setStartingLineup(new ArrayList<>(squad.subList(0, Math.min(6, squad.size()))));
            if (squad.size() > 6) {
                t.setSubstitutes(new ArrayList<>(squad.subList(6, squad.size())));
            }
        }
        return t;
    }

    @Override
    protected void applyStyle(ITactic currentTactic) {
        if (currentTactic instanceof TacticVolleyball tv) {
            tv.applyTacticStyle(gui.GUISquadManager.getCurrentTacticStyle());
        }
    }
}
