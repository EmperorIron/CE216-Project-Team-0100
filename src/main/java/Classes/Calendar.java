package Classes;

import Interface.ITeam;
import java.util.List;
import java.util.Map;

/**
 * Represents the league calendar and schedule for a sports league.
 * This abstract class defines the core functionalities for managing a sports league season,
 * including fixture generation, displaying schedules, and progressing through the season week by week.
 * A concrete class will implement the specific logic for fixture creation and simulation.
 */
public abstract class Calendar {
    protected Map<Integer, List<Game>> schedule;//A map to hold the entire season's schedule.
    protected int currentWeek;
    protected final GameRules rules;

    protected Calendar(GameRules rules) {
        if (rules == null) {
            ErrorHandler.logError("GameRules cannot be null for Calendar.");
        }
        this.rules = rules;
        this.currentWeek = 0;
    }
    public int getCurrentWeek() {
        return currentWeek;
    }
    public abstract void generateFixtures(List<ITeam> teams);
    public abstract void displayWeeklyFixture();
    public abstract void displayFixtureForWeek(int weekNumber);
    public abstract void displayTeamSchedule(ITeam team);
    public abstract void displayLeagueTable();
    public abstract void advanceToNextWeek();
    public Map<Integer, List<Game>> getSchedule() {
        return schedule;
    }
}
