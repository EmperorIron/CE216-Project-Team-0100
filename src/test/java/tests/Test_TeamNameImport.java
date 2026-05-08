package tests;

import Classes.TeamNameImport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Test_TeamNameImport {

    @BeforeEach
    void setUp() {
        // Reset to default state before each test
        TeamNameImport.setCustomNames(null);
        TeamNameImport.setUseCustomNames(false);
    }

    @Test
    void testCustomNamesFlag() {
        assertFalse(TeamNameImport.isUseCustomNames(), "Custom names should be disabled by default.");
        
        TeamNameImport.setUseCustomNames(true);
        assertTrue(TeamNameImport.isUseCustomNames(), "Custom names should be enabled after toggling the boolean.");
    }

    @Test
    void testSetAndGetCustomNames() {
        List<String> names = Arrays.asList("Galatasaray", "Fenerbahce", "Besiktas");
        TeamNameImport.setCustomNames(names);
        
        assertNotNull(TeamNameImport.getCustomTeamNames(), "Custom team names list should not be null after assignment.");
        assertEquals(3, TeamNameImport.getCustomTeamNames().size(), "List size should perfectly match the imported array size.");
        assertTrue(TeamNameImport.getCustomTeamNames().contains("Galatasaray"), "List should contain the imported custom names.");
        
        TeamNameImport.setCustomNames(null);
        assertTrue(TeamNameImport.getCustomTeamNames().isEmpty(), "Setting to null should return a safe empty list, not null.");
    }
}