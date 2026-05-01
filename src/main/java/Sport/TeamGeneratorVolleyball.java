package Sport;

import Classes.TeamGenerator;


public class TeamGeneratorVolleyball extends TeamGenerator {

    public static TeamVolleyball createRandomVolleyballTeam(GameRulesVolleyball rules) {
        String[] identity = generateTeamIdentity();

        TeamVolleyball team = new TeamVolleyball(identity[0], identity[1], "Global Volleyball League");
        team.setEmblemPath(identity[2]);

        int totalSquad = rules.getPlayerCount() + rules.getReservePlayerCount(); // 14

        String[] coreRoles = {
            "OUTSIDE_HITTER", "OUTSIDE_HITTER",
            "MIDDLE_BLOCKER", "MIDDLE_BLOCKER",
            "OPPOSITE",
            "SETTER",
            "LIBERO"
        };

        int jerseyNumber = 1;

        for (String role : coreRoles) {
            PlayerVolleyball p = PlayerGeneratorVolleyball.createVolleyballPlayerByRole(role, rules);
            p.setJerseyNumber(jerseyNumber++);
            team.addPlayer(p);
        }

        int remaining = totalSquad - coreRoles.length;
        for (int i = 0; i < remaining; i++) {
            PlayerVolleyball p = PlayerGeneratorVolleyball.createRandomVolleyballPlayer(rules);
            p.setJerseyNumber(jerseyNumber++);
            team.addPlayer(p);
        }

        team.addCoach(CoachGeneratorVolleyball.createVolleyballCoachByStyle("Attack",       rules));
        team.addCoach(CoachGeneratorVolleyball.createVolleyballCoachByStyle("Defense",      rules));
        team.addCoach(CoachGeneratorVolleyball.createVolleyballCoachByStyle("Serve-focused", rules));

        return team;
    }
}
