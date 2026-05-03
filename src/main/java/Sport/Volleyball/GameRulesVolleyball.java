package Sport.Volleyball;

import Classes.GameRules;
import java.util.HashMap;
import java.util.Map;


public class GameRulesVolleyball extends GameRules {

    public static final int REGULAR_SET_POINTS = 25;

    public static final int DECIDING_SET_POINTS = 15;

    public static final int WIN_BY = 2;

    public static final int MAX_SETS = 5;

    public GameRulesVolleyball() {
        super(
            5,                                       // periodCount  : up to 5 sets
            30,                                      // periodDuration: simulated rally ticks per set
            14,                                      // playerCount   : 14 players in squad (6 starters + 8 bench incl. Libero)
            6,                                       // fieldPlayerCount: 6 on court
            3,                                       // coachCount    : head coach + assistant + analyst
            8,                                       // reservePlayerCount
            6,                                       // substitutionCount: 6 per set (FIVB rule)
            false,                                   // canReEnter    : false (standard FIVB)
            createVolleyballScoreProbabilities(),
            3,                                       // victoryPoints
            0,                                       // drawPoints    : no draws in volleyball
            1,                                       // defeatPoints  : 1 point for winning ≥2 sets
            0,                                       // yellowCardsForRed: not used
            false,                                   // canReplaceRedCardedPlayer
            0,                                       // teamYellowCardLimit
            100.0f,                                  // expToLevelUpBase
            1.03f,                                   // expGrowthRate
            30,                                      // weekCount (shorter season)
            2,                                       // matchesBetweenTeams
            1,                                       // matchesPerWeek
            "0000001"                                // trainingOrMatch (match on Sunday)
        );
    }


    private static Map<Integer, Double> createVolleyballScoreProbabilities() {
        Map<Integer, Double> probabilities = new HashMap<>();
        probabilities.put(1, 1.0);
        return probabilities;
    }
}
