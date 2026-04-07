package Interface;

import java.util.List;
import java.util.Map;


public interface ITactic {
    String getFormation();
    List<IPlayer> getStartingLineup();
    void setStartingLineup(List<IPlayer> lineup);
    List<IPlayer> getSubstitutes();
    void setSubstitutes(List<IPlayer> substitutes);
    Map<String, String> getPlayerRoles();
    void setPlayerRole(String playerId, String role);
    void addStyle(String styleName, float xGMultiplier, float xGAMultiplier);
    float getTotalXGMultiplier();
    float getTotalXGAMultiplier();
}