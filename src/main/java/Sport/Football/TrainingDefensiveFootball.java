package Sport.Football;

import Classes.*;
import Interface.ICoach;

public class TrainingDefensiveFootball extends Training {

    public TrainingDefensiveFootball() {
        super("Defensive Drills", "Focuses on improving tackling, positioning, and other defensive attributes.", TrainingCategory.DEFENSIVE);
    }

    @Override
    protected String getCoachTraitName() {
        return "Defensive Coaching";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 120 + (coachSkill * 100);
    }
}
