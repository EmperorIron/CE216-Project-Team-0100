package tests;

import Classes.Positions;
import Interface.IPlayer;
import Interface.ITeam;
import Sport.Football.CalendarFootball;
import Sport.Football.GameRulesFootball;
import Sport.Football.LeagueFootball;
import io.SaveGame;
import io.SaveManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class Test_SaveManager {

    private SaveGame saveData;
    private final String TEST_FILENAME = "test_save_unit_test";
    private final String SAVE_PATH = SaveManager.getSaveDirectory() + TEST_FILENAME + ".json";

    @BeforeEach
    void setUp() {
        // 1. Setup mock domain objects
        GameRulesFootball rules = new GameRulesFootball();
        LeagueFootball league = new LeagueFootball("Test League", "Turkey", 4, rules);
        CalendarFootball calendar = new CalendarFootball(rules);
        ITeam playerTeam = league.getTeamRanking().get(0);
        
        IPlayer[][] pitchPlayers = new IPlayer[Positions.GRID_WIDTH][Positions.GRID_HEIGHT];
        LinkedList<IPlayer> onPitch = new LinkedList<>();
        LinkedList<IPlayer> reserve = new LinkedList<>();
        
        // 2. Initialize the SaveGame object with mock data
        saveData = new SaveGame("test_save_title", league, calendar, playerTeam, 
                                pitchPlayers, onPitch, reserve, "Attacking (xG: 1.20, xGA: 1.10)");
    }

    @AfterEach
    void tearDown() {
        // Clean up the temporary save file after each test so it doesn't pollute the user's directory
        File saveFile = new File(SAVE_PATH);
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }

    @Test
    void testSaveAndLoadGame() {
        // --- TEST SAVING ---
        boolean saveSuccess = SaveManager.saveGame(saveData, TEST_FILENAME);
        assertTrue(saveSuccess, "SaveManager should return true when saving successfully.");
        
        File saveFile = new File(SAVE_PATH);
        assertTrue(saveFile.exists(), "The save file should be created on disk.");

        // --- TEST LOADING ---
        SaveGame loadedData = SaveManager.loadGame(SAVE_PATH);
        assertNotNull(loadedData, "SaveManager should load the game without returning null.");
        
        // --- VALIDATE DATA INTEGRITY ---
        assertNotNull(loadedData.getCurrentLeague(), "Loaded league should not be null.");
        assertEquals("Test League", loadedData.getCurrentLeague().getName(), "Loaded league name should match the original.");
        
        assertNotNull(loadedData.getPlayerTeam(), "Loaded player team should not be null.");
        assertEquals(saveData.getPlayerTeam().getName(), loadedData.getPlayerTeam().getName(), "Loaded team name should match the original.");
        
        assertEquals("Attacking (xG: 1.20, xGA: 1.10)", loadedData.getTacticStyle(), "Loaded tactic style should match the original.");
    }
    
    @Test
    void testLoadNonExistentGame() {
        SaveGame loadedData = SaveManager.loadGame(SaveManager.getSaveDirectory() + "this_file_does_not_exist_at_all.json");
        assertNull(loadedData, "Loading a non-existent file should gracefully return null instead of crashing.");
    }
}