package Sport.Volleyball;

import Classes.Positions;
import Interface.ITactic;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class PositionsVolleyball extends Positions {

    // --- FRONT ROW (y: 7-9) ---
    private static final Set<Integer> LF_POSITIONS = defineZone(1, 3, 7, 9);   // Left Front (Outside Hitter)
    private static final Set<Integer> MF_POSITIONS = defineZone(4, 6, 7, 9);   // Middle Front (Middle Blocker)
    private static final Set<Integer> RF_POSITIONS = defineZone(7, 9, 7, 9);   // Right Front (Opposite / Setter)

    // --- BACK ROW (y: 1-3) ---
    private static final Set<Integer> LB_POSITIONS = defineZone(1, 3, 1, 3);   // Left Back (Outside Hitter back)
    private static final Set<Integer> MB_POSITIONS = defineZone(4, 6, 1, 3);   // Middle Back (Libero zone)
    private static final Set<Integer> RB_POSITIONS = defineZone(7, 9, 1, 3);   // Right Back (Opposite back)

    private static final Set<Integer> VALID_POSITIONS;
    private static final Map<Integer, Float> XG_MULTIPLIERS;
    private static final Map<Integer, Float> XGA_MULTIPLIERS;

    static {
        VALID_POSITIONS = new HashSet<>();
        VALID_POSITIONS.addAll(LF_POSITIONS);
        VALID_POSITIONS.addAll(MF_POSITIONS);
        VALID_POSITIONS.addAll(RF_POSITIONS);
        VALID_POSITIONS.addAll(LB_POSITIONS);
        VALID_POSITIONS.addAll(MB_POSITIONS);
        VALID_POSITIONS.addAll(RB_POSITIONS);

        XG_MULTIPLIERS  = createXgMap();
        XGA_MULTIPLIERS = createXgaMap();
    }

    @Override
    public Set<Integer> getValidPositions() {
        return VALID_POSITIONS;
    }

    @Override
    public Map<Integer, Float> getXgMultipliers() {
        return XG_MULTIPLIERS;
    }

    @Override
    public Map<Integer, Float> getXgaMultipliers() {
        return XGA_MULTIPLIERS;
    }

    private static Set<Integer> defineZone(int minX, int maxX, int minY, int maxY) {
        return IntStream.range(0, TOTAL_POSITIONS)
                .filter(id -> getX(id) >= minX && getX(id) <= maxX
                           && getY(id) >= minY && getY(id) <= maxY)
                .boxed()
                .collect(Collectors.toSet());
    }

    /** Front-row positions score more points (attack). */
    private static Map<Integer, Float> createXgMap() {
        Map<Integer, Float> map = new HashMap<>();
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            float multiplier = 0.5f + (getY(i) / (float)(GRID_HEIGHT - 1));
            map.put(i, multiplier);
        }
        return map;
    }

    private static Map<Integer, Float> createXgaMap() {
        Map<Integer, Float> map = new HashMap<>();
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            float multiplier = 1.5f - (getY(i) / (float)(GRID_HEIGHT - 1));
            map.put(i, multiplier);
        }
        return map;
    }


    public static boolean isFrontRowPosition(int posId) {
        return LF_POSITIONS.contains(posId) || MF_POSITIONS.contains(posId) || RF_POSITIONS.contains(posId);
    }

    public static boolean isBackRowPosition(int posId) {
        return LB_POSITIONS.contains(posId) || MB_POSITIONS.contains(posId) || RB_POSITIONS.contains(posId);
    }

    public static boolean isLiberoPosition(int posId) {
        return MB_POSITIONS.contains(posId);
    }

    public static int getRandomPositionForRole(String role) {
        List<Integer> positionList;
        switch (role.toUpperCase()) {
            case "LF": case "OUTSIDE_HITTER": positionList = new ArrayList<>(LF_POSITIONS); break;
            case "MF": case "MIDDLE_BLOCKER": positionList = new ArrayList<>(MF_POSITIONS); break;
            case "RF": case "OPPOSITE":       positionList = new ArrayList<>(RF_POSITIONS); break;
            case "LB":                        positionList = new ArrayList<>(LB_POSITIONS); break;
            case "MB": case "LIBERO":         positionList = new ArrayList<>(MB_POSITIONS); break;
            case "RB": case "SETTER":         positionList = new ArrayList<>(RB_POSITIONS); break;
            default:   positionList = new ArrayList<>(VALID_POSITIONS); break;
        }
        if (positionList.isEmpty()) positionList = new ArrayList<>(VALID_POSITIONS);
        return positionList.get(rand.nextInt(positionList.size()));
    }


    public static void resolvePositionCollisions(ITactic tactic) {
        Set<Integer> occupied = new HashSet<>();
        PositionsVolleyball posInfo = new PositionsVolleyball();
        Set<Integer> validPositions = posInfo.getValidPositions();

        for (Interface.IPlayer p : tactic.getStartingLineup()) {
            if (p instanceof Classes.Player player) {
                int pos = player.getCurrentPositionId();

                if (occupied.contains(pos) || !validPositions.contains(pos)) {
                    int bestPos = -1;
                    double minDistance = Double.MAX_VALUE;
                    int px = pos % 10, py = pos / 10;

                    for (int i = 0; i < TOTAL_POSITIONS; i++) {
                        if (!occupied.contains(i) && validPositions.contains(i)) {
                            int cx = i % 10, cy = i / 10;
                            double dist = Math.sqrt(Math.pow(px - cx, 2) + Math.pow(py - cy, 2));
                            if (dist < minDistance) { minDistance = dist; bestPos = i; }
                        }
                    }
                    if (bestPos != -1) {
                        pos = bestPos;
                        player.setCurrentPositionId(pos);
                    }
                }
                occupied.add(pos);
            }
        }
    }
}
