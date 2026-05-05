package Classes;

import Interface.ISportFactory;
import Interface.ITeam;

/**
 * Singleton class to securely manage the global state of the game.
 * Fixes the "Static Cling" anti-pattern.
 */
public class GameContext {
    private static GameContext instance;

    private ITeam playerTeam;
    private League activeLeague;
    private Calendar activeCalendar;
    private ISportFactory sportFactory;
    private String activeSport = "FOOTBALL";
    private boolean isMatchDay = false;
    private boolean tacticConfirmedForMatch = false;

    // Retained for backward-compatibility references
    private League activeVolleyballLeague;
    private Calendar activeVolleyballCalendar;

    private GameContext() {}

    public static GameContext getInstance() {
        if (instance == null) {
            instance = new GameContext();
        }
        return instance;
    }

    public ITeam getPlayerTeam() { return playerTeam; }
    public void setPlayerTeam(ITeam playerTeam) { this.playerTeam = playerTeam; }

    public League getActiveLeague() { return activeLeague; }
    public void setActiveLeague(League activeLeague) { this.activeLeague = activeLeague; }

    public Calendar getActiveCalendar() { return activeCalendar; }
    public void setActiveCalendar(Calendar activeCalendar) { this.activeCalendar = activeCalendar; }

    public ISportFactory getSportFactory() { return sportFactory; }
    public void setSportFactory(ISportFactory sportFactory) { this.sportFactory = sportFactory; }

    public String getActiveSport() { return activeSport; }
    public void setActiveSport(String activeSport) { this.activeSport = activeSport; }

    public boolean isMatchDay() { return isMatchDay; }
    public void setMatchDay(boolean matchDay) { this.isMatchDay = matchDay; }

    public boolean isTacticConfirmedForMatch() { return tacticConfirmedForMatch; }
    public void setTacticConfirmedForMatch(boolean tacticConfirmedForMatch) { this.tacticConfirmedForMatch = tacticConfirmedForMatch; }

    public League getActiveVolleyballLeague() { return activeVolleyballLeague; }
    public void setActiveVolleyballLeague(League activeVolleyballLeague) { this.activeVolleyballLeague = activeVolleyballLeague; }

    public Calendar getActiveVolleyballCalendar() { return activeVolleyballCalendar; }
    public void setActiveVolleyballCalendar(Calendar activeVolleyballCalendar) { this.activeVolleyballCalendar = activeVolleyballCalendar; }
}