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
        if (!pendingStarters.isEmpty()) {
            t.setStartingLineup(new ArrayList<>(pendingStarters));
            t.setSubstitutes(new ArrayList<>(pendingSubstitutes));
            t.applyTacticStyle(pendingStyle);
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
    protected void applyStyle(ITactic currentTactic, String style) {
        if (currentTactic instanceof TacticVolleyball tv) {
            tv.applyTacticStyle(style != null && !style.isEmpty() ? style : "Serve and Receive");
        }
    }
}
