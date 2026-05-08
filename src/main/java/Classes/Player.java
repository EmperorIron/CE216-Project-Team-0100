package Classes;

import java.util.HashMap;
import java.util.Map; 

import Interface.IPlayer;
 
public abstract class Player implements IPlayer {
    private final String name;
    private final String surname;
    private final String country;
    private final String id;
    private String teamName;
    private float xG; // Expected Goals
    private float xGA; // Expected Goals Against
    private final Map<Integer, Integer> positionProficiency;
    private final int primaryPositionId;
    private int currentPositionId;
    private final Map<String, Trait> traits; // Player's skills
    private int injuryDuration; 
    private int jerseyNumber;

    public Player(String name, String surname, String country, String teamName, String id, Map<Integer, Integer> positionProficiency) {
        this.name = name;
        this.surname = surname;
        this.country = country;
        this.teamName = teamName;
        this.id = id;
        this.traits = new HashMap<>();
        this.positionProficiency = positionProficiency;
        this.xG = 0.0f;
        this.xGA = 0.0f;
        this.injuryDuration = 0; 
        this.primaryPositionId = findBestPosition(positionProficiency);
        this.currentPositionId = this.primaryPositionId;
    }

    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void setInjuryDuration(int durationInMatches) {
        this.injuryDuration = durationInMatches;
    }
    @Override
    public float getxG() {
        return this.xG;
    }

    @Override
    public float getxGA() {
        return this.xGA;
    }
    @Override
    public void setxG(float xG) {
        this.xG = xG;
    }
    @Override
    public void setxGA(float xGA) {
        this.xGA = xGA;
    }
    @Override
    public String getId() {
        return id;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSurname() {
        return surname;
    }
    @Override
    public String getCountry() {
        return country;
    }
    @Override
    public String getTeamName() {
        return teamName;
    }
    @Override
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    @Override
    public Map<String, Trait> getTraits() {
        return traits;
    }
    @Override
    public int getInjuryDuration() {
        return injuryDuration;
    }
    ///////////////////////////////////////////////

    @Override
    public Map<Integer, Integer> getPositionProficiency() {
        return positionProficiency;
    }

    @Override
    public int getPrimaryPositionId() {
        return primaryPositionId;
    }

    public int getCurrentPositionId() {
        return currentPositionId;
    }

    public void setCurrentPositionId(int currentPositionId) {
        this.currentPositionId = currentPositionId;
    }

    public int getProficiencyAt(int positionId) {
        return this.positionProficiency.getOrDefault(positionId, 0);
    }

    private int findBestPosition(Map<Integer, Integer> proficiencyMap) {
        if (proficiencyMap == null || proficiencyMap.isEmpty()) {
            return -1; // Indicates no positions are available
        }
        return proficiencyMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }



    @Override
    public boolean isInjured() {
        return this.injuryDuration > 0;
    }

    @Override
    public void decrementInjury() {
        if (injuryDuration > 0) {
            injuryDuration--;
        }
    }

    public String getFullName() {
        return name + " " + surname;
    }

    @Override
    public void addTrait(Trait trait) {
        if (trait != null) {
            this.traits.put(trait.getName(), trait);
        }
    }
    @Override
    public Trait getTrait(String traitName) {
        return this.traits.get(traitName);
    }

    /**
     * Calculates a generic overall rating for the player.
     * This default implementation calculates the average of all trait levels.
     * It is expected that concrete subclasses (like PlayerFootball) will
     * override this method with a more sport-specific calculation.
     * @return The average of all the player's trait levels, or 0 if no traits exist.
     */
    public double calculateOverallRating() {
        if (isInjured()) {
            return 0.0;
        }
        if (traits.isEmpty()) {
            return 0.0;
        }
        double total = traits.values().stream().mapToInt(Trait::getCurrentLevel).sum();
        return total / traits.size();
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof IPlayer)) return false;
        IPlayer other = (IPlayer) obj;
        return this.id != null && this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}