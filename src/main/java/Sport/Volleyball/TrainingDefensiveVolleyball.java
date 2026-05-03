package Sport.Volleyball;

import Classes.Training;
import Classes.TrainingCategory;
import Interface.ICoach;

public class TrainingDefensiveVolleyball extends Training {

    public TrainingDefensiveVolleyball() {
        super("Dig & Block Drills",
              "Focuses on improving reception, digging, and net blocking.",
              TrainingCategory.DEFENSIVE);
    }

    @Override
    protected String getCoachTraitName() {
        return "Defense Coaching";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 110 + (coachSkill * 90);
    }
}
