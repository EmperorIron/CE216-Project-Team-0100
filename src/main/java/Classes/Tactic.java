package Classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Interface.IPlayer;
import Interface.ITactic;

public abstract class Tactic implements ITactic {

    private List<IPlayer> startingLineup;
    private List<IPlayer> substitutes;
    private Map<String, String> playerRoles;

    private Formation formation;

    private final Map<String, TacticStyle> styles;

    public Tactic(Formation formation) {
        this.formation = formation;
        this.startingLineup = new ArrayList<>();
        this.substitutes = new ArrayList<>();
        this.playerRoles = new HashMap<>();
        this.styles = new HashMap<>();
    }
    @Override
    public String getFormation() {
        return formation.getFormattedName();
    }
    
    public Formation getFormationObject() {
        return formation;
    }
    
    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    @Override
    public List<IPlayer> getStartingLineup() {
        return startingLineup;
    }

    @Override
    public void setStartingLineup(List<IPlayer> lineup) {
        this.startingLineup = lineup;
    }

    @Override
    public List<IPlayer> getSubstitutes() {
        return substitutes;
    }

    @Override
    public void setSubstitutes(List<IPlayer> substitutes) {
        this.substitutes = substitutes;
    }

    @Override
    public Map<String, String> getPlayerRoles() {
        return playerRoles;
    }
    @Override
    public void setPlayerRole(String playerId, String role) {
        if (playerId != null && role != null) {
            this.playerRoles.put(playerId, role);
        }
    }

    @Override
    public void addStyle(String styleName, float xGMultiplier, float xGAMultiplier) {
        if (styleName == null || styleName.trim().isEmpty()) {
            ErrorHandler.logError("Attempted to add a tactic style with null or empty name.");
            return;
        }
        if (xGMultiplier < 0 || xGAMultiplier < 0) {
            ErrorHandler.logError("Warning: Added tactic style '" + styleName + "' with a negative multiplier.");
        }
        this.styles.put(styleName, new TacticStyle(styleName, xGMultiplier, xGAMultiplier));
    }

    public void clearStyles() {
        this.styles.clear();
    }

    @Override
    public float getTotalXGMultiplier() {
        float totalMultiplier = formation.calculateFormationXgEffect(startingLineup);
        for (TacticStyle effect : styles.values()) {
            totalMultiplier *= effect.xgMult();
        }
        return totalMultiplier;
    }

    @Override
    public float getTotalXGAMultiplier() {
        float totalMultiplier = formation.calculateFormationXgaEffect(startingLineup);
        for (TacticStyle effect : styles.values()) {
            totalMultiplier *= effect.xgaMult();
        }
        return totalMultiplier;
    }

    public record TacticStyle(String name, float xgMult, float xgaMult) { }
}