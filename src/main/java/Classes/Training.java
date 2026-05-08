package Classes;

import Interface.ICoach;
import Interface.IPlayer;
import Interface.ITeam;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class Training {

    protected final String name;
    protected final String description;
    protected final TrainingCategory category;
    protected transient Random random;

    public Training(String name, String description, TrainingCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TrainingCategory getCategory() {
        return category;
    }

    public Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }


    public void apply(ITeam team) {
        if (team == null) {
            ErrorHandler.logError("Attempted to apply training to a null team.");
            return;
        }
        TraitCategory playerTraitCategory = getPlayerTraitCategory();
        String coachTraitName = getCoachTraitName();

        if (coachTraitName == null) {
            return;
        }

        List<ICoach> relevantCoaches = team.getCoaches().stream()
                .filter(c -> c.getTrait(coachTraitName) != null)
                .sorted(Comparator.comparingInt((ICoach c) -> c.getTrait(coachTraitName).getCurrentLevel()).reversed())
                .collect(Collectors.toList());

        if (relevantCoaches.isEmpty()) {
            return;
        }

        List<IPlayer> trainablePlayers = team.getPlayers().stream()
                .filter(p -> !p.isInjured())
                .sorted(Comparator.comparingDouble((IPlayer p) -> calculatePlayerCategoryScore(p, playerTraitCategory)).reversed())
                .collect(Collectors.toList());

        if (trainablePlayers.isEmpty()) {
            return;
        }

        int coachCount = relevantCoaches.size();
        int playerCount = trainablePlayers.size();
        int playerIndex = 0;

        for (int i = 0; i < coachCount; i++) {
            ICoach coach = relevantCoaches.get(i);

            int playersForThisCoachCount = playerCount / coachCount + (i < playerCount % coachCount ? 1 : 0);
            if (playersForThisCoachCount == 0) {
                continue;
            }

            List<IPlayer> playersForThisCoach = trainablePlayers.subList(playerIndex, playerIndex + playersForThisCoachCount);
            playerIndex += playersForThisCoachCount;

            int totalExperiencePoints = calculateExperiencePoints(coach, coachTraitName);

            for (int j = 0; j < playersForThisCoach.size(); j++) {
                IPlayer player = playersForThisCoach.get(j);
                int pointsForThisPlayer = totalExperiencePoints / playersForThisCoachCount + (j < totalExperiencePoints % playersForThisCoachCount ? 1 : 0);

                List<Trait> relevantPlayerTraits = player.getTraits().values().stream()
                        .filter(t -> t.getCategory() == playerTraitCategory)
                        .collect(Collectors.toList());

                if (relevantPlayerTraits.isEmpty()) {
                    continue;
                }

                int numRelevantTraits = relevantPlayerTraits.size();
                for (int k = 0; k < pointsForThisPlayer; k++) {
                    Trait traitToImprove = relevantPlayerTraits.get(k % numRelevantTraits);
                    traitToImprove.addExperience(1);
                }
            }
        }
    }


    protected abstract int calculateExperiencePoints(ICoach coach, String coachTraitName);

    private double calculatePlayerCategoryScore(IPlayer player, TraitCategory category) {
        return player.getTraits().values().stream()
                .filter(t -> t.getCategory() == category)
                .mapToInt(Trait::getCurrentLevel)
                .average()
                .orElse(0.0);
    }


    private TraitCategory getPlayerTraitCategory() {
        switch (this.category) {
            case OFFENSIVE: return TraitCategory.OFFENSE;
            case DEFENSIVE: return TraitCategory.DEFENSE;
            case PHYSICAL: return TraitCategory.PHYSICAL;
            case MENTAL: return TraitCategory.MENTAL;
            default: 
                ErrorHandler.logError("Unknown training category: " + this.category);
                return TraitCategory.PHYSICAL;
        }
    }


    protected abstract String getCoachTraitName();

    public String getCoachTraitForTraining() {
        return getCoachTraitName();
    }
}