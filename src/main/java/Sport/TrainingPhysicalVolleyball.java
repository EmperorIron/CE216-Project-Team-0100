package Sport;

import Classes.Training;
import Classes.TrainingCategory;
import Interface.ICoach;

public class TrainingPhysicalVolleyball extends Training {

    public TrainingPhysicalVolleyball() {
        super("Physical Conditioning",
              "Focuses on improving jump height, speed, and overall stamina.",
              TrainingCategory.PHYSICAL);
    }

    @Override
    protected String getCoachTraitName() {
        return "Motivation";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 120 + (coachSkill * 100);
    }
}
