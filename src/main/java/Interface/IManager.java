package Interface;

public interface IManager {
    ITeam getTeam();
    
    ITactic generateStartingTactic();
    
    void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber);
}