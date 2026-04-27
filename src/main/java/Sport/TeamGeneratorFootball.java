package Sport;
import Classes.*;


import Sport.TeamFootball;

public class TeamGeneratorFootball extends TeamGenerator {


    public static TeamFootball createRandomFootballTeam(GameRules rules) {

        String[] identity = generateTeamIdentity();

        TeamFootball team = new TeamFootball(identity[0], identity[1], "Global Football League");
        team.setEmblemPath(identity[2]);

        int totalSquadSize = rules.getPlayerCount() + rules.getReservePlayerCount();

        int numGoalkeepers = Math.max(2, (int) Math.round(totalSquadSize * 0.10));
        int numForwards = (int) Math.round(totalSquadSize * 0.20);
        int numMidfielders = (int) Math.round(totalSquadSize * 0.35);
        int numDefenders = totalSquadSize - numGoalkeepers - numForwards - numMidfielders;

        int jerseyNumber = 1;

        for (int i = 0; i < numGoalkeepers; i++) {
            PlayerFootball p = PlayerGeneratorFootball.createFootballPlayerByPosition("Goalkeeper", rules);
            p.setJerseyNumber(jerseyNumber++);
            team.addPlayer(p);
        }
        for (int i = 0; i < numDefenders; i++) {
            PlayerFootball p = PlayerGeneratorFootball.createFootballPlayerByPosition("Defender", rules);
            p.setJerseyNumber(jerseyNumber++);
            team.addPlayer(p);
        }
        for (int i = 0; i < numMidfielders; i++) {
            PlayerFootball p = PlayerGeneratorFootball.createFootballPlayerByPosition("Midfielder", rules);
            p.setJerseyNumber(jerseyNumber++);
            team.addPlayer(p);
        }
        for (int i = 0; i < numForwards; i++) {
            PlayerFootball p = PlayerGeneratorFootball.createFootballPlayerByPosition("Forward", rules);
            p.setJerseyNumber(jerseyNumber++);
            team.addPlayer(p);
        }

        team.addCoach(CoachGeneratorFootball.createFootballCoachByStyle("Offensive", rules));
        team.addCoach(CoachGeneratorFootball.createFootballCoachByStyle("Defensive", rules));
        team.addCoach(CoachGeneratorFootball.createFootballCoachByStyle("Youth-focused", rules));

        return team;
    }
}