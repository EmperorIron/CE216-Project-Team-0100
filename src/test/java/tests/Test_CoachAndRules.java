package tests;

import Classes.*;
import Sport.Football.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_CoachAndRules {

    private CoachFootball coach;
    private GameRulesFootball rules;

    @BeforeEach
    void setUp() {
        rules = new GameRulesFootball();
        coach = CoachGeneratorFootball.createRandomFootballCoach(rules);
    }


    @Test
    void testCoachHasName() {
        assertNotNull(coach.getName());
        assertFalse(coach.getName().isBlank());
    }

    @Test
    void testCoachHasPreferredFormation() {
        assertNotNull(coach.getPreferredFormation());
        assertFalse(coach.getPreferredFormation().isBlank());
    }

    @Test
    void testCoachTraitsInRange() {
        for (var trait : coach.getTraits().values()) {
            assertTrue(trait.getCurrentLevel() >= 0 && trait.getCurrentLevel() <= 100,
                    trait.getName() + " out of range: " + trait.getCurrentLevel());
        }
    }

    @Test
    void testCoachFullName() {
        String fullName = coach.getFullName();
        assertNotNull(fullName);
        assertTrue(fullName.contains(" "), "Full name should contain a space");
    }


    @Test
    void testFootballHasTwoHalves() {
        assertEquals(2, rules.getPeriodCount());
    }

    @Test
    void testFootballPeriodIs45Minutes() {
        assertEquals(45, rules.getPeriodDuration());
    }

    @Test
    void testFootballHas11FieldPlayers() {
        assertEquals(11, rules.getFieldPlayerCount());
    }

    @Test
    void testFootballVictoryPoints() {
        assertEquals(3, rules.getVictoryPoints());
    }

    @Test
    void testFootballDrawPoints() {
        assertEquals(1, rules.getDrawPoints());
    }

    @Test
    void testFootballDefeatPoints() {
        assertEquals(0, rules.getDefeatPoints());
    }

    @Test
    void testFootballSubstitutionCount() {
        assertEquals(5, rules.getSubstitutionCount());
    }

    @Test
    void testFootballCanReEnter() {
        assertTrue(rules.isCanReEnter(), "Football rules should allow substituted players to re-enter during the half-time break.");
    }

    @Test
    void testFootballScoreProbabilities() {
        assertNotNull(rules.getScoreProbabilities());
        assertTrue(rules.getScoreProbabilities().containsKey(1));
        assertEquals(1.0, rules.getScoreProbabilities().get(1));
    }

    @Test
    void testFootballWeekCount() {
        assertEquals(38, rules.getWeekCount());
    }

    @Test
    void testFootballMatchBetweenTeams() {
        assertEquals(2, rules.getMatchbetweenTeams());
    }

    @Test
    void testFootballTrainingScheduleFormat() {
        String schedule = rules.getTrainingormatch();
        assertNotNull(schedule);
        assertEquals(7, schedule.length(), "Training schedule should be 7 characters.");
        assertTrue(schedule.matches("[01]{7}"), "Schedule should only contain '0' and '1'.");
    }
}
