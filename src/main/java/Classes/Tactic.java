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

    private final Map<String, StyleEffect> styles;

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
        if (styleName != null && !styleName.trim().isEmpty()) {
            this.styles.put(styleName, new StyleEffect(xGMultiplier, xGAMultiplier));
        }
    }

    public void clearStyles() {
        this.styles.clear();
    }

    @Override
    public float getTotalXGMultiplier() {
        float totalMultiplier = formation.calculateFormationXgEffect(startingLineup);
        for (StyleEffect effect : styles.values()) {
            totalMultiplier *= effect.xGMultiplier;
        }
        return totalMultiplier;
    }

    @Override
    public float getTotalXGAMultiplier() {
        float totalMultiplier = formation.calculateFormationXgaEffect(startingLineup);
        for (StyleEffect effect : styles.values()) {
            totalMultiplier *= effect.xGAMultiplier;
        }
        return totalMultiplier;
    }


    private static class StyleEffect {
        float xGMultiplier;
        float xGAMultiplier;
        StyleEffect(float xGMultiplier, float xGAMultiplier) {
            this.xGMultiplier = xGMultiplier;
            this.xGAMultiplier = xGAMultiplier;
        }
    }
}