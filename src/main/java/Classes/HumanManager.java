package Classes;

import Interface.IGame;
import Interface.IManager;
import Interface.ITactic;
import Interface.ITeam;
import Interface.IPlayer;
import java.util.ArrayList;
import java.util.List;

public abstract class HumanManager implements IManager {
    protected final ITeam team;
    protected List<IPlayer> pendingStarters = new ArrayList<>();
    protected List<IPlayer> pendingSubstitutes = new ArrayList<>();
    protected String pendingStyle = "";

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

    public void setPreMatchTactic(List<IPlayer> starters, List<IPlayer> subs, String style) {
        this.pendingStarters = new ArrayList<>(starters);
        this.pendingSubstitutes = new ArrayList<>(subs);
        this.pendingStyle = style;
    }

    public void applyTacticalChanges(ITactic currentTactic, List<IPlayer> starters, List<IPlayer> subs, String style) {
        currentTactic.setStartingLineup(new ArrayList<>(starters));
        currentTactic.setSubstitutes(new ArrayList<>(subs));
        applyStyle(currentTactic, style);
    }

    protected abstract void applyStyle(ITactic currentTactic, String style);
}