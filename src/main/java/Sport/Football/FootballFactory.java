package Sport.Football;

import Classes.AI;
import Classes.Calendar;
import Classes.Game;
import Classes.GameRules;
import Classes.League;
import Classes.Player;
import Classes.Positions;
import Classes.Tactic.TacticStyle;
import Classes.Training;
import Classes.TrainingCategory;
import Interface.IPlayer;
import Interface.ISportFactory;
import Interface.ITactic;
import Interface.ITeam;
import gui.GUIGame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FootballFactory implements ISportFactory {
    
    @Override public GameRules createGameRules() { return new GameRulesFootball(); }
    @Override public League createLeague(String name, String country, int num, GameRules rules) { return new LeagueFootball(name, country, num, (GameRulesFootball) rules); }
    @Override public Calendar createCalendar(GameRules rules) { return new CalendarFootball(rules); }
    @Override public Positions createPositions() { return new PositionsFootball(); }
    @Override public AI createEasyAI(ITeam team) { return new AIAdaptableEasyFootball(team); }
    @Override public List<TacticStyle> getAvailableTacticStyles() { return TacticFootball.AVAILABLE_STYLES; }
    @Override public String getSportName() { return "FOOTBALL"; }

    @Override 
    public String validateFormation(List<IPlayer> playersOnPitch) {
        int gkCount = 0;
        for (IPlayer p : playersOnPitch) {
            if (p instanceof Player player) {
                if (PositionsFootball.isGoalkeeperPosition(player.getCurrentPositionId())) {
                    gkCount++;
                }
            }
        }
        if (gkCount != 1) return "Football requires exactly 1 Goalkeeper on the pitch.";
        return null;
    }

    @Override 
    public void setupInitialPitchPositions(ITactic tactic) {
        for (IPlayer p : tactic.getStartingLineup()) {
            if (p instanceof Player player) {
                player.setCurrentPositionId(p.getPrimaryPositionId());
            }
        }
        PositionsFootball.resolvePositionCollisions(tactic);
    }

    @Override 
    public List<IPlayer> getPlayersInSameZone(IPlayer[][] pitchPlayers, int destX, int destY) {
        List<IPlayer> toRemove = new ArrayList<>();
        int targetPosId = Positions.getPositionId(destX, destY);
        if (PositionsFootball.isGoalkeeperPosition(targetPosId)) {
            for (int i = 0; i < Positions.GRID_WIDTH; i++) {
                for (int j = 0; j < Positions.GRID_HEIGHT; j++) {
                    if (i == destX && j == destY) continue;
                    IPlayer p = pitchPlayers[i][j];
                    if (p != null && PositionsFootball.isGoalkeeperPosition(Positions.getPositionId(i, j))) {
                        toRemove.add(p);
                    }
                }
            }
        }
        return toRemove;
    }

    @Override 
    public void launchGame(Game game) {
        new GUIGame((GameFootball) game);
    }

    @Override
    public List<String> getDefaultCoachTraitNames() {
        return Arrays.asList("Offensive Coaching", "Defensive Coaching", "Player Management", "Motivation", "Youth Development");
    }

    @Override
    public List<String> getDefaultPlayerTraitNames() {
        return Arrays.asList("Defense", "Passing", "Shooting", "Speed", "Physical", "Mental", "Goalkeeping");
    }

    @Override
    public Training createTraining(TrainingCategory category) {
        return switch (category) {
            case OFFENSIVE -> new TrainingOffensiveFootball();
            case DEFENSIVE -> new TrainingDefensiveFootball();
            case PHYSICAL -> new TrainingPhysicalFootball();
            case MENTAL -> new TrainingMentalFootball();
        };
    }
}