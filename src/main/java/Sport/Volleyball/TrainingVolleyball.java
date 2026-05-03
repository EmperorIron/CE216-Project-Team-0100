package Sport.Volleyball;

import Classes.Training;
import Classes.TrainingCategory;
import Interface.ICoach;


public class TrainingVolleyball extends Training {

    public TrainingVolleyball() {
        super("Volleyball Training",
              "General volleyball practice covering all aspects of the game.",
              TrainingCategory.PHYSICAL);
    }

    @Override
    protected String getCoachTraitName() {
        return "Motivation";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 100 + (coachSkill * 80);
    }
}
