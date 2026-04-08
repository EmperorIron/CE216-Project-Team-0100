package Sport;
import Classes.*;


import Sport.TeamFootball;

public class TeamGeneratorFootball extends TeamGenerator {


    public static TeamFootball createRandomFootballTeam(GameRules rules) {

        String[] identity = generateTeamIdentity();

        TeamFootball team = new TeamFootball(identity[0], identity[1], "Global Football League");

        int totalSquadSize = rules.getPlayerCount() + rules.getReservePlayerCount();

        int numGoalkeepers = Math.max(2, (int) Math.round(totalSquadSize * 0.10));
        int numForwards = (int) Math.round(totalSquadSize * 0.20);
        int numMidfielders = (int) Math.round(totalSquadSize * 0.35);
        int numDefenders = totalSquadSize - numGoalkeepers - numForwards - numMidfielders;

        for (int i = 0; i < numGoalkeepers; i++) team.addPlayer(PlayerGeneratorFootball.createFootballPlayerByPosition("Goalkeeper", rules));
        for (int i = 0; i < numDefenders; i++) team.addPlayer(PlayerGeneratorFootball.createFootballPlayerByPosition("Defender", rules));
        for (int i = 0; i < numMidfielders; i++) team.addPlayer(PlayerGeneratorFootball.createFootballPlayerByPosition("Midfielder", rules));
        for (int i = 0; i < numForwards; i++) team.addPlayer(PlayerGeneratorFootball.createFootballPlayerByPosition("Forward", rules));
        
        team.addCoach(CoachGenerator.createFootballCoachByStyle("Offensive", rules));
        team.addCoach(CoachGenerator.createFootballCoachByStyle("Defensive", rules));
        team.addCoach(CoachGenerator.createFootballCoachByStyle("Youth-focused", rules));

        return team;
    }
}