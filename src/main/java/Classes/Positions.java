
package Classes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public abstract class Positions {

    public static final int GRID_WIDTH = 10;
    public static final int GRID_HEIGHT = 10;
    public static final int TOTAL_POSITIONS = GRID_WIDTH * GRID_HEIGHT;
    protected static final Random rand = new Random();


    public abstract Set<Integer> getValidPositions();


    public abstract Map<Integer, Float> getXgMultipliers();


    public abstract Map<Integer, Float> getXgaMultipliers();


    public static Map<Integer, Integer> generateProficiencyMap(int primaryPositionId, int primaryProficiency, double falloffRate) {
        Map<Integer, Integer> proficiencyMap = new HashMap<>();
        int primaryX = getX(primaryPositionId);
        int primaryY = getY(primaryPositionId);

        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            int currentX = getX(i);
            int currentY = getY(i);

            double distance = Math.sqrt(Math.pow(primaryX - currentX, 2) + Math.pow(primaryY - currentY, 2));

            int proficiency = (int) Math.round(primaryProficiency - (distance * falloffRate));

            proficiency += rand.nextInt(11) - 5;

            proficiency = Math.max(0, Math.min(100, proficiency));

            proficiencyMap.put(i, proficiency);
        }
        
        int finalPrimaryProficiency = Math.max(0, Math.min(100, primaryProficiency));
        
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            if (i != primaryPositionId) {
                int currentVal = proficiencyMap.get(i);
                if (currentVal >= finalPrimaryProficiency) {
                    proficiencyMap.put(i, Math.max(0, finalPrimaryProficiency - 1));
                }
            }
        }
        
        proficiencyMap.put(primaryPositionId, finalPrimaryProficiency);

        return proficiencyMap;
    }


    public static int getPositionId(int x, int y) {
        if (x < 0 || x >= GRID_WIDTH || y < 0 || y >= GRID_HEIGHT) {
            throw new IllegalArgumentException("Coordinates (" + x + "," + y + ") are out of bounds.");
        }
        return y * GRID_WIDTH + x;
    }

    public static int getX(int positionId) {
        if (positionId < 0 || positionId >= TOTAL_POSITIONS) {
            throw new IllegalArgumentException("Position ID " + positionId + " is out of bounds.");
        }
        return positionId % GRID_WIDTH;
    }

    public static int getY(int positionId) {
        if (positionId < 0 || positionId >= TOTAL_POSITIONS) {
            throw new IllegalArgumentException("Position ID " + positionId + " is out of bounds.");
        }
        return positionId / GRID_WIDTH;
    }
}
