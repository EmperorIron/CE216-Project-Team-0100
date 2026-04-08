package Sport;

import Classes.Tactic;

public class TacticFootball extends Tactic {

    public TacticFootball(String formation) {
        super(formation);
        this.addStyle("Balanced", 1.0f, 1.0f);
    }
}