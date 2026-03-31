package Interface;

import java.util.Map; 

import Classes.Trait;

public interface IPlayer {
    String getId();
    String getName();
    String getSurname();
    String getFullName(); 
    String getCountry();
    String getTeamName();
    void setTeamName(String teamName);
    float getxG();
    float getxGA();
    void setxG(float xG);
    void setxGA(float xGA);
    Map<String, Trait> getTraits();
    Trait getTrait(String traitName);
    void addTrait(Trait trait);
    
    boolean isInjured();
    int getInjuryDuration();
    void setInjuryDuration(int durationInMatches);
    void decrementInjury();
    double calculateOverallRating();

    Map<Integer, Integer> getPositionProficiency();
    int getPrimaryPositionId();
    int getProficiencyAt(int posId);
   
}