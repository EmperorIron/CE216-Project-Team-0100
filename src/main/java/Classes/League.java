package Classes;

import Interface.ILeague;
import Interface.ITeam; 
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sports league, which contains a collection of teams.
 * This is an abstract base class, designed to be extended by concrete league types
 * for specific sports (e.g., FootballLeague), which will implement their own
 * specific ranking logic.
 */
public abstract class League implements ILeague {
    private final String name;
    private final String country;
    
    /** The list of teams participating in the league. Accessible by subclasses. */
    protected final List<ITeam> teams;

    public League(String name, String country) {
        this.name = name;
        this.country = country;
        this.teams = new ArrayList<>();
    }
    // Setters and Getters
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getCountry() {
        return country;
    }
    @Override
    public List<ITeam> getTeamRanking() {
        return teams;
    }
    
    ///////////////////////////////////////////////////////////////////////////////
      @Override
    public void addTeam(ITeam team) {
        if (team != null && !this.teams.contains(team)) {
            this.teams.add(team);
        }
    }
}