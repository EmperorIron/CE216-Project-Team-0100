package Classes;
import java.util.Random;
import java.util.logging.Level;

/**
 * Represents a specific characteristic or skill of an entity (like a player or coach).
 * A trait has a name, a category, and a level that can be adjusted within defined bounds.
 */
public class Trait {
    private String name;
    private int maxLevel; // The maximum level of this trait (e.g., 5)
    private int minLevel; // The minimum level of this trait (e.g., 0)
    private int medianLevel; // The median level of this trait (e.g., 3)
    private int currentLevel;
    private float exp; // The experience points of this trait, used for progression
    private float expToLevelUp; // The experience points required to reach the next level
    private float expGrowthRate; // Determines how much the required experience points increase per level
    private TraitCategory category; // Enum called from a separate file

   
    public Trait(String name, TraitCategory category, int minLevel, int maxLevel, int medianLevel, int currentLevel, float initialExpToLevelUp, float expGrowthRate) {
        this.name = name;
        this.category = category;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.medianLevel = medianLevel;
        this.currentLevel = Math.max(minLevel, Math.min(maxLevel, currentLevel));
        this.exp = 0.0f;
        // Set initial values from game rules
        this.expToLevelUp = initialExpToLevelUp * (float) Math.pow(expGrowthRate, (this.currentLevel - this.minLevel));
        this.expGrowthRate = expGrowthRate;
    }

    // Setters and Getters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getMaxLevel() {
        return maxLevel;
    }
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    public int getMinLevel() {
        return minLevel;
    }
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }
    public int getMedianLevel() {
        return medianLevel;
    }
    public void setMedianLevel(int medianLevel) {
        this.medianLevel = medianLevel;
    }
    public int getCurrentLevel() {
        return currentLevel;
    }
    public TraitCategory getCategory() {
        return category;
    }
    public void setCategory(TraitCategory category) {
        this.category = category;
    }

    public float getExp() {
        return exp;
    }

    public float getExpToLevelUp() {
        return expToLevelUp;
    }

    ///////////////////////////////////////////////////////////////////////////////
     public void setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.max(minLevel, Math.min(maxLevel, currentLevel));
    }

    /**
     * Adds experience points to the trait. If enough experience is gained,
     * the trait's current level increases, and the experience required for the
     * next level is adjusted. This process repeats if multiple levels are gained.
     *
     * @param experienceToAdd The amount of experience points to add.
     */
    public void addExperience(int experienceToAdd) {
        if (experienceToAdd <= 0) {
            return; // Prevent negative or zero experience addition
        }
        if (currentLevel >= maxLevel) {
            return; // Cannot gain experience at max level
        }

        this.exp += experienceToAdd;

        // Loop in case enough experience is gained for multiple level-ups at once
        while (this.exp >= this.expToLevelUp && this.currentLevel < this.maxLevel) {
            this.currentLevel++;
            this.exp -= this.expToLevelUp;
            this.expToLevelUp *= this.expGrowthRate;
        }
    }
}