package Interface;

import Classes.AI;
import Classes.Calendar;
import Classes.Game;
import Classes.GameRules;
import Classes.League;
import Classes.Positions;
import Classes.Tactic.TacticStyle;
import Classes.TrainingCategory;
import Classes.Training;
import java.util.List;

public interface ISportFactory {
    GameRules createGameRules();
    League createLeague(String name, String country, int numberOfTeams, GameRules rules);
    Calendar createCalendar(GameRules rules);
    Positions createPositions();
    AI createEasyAI(ITeam team);
    List<TacticStyle> getAvailableTacticStyles();
    String validateFormation(List<IPlayer> playersOnPitch);
    void setupInitialPitchPositions(ITactic tactic);
    List<IPlayer> getPlayersInSameZone(IPlayer[][] pitchPlayers, int destX, int destY);
    void launchGame(Game game);
    String getSportName();
    List<String> getDefaultCoachTraitNames();
    List<String> getDefaultPlayerTraitNames();
    Training createTraining(TrainingCategory category);
}