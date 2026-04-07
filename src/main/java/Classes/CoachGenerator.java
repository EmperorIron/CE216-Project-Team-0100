package Classes;

import com.github.javafaker.Faker;

import Sport.CoachFootball;

import java.util.Random;

public class CoachGenerator {

    private static final Faker faker = new Faker();
    private static final Random rand = new Random();
    private static final RandomGenerator randGen = new RandomGenerator();

    // Football specific formations and styles
    private static final String[] FOOTBALL_FORMATIONS = {"4-4-2", "4-3-3", "3-5-2", "4-2-3-1", "5-3-2"};
    private static final String[] COACH_STYLES = {"Offensive", "Defensive", "Balanced", "Youth-focused"};

    // 1. GENEL METOT: Sadece "Koç Kimliği" (İsim, Soyisim, Ülke, ID) Üretir
    private static String[] generateCoachIdentity() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String country = faker.address().country();
        String id = "CID-" + faker.number().numberBetween(100, 999); // Coach ID

        return new String[]{firstName, lastName, country, id};
    }

    // 2. SPORA ÖZEL METOT: FUTBOL KOÇU ÜRETİMİ (Belirli Stile Göre)
    public static CoachFootball createFootballCoachByStyle(String exactStyle, GameRules rules) {
        String[] identity = generateCoachIdentity();
        String firstName = identity[0];
        String lastName = identity[1];
        String country = identity[2];
        String id = identity[3];

        // --- SADECE FUTBOL KOÇUNA ÖZEL YETENEK ZARLARI (Genel) ---
        int offensiveCoaching = randGen.generateBellCurveStat(65, 15, 30, 99);
        int defensiveCoaching = randGen.generateBellCurveStat(65, 15, 30, 99);
        int playerManagement = randGen.generateBellCurveStat(70, 12, 40, 99);
        int motivation = randGen.generateBellCurveStat(70, 15, 40, 99);
        int youthDevelopment = randGen.generateBellCurveStat(60, 20, 20, 99);

        // Stile göre uzmanlık alanı artışı
        if (exactStyle.equalsIgnoreCase("Offensive")) {
            offensiveCoaching = randGen.generateBellCurveStat(85, 8, 70, 99);
            defensiveCoaching = randGen.generateBellCurveStat(50, 10, 30, 70);
        } else if (exactStyle.equalsIgnoreCase("Defensive")) {
            defensiveCoaching = randGen.generateBellCurveStat(85, 8, 70, 99);
            offensiveCoaching = randGen.generateBellCurveStat(50, 10, 30, 70);
        } else if (exactStyle.equalsIgnoreCase("Youth-focused")) {
            youthDevelopment = randGen.generateBellCurveStat(90, 5, 80, 99);
        }

        String formation = FOOTBALL_FORMATIONS[rand.nextInt(FOOTBALL_FORMATIONS.length)];

        return new CoachFootball(firstName, lastName, country, "Free Agent", id, formation,
                                 offensiveCoaching, defensiveCoaching, playerManagement, motivation, youthDevelopment,
                                 rules.getExpToLevelUpBase(), rules.getExpGrowthRate());
    }

    // 3. SPORA ÖZEL METOT: RASTGELE FUTBOL KOÇU ÜRETİMİ
    public static CoachFootball createRandomFootballCoach(GameRules rules) {
        String randomStyle = COACH_STYLES[rand.nextInt(COACH_STYLES.length)];
        return createFootballCoachByStyle(randomStyle, rules);
    }
}
