package Sport.Football;
import Classes.*;

import Interface.ITeam;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



public class LeagueFootball extends League {

    public LeagueFootball(String name, String country, int numberOfTeams, GameRulesFootball rules) {
        super(name, country);
        for (int i = 0; i < numberOfTeams; i++) {
            TeamFootball team = TeamGeneratorFootball.createRandomFootballTeam(rules);
            this.addTeam(team);
        }
    }

    @Override
    public List<ITeam> getTeamRanking() {
        List<ITeam> rankedTeams = new ArrayList<>(this.teams);
        rankedTeams.sort(Comparator
                .comparingInt(ITeam::getPoints)
                .thenComparingInt(ITeam::getGoalDifference)
                .thenComparingInt(ITeam::getGoalsScored)
                .reversed()); 
        return rankedTeams;
    }
}
