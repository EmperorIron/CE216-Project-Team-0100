package Sport.Volleyball;

import Classes.Training;
import Classes.TrainingCategory;
import Interface.ICoach;

public class TrainingOffensiveVolleyball extends Training {

    public TrainingOffensiveVolleyball() {
        super("Serve & Attack Drills",
              "Focuses on improving serving power, accuracy, and spiking technique.",
              TrainingCategory.OFFENSIVE);
    }

    @Override
    protected String getCoachTraitName() {
        return "Attack Coaching";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 110 + (coachSkill * 90);
    }
}
