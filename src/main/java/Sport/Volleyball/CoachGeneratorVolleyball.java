package Sport.Volleyball;

import Classes.CoachGenerator;
import Classes.GameRules;
import Classes.RandomGenerator;

public class CoachGeneratorVolleyball extends CoachGenerator {

    private static final String[] VOLLEYBALL_FORMATIONS = {"5-1", "6-2", "4-2"};
    private static final String[] COACH_STYLES = {"Attack", "Defense", "Balanced", "Serve-focused"};

    public static CoachVolleyball createVolleyballCoachByStyle(String style, GameRules rules) {
        String[] identity = generateCoachIdentity();

        int serveCoaching    = RandomGenerator.generateBellCurveStat(65, 15, 30, 99);
        int attackCoaching   = RandomGenerator.generateBellCurveStat(65, 15, 30, 99);
        int defenseCoaching  = RandomGenerator.generateBellCurveStat(65, 15, 30, 99);
        int playerManagement = RandomGenerator.generateBellCurveStat(70, 12, 40, 99);
        int motivation       = RandomGenerator.generateBellCurveStat(70, 15, 40, 99);

        switch (style.toLowerCase()) {
            case "attack":
                attackCoaching = RandomGenerator.generateBellCurveStat(88, 7, 72, 99);
                defenseCoaching = RandomGenerator.generateBellCurveStat(50, 10, 30, 70);
                break;
            case "defense":
                defenseCoaching = RandomGenerator.generateBellCurveStat(88, 7, 72, 99);
                attackCoaching = RandomGenerator.generateBellCurveStat(50, 10, 30, 70);
                break;
            case "serve-focused":
                serveCoaching = RandomGenerator.generateBellCurveStat(90, 5, 78, 99);
                break;
            default: // balanced – no change
                break;
        }

        String formation = VOLLEYBALL_FORMATIONS[rand.nextInt(VOLLEYBALL_FORMATIONS.length)];

        return new CoachVolleyball(
                identity[0], identity[1], identity[2], "Free Agent", identity[3],
                formation,
                serveCoaching, attackCoaching, defenseCoaching, playerManagement, motivation,
                rules.getExpToLevelUpBase(), rules.getExpGrowthRate()
        );
    }

    public static CoachVolleyball createRandomVolleyballCoach(GameRules rules) {
        String style = COACH_STYLES[rand.nextInt(COACH_STYLES.length)];
        return createVolleyballCoachByStyle(style, rules);
    }
}
