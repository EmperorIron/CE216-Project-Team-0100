package tests;

import Classes.*;
import Interface.ITactic;
import Interface.ITeam;
import Sport.Football.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_GameFootball {
    private GameFootball game;
    private ITeam homeTeam;
    private ITeam awayTeam;
    private GameRulesFootball rules;
    private ITactic homeTactic;
    private ITactic awayTactic;

    @BeforeEach
    void setUp() {
        rules = new GameRulesFootball();
        homeTeam = TeamGeneratorFootball.createRandomFootballTeam(rules);
        awayTeam = TeamGeneratorFootball.createRandomFootballTeam(rules);
        
        AIAdaptableHardFootball homeAI = new AIAdaptableHardFootball(homeTeam);
        AIAdaptableHardFootball awayAI = new AIAdaptableHardFootball(awayTeam);
        
        homeTactic = homeAI.generateStartingTactic();
        awayTactic = awayAI.generateStartingTactic();
        
        game = new GameFootball(homeTeam, awayTeam, rules, homeTactic, awayTactic);
    }

    @Test
    void testInitialScoreIsZero() {
        assertEquals(0, game.getHomeScore(), "Home score should initialize to 0.");
        assertEquals(0, game.getAwayScore(), "Away score should initialize to 0.");
    }

    @Test
    void testGameCompletion() {
        assertFalse(game.isCompleted(), "Game should not be marked as completed before play().");
        game.play();
        assertTrue(game.isCompleted(), "Game should be marked as completed after play() finishes.");
    }

    @Test
    void testGameLogsGenerated() {
        game.play();
        assertFalse(game.getGameLog().isEmpty(), "Game log should not be empty after playing.");
        assertTrue(game.getGameLog().size() > 5, "There should be multiple events recorded in the game log.");
    }

    @Test
    void testEventTypeClassification() {
        assertEquals("GOAL", game.getEventType("12'. GOOOAALLL! Team A finds the net!"));
        assertEquals("YELLOW", game.getEventType("34'. YELLOW CARD! (Team A)"));
        assertEquals("RED", game.getEventType("55'. RED CARD! (Team B)"));
        assertEquals("INJURY", game.getEventType("67'. INJURY! Player X is injured"));
        assertEquals("SUB", game.getEventType("HT. Tactical Sub: Player Y In"));
        assertEquals("INFO", game.getEventType("Kickoff!"));
    }

    @Test
    void testPointsAssignmentAfterMatch() {
        game.play();
        ITeam winner = game.getWinner();
        if (winner != null) {
            ITeam loser = (winner == homeTeam) ? awayTeam : homeTeam;
            assertEquals(rules.getVictoryPoints(), winner.getPoints(), "Winner should receive victory points.");
            assertEquals(rules.getDefeatPoints(), loser.getPoints(), "Loser should receive defeat points.");
        } else {
            assertEquals(rules.getDrawPoints(), homeTeam.getPoints(), "Home team should receive draw points.");
            assertEquals(rules.getDrawPoints(), awayTeam.getPoints(), "Away team should receive draw points.");
        }
    }

    @Test
    void testGoalDifferenceUpdates() {
        game.play();
        assertEquals(game.getHomeScore() - game.getAwayScore(), homeTeam.getGoalDifference());
        assertEquals(game.getAwayScore() - game.getHomeScore(), awayTeam.getGoalDifference());
    }
}