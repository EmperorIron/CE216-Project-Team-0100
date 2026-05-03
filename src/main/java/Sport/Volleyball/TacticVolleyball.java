package Sport.Volleyball;

import Classes.Tactic;
import java.util.Arrays;
import java.util.List;


public class TacticVolleyball extends Tactic {

    public static final List<Tactic.TacticStyle> AVAILABLE_STYLES = Arrays.asList(
        new Tactic.TacticStyle("Serve and Receive", 1.0f, 1.0f),
        new Tactic.TacticStyle("Power Attack", 1.30f, 1.10f),
        new Tactic.TacticStyle("Defensive Wall", 0.80f, 0.70f),
        new Tactic.TacticStyle("Fast Tempo", 1.40f, 1.35f),        // Rapid setting, high attack but high error rate
        new Tactic.TacticStyle("Libero Centric", 0.85f, 0.65f),    // Maximize digging and reception
        new Tactic.TacticStyle("Pipe Attack Focus", 1.20f, 1.15f)  // Sneaky back-row attacks, slightly risky
    );

    public TacticVolleyball(String formation) {
        super(new FormationVolleyball(formation));
        applyTacticStyle(AVAILABLE_STYLES.get(0).name());
    }

    public void applyTacticStyle(String style) {
        clearStyles();
        for (Tactic.TacticStyle s : AVAILABLE_STYLES) {
            if (s.name().equals(style)) {
                addStyle(s.name(), s.xgMult(), s.xgaMult());
                return;
            }
        }
        addStyle(AVAILABLE_STYLES.get(0).name(), AVAILABLE_STYLES.get(0).xgMult(), AVAILABLE_STYLES.get(0).xgaMult());
    }
}
