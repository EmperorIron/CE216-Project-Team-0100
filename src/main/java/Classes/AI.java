package Classes;

import Interface.IGame;
import Interface.ITactic;
import Interface.ITeam;
import Interface.IManager;

/**
 * Abstract base class representing an AI manager for a sports team.
 * Handles forming lineups, selecting tactics, and making tactical adjustments
 * or substitutions during the game's breaks.
 */
public abstract class AI implements IManager {
    
    protected final ITeam team;

    public AI(ITeam team) {
        this.team = team;
    }

    public ITeam getTeam() {
        return team;
    }

    /**
     * Selects and generates the initial tactic, choosing an optimal formation 
     * based on available players, picking the starting lineup, and placing the rest on the bench.
     * 
     * @return The initial tactic for the game.
     */
    public abstract ITactic generateStartingTactic();

    /**
     * Called by the game engine during a break (e.g., half-time or between periods).
     * Analyzes the current game state and executes tactical adjustments, substitutions, or formation changes.
     * 
     * @param game The current match being played.
     * @param currentTactic The active tactic for the AI's team.
     * @param periodNumber The period that just ended.
     */
    public abstract void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber);

    /**
     * Helper method to perform player substitutions based on factors like fatigue, injuries, or underperformance.
     */
    protected abstract void makeSubstitutions(IGame game, ITactic currentTactic);

    /**
     * Helper method to adapt the team's tactical style or formation mid-game depending on whether 
     * the team is winning, losing, or drawing.
     */
    protected abstract void adaptTactics(IGame game, ITactic currentTactic);
}
