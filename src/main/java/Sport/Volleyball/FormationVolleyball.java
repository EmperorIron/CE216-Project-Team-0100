package Sport.Volleyball;

import Classes.Formation;
import java.util.Arrays;
import java.util.List;



public class FormationVolleyball extends Formation {

    public FormationVolleyball(String formationString) {
        super(formationString);

        PositionsVolleyball positions = new PositionsVolleyball();
        positions.getXgMultipliers().forEach(this::setPositionXgMultiplier);
        positions.getXgaMultipliers().forEach(this::setPositionXgaMultiplier);
    }


    @Override
    public List<Integer> getMaxPlayersPerLine() {
        return Arrays.asList(3, 3);
    }


    @Override
    public float calculateCohesionMultiplier() {
        if (getLines().isEmpty()) return 1.0f;
        // lines[0] = front-row count, lines[1] = back-row count
        if (getLines().size() < 2) return 0.85f;
        int frontRow = getLines().get(0);
        int backRow  = getLines().get(1);
        int diff = Math.abs(frontRow - backRow);
        return diff == 0 ? 1.0f : (float) Math.pow(0.92, diff);
    }
}
