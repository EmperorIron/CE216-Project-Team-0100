package Sport;

import Classes.AI;
import Interface.IPlayer;
import Interface.ITeam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AIFootball extends AI {

    public AIFootball(ITeam team) {
        super(team);
    }

    protected String getCoachPreferredFormation() {
        if (!team.getCoaches().isEmpty()) {
            var headCoach = team.getCoaches().get(0);
            if (headCoach instanceof CoachFootball coachFootball) {
                return coachFootball.getPreferredFormation();
            }
        }
        return "1-4-4-2"; // Default fallback
    }

    protected List<IPlayer> getAvailablePlayers() {
        return team.getPlayers().stream().filter(p -> !p.isInjured()).collect(Collectors.toList());
    }
}
