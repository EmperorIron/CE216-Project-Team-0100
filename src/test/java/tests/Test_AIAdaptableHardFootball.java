package tests;

import Classes.*;
import Interface.ITactic;
import Sport.Football.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_AIAdaptableHardFootball {
    private GameRulesFootball rules;
    private TeamFootball team;
    private AIAdaptableHardFootball ai;

    @BeforeEach
    void setUp() {
        rules = new GameRulesFootball();
        team = TeamGeneratorFootball.createRandomFootballTeam(rules);
        ai = new AIAdaptableHardFootball(team);
    }

    @Test
    void testGenerateStartingTactic() {
        ITactic tactic = ai.generateStartingTactic();
        assertNotNull(tactic, "AI should successfully generate a starting tactic.");
        assertFalse(tactic.getStartingLineup().isEmpty(), "AI starting lineup should not be empty.");
        assertEquals(rules.getFieldPlayerCount(), tactic.getStartingLineup().size(), "AI should pick exactly the required field players.");
        assertTrue(tactic.getSubstitutes().size() > 0, "AI should pick substitute players.");
    }

    @Test
    void testHandlePeriodBreakWithoutCrashing() {
        ITactic tactic = ai.generateStartingTactic();
        GameFootball fakeGame = new GameFootball(team, team, rules, tactic, tactic);
        
        assertDoesNotThrow(() -> {
            ai.handlePeriodBreak(fakeGame, tactic, 1);
        }, "AI should be able to analyze and adapt tactics mid-game without throwing exceptions.");
    }
}