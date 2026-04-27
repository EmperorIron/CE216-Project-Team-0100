package Sport;

import Classes.*;
import Interface.ICoach;

public class TrainingMentalFootball extends Training {

    public TrainingMentalFootball() {
        super("Mental Drills", "Focuses on improving tactical awareness, composure, and other mental attributes.", TrainingCategory.MENTAL);
    }

    @Override
    protected String getCoachTraitName() {
        return "Player Management";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 120 + (coachSkill * 100);
    }
}
