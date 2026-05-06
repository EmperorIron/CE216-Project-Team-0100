package Sport.Volleyball;

import Classes.Player;
import Classes.Trait;
import Classes.TraitCategory;

import java.util.Map;


public class PlayerVolleyball extends Player {

    public PlayerVolleyball(String name, String surname, String country, String teamName, String id,
                            Map<Integer, Integer> positionProficiency,
                            int servingVal, int spikingVal, int settingVal,
                            int blockingVal, int diggingVal,
                            int speedVal, int physicalVal, int mentalVal,
                            float initialExpToLevelUp, float expGrowthRate) {

        super(name, surname, country, teamName, id, positionProficiency);

        addTrait(new Trait("Serving",  TraitCategory.OFFENSE,  0, 100, 50, servingVal,  initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Spiking",  TraitCategory.OFFENSE,  0, 100, 50, spikingVal,  initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Setting",  TraitCategory.OFFENSE,  0, 100, 50, settingVal,  initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Blocking", TraitCategory.DEFENSE,  0, 100, 50, blockingVal, initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Digging",  TraitCategory.DEFENSE,  0, 100, 50, diggingVal,  initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Speed",    TraitCategory.PHYSICAL, 0, 100, 50, speedVal,    initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Physical", TraitCategory.PHYSICAL, 0, 100, 50, physicalVal, initialExpToLevelUp, expGrowthRate));
        addTrait(new Trait("Mental",   TraitCategory.MENTAL,   0, 100, 50, mentalVal,   initialExpToLevelUp, expGrowthRate));
    }

    @Override
    public double calculateOverallRating() {
        if (isInjured()) {
            return 0.0;
        }
        Trait serving  = getTrait("Serving");
        Trait spiking  = getTrait("Spiking");
        Trait setting  = getTrait("Setting");
        Trait blocking = getTrait("Blocking");
        Trait digging  = getTrait("Digging");
        Trait speed    = getTrait("Speed");
        Trait physical = getTrait("Physical");
        Trait mental   = getTrait("Mental");

        if (serving == null || spiking == null || setting == null || blocking == null ||
            digging == null || speed == null || physical == null || mental == null) {
            return 0.0;
        }

        int posId = getCurrentPositionId();
        double baseRating;

        if (PositionsVolleyball.isLiberoPosition(posId)) {
            baseRating = digging.getCurrentLevel()  * 0.45
                       + speed.getCurrentLevel()    * 0.20
                       + mental.getCurrentLevel()   * 0.20
                       + physical.getCurrentLevel() * 0.15;
        } else if (PositionsVolleyball.isBackRowPosition(posId)) {
            if (setting.getCurrentLevel() > spiking.getCurrentLevel()) {
                baseRating = setting.getCurrentLevel()  * 0.35
                           + mental.getCurrentLevel()   * 0.20
                           + digging.getCurrentLevel()  * 0.20
                           + serving.getCurrentLevel()  * 0.15
                           + speed.getCurrentLevel()    * 0.10;
            } else {
                baseRating = digging.getCurrentLevel()  * 0.30
                           + serving.getCurrentLevel()  * 0.25
                           + spiking.getCurrentLevel()  * 0.20
                           + speed.getCurrentLevel()    * 0.15
                           + mental.getCurrentLevel()   * 0.10;
            }
        } else {
            baseRating = spiking.getCurrentLevel()  * 0.30
                       + blocking.getCurrentLevel() * 0.25
                       + serving.getCurrentLevel()  * 0.15
                       + physical.getCurrentLevel() * 0.15
                       + mental.getCurrentLevel()   * 0.15;
        }

        int proficiency = getProficiencyAt(posId);
        double proficiencyModifier = (proficiency + 5) / 105.0;
        return baseRating * proficiencyModifier;
    }
}
