package Sport.Volleyball;

import Classes.*;
import Interface.IManager;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CalendarVolleyball extends Calendar {

    private List<ITeam> teams;

    public CalendarVolleyball(GameRules rules) {
        super(rules);
        this.schedule = new HashMap<>();
    }

    @Override
    public void generateFixtures(List<ITeam> teams) {
        if (teams == null || teams.isEmpty()) {
            ErrorHandler.logError("Attempted to generate fixtures with a null or empty team list.");
            return;
        }
        this.teams = new ArrayList<>(teams);
        this.schedule.clear();
        this.currentWeek = 0;

        List<ITeam> teamList = new ArrayList<>(this.teams);
        ITeam byeTeam = null;

        if (teamList.size() % 2 != 0) {
            byeTeam = new TeamVolleyball("BYE", "N/A", "N/A");
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

        IManager homeManager;
        IManager awayManager;
        ITactic homeTactic;
        ITactic awayTactic;

        if (home.isManagerAI()) {
            homeManager = createRandomAIForTeam(home);
        } else {
            homeManager = new HumanManagerVolleyball(home);
        }
        homeTactic = new TacticVolleyball("5-1");

        if (away.isManagerAI()) {
            awayManager = createRandomAIForTeam(away);
        } else {
            awayManager = new HumanManagerVolleyball(away);
        }
        awayTactic = new TacticVolleyball("5-1");

        GameVolleyball game = new GameVolleyball(home, away, rules, homeTactic, awayTactic);
        game.setHomeManager(homeManager);
        game.setAwayManager(awayManager);
        weeklyGames.add(game);
    }

    private Classes.AI createRandomAIForTeam(ITeam team) {
        int randomChoice = new java.util.Random().nextInt(3); // 3 types: Adaptable, Attack, Defense
        boolean isHard = new java.util.Random().nextBoolean();
        return switch (randomChoice) {
            case 0 -> isHard ? new AIAdaptableHardVolleyball(team) : new AIAdaptableEasyVolleyball(team);
            case 1 -> isHard ? new AIAttackHardVolleyball(team)    : new AIAttackEasyVolleyball(team);
            default -> isHard ? new AIDefenseHardVolleyball(team)  : new AIDefenseEasyVolleyball(team);
        };
    }

    @Override
    public void displayWeeklyFixture() {
    }

    @Override
    public void displayFixtureForWeek(int weekNumber) {
    }

    @Override
    public void displayTeamSchedule(ITeam team) {
    }

    @Override
    public void displayLeagueTable() {
    }

    @Override
    public void advanceToNextWeek() {
        if (currentWeek >= schedule.size()) {
            return;
        }

        currentWeek++;

        String weeklySchedule = rules.getTrainingormatch();
        Training genericTraining = GameContext.getInstance().getSportFactory().createTraining(TrainingCategory.PHYSICAL);
        boolean matchPlayedThisWeek = false;

        for (char dayActivity : weeklySchedule.toCharArray()) {
            if (dayActivity == '0') {
                for (ITeam team : this.teams) {
                    if (!team.getName().equals("BYE") && team.isManagerAI()) {
                        genericTraining.apply(team);
                    }
                }
            } else if (dayActivity == '1' && !matchPlayedThisWeek) {
                List<Game> gamesThisWeek = schedule.get(currentWeek);
                if (gamesThisWeek != null && !gamesThisWeek.isEmpty()) {
                    for (Game game : gamesThisWeek) {
                        if (!game.isCompleted()) {
                            game.play();
                        }
                    }
                }
                matchPlayedThisWeek = true; 
            }
        }
        
        displayLeagueTable();
    }
}