package Interface;

import java.util.List;

public interface ITeam {
    String getName();
    String getCountry();
    String getLeague();
    
    List<ICoach> getCoaches();
    
    float getTotalOffensiveRating(); 
    float getTotalDefensiveRating();

    List<IPlayer> getPlayers();
   

    int getPoints();
    int getWins();
    int getDraws();
    int getLosses();
    int getGoalsScored();   
    int getGoalsConceded();  
    int getGoalDifference(); 
	void setGoalsConceded(int goalsConceded);
    void setGoalsScored(int goalsScored);
    void setLosses(int losses);
    void setDraws(int draws);
    void setWins(int wins);
    void setPoints(int points);
    boolean isManagerAI();
    void setManagerAI(boolean isAI);
    void addCoach(ICoach coach);
    void addPlayer(IPlayer player);
}