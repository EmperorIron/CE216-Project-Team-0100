package Sport;

import java.util.Map;

import Classes.GameRules;
import Classes.PlayerGenerator;
import Classes.RandomGenerator;
import Classes.Positions;

public class PlayerGeneratorFootball extends PlayerGenerator {

    private static final String[] FOOTBALL_POSITIONS = {
        "ST", "LW", "RW", "CAM", "CM", "CDM", "LB", "CB", "RB", "GK"
    };


    public static PlayerFootball createFootballPlayerByPosition(String exactPosition, GameRules rules) {
        
        String[] identity = generatePlayerIdentity();
        String firstName = identity[0];
        String lastName = identity[1];
        String country = identity[2];
        String id = identity[3];

        int defVal = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int pasVal = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int sutVal = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int hizVal = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int fizVal = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int menVal = RandomGenerator.generateBellCurveStat(50, 15, 10, 100);
        int gkVal  = RandomGenerator.generateBellCurveStat(10, 5, 0, 100); 

        switch (exactPosition.toUpperCase()) {
            case "ST": 
                sutVal = RandomGenerator.generateBellCurveStat(85, 7, 60, 100); 
                fizVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                hizVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "LW": 
            case "RW": 
                hizVal = RandomGenerator.generateBellCurveStat(88, 6, 70, 100);
                sutVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                pasVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "CAM": 
                pasVal = RandomGenerator.generateBellCurveStat(85, 7, 60, 100);
                menVal = RandomGenerator.generateBellCurveStat(82, 8, 50, 100);
                sutVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "CM": 
                pasVal = RandomGenerator.generateBellCurveStat(80, 8, 60, 100);
                menVal = RandomGenerator.generateBellCurveStat(80, 8, 60, 100);
                fizVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                defVal = RandomGenerator.generateBellCurveStat(70, 10, 40, 90);
                break;
            case "CDM": 
                defVal = RandomGenerator.generateBellCurveStat(82, 8, 60, 100);
                fizVal = RandomGenerator.generateBellCurveStat(82, 8, 60, 100);
                pasVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "CB": 
                defVal = RandomGenerator.generateBellCurveStat(88, 6, 70, 100);
                fizVal = RandomGenerator.generateBellCurveStat(85, 7, 60, 100);
                hizVal = RandomGenerator.generateBellCurveStat(50, 15, 20, 85); // Stoperler genelde yavaştır
                break;
            case "LB": 
            case "RB": 
                hizVal = RandomGenerator.generateBellCurveStat(85, 7, 60, 100);
                defVal = RandomGenerator.generateBellCurveStat(75, 10, 50, 100);
                pasVal = RandomGenerator.generateBellCurveStat(70, 12, 50, 100);
                break;
            case "GK": 
                gkVal = RandomGenerator.generateBellCurveStat(85, 5, 60, 100);
                defVal = RandomGenerator.generateBellCurveStat(30, 10, 10, 60);
                hizVal = RandomGenerator.generateBellCurveStat(40, 15, 20, 70);
                break;
            
            case "FORWARD": 
                sutVal = RandomGenerator.generateBellCurveStat(85, 7, 60, 100); break;
            case "DEFENDER": 
                defVal = RandomGenerator.generateBellCurveStat(85, 7, 60, 100); break;
            case "MIDFIELDER": 
                pasVal = RandomGenerator.generateBellCurveStat(85, 7, 60, 100); break;
            case "GOALKEEPER": 
                gkVal = RandomGenerator.generateBellCurveStat(85, 5, 60, 100); break;
        }

        int primaryPositionId = PositionsFootball.getRandomPositionForRole(exactPosition);
        int primaryProficiency = RandomGenerator.generateBellCurveStat(95, 5, 90, 100);
        
        double falloffRate = RandomGenerator.generateBellCurveStat(8, 2, 5, 12); 

        Map<Integer, Integer> proficiencyMap = Positions.generateProficiencyMap(primaryPositionId, primaryProficiency, falloffRate);

        if (exactPosition.equalsIgnoreCase("GK") || exactPosition.equalsIgnoreCase("GOALKEEPER")) {
            for (int i = 0; i < Positions.TOTAL_POSITIONS; i++) {
                if (!PositionsFootball.isGoalkeeperPosition(i)) {
                    proficiencyMap.put(i, 0);
                }
            }
        } else {
            for (int i = 0; i < Positions.TOTAL_POSITIONS; i++) {
                if (PositionsFootball.isGoalkeeperPosition(i)) {
                    proficiencyMap.put(i, 0);
                }
            }
        }

        return new PlayerFootball(firstName, lastName, country, "Free Agent", id, proficiencyMap, 
                                  defVal, pasVal, sutVal, hizVal, fizVal, menVal, gkVal, 
                                  rules.getExpToLevelUpBase(), rules.getExpGrowthRate());
    }

    public static PlayerFootball createRandomFootballPlayer(GameRules rules) {
        String randomPosition = FOOTBALL_POSITIONS[rand.nextInt(FOOTBALL_POSITIONS.length)];
        return createFootballPlayerByPosition(randomPosition, rules); 
    }
}
