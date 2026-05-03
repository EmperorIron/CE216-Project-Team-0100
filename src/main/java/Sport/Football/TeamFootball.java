package Sport.Football;
import Classes.Team;
import Interface.ICoach;
import Interface.IPlayer;

public class TeamFootball extends Team {

    public TeamFootball(String name, String country, String league) {
        super(name, country, league);
    }


    @Override
    public float getTotalOffensiveRating() {
        return (float) this.getPlayers().stream()
                .filter(p -> p.getPositionProficiency().entrySet().stream()
                        .anyMatch(entry ->
                                (PositionsFootball.isMidfielderPosition(entry.getKey()) || PositionsFootball.isForwardPosition(entry.getKey()))
                                && entry.getValue() > 70
                        )
                )
                .mapToDouble(IPlayer::calculateOverallRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public float getTotalDefensiveRating() {
        return (float) this.getPlayers().stream()
                .filter(p -> p.getPositionProficiency().entrySet().stream()
                        .anyMatch(entry ->
                                (PositionsFootball.isGoalkeeperPosition(entry.getKey()) || PositionsFootball.isDefenderPosition(entry.getKey()) || PositionsFootball.isMidfielderPosition(entry.getKey()))
                                && entry.getValue() > 70
                        )
                )
                .mapToDouble(IPlayer::calculateOverallRating)
                .average()
                .orElse(0.0);
    }
    
  
}