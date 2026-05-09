package tests;

import Classes.*;
import Sport.Football.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Test_Player {

    private PlayerFootball forward;
    private PlayerFootball goalkeeper;
    private PlayerFootball defender;
    private GameRulesFootball rules;

    @BeforeEach
    void setUp() {
        rules      = new GameRulesFootball();
        forward    = PlayerGeneratorFootball.createFootballPlayerByPosition("Forward", rules);
        goalkeeper = PlayerGeneratorFootball.createFootballPlayerByPosition("Goalkeeper", rules);
        defender   = PlayerGeneratorFootball.createFootballPlayerByPosition("Defender", rules);
    }

    @Test
    void testPlayerPrimaryPositionIsInRoleZone() {
        assertTrue(PositionsFootball.isForwardPosition(forward.getPrimaryPositionId()),
                "Forward's primary position should be in the forward zone.");
        assertTrue(PositionsFootball.isGoalkeeperPosition(goalkeeper.getPrimaryPositionId()),
                "Goalkeeper's primary position should be in the GK zone.");
        assertTrue(PositionsFootball.isDefenderPosition(defender.getPrimaryPositionId()),
                "Defender's primary position should be in the defender zone.");
    }

    @Test
    void testPlayerHasAllTraits() {
        assertNotNull(forward.getTrait("Shooting"));
        assertNotNull(forward.getTrait("Speed"));
        assertNotNull(forward.getTrait("Defense"));
        assertNotNull(forward.getTrait("Passing"));
        assertNotNull(forward.getTrait("Physical"));
        assertNotNull(forward.getTrait("Mental"));
        assertNotNull(forward.getTrait("Goalkeeping"));
    }

    @Test
    void testTraitValuesAreInRange() {
        for (Trait trait : forward.getTraits().values()) {
            assertTrue(trait.getCurrentLevel() >= 0 && trait.getCurrentLevel() <= 100,
                    trait.getName() + " out of range: " + trait.getCurrentLevel());
        }
    }

    @Test
    void testForwardHasHighShootingAndSpeed() {
        int shooting = forward.getTrait("Shooting").getCurrentLevel();
        int gk       = forward.getTrait("Goalkeeping").getCurrentLevel();
        assertTrue(shooting > gk, "Forward shooting should be higher than goalkeeping");
    }

    @Test
    void testGoalkeeperHasHighGKStat() {
        int gk = goalkeeper.getTrait("Goalkeeping").getCurrentLevel();
        assertTrue(gk > 50, "Goalkeeper GK stat should be > 50, was: " + gk);
    }

    @Test
    void testOverallRatingIsPositive() {
        assertTrue(forward.calculateOverallRating() > 0);
        assertTrue(goalkeeper.calculateOverallRating() > 0);
        assertTrue(defender.calculateOverallRating() > 0);
    }

    @Test
    void testInjurySystem() {
        assertFalse(forward.isInjured());
        forward.setInjuryDuration(3);
        assertTrue(forward.isInjured());
        assertEquals(3, forward.getInjuryDuration());

        forward.decrementInjury();
        assertEquals(2, forward.getInjuryDuration());

        forward.decrementInjury();
        forward.decrementInjury();
        assertFalse(forward.isInjured());
    }

    @Test
    void testSetTraitLevel() {
        forward.getTrait("Shooting").setCurrentLevel(95);
        assertEquals(95, forward.getTrait("Shooting").getCurrentLevel());
    }

    @Test
    void testTraitCannotExceedMax() {
        forward.getTrait("Shooting").setCurrentLevel(200);
        assertEquals(100, forward.getTrait("Shooting").getCurrentLevel());
    }

    @Test
    void testTraitCannotGoBelowMin() {
        forward.getTrait("Shooting").setCurrentLevel(-50);
        assertEquals(0, forward.getTrait("Shooting").getCurrentLevel());
    }

    @Test
    void testSetTeamName() {
        forward.setTeamName("Galatasaray");
        assertEquals("Galatasaray", forward.getTeamName());
    }

    @Test
    void testPlayerFullName() {
        assertNotNull(forward.getFullName());
        assertFalse(forward.getFullName().isBlank());
    }

    @Test
    void testPositionProficiencyMapSize() {
        assertEquals(Positions.TOTAL_POSITIONS, forward.getPositionProficiency().size(),
                "Proficiency map should have 100 entries (10x10 grid).");
    }

    @Test
    void testPrimaryPositionProficiencyIsHigh() {
        int primaryProficiency = forward.getProficiencyAt(forward.getPrimaryPositionId());
        assertTrue(primaryProficiency >= 90,
                "Primary position proficiency should be >= 90, was: " + primaryProficiency);
    }

    @Test
    void testPlayerEqualityAndHashCode() {
        assertEquals(forward, forward, "Player should be equal to itself.");
        assertNotEquals(forward, goalkeeper, "Different players should not be equal.");
        assertNotEquals(forward, null, "Player should not be equal to null.");
        assertNotNull(forward.hashCode(), "Player hash code should not be null.");
    }

    @Test
    void testSetJerseyNumber() {
        forward.setJerseyNumber(10);
        assertEquals(10, forward.getJerseyNumber(), "Player jersey number should update correctly.");
    }
    
    @Test
    void testExpectedGoalsAssignment() {
        forward.setxG(1.5f);
        assertEquals(1.5f, forward.getxG(), "Expected Goals (xG) should update correctly.");
    }
}
