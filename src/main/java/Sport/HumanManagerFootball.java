package Sport;

import Interface.IGame;
import Interface.IManager;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class HumanManagerFootball implements IManager {
    private final ITeam team;
    private final Scanner scanner;

    public HumanManagerFootball(ITeam team) {
        this.team = team;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public ITeam getTeam() { return team; }

    @Override
    public ITactic generateStartingTactic() {
        System.out.println("\n=======================================================");
        System.out.println(">>> MAÇ ÖNCESİ HAZIRLIK: " + team.getName() + " <<<");
        System.out.println("=======================================================");
        
        System.out.println("Önerilen Formasyonlar: " + TacticFootball.BALANCED_FORMATIONS);
        System.out.print("Formasyon girin (Varsayılan 1-4-4-2): ");
        String formationStr = scanner.nextLine();
        if (formationStr.trim().isEmpty()) formationStr = "1-4-4-2";
        
        TacticFootball tactic = new TacticFootball(formationStr);
        
        List<IPlayer> players = new ArrayList<>(team.getPlayers());
        players.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());
        
        List<IPlayer> starting11 = new ArrayList<>();
        List<IPlayer> bench = new ArrayList<>();
        
        boolean lineupSelected = false;
        while (!lineupSelected) {
            System.out.println("\nKadro Seçimi:");
            System.out.println("1. Otomatik (En yüksek reytinge göre)");
            System.out.println("2. Manuel");
            System.out.print("Seçiminiz: ");
            String choice = scanner.nextLine();
            
            if ("2".equals(choice)) {
                starting11.clear();
                while (starting11.size() < 11) {
                    System.out.println("\n--- MANUEL KADRO SEÇİMİ (" + starting11.size() + "/11) ---");
                    printInteractiveGrid(starting11);
                    System.out.print("Bir saha pozisyonu girin (0-99 arası, çıkmak için -1): ");
                    try {
                        int posId = Integer.parseInt(scanner.nextLine().trim());
                        if (posId == -1) {
                            starting11.clear();
                            break;
                        }
                        if (posId < 0 || posId > 99) {
                            System.out.println("Lütfen 0 ile 99 arasında geçerli bir pozisyon girin.");
                            continue;
                        }

                        List<IPlayer> available = new ArrayList<>(players);
                        available.removeAll(starting11);
                        final int targetPos = posId;
                        available.sort((p1, p2) -> {
                            int prof1 = p1.getProficiencyAt(targetPos);
                            int prof2 = p2.getProficiencyAt(targetPos);
                            if (prof1 != prof2) return Integer.compare(prof2, prof1);
                            return Double.compare(p2.calculateOverallRating(), p1.calculateOverallRating());
                        });

                        System.out.println("\nPozisyon (" + posId + ") için en uygun oyuncular:");
                        System.out.printf("%-3s %-25s %-10s %-5s %-5s %-5s %-5s\n", "No", "Oyuncu Adı", "Mevki", "OVR", "Uyum", "xG", "xGA");
                        System.out.println("-------------------------------------------------------------------------");
                        int limit = Math.min(15, available.size());
                        for (int i = 0; i < limit; i++) {
                            IPlayer p = available.get(i);
                            System.out.printf("%-3d %-25s %-10s %-5.1f %-5d %-5.2f %-5.2f\n", 
                                (i + 1), p.getFullName(), getPlayerRoleString(p), p.calculateOverallRating(), p.getProficiencyAt(targetPos), p.getxG(), p.getxGA());
                        }

                        System.out.print("Bu pozisyona yerleştirmek istediğiniz oyuncunun numarasını girin (İptal için 0): ");
                        int playerIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (playerIdx >= 0 && playerIdx < limit) {
                            IPlayer selected = available.get(playerIdx);
                            if (selected instanceof Classes.Player) {
                                ((Classes.Player) selected).setCurrentPositionId(targetPos);
                            }
                            starting11.add(selected);
                            System.out.println(">>> " + selected.getFullName() + ", " + posId + " numaralı pozisyona yerleştirildi.");
                        } else if (playerIdx != -1) {
                            System.out.println("Geçersiz seçim.");
                        }
                    } catch (Exception e) {
                        System.out.println("Hatalı giriş!");
                    }
                }

                if (starting11.size() == 11) {
                    System.out.println("\n--- SEÇİLEN İLK 11 ---");
                    for (IPlayer p : starting11) {
                        int pos = -1;
                        if (p instanceof Classes.Player) pos = ((Classes.Player) p).getCurrentPositionId();
                        System.out.printf("Pozisyon: %-3d | %-25s (OVR: %.1f)\n", pos, p.getFullName(), p.calculateOverallRating());
                    }
                    System.out.print("Kadroyu onaylıyor musunuz? (E/H): ");
                    String confirm = scanner.nextLine().trim().toUpperCase();
                    if ("E".equals(confirm)) {
                        lineupSelected = true;
                    } else {
                        starting11.clear();
                        System.out.println("Kadro sıfırlandı, baştan seçebilirsiniz.");
                    }
                }
            } else {
                starting11.clear();
                if (players.size() >= 11) starting11.addAll(players.subList(0, 11));
                else starting11.addAll(players);
                lineupSelected = true;
            }
        }
        
        for (IPlayer p : players) {
            if (bench.size() >= 9) break; // Maksimum yedek sayısı (9)
            if (!starting11.contains(p)) bench.add(p);
        }
        
        tactic.setStartingLineup(starting11);
        tactic.setSubstitutes(bench);
        
        System.out.println("\nTaktiksel Oyun Stili:");
        System.out.println("1. Dengeli (Balanced)");
        System.out.println("2. Tam Saldırı (All Out Attack)");
        System.out.println("3. Katı Savunma (Park the Bus)");
        System.out.print("Seçiminiz: ");
        String styleChoice = scanner.nextLine();
        if ("2".equals(styleChoice)) tactic.applyTacticStyle(TacticFootball.ALL_OUT_ATTACK);
        else if ("3".equals(styleChoice)) tactic.applyTacticStyle(TacticFootball.PARK_THE_BUS);
        else tactic.applyTacticStyle(TacticFootball.BALANCED);
        
        System.out.println("Takım sahaya çıkıyor...\n");
        return tactic;
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        boolean inMenu = true;
        while(inMenu) {
            System.out.println("\n=======================================================");
            System.out.println(">>> DEVRE ARASI MOLASI: " + team.getName() + " <<<");
            System.out.println("Mevcut Skor: " + game.getHomeScore() + " - " + game.getAwayScore());
            System.out.println("=======================================================");
            System.out.println("1. Oyuncu Değiştir");
            System.out.println("2. Taktiksel Stili Değiştir");
            System.out.println("3. Formasyonu Değiştir");
            System.out.println("0. Maça Devam Et");
            System.out.print("Seçiminiz: ");
            String choice = scanner.nextLine();
            
            switch(choice) {
                case "1":
                    boolean isHome = game.getHomeTeam().equals(team);
                    int subsLeft = isHome ? game.getHomeSubsLeft() : game.getAwaySubsLeft();
                    if (subsLeft <= 0) { System.out.println("Değişiklik hakkınız kalmadı!"); break; }
                    
                    System.out.println("\n--- SAHA ---");
                    System.out.printf("%-3s %-25s %-5s %-5s %-5s\n", "No", "Oyuncu Adı", "Poz", "Rol", "OVR");
                    List<IPlayer> starters = currentTactic.getStartingLineup();
                    for(int i=0; i<starters.size(); i++) {
                        IPlayer p = starters.get(i);
                        int posId = (p instanceof Classes.Player) ? ((Classes.Player) p).getCurrentPositionId() : -1;
                        System.out.printf("%-3d %-25s %-5d %-5s %-5.1f\n", (i+1), p.getFullName(), posId, getPlayerRoleString(p), p.calculateOverallRating());
                    }
                    
                    try {
                        System.out.print("\nÇıkacak oyuncu no (Saha) (İptal için 0): "); 
                        int outIdx = Integer.parseInt(scanner.nextLine()) - 1;
                        if (outIdx == -1) break;
                        
                        IPlayer pOut = starters.get(outIdx);
                        int targetPos = (pOut instanceof Classes.Player) ? ((Classes.Player) pOut).getCurrentPositionId() : pOut.getPrimaryPositionId();

                        List<IPlayer> subs = new ArrayList<>(currentTactic.getSubstitutes());
                        subs.sort((p1, p2) -> {
                            int prof1 = p1.getProficiencyAt(targetPos);
                            int prof2 = p2.getProficiencyAt(targetPos);
                            if (prof1 != prof2) return Integer.compare(prof2, prof1);
                            return Double.compare(p2.calculateOverallRating(), p1.calculateOverallRating());
                        });

                        System.out.println("\n--- YEDEK (Pozisyon " + targetPos + " için uyuma göre sıralı) ---");
                        System.out.printf("%-3s %-25s %-5s %-5s %-5s\n", "No", "Oyuncu Adı", "Rol", "OVR", "Uyum");
                        for(int i=0; i<subs.size(); i++) {
                            IPlayer p = subs.get(i);
                            System.out.printf("%-3d %-25s %-5s %-5.1f %-5d\n", (i+1), p.getFullName(), getPlayerRoleString(p), p.calculateOverallRating(), p.getProficiencyAt(targetPos));
                        }

                        System.out.print("\nGirecek oyuncu no (Yedek) (İptal için 0): "); 
                        int inIdx = Integer.parseInt(scanner.nextLine()) - 1;
                        if (inIdx == -1) break;

                        IPlayer pIn = subs.get(inIdx);
                        
                        if (pOut instanceof Classes.Player && pIn instanceof Classes.Player) {
                            ((Classes.Player) pIn).setCurrentPositionId(targetPos);
                        }
                        
                        currentTactic.getStartingLineup().remove(pOut); 
                        currentTactic.getSubstitutes().remove(pIn);
                        currentTactic.getStartingLineup().add(pIn); 
                        currentTactic.getSubstitutes().add(pOut);
                        
                        if(isHome) game.setHomeSubsLeft(subsLeft - 1); else game.setAwaySubsLeft(subsLeft - 1);
                        game.addLogEntry("DEVRE ARASI DEĞİŞİKLİK (" + team.getName() + ") -> Çıkan: " + pOut.getFullName() + " | Giren: " + pIn.getFullName());
                        System.out.println(">>> Değişiklik yapıldı!");
                    } catch (Exception e) { System.out.println("Hatalı giriş veya işlem iptal edildi."); }
                    break;
                case "2":
                    System.out.print("1. Dengeli | 2. Tam Saldırı | 3. Katı Savunma -> Seçim: ");
                    String s = scanner.nextLine();
                    if (currentTactic instanceof TacticFootball tf) {
                        if ("2".equals(s)) tf.applyTacticStyle(TacticFootball.ALL_OUT_ATTACK); else if ("3".equals(s)) tf.applyTacticStyle(TacticFootball.PARK_THE_BUS); else tf.applyTacticStyle(TacticFootball.BALANCED);
                        game.addLogEntry("Taktik değiştirildi (" + team.getName() + ").");
                    }
                    break;
                case "3":
                    System.out.print("Yeni Formasyon (örn. 1-4-3-3): ");
                    String form = scanner.nextLine();
                    if (currentTactic instanceof Classes.Tactic tacticBase) {
                        tacticBase.setFormation(new FormationFootball(form));
                        game.addLogEntry("Formasyon değiştirildi (" + team.getName() + ") -> " + form);
                    }
                    break;
                case "0": inMenu = false; break;
            }
        }
    }

    private void printInteractiveGrid(List<IPlayer> starting11) {
        System.out.println("+----------------------------------------+");
        for (int y = 9; y >= 0; y--) {
            StringBuilder row = new StringBuilder("|");
            for (int x = 0; x < 10; x++) {
                int posId = y * 10 + x;
                String cell = String.format("%02d", posId); 
                
                for (IPlayer p : starting11) {
                    if (p instanceof Classes.Player && ((Classes.Player) p).getCurrentPositionId() == posId) {
                        cell = "XX";
                        break;
                    }
                }
                row.append(String.format("%-4s", cell));
            }
            row.append("|");
            System.out.println(row.toString());
        }
        System.out.println("+----------------------------------------+");
        System.out.println("Not: 'XX' dolu pozisyonları, sayılar ise seçebileceğiniz koordinatları (ID) gösterir.");
    }

    private String getPlayerRoleString(IPlayer p) {
        if (PositionsFootball.isGoalkeeperPosition(p.getPrimaryPositionId())) return "GK";
        if (PositionsFootball.isDefenderPosition(p.getPrimaryPositionId())) return "DEF";
        if (PositionsFootball.isMidfielderPosition(p.getPrimaryPositionId())) return "MID";
        if (PositionsFootball.isForwardPosition(p.getPrimaryPositionId())) return "FWD";
        return "U";
    }
}