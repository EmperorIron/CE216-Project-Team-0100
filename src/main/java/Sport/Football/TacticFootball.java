package Sport.Football;

import Classes.Tactic;
import java.util.Arrays;
import java.util.List;

public class TacticFootball extends Tactic {

    public static final List<Tactic.TacticStyle> AVAILABLE_STYLES = Arrays.asList(
        new Tactic.TacticStyle("Balanced", 1.0f, 1.0f),
        new Tactic.TacticStyle("All Out Attack", 1.25f, 1.15f),
        new Tactic.TacticStyle("Park the Bus", 0.85f, 0.75f),
        new Tactic.TacticStyle("Tiki-Taka", 1.10f, 0.85f),         // High control, good offense, solid defense
        new Tactic.TacticStyle("Gegenpressing", 1.35f, 1.30f),     // Very high risk, very high reward
        new Tactic.TacticStyle("Catenaccio", 0.80f, 0.65f)         // Extreme defense, relying on rare counters
    );

    public TacticFootball(String formation) {
        super(new FormationFootball(formation));
        this.applyTacticStyle(AVAILABLE_STYLES.get(0).name());
    }

    public void applyTacticStyle(String style) {
        this.clearStyles();
        for (Tactic.TacticStyle s : AVAILABLE_STYLES) {
            if (s.name().equals(style)) {
                this.addStyle(s.name(), s.xgMult(), s.xgaMult());
                return;
            }
        }
        this.addStyle(AVAILABLE_STYLES.get(0).name(), AVAILABLE_STYLES.get(0).xgMult(), AVAILABLE_STYLES.get(0).xgaMult());
    }
}