package tests;

import Classes.*;
import Interface.ICoach;
import Interface.IPlayer;
import Sport.Football.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_Team {

    private TeamFootball team;
    private GameRulesFootball rules;

    @BeforeEach
    void setUp() {
        rules = new GameRulesFootball();
        team  = TeamGeneratorFootball.createRandomFootballTeam(rules);
    }

    @Test
    void testTeamHasName() {
        assertNotNull(team.getName());
        assertFalse(team.getName().isBlank());
    }

    @Test
    void testTeamHasCorrectSquadSize() {
        int expected = rules.getPlayerCount() + rules.getReservePlayerCount();
        assertEquals(expected, team.getPlayers().size(),
                "Squad size should be " + expected + " but was " + team.getPlayers().size());
    }

    @Test
    void testTeamHasThreeCoaches() {
        assertEquals(3, team.getCoaches().size());
    }

    @Test
    void testTeamHasGoalkeeper() {
        boolean hasGK = team.getPlayers().stream()
                .anyMatch(p -> PositionsFootball.isGoalkeeperPosition(p.getPrimaryPositionId()));
        assertTrue(hasGK, "Team should have at least one goalkeeper");
    }

    @Test
    void testOffensiveRatingIsPositive() {
        assertTrue(team.getTotalOffensiveRating() > 0);
    }

    @Test
    void testDefensiveRatingIsPositive() {
        assertTrue(team.getTotalDefensiveRating() > 0);
    }

    @Test
    void testInitialStatsAreZero() {
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getWins());
        assertEquals(0, team.getDraws());
        assertEquals(0, team.getLosses());
        assertEquals(0, team.getGoalsScored());
        assertEquals(0, team.getGoalsConceded());
    }

    @Test
    void testGoalDifference() {
        team.setGoalsScored(5);
        team.setGoalsConceded(3);
        assertEquals(2, team.getGoalDifference());
    }

    @Test
    void testSetPoints() {
        team.setPoints(9);
        assertEquals(9, team.getPoints());
    }

    @Test
    void testAddPlayer() {
        int before = team.getPlayers().size();
        team.addPlayer(PlayerGeneratorFootball.createFootballPlayerByPosition("Forward", rules));
        assertEquals(before + 1, team.getPlayers().size());
    }

    @Test
    void testCoachHasTraits() {
        for (ICoach coach : team.getCoaches()) {
            assertNotNull(coach.getTrait("Offensive Coaching"));
            assertNotNull(coach.getTrait("Defensive Coaching"));
            assertNotNull(coach.getTrait("Youth Development"));
        }
    }

    @Test
    void testManagerAIDefault() {
        assertTrue(team.isManagerAI());
        team.setManagerAI(false);
        assertFalse(team.isManagerAI());
    }

    @Test
    void testAllPlayersHaveProficiencyMap() {
        for (IPlayer player : team.getPlayers()) {
            assertNotNull(player.getPositionProficiency(),
                    player.getFullName() + " has null proficiency map.");
            assertEquals(Positions.TOTAL_POSITIONS, player.getPositionProficiency().size(),
                    player.getFullName() + " proficiency map should have 100 entries.");
        }
    }

    @Test
    void testTeamEqualityAndHashCode() {
        assertEquals(team, team, "Team should be equal to itself.");
        assertNotEquals(team, null, "Team should not be equal to null.");
        assertNotNull(team.hashCode(), "Team hash code should not be null.");
    }

    @Test
    void testSetEmblemPath() {
        String newPath = "images/custom_logo.png";
        team.setEmblemPath(newPath);
        assertEquals(newPath, team.getEmblemPath(), "Team emblem path should update correctly.");
    }
}
