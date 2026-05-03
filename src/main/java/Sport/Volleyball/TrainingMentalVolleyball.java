package Sport.Volleyball;

import Classes.Training;
import Classes.TrainingCategory;
import Interface.ICoach;

public class TrainingMentalVolleyball extends Training {

    public TrainingMentalVolleyball() {
        super("Mental Conditioning",
              "Focuses on composure, game-reading, and pressure management.",
              TrainingCategory.MENTAL);
    }

    @Override
    protected String getCoachTraitName() {
        return "Player Management";
    }

    @Override
    protected int calculateExperiencePoints(ICoach coach, String coachTraitName) {
        int coachSkill = coach.getTrait(coachTraitName).getCurrentLevel();
        return 100 + (coachSkill * 80);
    }
}
