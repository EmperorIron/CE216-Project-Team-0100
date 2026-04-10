package core;

import java.util.List;
import java.util.Scanner;

import Classes.Game;
import Classes.Positions;
import Classes.Trait;
import Interface.ICoach;
import Interface.IPlayer;
import Interface.ITeam;
import Sport.CalendarFootball;
import Sport.GameRulesFootball;
import Sport.LeagueFootball;
import Sport.PositionsFootball;

public class Main {
    public static void main(String[] args) {
        


        GameRulesFootball rules = new GameRulesFootball();
        LeagueFootball superLig = new LeagueFootball("Süper Lig", "Türkiye", 4, rules); 
        List<ITeam> teams = superLig.getTeamRanking();

        System.out.println("[SİSTEM] " + superLig.getName() + " oluşturuldu. Takımlar: ");
        for(ITeam team : teams) {
            System.out.printf("- %-20s (Hücum Gücü: %5.1f | Savunma Gücü: %5.1f)\n", 
                              team.getName(), team.getTotalOffensiveRating(), team.getTotalDefensiveRating());
        }
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        System.out.println("[SİSTEM] Hangi takımı yönetmek istersiniz?");
        System.out.println("0. Sadece Simüle Et (Tüm Takımlar AI)");
        for (int i = 0; i < teams.size(); i++) {
            System.out.println((i + 1) + ". " + teams.get(i).getName());
        }
        System.out.print("Seçiminiz: ");
        try {
            int teamChoice = Integer.parseInt(scanner.nextLine());
            if (teamChoice > 0 && teamChoice <= teams.size()) {
                teams.get(teamChoice - 1).setManagerAI(false); // YAPAY ZEKA DEĞİL, İNSAN!
                System.out.println("\n>>> " + teams.get(teamChoice - 1).getName() + " takımının menajeri sizsiniz! <<<\n");
            }
        } catch(Exception e) {}

        CalendarFootball calendar = new CalendarFootball(rules);
        calendar.generateFixtures(teams);
        System.out.println("[SİSTEM] Fikstür oluşturuldu. Sezon başlıyor!\n");

        int totalWeeks = calendar.getSchedule().size();
        boolean running = true;

        while (running) {
            int currentWeek = calendar.getCurrentWeek();
            if (currentWeek >= totalWeeks) {
                System.out.println("\n=======================================================================================");
                System.out.println("||                             TÜM HAFTALAR OYNANDI                                  ||");
                System.out.println("=======================================================================================");
                System.out.println("Final Lig Tablosu:");
                calendar.displayLeagueTable();
                
                // Sezon bitse bile geçmiş maçlara bakabilmek için menüyü açık tutuyoruz
                System.out.println("\n[1] Geçmiş Maç Raporlarına Bak | [0] Çıkış");
                String choice = scanner.nextLine();
                if(choice.equals("1")) {
                    viewPastMatches(scanner, calendar);
                } else {
                    running = false;
                }
                continue;
            }

            displayMainMenu(currentWeek + 1);
            System.out.print("Seçiminiz: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    calendar.advanceToNextWeek();
                    break;
                case "2":
                    calendar.displayLeagueTable();
                    break;
                case "3":
                    viewTeamDetails(scanner, teams);
                    break;
                case "4":
                    // DOSTLUK MAÇI YERİNE GEÇMİŞ MAÇLAR EKLENDİ
                    viewPastMatches(scanner, calendar);
                    break;
                case "5":
                    running = false;
                    System.out.println("Simülasyondan çıkılıyor...");
                    break;
                default:
                    System.out.println("Geçersiz seçim. Lütfen 1-5 arasında bir sayı girin.");
                    break;
            }
        }
        scanner.close();
    }

    private static void displayMainMenu(int nextWeek) {
        System.out.println("\n--- ANA MENÜ ---");
        System.out.println("1. " + nextWeek + ". Haftayı Oyna");
        System.out.println("2. Lig Tablosunu Görüntüle");
        System.out.println("3. Takım Detaylarını İncele");
        System.out.println("4. Geçmiş Maç Raporlarını Oku"); // GÜNCELLENDİ
        System.out.println("5. Simülasyondan Çık");
        System.out.println("----------------");
    }

    // YENİ EKLENEN METOT: GEÇMİŞ MAÇLARI LİSTELEME
    private static void viewPastMatches(Scanner scanner, CalendarFootball calendar) {
        int currentWeek = calendar.getCurrentWeek();
        if (currentWeek == 0) {
            System.out.println("Henüz oynanmış bir maç bulunmuyor.");
            return;
        }

        System.out.print("\nHangi haftanın maçlarını görmek istiyorsunuz? (1 - " + currentWeek + " arası): ");
        try {
            int weekChoice = Integer.parseInt(scanner.nextLine());
            if (weekChoice >= 1 && weekChoice <= currentWeek) {
                List<Game> games = calendar.getSchedule().get(weekChoice);
                if (games != null && !games.isEmpty()) {
                    System.out.println("\n--- " + weekChoice + ". HAFTA SONUÇLARI ---");
                    for (int i = 0; i < games.size(); i++) {
                        Game g = games.get(i);
                        System.out.printf("%d. %s %d - %d %s\n", (i + 1), g.getHomeTeam().getName(), g.getHomeScore(), g.getAwayScore(), g.getAwayTeam().getName());
                    }
                    
                    System.out.print("Detaylı simülasyon logunu görmek istediğiniz maçın numarasını girin (İptal için 0): ");
                    int matchChoice = Integer.parseInt(scanner.nextLine());
                    if (matchChoice > 0 && matchChoice <= games.size()) {
                        Game selectedGame = games.get(matchChoice - 1);
                        System.out.println("\n=======================================================");
                        System.out.println("|| MAÇ RAPORU: " + selectedGame.getHomeTeam().getName() + " vs " + selectedGame.getAwayTeam().getName() + " ||");
                        System.out.println("=======================================================");
                        selectedGame.getGameLog().forEach(System.out::println);
                        System.out.println("-------------------------------------------------------");
                        System.out.println("(Devam etmek için Enter'a basın)");
                        scanner.nextLine();
                    }
                }
            } else {
                System.out.println("Geçersiz hafta numarası girdiniz.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Lütfen sadece sayı girin.");
        }
    }

    // viewTeamDetails, manageTeamSubMenu, viewPlayerRoster, displayPlayerInfo, viewCoachingStaff, displayCoachInfo METOTLARI BİR ÖNCEKİ MESAJDAKİ GİBİ AYNI KALACAK.
    private static void viewTeamDetails(Scanner scanner, List<ITeam> teams) {
        // (Bu kısımlar dokunulmadan aynı kalacak, kod kalabalığı yapmaması için atlıyorum)
        System.out.println("\n--- TAKIM SEÇ ---");
        for (int i = 0; i < teams.size(); i++) {
            System.out.println((i + 1) + ". " + teams.get(i).getName());
        }
        System.out.println("-----------------");
        System.out.print("İncelemek istediğiniz takımın numarasını girin (veya '0' ile geri dön): ");
        
        try {
            int teamChoice = Integer.parseInt(scanner.nextLine());
            if (teamChoice > 0 && teamChoice <= teams.size()) {
                ITeam selectedTeam = teams.get(teamChoice - 1);
                manageTeamSubMenu(scanner, selectedTeam);
            } else if (teamChoice != 0) {
                System.out.println("Geçersiz takım numarası.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Lütfen bir sayı girin.");
        }
    }
    
    private static void manageTeamSubMenu(Scanner scanner, ITeam team) {
        // (Aynı kalacak)
        boolean inSubMenu = true;
        while (inSubMenu) {
            System.out.println("\n--- " + team.getName().toUpperCase() + " YÖNETİM PANELİ ---");
            System.out.println("1. Takım Genel Analizi");
            System.out.println("2. Oyuncu Kadrosunu Görüntüle");
            System.out.println("3. Teknik Ekibi Görüntüle");
            System.out.println("0. Ana Menüye Dön");
            System.out.print("Seçiminiz: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": viewTeamStats(team); break;
                case "2": viewPlayerRoster(scanner, team); break;
                case "3": viewCoachingStaff(scanner, team); break;
                case "0": inSubMenu = false; break;
                default: System.out.println("Geçersiz seçim."); break;
            }
        }
    }

    private static void viewTeamStats(ITeam team) {
        // (Aynı kalacak)
        System.out.println("\n--- TAKIM GENEL ANALİZİ ---");
        System.out.println("Takım Adı   : " + team.getName() + " (" + team.getCountry() + ")");
        System.out.printf("Hücum Gücü  : %.2f\n", team.getTotalOffensiveRating());
        System.out.printf("Savunma Gücü: %.2f\n", team.getTotalDefensiveRating());
        System.out.println("\n--- Lig İstatistikleri ---");
        System.out.printf("Puan: %d | G: %d | B: %d | M: %d\n", team.getPoints(), team.getWins(), team.getDraws(), team.getLosses());
        System.out.printf("Atılan Gol: %d | Yenilen Gol: %d | Averaj: %d\n", team.getGoalsScored(), team.getGoalsConceded(), team.getGoalDifference());
        System.out.println("---------------------------");
    }

    private static void viewPlayerRoster(Scanner scanner, ITeam team) {
         // (Aynı kalacak)
        System.out.println("\n--- " + team.getName() + " KADROSU ---");
        List<IPlayer> players = team.getPlayers();
        System.out.printf("%-3s %-25s %-5s %-5s %s\n", "No", "Oyuncu Adı", "Mevki", "OVR", "Durum");
        System.out.println("---------------------------------------------------------");
        
        for (int i = 0; i < players.size(); i++) {
            IPlayer player = players.get(i);
            String primaryPos = PositionsFootball.isGoalkeeperPosition(player.getPrimaryPositionId()) ? "GK" :
                                PositionsFootball.isDefenderPosition(player.getPrimaryPositionId()) ? "DEF" :
                                PositionsFootball.isMidfielderPosition(player.getPrimaryPositionId()) ? "MID" :
                                PositionsFootball.isForwardPosition(player.getPrimaryPositionId()) ? "FWD" : "U";
            double overall = player.calculateOverallRating();
            String status = player.isInjured() ? "[SAKAT: " + player.getInjuryDuration() + " Hafta]" : "[SAĞLAM]";
            System.out.printf("%-3d %-25s %-5s %-5.1f %s\n", (i + 1), player.getFullName(), primaryPos, overall, status);
        }
        System.out.println("---------------------------------------------------------");
        System.out.print("İncelemek istediğiniz oyuncunun numarasını girin (veya '0' ile geri dön): ");

        try {
            int playerChoice = Integer.parseInt(scanner.nextLine());
            if (playerChoice > 0 && playerChoice <= players.size()) {
                IPlayer selectedPlayer = players.get(playerChoice - 1);
                displayPlayerInfo(selectedPlayer);
                System.out.println("\n(Devam etmek için Enter'a basın)");
                scanner.nextLine();
            } else if (playerChoice != 0) {
                System.out.println("Geçersiz oyuncu numarası.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Lütfen bir sayı girin.");
        }
    }

    private static void displayPlayerInfo(IPlayer player) {
        // (Aynı kalacak)
        System.out.println("\n--- OYUNCU DETAYLARI ---");
        System.out.println("Adı Soyadı : " + player.getFullName());
        System.out.println("Ülkesi     : " + player.getCountry());
        System.out.printf("Genel Güç  : %.1f\n", player.calculateOverallRating());
        if (player.isInjured()) {
            System.out.println("Sakatlık   : " + player.getInjuryDuration() + " Hafta oynayamayacak.");
        }
        System.out.println("\n--- Yetenekler (Seviye | Mevcut XP / Hedef XP) ---");
        for (Trait trait : player.getTraits().values()) {
            System.out.printf("%-18s: %-3d (%6.1f / %-7.1f)\n",
                    trait.getName(),
                    trait.getCurrentLevel(),
                    trait.getExp(),
                    trait.getExpToLevelUp());
        }
        System.out.println("----------------------------------------------------");

        System.out.println("\n--- Mevki Yatkınlığı (10x10 Saha) ---");
        System.out.println("(* : Birincil Mevki)");
        int primaryPosId = player.getPrimaryPositionId();

        // Sütun başlıkları (X ekseni)
        System.out.print(" Y\\X ");
        for (int x = 0; x < Positions.GRID_WIDTH; x++) {
            System.out.printf("| %-3s ", x);
        }
        System.out.println("|");

        // Ayırıcı çizgi
        System.out.print("-----");
        for (int x = 0; x < Positions.GRID_WIDTH; x++) {
            System.out.print("+-----");
        }
        System.out.println("+");

        // Satırlar (Y ekseni) ve yetkinlikler
        for (int y = 0; y < Positions.GRID_HEIGHT; y++) {
            System.out.printf(" %-3s ", y);
            for (int x = 0; x < Positions.GRID_WIDTH; x++) {
                int posId = Positions.getPositionId(x, y);
                int proficiency = player.getProficiencyAt(posId);
                String marker = (posId == primaryPosId) ? "*" : " ";
                System.out.printf("|%s%-3d ", marker, proficiency);
            }
            System.out.println("|");
        }
    }

    private static void viewCoachingStaff(Scanner scanner, ITeam team) {
        // (Aynı kalacak)
        System.out.println("\n--- " + team.getName() + " TEKNİK EKİBİ ---");
        List<ICoach> coaches = team.getCoaches();
        for (int i = 0; i < coaches.size(); i++) {
            ICoach coach = coaches.get(i);
            System.out.printf("%d. %-25s (Tercih: %s)\n", (i + 1), coach.getFullName(), coach.getPreferredFormation());
        }
        System.out.println("---------------------------------");
        System.out.print("İncelemek istediğiniz antrenörün numarasını girin (veya '0' ile geri dön): ");

        try {
            int coachChoice = Integer.parseInt(scanner.nextLine());
            if (coachChoice > 0 && coachChoice <= coaches.size()) {
                ICoach selectedCoach = coaches.get(coachChoice - 1);
                displayCoachInfo(selectedCoach);
                System.out.println("\n(Devam etmek için Enter'a basın)");
                scanner.nextLine();
            } else if (coachChoice != 0) {
                System.out.println("Geçersiz antrenör numarası.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Lütfen bir sayı girin.");
        }
    }

    private static void displayCoachInfo(ICoach coach) {
        // (Aynı kalacak)
        System.out.println("\n--- ANTRENÖR DETAYLARI ---");
        System.out.println("Adı Soyadı : " + coach.getFullName());
        System.out.println("Ülkesi     : " + coach.getCountry());
        System.out.println("Formasyon  : " + coach.getPreferredFormation());
        System.out.println("\n--- Yetenekler (Seviye | Mevcut XP / Hedef XP) ---");
        for (Trait trait : coach.getTraits().values()) {
            System.out.printf("%-20s: %-3d (%6.1f / %-7.1f)\n",
                    trait.getName(),
                    trait.getCurrentLevel(),
                    trait.getExp(),
                    trait.getExpToLevelUp());
        }
        System.out.println("----------------------------------------------------");
    }
}