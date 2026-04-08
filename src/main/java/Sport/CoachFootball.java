package Sport;

import Classes.Coach;
import Classes.Trait;
import Classes.TraitCategory;

public class CoachFootball extends Coach {

    private final String preferredFormation;

    public CoachFootball(String name, String surname, String country, String team, String id, String preferredFormation,
                         int offensiveCoaching, int defensiveCoaching, int playerManagement, int motivation, int youthDevelopment,
                         float initialExpToLevelUp, float expGrowthRate) {
        super(name, surname, country, team, id);
        this.preferredFormation = preferredFormation;
        
        this.addTrait(new Trait("Offensive Coaching", TraitCategory.OFFENSE, 0, 100, 50, offensiveCoaching, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Defensive Coaching", TraitCategory.DEFENSE, 0, 100, 50, defensiveCoaching, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Player Management", TraitCategory.MENTAL, 0, 100, 50, playerManagement, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Motivation", TraitCategory.MENTAL, 0, 100, 50, motivation, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Youth Development", TraitCategory.MENTAL, 0, 100, 50, youthDevelopment, initialExpToLevelUp, expGrowthRate));
    }

    @Override
    public String getPreferredFormation() {
        return this.preferredFormation;
    }
}