package Sport;
import Classes.*;
import Interface.ICoach;


public class TrainingOffensiveFootball extends Training {


    public TrainingOffensiveFootball() {
        super("Offensive Drills", "Focuses on improving shooting, passing, and other offensive attributes.", TrainingCategory.OFFENSIVE);
    }

    @Override
    protected String getCoachTraitName() {
        return "Offensive Coaching";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 120 + (coachSkill *100);
    }
}
