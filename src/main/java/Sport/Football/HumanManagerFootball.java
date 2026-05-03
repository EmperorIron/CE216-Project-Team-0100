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
        if (!gui.GUISquadManager.getPlayersOnPitchQueue().isEmpty()) {
            t.setStartingLineup(new ArrayList<>(gui.GUISquadManager.getPlayersOnPitchQueue()));
            t.setSubstitutes(new ArrayList<>(gui.GUISquadManager.getReservePlayersQueue()));
            t.applyTacticStyle(gui.GUISquadManager.getCurrentTacticStyle());
        }
        return t;
    }

    @Override
    protected void applyStyle(ITactic currentTactic) {
        if (currentTactic instanceof TacticFootball tf) {
            tf.applyTacticStyle(gui.GUISquadManager.getCurrentTacticStyle());
        }
    }
}