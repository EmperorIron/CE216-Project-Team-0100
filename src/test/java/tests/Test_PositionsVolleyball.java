package tests;

import Sport.Volleyball.PositionsVolleyball;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
public class Test_PositionsVolleyball {

    @Test
    void testFrontRowPositions() {
        // Front row boundaries are Y: 7-9. Example valid front row id: 74 (X:4, Y:7)
        int frontRowId = 74;
        assertTrue(PositionsVolleyball.isFrontRowPosition(frontRowId), "Position 74 should be registered as a Front Row position.");
        assertFalse(PositionsVolleyball.isBackRowPosition(frontRowId), "Position 74 should NOT be registered as a Back Row position.");
    }

    @Test
    void testLiberoPositions() {
        // Libero boundaries are middle back. Y: 1-3, X: 4-6. Example valid libero id: 25 (X:5, Y:2)
        int liberoId = 25;
        assertTrue(PositionsVolleyball.isLiberoPosition(liberoId), "Position 25 should be registered as a valid Libero (MB) position.");
        assertTrue(PositionsVolleyball.isBackRowPosition(liberoId), "Libero position 25 must strictly belong to the Back Row.");
        assertFalse(PositionsVolleyball.isFrontRowPosition(liberoId), "Libero position 25 cannot belong to the Front Row.");
    }

    @Test
    void testGetRandomPositionForRole() {
        int posId = PositionsVolleyball.getRandomPositionForRole("LIBERO");
        assertTrue(PositionsVolleyball.isLiberoPosition(posId), "Random generator should return a valid position for LIBERO.");
        
        int posIdOH = PositionsVolleyball.getRandomPositionForRole("OUTSIDE_HITTER");
        assertTrue(PositionsVolleyball.isFrontRowPosition(posIdOH) || PositionsVolleyball.isBackRowPosition(posIdOH), 
                   "Outside Hitter should spawn in a valid LF or LB boundary.");
    }

    @Test
    void testMultiplierGenerators() {
        PositionsVolleyball posRules = new PositionsVolleyball();
        
        assertNotNull(posRules.getXgMultipliers(), "xG multiplier map should initialize successfully.");
        assertNotNull(posRules.getXgaMultipliers(), "xGA multiplier map should initialize successfully.");
        
        // Front row positions should have a higher attacking multiplier than the back row
        float frontAttacking = posRules.getXgMultipliers().get(85); // Y = 8
        float backAttacking = posRules.getXgMultipliers().get(25);  // Y = 2
        
        assertTrue(frontAttacking > backAttacking, "Front row players should have a higher offensive multiplier.");
    }
}