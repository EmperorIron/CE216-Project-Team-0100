package Sport;
import java.util.HashMap;
import java.util.Map;

import Classes.GameRules;


public class GameRulesFootball extends GameRules {

    public GameRulesFootball() {
        // Calling the parent constructor with standard football rules.
        super(
            2,                                  // periodCount: 2 halves
            45,                                 // periodDuration: 45 minutes per half
            20,                                 // playerCount: 20 total players in squad
            11,                                 // fieldPlayerCount: 11 players on the field
            3,                                  // coachCount: 3 coaches (Offensive, Defensive, Youth)
            9,                                  // reservePlayerCount: 9 substitutes on the bench
            5,                                  // substitutionCount: 5 substitutions allowed
            false,                              // canReEnter: Substituted players cannot re-enter
            createFootballScoreProbabilities(), // scoreProbabilities: {1: 1.0} for a goal
            3,                                  // victoryPoints: 3 points for a win
            1,                                  // drawPoints: 1 point for a draw
            0,                                  // defeatPoints: 0 points for a loss
            2,                                  // yellowCardsForRed: 2 yellow cards equal a red card
            false,                              // canReplaceRedCardedPlayer: A red-carded player cannot be replaced
            0,                                  // teamYellowCardLimit: No specific team-wide limit (0)
            100.0f,                             // expToLevelUpBase: Base XP for level up
            1.03f,                              // expGrowthRate: XP requirement growth per level
            38,                                 // weekCount: Total weeks in a season
            2,                                  // matchbetweenTeams: Each team plays others twice
            1,                                  // matchesPerWeek: One match per week
            "0000001"                           // trainingormatch: A 7-char string (Mon-Sun), '1' is a match day. "0000001" = Match on Sunday.
        );
    }

    private static Map<Integer, Double> createFootballScoreProbabilities() {
        Map<Integer, Double> probabilities = new HashMap<>();
        // In football, a goal is worth 1 point, and it's the only way to score.
        // So, the probability of a score being worth 1 point is 100%.
        probabilities.put(1, 1.0);
        return probabilities;
    }
}
