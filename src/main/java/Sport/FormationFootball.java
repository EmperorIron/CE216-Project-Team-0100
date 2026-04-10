package Sport;

import Classes.Formation;
import java.util.Arrays;
import java.util.List;

public class FormationFootball extends Formation {

    public FormationFootball(String formationString) {
        super(formationString);
        
        // Hook up the sports-specific positional multipliers for Expected Goals (xG & xGA)
        PositionsFootball positions = new PositionsFootball();
        positions.getXgMultipliers().forEach(this::setPositionXgMultiplier);
        positions.getXgaMultipliers().forEach(this::setPositionXgaMultiplier);
    }

    @Override
    public List<Integer> getMaxPlayersPerLine() {
        // Line 0: Goalkeeper (Max 1)
        // Line 1: Defenders (Max 6)
        // Line 2: Midfielders / Defensive Mid (Max 6)
        // Line 3: Attackers / Attacking Mid (Max 6)
        // Line 4: Strikers (Max 6) - For 5-line formations like 1-4-2-3-1
        return Arrays.asList(1, 6, 6, 6, 6);
    }

    /**
     * Calculates cohesion to prevent extreme formations like 1-0-0-0-10 or 1-5-0-0-5 in football.
     * Enforces a rule where the difference in player counts between adjacent lines can be at most 1.
     * Applies a 0.9x debuff multiplier for each player missing to bridge the gap.
     */
    @Override
    public float calculateCohesionMultiplier() {
        if (getLines().isEmpty()) return 1.0f;
        
        float multiplier = 1.0f;
        for (int i = 0; i < getLines().size() - 1; i++) {
            int currentLine = getLines().get(i);
            int nextLine = getLines().get(i + 1);
            int diff = Math.abs(currentLine - nextLine);
            
            if (diff > 1) {
                // Apply 0.9x debuff for each extra gap/empty space between adjacent lines
                multiplier *= (float) Math.pow(0.9, diff - 1);
            }
        }
        return multiplier;
    }
}
