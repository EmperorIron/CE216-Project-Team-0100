package Classes;

import java.util.ArrayList;
import java.util.List;

import Interface.ICoach;
import Interface.IPlayer;
import Interface.ITeam;

public abstract class Team implements ITeam {
    private final String name;
    private final String country;
    private final String league;
    private int points;
    private int wins;
    private int draws;
    private int losses;
    private boolean manager; //player or ai
    private int goalsScored;
    private int goalsConceded;
    private final List<IPlayer> players;
    private final List<ICoach> coaches; //trainers they effect training class.

    public Team(String name, String country, String league) {
        this.name = name;
        this.country = country;
        this.league = league;
        this.players = new ArrayList<>();
        this.coaches = new ArrayList<>();
        this.points = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.goalsScored = 0;
        this.goalsConceded = 0;
        this.manager = true; // Default to AI controlled
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
    public String getLeague() {
        return league;
    }
   

  
    @Override
    public boolean isManagerAI() {
        return manager;
    }
       @Override
    public void setManagerAI(boolean isAI) {
        this.manager = isAI;
    }

    @Override
    public List<ICoach> getCoaches() {
        return coaches;
    }

    @Override
    public int getPoints() {
        return points;
    }
    @Override
    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public int getWins() {
        return wins;
    }
     @Override
    public void setWins(int wins) {
        this.wins = wins;
    }

    @Override
    public int getDraws() {
        return draws;
    }
    @Override
    public void setDraws(int draws) {
        this.draws = draws;
    }

    @Override
    public int getLosses() {
        return losses;
    }
    @Override
    public void setLosses(int losses) {
        this.losses = losses;
    }

    @Override
    public int getGoalsScored() {
        return goalsScored;
    }
    @Override
    public void setGoalsScored(int goalsScored) {
        this.goalsScored = goalsScored;
    }

    @Override
    public int getGoalsConceded() {
        return goalsConceded;
    }

    @Override
    public void setGoalsConceded(int goalsConceded) {
        this.goalsConceded = goalsConceded;
    }

    @Override
    public int getGoalDifference() {
        return goalsScored - goalsConceded;
    }

    @Override
    public List<IPlayer> getPlayers() {
        return players;
    }
    @Override
    public void addPlayer(IPlayer player) {
        this.players.add(player);
    }

        @Override
    public void addCoach(ICoach coach) {
        this.coaches.add(coach);
    }
     
}
