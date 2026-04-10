package Sport;

import Classes.Tactic;
import java.util.Arrays;
import java.util.List;

public class TacticFootball extends Tactic {

    public static final String BALANCED = "Balanced";
    public static final String ALL_OUT_ATTACK = "All Out Attack";
    public static final String PARK_THE_BUS = "Park the Bus";

    public static final List<String> ATTACKING_FORMATIONS = Arrays.asList("1-4-3-3", "1-3-4-3", "1-4-2-4", "1-4-2-3-1");
    public static final List<String> BALANCED_FORMATIONS = Arrays.asList("1-4-4-2", "1-4-2-3-1", "1-3-5-2");
    public static final List<String> DEFENSIVE_FORMATIONS = Arrays.asList("1-5-3-2", "1-5-4-1", "1-4-5-1", "1-4-4-2");

    public TacticFootball(String formation) {
        super(new FormationFootball(formation));
        this.applyTacticStyle(BALANCED);
    }

    public void applyTacticStyle(String style) {
        this.clearStyles();
        switch (style) {
            case ALL_OUT_ATTACK:
                this.addStyle(ALL_OUT_ATTACK, 1.25f, 1.15f);
                break;
            case PARK_THE_BUS:
                this.addStyle(PARK_THE_BUS, 0.85f, 0.75f);
                break;
            case BALANCED:
            default:
                this.addStyle(BALANCED, 1.0f, 1.0f);
                break;
        }
    }
}