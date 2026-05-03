package Sport.Volleyball;

import Classes.Team;
import Interface.IPlayer;


public class TeamVolleyball extends Team {

    public TeamVolleyball(String name, String country, String league) {
        super(name, country, league);
    }

    @Override
    public float getTotalOffensiveRating() {
        return (float) getPlayers().stream()
                .filter(p -> PositionsVolleyball.isFrontRowPosition(p.getPrimaryPositionId()))
                .mapToDouble(IPlayer::calculateOverallRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public float getTotalDefensiveRating() {
        return (float) getPlayers().stream()
                .filter(p -> PositionsVolleyball.isBackRowPosition(p.getPrimaryPositionId())
                          || PositionsVolleyball.isLiberoPosition(p.getPrimaryPositionId()))
                .mapToDouble(IPlayer::calculateOverallRating)
                .average()
                .orElse(0.0);
    }
}
