package Sport;

import Classes.*;
import Interface.ICoach;

public class TrainingPhysicalFootball extends Training {

    public TrainingPhysicalFootball() {
        super("Physical Drills", "Focuses on improving speed, stamina, and other physical attributes.", TrainingCategory.PHYSICAL);
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
