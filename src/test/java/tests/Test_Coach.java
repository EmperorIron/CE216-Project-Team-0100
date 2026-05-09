package tests;

import Classes.*;
import Sport.Football.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_Coach {
    private GameRulesFootball rules;
    private CoachFootball coach1;
    private CoachFootball coach2;

    @BeforeEach
    void setUp() {
        rules = new GameRulesFootball();
        coach1 = CoachGeneratorFootball.createRandomFootballCoach(rules);
        coach2 = CoachGeneratorFootball.createRandomFootballCoach(rules);
    }

    @Test
    void testCoachEquality() {
        assertEquals(coach1, coach1, "Coach should be equal to itself.");
        assertNotEquals(coach1, null, "Coach should not be equal to null.");
    }

    @Test
    void testSetTeam() {
        coach1.setTeam("New Team");
        assertEquals("New Team", coach1.getTeam(), "Coach team assignment should update correctly.");
    }

    @Test
    void testGetAllTraits() {
        assertNotNull(coach1.getAllTraits(), "Coach trait map should not be null.");
        assertFalse(coach1.getAllTraits().isEmpty(), "Coach should be generated with assigned traits.");
        assertEquals(coach1.getTraits(), coach1.getAllTraits(), "getTraits() and getAllTraits() should return the same map.");
    }
}