package Sport.Football;

import java.util.Map;

import Classes.Player;
import Classes.Trait;
import Classes.TraitCategory;


public class PlayerFootball extends Player {

    public PlayerFootball(String name, String surname, String country, String teamName, String id, Map<Integer, Integer> positionProficiency,
                          int defVal, int passVal, int shotVal, int speedVal, int physVal, int mentVal, int gkVal,
                          float initialExpToLevelUp, float expGrowthRate) {
        
        super(name, surname, country, teamName, id, positionProficiency);

        this.addTrait(new Trait("Defense", TraitCategory.DEFENSE, 0, 100, 50, defVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Passing", TraitCategory.OFFENSE, 0, 100, 50, passVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Shooting", TraitCategory.OFFENSE, 0, 100, 50, shotVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Speed", TraitCategory.PHYSICAL, 0, 100, 50, speedVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Physical", TraitCategory.PHYSICAL, 0, 100, 50, physVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Mental", TraitCategory.MENTAL, 0, 100, 50, mentVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Goalkeeping", TraitCategory.DEFENSE, 0, 100, 50, gkVal, initialExpToLevelUp, expGrowthRate));
    }

    @Override
    public double calculateOverallRating() {
        Trait def = this.getTrait("Defense");
        Trait pass = this.getTrait("Passing");
        Trait shot = this.getTrait("Shooting");
        Trait speed = this.getTrait("Speed");
        Trait phys = this.getTrait("Physical");
        Trait ment = this.getTrait("Mental");
        Trait gk  = this.getTrait("Goalkeeping");

        if (def == null || pass == null || shot == null || speed == null || phys == null || ment == null || gk == null) {
            return 0.0;
        }

        double baseRating;
        boolean isGoalkeeper = PositionsFootball.isGoalkeeperPosition(this.getPrimaryPositionId());

        if (isGoalkeeper) {
            baseRating = (gk.getCurrentLevel() * 0.70) + (ment.getCurrentLevel() * 0.20) + (pass.getCurrentLevel() * 0.10);
        } else {
            baseRating = (pass.getCurrentLevel() * 0.20) + (ment.getCurrentLevel() * 0.15) +
                         (speed.getCurrentLevel() * 0.15) + (def.getCurrentLevel() * 0.20) +
                         (shot.getCurrentLevel() * 0.20) + (phys.getCurrentLevel() * 0.10);
        }

        int proficiency = this.getProficiencyAt(this.getCurrentPositionId());


        double proficiencyModifier = (proficiency + 5) / 105.0;

        return baseRating * proficiencyModifier;
    }
}