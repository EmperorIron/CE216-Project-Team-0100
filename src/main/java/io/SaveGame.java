package io;

import Classes.Calendar;
import Classes.League;
import Interface.IPlayer;
import Interface.ITeam;

import java.util.LinkedList;

public class SaveGame {
    private String saveName;
    private League currentLeague;
    private Calendar calendar;
    private ITeam playerTeam;

    // --- Tactic and Squad Information ---
    private IPlayer[][] pitchPlayers;
    private LinkedList<IPlayer> playersOnPitchQueue;
    private LinkedList<IPlayer> reservePlayersQueue;
    private String tacticStyle;

    public SaveGame(String saveName, League currentLeague, Calendar calendar, ITeam playerTeam,
                    IPlayer[][] pitchPlayers, LinkedList<IPlayer> playersOnPitchQueue, LinkedList<IPlayer> reservePlayersQueue) {
        this(saveName, currentLeague, calendar, playerTeam, pitchPlayers, playersOnPitchQueue, reservePlayersQueue, null);
    }

    public SaveGame(String saveName, League currentLeague, Calendar calendar, ITeam playerTeam,
                    IPlayer[][] pitchPlayers, LinkedList<IPlayer> playersOnPitchQueue, LinkedList<IPlayer> reservePlayersQueue, String tacticStyle) {
        this.saveName = saveName;
        this.currentLeague = currentLeague;
        this.calendar = calendar;
        this.playerTeam = playerTeam;
        this.pitchPlayers = pitchPlayers;
        this.playersOnPitchQueue = playersOnPitchQueue;
        this.reservePlayersQueue = reservePlayersQueue;
        this.tacticStyle = tacticStyle;
    }

    // For backward compatibility or records before tactics were determined (for old saves)
    public SaveGame(String saveName, League currentLeague, Calendar calendar, ITeam playerTeam) {
        this(saveName, currentLeague, calendar, playerTeam, null, null, null, null);
    }

    public String getSaveName() { return saveName; }
    public League getCurrentLeague() { return currentLeague; }
    public Calendar getCalendar() { return calendar; }
    public ITeam getPlayerTeam() { return playerTeam; }
    public IPlayer[][] getPitchPlayers() { return pitchPlayers; }
    public LinkedList<IPlayer> getPlayersOnPitchQueue() { return playersOnPitchQueue; }
    public LinkedList<IPlayer> getReservePlayersQueue() { return reservePlayersQueue; }
    public String getTacticStyle() { return tacticStyle; }
}
