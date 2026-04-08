package tests;
import Classes.*;
import Interface.IPlayer;
import Sport.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class Test_Training {

    private GameRulesFootball rules;
    private TeamFootball team;
    private TrainingOffensiveFootball offensiveTraining;

    @BeforeEach
    void setUp() {
        rules = new GameRulesFootball();
        team = TeamGeneratorFootball.createRandomFootballTeam(rules);
        offensiveTraining = new TrainingOffensiveFootball();
    }


    private double getTotalExperienceForCategory(TeamFootball team, TraitCategory category) {
        return team.getPlayers().stream()
                .flatMap(p -> p.getTraits().values().stream())
                .filter(t -> t.getCategory() == category)
                .mapToDouble(Trait::getExp)
                .sum();
    }

    @Test
    void testOffensiveTrainingIncreasesExperience() {
        double initialOffensiveExp = getTotalExperienceForCategory(team, TraitCategory.OFFENSE);

        offensiveTraining.apply(team);

        double finalOffensiveExp = getTotalExperienceForCategory(team, TraitCategory.OFFENSE);

        assertTrue(finalOffensiveExp > initialOffensiveExp,
                "Offensive training should increase the total offensive experience of players.");
    }

    @Test
    void testInjuredPlayerIsNotTrained() {
        IPlayer bestPlayer = team.getPlayers().stream()
                .filter(p -> !p.isInjured())
                .max(Comparator.comparingDouble(p -> p.getTraits().values().stream()
                        .filter(t -> t.getCategory() == TraitCategory.OFFENSE)
                        .mapToInt(Trait::getCurrentLevel)
                        .average().orElse(0.0)))
                .orElse(null);

        assertNotNull(bestPlayer, "Team should have at least one player to test with.");

        bestPlayer.setInjuryDuration(3);
        assertTrue(bestPlayer.isInjured());

        double initialPlayerExp = bestPlayer.getTraits().values().stream()
                .filter(t -> t.getCategory() == TraitCategory.OFFENSE)
                .mapToDouble(Trait::getExp)
                .sum();

        offensiveTraining.apply(team);

        double finalPlayerExp = bestPlayer.getTraits().values().stream()
                .filter(t -> t.getCategory() == TraitCategory.OFFENSE)
                .mapToDouble(Trait::getExp)
                .sum();

        assertEquals(initialPlayerExp, finalPlayerExp, "Injured player should not gain experience from training.");
    }

    @Test
    void testTrainingWithNoRelevantCoach() {
        TeamFootball teamWithNoCoaches = new TeamFootball("No Coach FC", "Nowhere", "Test League");
        for (int i = 0; i < 15; i++) {
            teamWithNoCoaches.addPlayer(PlayerGeneratorFootball.createRandomFootballPlayer(rules));
        }

        double initialExp = getTotalExperienceForCategory(teamWithNoCoaches, TraitCategory.OFFENSE);

        offensiveTraining.apply(teamWithNoCoaches);

        double finalExp = getTotalExperienceForCategory(teamWithNoCoaches, TraitCategory.OFFENSE);

        assertEquals(initialExp, finalExp, "Training should have no effect if there are no coaches.");
    }
}