package Sport;

import Classes.AI;
import Interface.IPlayer;
import Interface.ITeam;

import java.util.List;
import java.util.stream.Collectors;


public abstract class AIVolleyball extends AI {

    public AIVolleyball(ITeam team) {
        super(team);
    }

    protected String getCoachPreferredFormation() {
        if (!team.getCoaches().isEmpty()) {
            var headCoach = team.getCoaches().get(0);
            if (headCoach instanceof CoachVolleyball cv) {
                return cv.getPreferredFormation();
            }
        }
        return "5-1"; // Default volleyball formation
    }

    protected List<IPlayer> getAvailablePlayers() {
        return team.getPlayers().stream()
                .filter(p -> !p.isInjured())
                .collect(Collectors.toList());
    }
}
