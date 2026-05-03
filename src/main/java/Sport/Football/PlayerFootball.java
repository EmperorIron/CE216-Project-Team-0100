package Sport.Football;

import java.util.Map;

import Classes.Player;
import Classes.Trait;
import Classes.TraitCategory;


public class PlayerFootball extends Player {

    public PlayerFootball(String name, String surname, String country, String teamName, String id, Map<Integer, Integer> positionProficiency,
                          int defVal, int pasVal, int sutVal, int hizVal, int fizVal, int menVal, int gkVal,
                          float initialExpToLevelUp, float expGrowthRate) {
        
        super(name, surname, country, teamName, id, positionProficiency);

        this.addTrait(new Trait("Defense", TraitCategory.DEFENSE, 0, 100, 50, defVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Passing", TraitCategory.OFFENSE, 0, 100, 50, pasVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Shooting", TraitCategory.OFFENSE, 0, 100, 50, sutVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Speed", TraitCategory.PHYSICAL, 0, 100, 50, hizVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Physical", TraitCategory.PHYSICAL, 0, 100, 50, fizVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Mental", TraitCategory.MENTAL, 0, 100, 50, menVal, initialExpToLevelUp, expGrowthRate));
        this.addTrait(new Trait("Goalkeeping", TraitCategory.DEFENSE, 0, 100, 50, gkVal, initialExpToLevelUp, expGrowthRate));
    }

    @Override
    public double calculateOverallRating() {
        Trait def = this.getTrait("Defense");
        Trait pas = this.getTrait("Passing");
        Trait sut = this.getTrait("Shooting");
        Trait hiz = this.getTrait("Speed");
        Trait fiz = this.getTrait("Physical");
        Trait men = this.getTrait("Mental");
        Trait gk  = this.getTrait("Goalkeeping");

        if (def == null || pas == null || sut == null || hiz == null || fiz == null || men == null || gk == null) {
            return 0.0;
        }

        double baseRating;
        boolean isGoalkeeper = PositionsFootball.isGoalkeeperPosition(this.getPrimaryPositionId());

        if (isGoalkeeper) {
            baseRating = (gk.getCurrentLevel() * 0.70) + (men.getCurrentLevel() * 0.20) + (pas.getCurrentLevel() * 0.10);
        } else {
            baseRating = (pas.getCurrentLevel() * 0.20) + (men.getCurrentLevel() * 0.15) +
                         (hiz.getCurrentLevel() * 0.15) + (def.getCurrentLevel() * 0.20) +
                         (sut.getCurrentLevel() * 0.20) + (fiz.getCurrentLevel() * 0.10);
        }

        int proficiency = this.getProficiencyAt(this.getCurrentPositionId());


        double proficiencyModifier = (proficiency + 5) / 105.0;

        return baseRating * proficiencyModifier;
    }
}