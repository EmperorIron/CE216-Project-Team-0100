package Classes;

import Interface.IGame;
import Interface.IManager;
import Interface.ITactic;
import Interface.ITeam;
import java.util.ArrayList;

public abstract class HumanManager implements IManager {
    protected final ITeam team;

    public HumanManager(ITeam team) {
        this.team = team;
    }

    @Override
    public ITeam getTeam() {
        return team;
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        // Asynchronous breaks are handled by the GUI Timeline events
    }

    public void applyChangesFromGUI(ITactic currentTactic) {
        currentTactic.setStartingLineup(new ArrayList<>(gui.GUISquadManager.getPlayersOnPitchQueue()));
        currentTactic.setSubstitutes(new ArrayList<>(gui.GUISquadManager.getReservePlayersQueue()));
        applyStyle(currentTactic);
    }

    protected abstract void applyStyle(ITactic currentTactic);
}