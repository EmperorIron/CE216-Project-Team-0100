package Sport.Football;

import Classes.*;
import Classes.Calendar;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;
import Interface.IManager;

import java.util.*;


public class CalendarFootball extends Calendar {

    private List<ITeam> teams;

    public CalendarFootball(GameRules rules) {
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

        IManager homeManager = null;
        IManager awayManager = null;
        ITactic homeTactic;
        ITactic awayTactic;

        if (home.isManagerAI()) {
            homeManager = createRandomAIForTeam(home);
        } else {
            homeManager = new HumanManagerFootball(home);
        }
        homeTactic = new TacticFootball("1-4-4-2"); // Temporary tactic to be filled when match starts

        if (away.isManagerAI()) {
            awayManager = createRandomAIForTeam(away);
        } else {
            awayManager = new HumanManagerFootball(away);
        }
        awayTactic = new TacticFootball("1-4-4-2");

        GameFootball game = new GameFootball(home, away, rules, homeTactic, awayTactic);
        if (homeManager != null) game.setHomeManager(homeManager);
        if (awayManager != null) game.setAwayManager(awayManager);
        weeklyGames.add(game);
    }

    private AI createRandomAIForTeam(ITeam team) {
        int randomChoice = new Random().nextInt(3); // 3 types: Adaptable, Attack, Defense
        boolean isHard = new Random().nextBoolean(); // 2 difficulties: Easy, Hard
        return switch (randomChoice) {
            case 0 -> isHard ? new AIAdaptableHardFootball(team) : new AIAdaptableEasyFootball(team);
            case 1 -> isHard ? new AIAttackHardFootball(team) : new AIAttackEasyFootball(team);
            default -> isHard ? new AIDefenseHardFootball(team) : new AIDefenseEasyFootball(team);
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
        Training genericTraining = GameContext.getInstance().getSportFactory().createTraining(TrainingCategory.OFFENSIVE);
        boolean matchPlayedThisWeek = false;

        for (char dayActivity : weeklySchedule.toCharArray()) {
            if (dayActivity == '0') {
                // It's a training day
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