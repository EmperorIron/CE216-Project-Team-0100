package Sport;

import Classes.Coach;
import Classes.Trait;
import Classes.TraitCategory;



public class CoachVolleyball extends Coach {

    private final String preferredFormation;

    public CoachVolleyball(String name, String surname, String country, String team, String id,
                           String preferredFormation,
                           int serveCoaching, int attackCoaching, int defenseCoaching,
                           int playerManagement, int motivation,
                           float initialExpToLevelUp, float expGrowthRate) {
        super(name, surname, country, team, id);
        this.preferredFormation = preferredFormation;

        addTrait(new Trait("Serve Coaching",    TraitCategory.OFFENSE, 0, 100, 50, serveCoaching,    initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Attack Coaching",   TraitCategory.OFFENSE, 0, 100, 50, attackCoaching,   initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Defense Coaching",  TraitCategory.DEFENSE, 0, 100, 50, defenseCoaching,  initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Player Management", TraitCategory.MENTAL,  0, 100, 50, playerManagement, initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Motivation",        TraitCategory.MENTAL,  0, 100, 50, motivation,       initialExpToLevelUp, expGrowthRate));
    }

    @Override
    public String getPreferredFormation() {
        return preferredFormation;
    }
}
