package Sport;

import Classes.CoachGenerator;
import Classes.GameRules;
import Classes.RandomGenerator;

public class CoachGeneratorFootball extends CoachGenerator {

   
    private static final String[] FOOTBALL_FORMATIONS = {"1-4-4-2", "1-4-3-3", "1-3-5-2", "1-4-2-3-1", "1-5-3-2"};
    private static final String[] COACH_STYLES = {"Offensive", "Defensive", "Balanced", "Youth-focused"};

    
    public static CoachFootball createFootballCoachByStyle(String exactStyle, GameRules rules) {
        String[] identity = generateCoachIdentity(); 
        String firstName = identity[0];
        String lastName = identity[1];
        String country = identity[2];
        String id = identity[3];

       
        int offensiveCoaching = RandomGenerator.generateBellCurveStat(65, 15, 30, 99);
        int defensiveCoaching = RandomGenerator.generateBellCurveStat(65, 15, 30, 99);
        int playerManagement = RandomGenerator.generateBellCurveStat(70, 12, 40, 99);
        int motivation = RandomGenerator.generateBellCurveStat(70, 15, 40, 99);
        int youthDevelopment = RandomGenerator.generateBellCurveStat(60, 20, 20, 99);

      
        if (exactStyle.equalsIgnoreCase("Offensive")) {
            offensiveCoaching = RandomGenerator.generateBellCurveStat(85, 8, 70, 99);
            defensiveCoaching = RandomGenerator.generateBellCurveStat(50, 10, 30, 70);
        } else if (exactStyle.equalsIgnoreCase("Defensive")) {
            defensiveCoaching = RandomGenerator.generateBellCurveStat(85, 8, 70, 99);
            offensiveCoaching = RandomGenerator.generateBellCurveStat(50, 10, 30, 70);
        } else if (exactStyle.equalsIgnoreCase("Youth-focused")) {
            youthDevelopment = RandomGenerator.generateBellCurveStat(90, 5, 80, 99);
        }

        String formation = FOOTBALL_FORMATIONS[rand.nextInt(FOOTBALL_FORMATIONS.length)]; 

        return new CoachFootball(firstName, lastName, country, "Free Agent", id, formation,
                                 offensiveCoaching, defensiveCoaching, playerManagement, motivation, youthDevelopment,
                                 rules.getExpToLevelUpBase(), rules.getExpGrowthRate());
    }

  
    public static CoachFootball createRandomFootballCoach(GameRules rules) {
        String randomStyle = COACH_STYLES[rand.nextInt(COACH_STYLES.length)]; 
        return createFootballCoachByStyle(randomStyle, rules);
    }
}
