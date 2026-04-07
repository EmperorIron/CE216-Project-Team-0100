 package Interface;

import java.util.List;

public interface IGame {
    ITeam getHomeTeam();
    ITeam getAwayTeam();
    int getHomeScore();
    int getAwayScore();
    ITeam getWinner(); // Returns null for a draw
    boolean isCompleted();
    List<String> getGameLog();
    void addLogEntry(String entry);
    void setHomeScore(int homeScore);
    void setAwayScore(int awayScore);
    void setCompleted(boolean completed);
    ITactic getHomeTactic();
    ITactic getAwayTactic();

    /**
     * Simulates the entire game from start to finish.
     * After this method is called, the game is considered completed,
     * scores are final, and team stats are updated.
     */
    void play();
}