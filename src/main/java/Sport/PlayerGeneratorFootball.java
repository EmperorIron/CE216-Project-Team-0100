package Sport;

import java.util.Map;

import Classes.GameRules;
import Classes.PlayerGenerator;
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

        int defVal = randGen.generateBellCurveStat(50, 15, 10, 100);
        int pasVal = randGen.generateBellCurveStat(50, 15, 10, 100);
        int sutVal = randGen.generateBellCurveStat(50, 15, 10, 100);
        int hizVal = randGen.generateBellCurveStat(50, 15, 10, 100);
        int fizVal = randGen.generateBellCurveStat(50, 15, 10, 100);
        int menVal = randGen.generateBellCurveStat(50, 15, 10, 100);
        int gkVal  = randGen.generateBellCurveStat(10, 5, 0, 100); 

        switch (exactPosition.toUpperCase()) {
            case "ST": 
                sutVal = randGen.generateBellCurveStat(85, 7, 60, 100); 
                fizVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                hizVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "LW": 
            case "RW": 
                hizVal = randGen.generateBellCurveStat(88, 6, 70, 100);
                sutVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                pasVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "CAM": 
                pasVal = randGen.generateBellCurveStat(85, 7, 60, 100);
                menVal = randGen.generateBellCurveStat(82, 8, 50, 100);
                sutVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "CM": 
                pasVal = randGen.generateBellCurveStat(80, 8, 60, 100);
                menVal = randGen.generateBellCurveStat(80, 8, 60, 100);
                fizVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                defVal = randGen.generateBellCurveStat(70, 10, 40, 90);
                break;
            case "CDM": 
                defVal = randGen.generateBellCurveStat(82, 8, 60, 100);
                fizVal = randGen.generateBellCurveStat(82, 8, 60, 100);
                pasVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                break;
            case "CB": 
                defVal = randGen.generateBellCurveStat(88, 6, 70, 100);
                fizVal = randGen.generateBellCurveStat(85, 7, 60, 100);
                hizVal = randGen.generateBellCurveStat(50, 15, 20, 85); // Stoperler genelde yavaştır
                break;
            case "LB": 
            case "RB": 
                hizVal = randGen.generateBellCurveStat(85, 7, 60, 100);
                defVal = randGen.generateBellCurveStat(75, 10, 50, 100);
                pasVal = randGen.generateBellCurveStat(70, 12, 50, 100);
                break;
            case "GK": 
                gkVal = randGen.generateBellCurveStat(85, 5, 60, 100);
                defVal = randGen.generateBellCurveStat(30, 10, 10, 60);
                hizVal = randGen.generateBellCurveStat(40, 15, 20, 70);
                break;
            
            case "FORWARD": 
                sutVal = randGen.generateBellCurveStat(85, 7, 60, 100); break;
            case "DEFENDER": 
                defVal = randGen.generateBellCurveStat(85, 7, 60, 100); break;
            case "MIDFIELDER": 
                pasVal = randGen.generateBellCurveStat(85, 7, 60, 100); break;
            case "GOALKEEPER": 
                gkVal = randGen.generateBellCurveStat(85, 5, 60, 100); break;
        }

        int primaryPositionId = PositionsFootball.getRandomPositionForRole(exactPosition);
        int primaryProficiency = randGen.generateBellCurveStat(95, 5, 90, 100);
        
        double falloffRate = randGen.generateBellCurveStat(8, 2, 5, 12); 

        Map<Integer, Integer> proficiencyMap = Positions.generateProficiencyMap(primaryPositionId, primaryProficiency, falloffRate);

        return new PlayerFootball(firstName, lastName, country, "Free Agent", id, proficiencyMap, 
                                  defVal, pasVal, sutVal, hizVal, fizVal, menVal, gkVal, 
                                  rules.getExpToLevelUpBase(), rules.getExpGrowthRate());
    }

    public static PlayerFootball createRandomFootballPlayer(GameRules rules) {
        String randomPosition = FOOTBALL_POSITIONS[rand.nextInt(FOOTBALL_POSITIONS.length)];
        return createFootballPlayerByPosition(randomPosition, rules); 
    }
}