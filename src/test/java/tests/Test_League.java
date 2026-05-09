package tests;

import Interface.ITeam;
import Sport.Football.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Test_League {

    private LeagueFootball league;
    private GameRulesFootball rules;

    @BeforeEach
    void setUp() {
        rules  = new GameRulesFootball();
        league = new LeagueFootball("Süper Lig", "Turkey", 4, rules);
    }

    @Test
    void testLeagueHasCorrectName() {
        assertEquals("Süper Lig", league.getName());
    }

    @Test
    void testLeagueHasCorrectCountry() {
        assertEquals("Turkey", league.getCountry());
    }

    @Test
    void testLeagueHasCorrectTeamCount() {
        assertEquals(4, league.getTeamRanking().size());
    }

    @Test
    void testAllTeamsHavePlayers() {
        for (ITeam team : league.getTeamRanking()) {
            assertFalse(team.getPlayers().isEmpty(),
                    team.getName() + " has no players");
        }
    }

    @Test
    void testRankingByPoints() {
        List<ITeam> teams = league.getTeamRanking();
        teams.get(0).setPoints(9);
        teams.get(1).setPoints(6);
        teams.get(2).setPoints(3);
        teams.get(3).setPoints(0);

        List<ITeam> ranked = league.getTeamRanking();
        assertTrue(ranked.get(0).getPoints() >= ranked.get(1).getPoints());
        assertTrue(ranked.get(1).getPoints() >= ranked.get(2).getPoints());
        assertTrue(ranked.get(2).getPoints() >= ranked.get(3).getPoints());
    }

    @Test
    void testRankingByGoalDifferenceWhenPointsEqual() {
        List<ITeam> teams = league.getTeamRanking();
        teams.get(0).setPoints(6);
        teams.get(0).setGoalsScored(8);
        teams.get(0).setGoalsConceded(2);

        teams.get(1).setPoints(6);
        teams.get(1).setGoalsScored(4);
        teams.get(1).setGoalsConceded(3);

        List<ITeam> ranked = league.getTeamRanking();
        assertTrue(ranked.get(0).getGoalDifference() >= ranked.get(1).getGoalDifference());
    }

    @Test
    void testAddTeam() {
        int before = league.getTeamRanking().size();
        league.addTeam(new TeamFootball("Test FC", "Turkey", "Süper Lig"));
        assertEquals(before + 1, league.getTeamRanking().size());
    }

    @Test
    void testAddDuplicateTeamIgnored() {
        ITeam existing = league.getTeamRanking().get(0);
        int before = league.getTeamRanking().size();
        league.addTeam(existing);
        assertEquals(before, league.getTeamRanking().size());
    }

    @Test
    void testAllTeamsHaveCoaches() {
        for (ITeam team : league.getTeamRanking()) {
            assertFalse(team.getCoaches().isEmpty(),
                    team.getName() + " has no coaches");
        }
    }

    @Test
    void testFixtureGeneration() {
        CalendarFootball calendar = new CalendarFootball(rules);
        calendar.generateFixtures(league.getTeamRanking());
        assertFalse(calendar.getSchedule().isEmpty(), "Schedule should not be empty after fixture generation.");
    }

    @Test
    void testFixtureWeekCount() {
        CalendarFootball calendar = new CalendarFootball(rules);
        List<ITeam> teams = league.getTeamRanking();
        calendar.generateFixtures(teams);
        // For 4 teams, each playing twice: (4-1) * 2 = 6 weeks
        int expectedWeeks = (teams.size() - 1) * rules.getMatchbetweenTeams();
        assertEquals(expectedWeeks, calendar.getSchedule().size(),
                "Schedule should have " + expectedWeeks + " weeks.");
    }
}
