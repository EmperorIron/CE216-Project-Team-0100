package Sport.Football;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import Classes.Positions;

public class PositionsFootball extends Positions {
    private static final Set<Integer> VALID_POSITIONS = definePitch();
    private static final Set<Integer> GK_POSITIONS = defineZone(4, 4, 0, 0); // Goalkeeper: Only 1 position
    
    // --- DEFENSE (Y: between 1-3) ---
    private static final Set<Integer> LB_POSITIONS = defineZone(1, 2, 1, 3); // Left Back
    private static final Set<Integer> CB_POSITIONS = defineZone(3, 6, 1, 3); // // Center Back (Central Defender)
    private static final Set<Integer> RB_POSITIONS = defineZone(7, 8, 1, 3); // Right Back

    // --- MIDFIELD (Y: between 4-6) ---
    private static final Set<Integer> CDM_POSITIONS = defineZone(3, 6, 4, 4); // Defensive Midfielder
    private static final Set<Integer> CM_POSITIONS  = defineZone(3, 6, 5, 5); // Central Midfielder
    private static final Set<Integer> CAM_POSITIONS = defineZone(3, 6, 6, 6); // Attacking Midfielder (Number 10)
    private static final Set<Integer> LM_POSITIONS  = defineZone(1, 2, 4, 6); // Left Midfielder
    private static final Set<Integer> RM_POSITIONS  = defineZone(7, 8, 4, 6); // Right Midfielder

    // --- ATTACK (Y: between 7-9) ---
    private static final Set<Integer> LW_POSITIONS = defineZone(1, 2, 7, 9); // Left Winger
    private static final Set<Integer> ST_POSITIONS = defineZone(3, 6, 7, 9); // Striker / Forward
    private static final Set<Integer> RW_POSITIONS = defineZone(7, 8, 7, 9); // Right Winger

    private static final Map<Integer, Float> XG_MULTIPLIERS = createXgMap();
    private static final Map<Integer, Float> XGA_MULTIPLIERS = createXgaMap();

    @Override
    public Set<Integer> getValidPositions() {
        return VALID_POSITIONS;
    }

    private static Set<Integer> definePitch() {
        Set<Integer> pitch = new HashSet<>();
        for (int y = 1; y < GRID_HEIGHT; y++) {
            for (int x = 1; x < GRID_WIDTH - 1; x++) {
                pitch.add(getPositionId(x, y));
            }
        }
        pitch.add(getPositionId(4, 0)); // Only the exact center Goalkeeper spot is valid
        return pitch;
    }

    
    private static Set<Integer> defineZone(int minX, int maxX, int minY, int maxY) {
        return IntStream.range(0, TOTAL_POSITIONS)
                .filter(id -> getX(id) >= minX && getX(id) <= maxX && getY(id) >= minY && getY(id) <= maxY)
                .boxed().collect(Collectors.toSet());
    }

    public static int getRandomPositionForRole(String role) {
        List<Integer> positionList;
        switch (role.toUpperCase()) {
            case "GK":  positionList = new ArrayList<>(GK_POSITIONS); break;
            case "LB":  positionList = new ArrayList<>(LB_POSITIONS); break;
            case "CB":  positionList = new ArrayList<>(CB_POSITIONS); break;
            case "RB":  positionList = new ArrayList<>(RB_POSITIONS); break;
            case "CDM": positionList = new ArrayList<>(CDM_POSITIONS); break;
            case "CM":  positionList = new ArrayList<>(CM_POSITIONS); break;
            case "CAM": positionList = new ArrayList<>(CAM_POSITIONS); break;
            case "LM":  positionList = new ArrayList<>(LM_POSITIONS); break;
            case "RM":  positionList = new ArrayList<>(RM_POSITIONS); break;
            case "LW":  positionList = new ArrayList<>(LW_POSITIONS); break;
            case "ST":  positionList = new ArrayList<>(ST_POSITIONS); break;
            case "RW":  positionList = new ArrayList<>(RW_POSITIONS); break;
            
            case "FORWARD": positionList = new ArrayList<>(ST_POSITIONS); break;
            case "DEFENDER": positionList = new ArrayList<>(CB_POSITIONS); break;
            case "MIDFIELDER": positionList = new ArrayList<>(CM_POSITIONS); break;
            case "GOALKEEPER": positionList = new ArrayList<>(GK_POSITIONS); break;
            
            default:
                positionList = new ArrayList<>(VALID_POSITIONS);
                break;
        }
        
        if (positionList.isEmpty()) {
            positionList = new ArrayList<>(VALID_POSITIONS);
        }
        return positionList.get(rand.nextInt(positionList.size()));
    }

    public static boolean isGoalkeeperPosition(int positionId) { return GK_POSITIONS.contains(positionId); }
    public static boolean isDefenderPosition(int positionId) { 
        return LB_POSITIONS.contains(positionId) || CB_POSITIONS.contains(positionId) || RB_POSITIONS.contains(positionId); 
    }
    public static boolean isMidfielderPosition(int positionId) { 
        return CDM_POSITIONS.contains(positionId) || CM_POSITIONS.contains(positionId) || CAM_POSITIONS.contains(positionId) || LM_POSITIONS.contains(positionId) || RM_POSITIONS.contains(positionId); 
    }
    public static boolean isForwardPosition(int positionId) { 
        return LW_POSITIONS.contains(positionId) || ST_POSITIONS.contains(positionId) || RW_POSITIONS.contains(positionId); 
    }

    @Override
    public Map<Integer, Float> getXgMultipliers() {
        return XG_MULTIPLIERS;
    }

    @Override
    public Map<Integer, Float> getXgaMultipliers() {
        return XGA_MULTIPLIERS;
    }

    private static Map<Integer, Float> createXgMap() {
        Map<Integer, Float> map = new HashMap<>();
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            float multiplier = 0.5f + (getY(i) / (float) (GRID_HEIGHT - 1));
            map.put(i, multiplier);
        }
        return map;
    }

    private static Map<Integer, Float> createXgaMap() {
        Map<Integer, Float> map = new HashMap<>();
        for (int i = 0; i < TOTAL_POSITIONS; i++) {
            float multiplier = 1.5f - (getY(i) / (float) (GRID_HEIGHT - 1));
            map.put(i, multiplier);
        }
        return map;
    }

    public static void resolvePositionCollisions(Interface.ITactic tactic) {
        if (tactic == null || tactic.getStartingLineup() == null) {
            Classes.ErrorHandler.logError("Attempted to resolve position collisions for a null tactic or lineup.");
            return;
        }
        java.util.Set<Integer> occupied = new java.util.HashSet<>();
        PositionsFootball posInfo = new PositionsFootball();
        java.util.Set<Integer> validPositions = posInfo.getValidPositions();

        for (Interface.IPlayer p : tactic.getStartingLineup()) {
            if (p instanceof Classes.Player) {
                Classes.Player player = (Classes.Player) p;
                int pos = player.getCurrentPositionId();
                
                if (occupied.contains(pos) || !validPositions.contains(pos)) {
                    int bestPos = pos;
                    double minDistance = Double.MAX_VALUE;
                    int px = pos % 10;
                    int py = pos / 10;
                    boolean isGkPos = isGoalkeeperPosition(pos);
                    
                    for (int i = 0; i < 100; i++) {
                        if (!occupied.contains(i) && validPositions.contains(i)) {
                            boolean checkIsGk = isGoalkeeperPosition(i);
                            if (isGkPos == checkIsGk) {
                                int cx = i % 10;
                                int cy = i / 10;
                                double dist = Math.sqrt(Math.pow(px - cx, 2) + Math.pow(py - cy, 2));
                                if (dist < minDistance) {
                                    minDistance = dist;
                                    bestPos = i;
                                }
                            }
                        }
                    }
                    if (bestPos == pos) {
                        for (int i = 0; i < 100; i++) {
                            if (!occupied.contains(i) && validPositions.contains(i)) {
                                int cx = i % 10;
                                int cy = i / 10;
                                double dist = Math.sqrt(Math.pow(px - cx, 2) + Math.pow(py - cy, 2));
                                if (dist < minDistance) {
                                    minDistance = dist;
                                    bestPos = i;
                                }
                            }
                        }
                    }
                    pos = bestPos;
                    player.setCurrentPositionId(pos);
                }
                occupied.add(pos);
            }
        }
    }
}