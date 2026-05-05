package Sport.Volleyball;

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

public class VolleyballFactory implements ISportFactory {
    
    @Override public GameRules createGameRules() { return new GameRulesVolleyball(); }
    @Override public League createLeague(String name, String country, int num, GameRules rules) { return new LeagueVolleyball(name, country, num, (GameRulesVolleyball) rules); }
    @Override public Calendar createCalendar(GameRules rules) { return new CalendarVolleyball(rules); }
    @Override public Positions createPositions() { return new PositionsVolleyball(); }
    @Override public AI createEasyAI(ITeam team) { return new AIAdaptableEasyVolleyball(team); }
    @Override public List<TacticStyle> getAvailableTacticStyles() { return TacticVolleyball.AVAILABLE_STYLES; }
    @Override public String getSportName() { return "VOLLEYBALL"; }

    @Override 
    public String validateFormation(List<IPlayer> playersOnPitch) {
        int lf = 0, mf = 0, rf = 0, lb = 0, mb = 0, rb = 0;
        for (IPlayer p : playersOnPitch) {
            if (p instanceof Player player) {
                int posId = player.getCurrentPositionId();
                int x = Positions.getX(posId);
                int y = Positions.getY(posId);
                if (x >= 1 && x <= 3 && y >= 7 && y <= 9) lf++;
                else if (x >= 4 && x <= 6 && y >= 7 && y <= 9) mf++;
                else if (x >= 7 && x <= 9 && y >= 7 && y <= 9) rf++;
                else if (x >= 1 && x <= 3 && y >= 1 && y <= 3) lb++;
                else if (x >= 4 && x <= 6 && y >= 1 && y <= 3) mb++;
                else if (x >= 7 && x <= 9 && y >= 1 && y <= 3) rb++;
            }
        }
        if (lf != 1 || mf != 1 || rf != 1 || lb != 1 || mb != 1 || rb != 1) {
            return "Volleyball requires exactly 1 player in each of the 6 court zones (Left/Mid/Right Front and Back).";
        }
        return null;
    }

    @Override 
    public void setupInitialPitchPositions(ITactic tactic) {
        int[] vbSpots = {82, 85, 88, 22, 25, 28};
        int idx = 0;
        for (IPlayer p : tactic.getStartingLineup()) {
            if (p instanceof Player player) {
                if (idx < 6) {
                    player.setCurrentPositionId(vbSpots[idx]);
                    idx++;
                } else {
                    player.setCurrentPositionId(p.getPrimaryPositionId());
                }
            }
        }
    }

    @Override 
    public List<IPlayer> getPlayersInSameZone(IPlayer[][] pitchPlayers, int destX, int destY) {
        List<IPlayer> toRemove = new ArrayList<>();
        int zMinX = -1, zMaxX = -1, zMinY = -1, zMaxY = -1;
        if (destX >= 1 && destX <= 3 && destY >= 7 && destY <= 9) { zMinX = 1; zMaxX = 3; zMinY = 7; zMaxY = 9; }
        else if (destX >= 4 && destX <= 6 && destY >= 7 && destY <= 9) { zMinX = 4; zMaxX = 6; zMinY = 7; zMaxY = 9; }
        else if (destX >= 7 && destX <= 9 && destY >= 7 && destY <= 9) { zMinX = 7; zMaxX = 9; zMinY = 7; zMaxY = 9; }
        else if (destX >= 1 && destX <= 3 && destY >= 1 && destY <= 3) { zMinX = 1; zMaxX = 3; zMinY = 1; zMaxY = 3; }
        else if (destX >= 4 && destX <= 6 && destY >= 1 && destY <= 3) { zMinX = 4; zMaxX = 6; zMinY = 1; zMaxY = 3; }
        else if (destX >= 7 && destX <= 9 && destY >= 1 && destY <= 3) { zMinX = 7; zMaxX = 9; zMinY = 1; zMaxY = 3; }

        if (zMinX != -1) {
            for (int i = zMinX; i <= zMaxX; i++) {
                for (int j = zMinY; j <= zMaxY; j++) {
                    if (i == destX && j == destY) continue;
                    if (pitchPlayers[i][j] != null) toRemove.add(pitchPlayers[i][j]);
                }
            }
        }
        return toRemove;
    }

    @Override 
    public void launchGame(Game game) {
        new GUIGame((GameVolleyball) game);
    }

    @Override
    public List<String> getDefaultCoachTraitNames() {
        return Arrays.asList("Serve Coaching", "Attack Coaching", "Defense Coaching", "Player Management", "Motivation");
    }

    @Override
    public List<String> getDefaultPlayerTraitNames() {
        return Arrays.asList("Serving", "Spiking", "Setting", "Blocking", "Digging", "Speed", "Physical", "Mental");
    }

    @Override
    public Training createTraining(TrainingCategory category) {
        return switch (category) {
            case OFFENSIVE -> new TrainingOffensiveVolleyball();
            case DEFENSIVE -> new TrainingDefensiveVolleyball();
            case PHYSICAL -> new TrainingPhysicalVolleyball();
            case MENTAL -> new TrainingMentalVolleyball();
        };
    }
}