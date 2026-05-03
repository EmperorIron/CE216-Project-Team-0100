package Sport.Volleyball;

import Classes.GameRules;
import Classes.PlayerGenerator;
import Classes.Positions;
import Classes.RandomGenerator;

import java.util.Map;


public class PlayerGeneratorVolleyball extends PlayerGenerator {

    private static final String[] VOLLEYBALL_ROLES = {
        "OUTSIDE_HITTER", "OUTSIDE_HITTER",   // 2x (common role)
        "MIDDLE_BLOCKER", "MIDDLE_BLOCKER",
        "OPPOSITE",
        "SETTER",
        "LIBERO"
    };

    public static PlayerVolleyball createVolleyballPlayerByRole(String role, GameRules rules) {
        String[] identity = generatePlayerIdentity();

        // Base stats – bell-curve around average
        int serving  = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int spiking  = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int setting  = RandomGenerator.generateBellCurveStat(40, 15, 10, 100);
        int blocking = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int digging  = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int speed    = RandomGenerator.generateBellCurveStat(60, 12, 20, 100);
        int physical = RandomGenerator.generateBellCurveStat(60, 12, 20, 100);
        int mental   = RandomGenerator.generateBellCurveStat(60, 12, 20, 100);

        String positionRole;   // for position-id lookup

        switch (role.toUpperCase()) {
            case "OUTSIDE_HITTER":
                spiking  = RandomGenerator.generateBellCurveStat(80, 8, 60, 100);
                serving  = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                digging  = RandomGenerator.generateBellCurveStat(72, 10, 50, 100);
                speed    = RandomGenerator.generateBellCurveStat(78, 8, 60, 100);
                positionRole = "LF";
                break;

            case "MIDDLE_BLOCKER":
                blocking = RandomGenerator.generateBellCurveStat(88, 6, 70, 100);
                spiking  = RandomGenerator.generateBellCurveStat(80, 8, 60, 100);
                physical = RandomGenerator.generateBellCurveStat(82, 8, 65, 100);
                speed    = RandomGenerator.generateBellCurveStat(55, 12, 30, 80); // less lateral speed
                positionRole = "MF";
                break;

            case "OPPOSITE":
                spiking  = RandomGenerator.generateBellCurveStat(90, 5, 75, 100);
                serving  = RandomGenerator.generateBellCurveStat(85, 7, 65, 100);
                blocking = RandomGenerator.generateBellCurveStat(70, 10, 50, 90);
                physical = RandomGenerator.generateBellCurveStat(80, 8, 60, 100);
                positionRole = "RF";
                break;

            case "SETTER":
                setting  = RandomGenerator.generateBellCurveStat(92, 5, 80, 100);
                mental   = RandomGenerator.generateBellCurveStat(85, 7, 70, 100);
                speed    = RandomGenerator.generateBellCurveStat(80, 8, 60, 100);
                spiking  = RandomGenerator.generateBellCurveStat(45, 12, 20, 70);
                positionRole = "RB";
                break;

            case "LIBERO":
                digging  = RandomGenerator.generateBellCurveStat(93, 4, 82, 100);
                speed    = RandomGenerator.generateBellCurveStat(88, 6, 72, 100);
                mental   = RandomGenerator.generateBellCurveStat(82, 8, 65, 100);
                spiking  = 5;   // Libero cannot spike
                blocking = 5;   // Libero cannot block at net
                positionRole = "LIBERO";
                break;

            default:
                positionRole = "LF";
                break;
        }

        int primaryPositionId  = PositionsVolleyball.getRandomPositionForRole(positionRole);
        int primaryProficiency = RandomGenerator.generateBellCurveStat(95, 5, 88, 100);
        double falloffRate     = RandomGenerator.generateBellCurveStat(8, 2, 5, 12);

        Map<Integer, Integer> proficiencyMap = Positions.generateProficiencyMap(
                primaryPositionId, primaryProficiency, falloffRate);

        return new PlayerVolleyball(
                identity[0], identity[1], identity[2], "Free Agent", identity[3],
                proficiencyMap,
                serving, spiking, setting, blocking, digging, speed, physical, mental,
                rules.getExpToLevelUpBase(), rules.getExpGrowthRate()
        );
    }

    public static PlayerVolleyball createRandomVolleyballPlayer(GameRules rules) {
        String role = VOLLEYBALL_ROLES[rand.nextInt(VOLLEYBALL_ROLES.length)];
        return createVolleyballPlayerByRole(role, rules);
    }
}
