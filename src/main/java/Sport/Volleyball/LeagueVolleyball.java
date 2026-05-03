package Sport.Volleyball;

import Interface.ITeam;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class LeagueVolleyball extends Classes.League {

    public LeagueVolleyball(String name, String country, int numberOfTeams, GameRulesVolleyball rules) {
        super(name, country);
        for (int i = 0; i < numberOfTeams; i++) {
            TeamVolleyball team = TeamGeneratorVolleyball.createRandomVolleyballTeam(rules);
            addTeam(team);
        }
    }

    @Override
    public List<ITeam> getTeamRanking() {
        List<ITeam> ranked = new ArrayList<>(teams);
        ranked.sort(Comparator
                .comparingInt(ITeam::getPoints)
                .thenComparingDouble(t -> t.getGoalDifference())   // set difference
                .thenComparingInt(ITeam::getGoalsScored)
                .reversed());
        return ranked;
    }
}
