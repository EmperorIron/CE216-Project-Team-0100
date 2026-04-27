package io;

import Interface.IPlayer;
import Interface.ITeam;
import Sport.CalendarFootball;
import Sport.LeagueFootball;

import java.util.LinkedList;

public class SaveGame {
    private String saveName;
    private LeagueFootball currentLeague;
    private CalendarFootball calendar;
    private ITeam playerTeam;

    // --- Taktik ve Kadro Bilgileri ---
    private IPlayer[][] pitchPlayers;
    private LinkedList<IPlayer> playersOnPitchQueue;
    private LinkedList<IPlayer> reservePlayersQueue;
    private String tacticStyle;

    public SaveGame(String saveName, LeagueFootball currentLeague, CalendarFootball calendar, ITeam playerTeam,
                    IPlayer[][] pitchPlayers, LinkedList<IPlayer> playersOnPitchQueue, LinkedList<IPlayer> reservePlayersQueue) {
        this(saveName, currentLeague, calendar, playerTeam, pitchPlayers, playersOnPitchQueue, reservePlayersQueue, null);
    }

    public SaveGame(String saveName, LeagueFootball currentLeague, CalendarFootball calendar, ITeam playerTeam,
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

    // Geriye dönük uyumluluk veya taktik belirlenmeden önceki kayıtlar için (eski save'ler için)
    public SaveGame(String saveName, LeagueFootball currentLeague, CalendarFootball calendar, ITeam playerTeam) {
        this(saveName, currentLeague, calendar, playerTeam, null, null, null, null);
    }

    public String getSaveName() { return saveName; }
    public LeagueFootball getCurrentLeague() { return currentLeague; }
    public CalendarFootball getCalendar() { return calendar; }
    public ITeam getPlayerTeam() { return playerTeam; }
    public IPlayer[][] getPitchPlayers() { return pitchPlayers; }
    public LinkedList<IPlayer> getPlayersOnPitchQueue() { return playersOnPitchQueue; }
    public LinkedList<IPlayer> getReservePlayersQueue() { return reservePlayersQueue; }
    public String getTacticStyle() { return tacticStyle; }
}
