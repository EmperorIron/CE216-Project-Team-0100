package Sport.Football;

import Classes.HumanManager;
import Interface.ITactic;
import Interface.ITeam;
import java.util.ArrayList;

public class HumanManagerFootball extends HumanManager {

    public HumanManagerFootball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        TacticFootball t = new TacticFootball("1-4-4-2");
        if (!pendingStarters.isEmpty()) {
            t.setStartingLineup(new ArrayList<>(pendingStarters));
            t.setSubstitutes(new ArrayList<>(pendingSubstitutes));
            t.applyTacticStyle(pendingStyle);
        }
        return t;
    }

    @Override
    protected void applyStyle(ITactic currentTactic, String style) {
        if (currentTactic instanceof TacticFootball tf) {
            tf.applyTacticStyle(style != null && !style.isEmpty() ? style : "Balanced");
        }
    }
}