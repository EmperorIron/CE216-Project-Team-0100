package Classes;

import Interface.IPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Formation {
    
    private final String formationString;
    protected final List<Integer> lines;
    private final Map<Integer, Float> positionXgMultipliers;
    private final Map<Integer, Float> positionXgaMultipliers;

    public Formation(String formationString) {
        this.formationString = formationString != null ? formationString : "";
        this.lines = new ArrayList<>();
        this.positionXgMultipliers = new HashMap<>();
        this.positionXgaMultipliers = new HashMap<>();
        parseFormation(this.formationString);
    }

    private void parseFormation(String formation) {
        if (formation == null || formation.trim().isEmpty()) {
            ErrorHandler.logError("Attempted to parse a null or empty formation string.");
            return;
        }

        String[] parts = formation.split("-");
        for (String part : parts) {
            try {
                lines.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // Safely ignore invalid numeric parts
            }
        }
    }

    public String getFormationString() {
        return formationString;
    }

    /**
     * Returns the formation formatted by grouping the first two lines,
     * separated by a space from the rest of the lines.
     * Example: "1-4-2-3-1" becomes "1-4 2-3-1".
     */
    public String getFormattedName() {
        if (lines.isEmpty()) return formationString;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i));
            if (i < lines.size() - 1) {
                sb.append(i == 1 ? " " : "-");
            }
        }
        return sb.toString();
    }

    public List<Integer> getLines() {
        return lines;
    }

    public Map<Integer, Float> getPositionXgMultipliers() {
        return positionXgMultipliers;
    }

    public Map<Integer, Float> getPositionXgaMultipliers() {
        return positionXgaMultipliers;
    }

    public void setPositionXgMultiplier(int positionId, float multiplier) {
        this.positionXgMultipliers.put(positionId, multiplier);
    }

    public void setPositionXgaMultiplier(int positionId, float multiplier) {
        this.positionXgaMultipliers.put(positionId, multiplier);
    }

    /**
     * Returns the maximum number of players allowed in each respective line.
     * For example, index 0 might be Goalkeepers, index 1 Defenders, etc.
     */
    public abstract List<Integer> getMaxPlayersPerLine();

    /**
     * Checks if the formation adheres to the maximum player limits per line.
     */
    public boolean isValid() {
        List<Integer> maxPlayers = getMaxPlayersPerLine();
        for (int i = 0; i < lines.size(); i++) {
            int maxAllowed = (i < maxPlayers.size()) ? maxPlayers.get(i) : Integer.MAX_VALUE;
            if (lines.get(i) < 0 || lines.get(i) > maxAllowed) {
                return false;
            }
        }
        return true;
    }

    /**
     * Abstract method that calculates cohesion to prevent extreme formations.
     * Implemented per sport to enforce sport-specific line spacing limits and debuffs.
     */
    public abstract float calculateCohesionMultiplier();

    /**
     * Calculates the overall formation effect on xG based on position multipliers and cohesion.
     */
    public float calculateFormationXgEffect(List<IPlayer> lineup) {
        float totalXgMultiplier = 0.0f;
        int count = 0;

        for (IPlayer player : lineup) {
            int posId = player.getPrimaryPositionId();
            if (player instanceof Player) {
                posId = ((Player) player).getCurrentPositionId();
            }
            totalXgMultiplier += positionXgMultipliers.getOrDefault(posId, 1.0f);
            count++;
        }

        float averageXgMultiplier = (count > 0) ? (totalXgMultiplier / count) : 1.0f;
        return averageXgMultiplier * calculateCohesionMultiplier();
    }

    /**
     * Calculates the overall formation effect on xGA based on position multipliers and cohesion.
     */
    public float calculateFormationXgaEffect(List<IPlayer> lineup) {
        float totalXgaMultiplier = 0.0f;
        int count = 0;

        for (IPlayer player : lineup) {
            int posId = player.getPrimaryPositionId();
            if (player instanceof Player) {
                posId = ((Player) player).getCurrentPositionId();
            }
            totalXgaMultiplier += positionXgaMultipliers.getOrDefault(posId, 1.0f);
            count++;
        }

        float averageXgaMultiplier = (count > 0) ? (totalXgaMultiplier / count) : 1.0f;
        
        // For Expected Goals Against (xGA), lower is better.
        // Poor cohesion increases vulnerability, so we divide by the (<= 1.0) cohesion multiplier.
        float cohesion = calculateCohesionMultiplier();
        return cohesion > 0 ? (averageXgaMultiplier / cohesion) : averageXgaMultiplier;
    }
}
