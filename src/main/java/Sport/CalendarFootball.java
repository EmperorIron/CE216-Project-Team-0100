package Sport;

import Classes.*;
import Classes.Calendar;
import Interface.ITactic;
import Interface.ITeam;

import java.util.*;


public class CalendarFootball extends Calendar {

    private List<ITeam> teams;

    public CalendarFootball(GameRules rules) {
        super(rules);
        this.schedule = new HashMap<>();
    }

    @Override
    public void generateFixtures(List<ITeam> teams) {
        this.teams = new ArrayList<>(teams);
        this.schedule.clear();
        this.currentWeek = 0;

        List<ITeam> teamList = new ArrayList<>(this.teams);
        ITeam byeTeam = null;

        if (teamList.size() % 2 != 0) {
            byeTeam = new TeamFootball("BYE", "N/A", "N/A");
            teamList.add(byeTeam);
        }

        int numTeams = teamList.size();
        int numRounds = numTeams - 1;

        for (int round = 0; round < numRounds; round++) {
            List<Game> weeklyGames = new ArrayList<>();
            for (int i = 0; i < numTeams / 2; i++) {
                ITeam home = teamList.get(i);
                ITeam away = teamList.get(numTeams - 1 - i);

                if (round % 2 != 0 && i == 0) {
                    addMatch(weeklyGames, away, home, byeTeam);
                } else {
                    addMatch(weeklyGames, home, away, byeTeam);
                }
            }
            schedule.put(round + 1, weeklyGames);

            ITeam firstTeam = teamList.remove(0);
            teamList.add(teamList.size() - 1, firstTeam);
        }

        if (rules.getMatchbetweenTeams() > 1) {
            for (int i = 1; i < rules.getMatchbetweenTeams(); i++) {
                for (int week = 1; week <= numRounds; week++) {
                    List<Game> originalGames = schedule.get(week);
                    List<Game> returnGames = new ArrayList<>();
                    for (Game game : originalGames) {
                        addMatch(returnGames, game.getAwayTeam(), game.getHomeTeam(), byeTeam);
                    }
                    schedule.put(numRounds * i + week, returnGames);
                }
            }
        }
    }

    private void addMatch(List<Game> weeklyGames, ITeam home, ITeam away, ITeam byeTeam) {
        if (home == byeTeam || away == byeTeam) {
            return;
        }
        ITactic defaultTactic = new TacticFootball("4-4-2");
        weeklyGames.add(new GameFootball(home, away, rules, defaultTactic, defaultTactic));
    }

    @Override
    public void displayWeeklyFixture() {
        if (currentWeek < 1 || currentWeek > schedule.size()) {
            System.out.println("No matches scheduled for week " + currentWeek + ".");
            return;
        }
        System.out.println("--- Week " + currentWeek + " Fixture ---");
        List<Game> games = schedule.get(currentWeek);
        if (games == null || games.isEmpty()) {
            System.out.println("No matches this week.");
            return;
        }
        for (Game game : games) {
            System.out.println(game.getHomeTeam().getName() + " vs " + game.getAwayTeam().getName());
        }
        System.out.println("--------------------");
    }

    @Override
    public void displayFixtureForWeek(int weekNumber) {
        if (weekNumber < 1 || weekNumber > schedule.size()) {
            System.out.println("No matches scheduled for week " + weekNumber + ".");
            return;
        }
        System.out.println("--- Week " + weekNumber + " Fixture ---");
        List<Game> games = schedule.get(weekNumber);
        if (games == null || games.isEmpty()) {
            System.out.println("No matches this week.");
            return;
        }
        for (Game game : games) {
            System.out.println(game.getHomeTeam().getName() + " vs " + game.getAwayTeam().getName());
        }
        System.out.println("--------------------");
    }

    @Override
    public void displayTeamSchedule(ITeam team) {
    }

    @Override
    public void displayLeagueTable() {
        if (this.teams == null || this.teams.isEmpty()) {
            System.out.println("No teams in the league to display.");
            return;
        }

        this.teams.sort(Comparator.comparingInt(ITeam::getPoints).reversed().thenComparingInt(ITeam::getGoalDifference).reversed().thenComparingInt(ITeam::getGoalsScored).reversed());

        System.out.println("\n--- League Table ---");
        System.out.printf("%-4s %-20s %-3s %-3s %-3s %-3s %-3s %-3s %-4s %-3s%n", "Pos", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts");
        System.out.println("---------------------------------------------------------------------");

        int pos = 1;
        for (ITeam team : this.teams) {
            System.out.printf("%-4d %-20s %-3d %-3d %-3d %-3d %-3d %-3d %-4d %-3d%n", pos++, team.getName(), team.getWins() + team.getDraws() + team.getLosses(), team.getWins(), team.getDraws(), team.getLosses(), team.getGoalsScored(), team.getGoalsConceded(), team.getGoalDifference(), team.getPoints());
        }
        System.out.println("---------------------------------------------------------------------");
    }

    @Override
    public void advanceToNextWeek() {
        if (currentWeek >= schedule.size()) {
            System.out.println("Season has already ended.");
            return;
        }

        currentWeek++;
        System.out.println("\n>>> Advancing to Week " + currentWeek + " <<<");

        String weeklySchedule = rules.getTrainingormatch();
        Training genericTraining = new TrainingOffensiveFootball();
        boolean matchPlayedThisWeek = false;

        for (char dayActivity : weeklySchedule.toCharArray()) {
            if (dayActivity == '0') {
                // It's a training day
                System.out.println("Simulating a day of training...");
                for (ITeam team : this.teams) {
                    if (!team.getName().equals("BYE")) {
                        genericTraining.apply(team);
                    }
                }
            } else if (dayActivity == '1' && !matchPlayedThisWeek) {
                System.out.println("Matchday Results:");
                List<Game> gamesThisWeek = schedule.get(currentWeek);
                if (gamesThisWeek != null && !gamesThisWeek.isEmpty()) {
                    for (Game game : gamesThisWeek) {
                        if (!game.isCompleted()) {
                            game.play();
                            System.out.println(" - " + game.getHomeTeam().getName() + " " + game.getHomeScore() + " - " + game.getAwayScore() + " " + game.getAwayTeam().getName());
                        }
                    }
                }
                matchPlayedThisWeek = true; /
            }
        }
        
        displayLeagueTable();
    }
}