package io; // Kendi paket yapına göre ayarla

import Sport.LeagueFootball;
import Sport.HumanManagerFootball;

public class SaveData {
    // Sadece kaydedilmesini istediğin ana değişkenleri buraya koyuyorsun
    private String saveName;
    private int currentSeason;
    private LeagueFootball currentLeague;
    private HumanManagerFootball humanManager;

    // Parametreli Constructor (Oyun kaydedilirken kullanılacak)
    public SaveData(String saveName, int currentSeason, LeagueFootball currentLeague, HumanManagerFootball humanManager) {
        this.saveName = saveName;
        this.currentSeason = currentSeason;
        this.currentLeague = currentLeague;
        this.humanManager = humanManager;
    }

    // Getter Metotları (Oyun yüklenirken kullanılacak)
    public String getSaveName() { return saveName; }
    public int getCurrentSeason() { return currentSeason; }
    public LeagueFootball getCurrentLeague() { return currentLeague; }
    public HumanManagerFootball getHumanManager() { return humanManager; }
}