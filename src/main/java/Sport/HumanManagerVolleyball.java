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


public class HumanManagerVolleyball implements IManager {

    private final ITeam team;
    private transient Scanner scanner;

    public HumanManagerVolleyball(ITeam team) {
        this.team = team;
    }

    private Scanner getScanner() {
        if (scanner == null) scanner = new Scanner(System.in);
        return scanner;
    }

    @Override
    public ITeam getTeam() { return team; }

    @Override
    public ITactic generateStartingTactic() {
        System.out.println("\n=======================================================");
        System.out.println(">>> SET ÖNCESİ HAZIRLIK: " + team.getName() + " <<<");
        System.out.println("=======================================================");

        System.out.println("Mevcut Sistemler: " + FormationVolleyball.BALANCED_FORMATIONS);
        System.out.print("Sistem girin (Varsayılan 5-1): ");
        String formationStr = getScanner().nextLine().trim();
        if (formationStr.isEmpty()) formationStr = "5-1";

        TacticVolleyball tactic = new TacticVolleyball(formationStr);

        System.out.println("\nTaktik Stili:");
        System.out.println("1. Serve and Receive (Dengeli)");
        System.out.println("2. Power Attack (Agresif Hücum)");
        System.out.println("3. Defensive Wall (Savunmacı)");
        System.out.print("Seçiminiz (varsayılan 1): ");
        String styleChoice = getScanner().nextLine().trim();
        switch (styleChoice) {
            case "2" -> tactic.applyTacticStyle(TacticVolleyball.POWER_ATTACK);
            case "3" -> tactic.applyTacticStyle(TacticVolleyball.DEFENSIVE_WALL);
            default  -> tactic.applyTacticStyle(TacticVolleyball.SERVE_AND_RECEIVE);
        }

        // Auto-select best 6
        List<IPlayer> players = new ArrayList<>(team.getPlayers().stream()
                .filter(p -> !p.isInjured()).toList());
        players.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());

        List<IPlayer> starting = new ArrayList<>();
        List<IPlayer> bench    = new ArrayList<>();

        if (players.size() >= 6) {
            starting = new ArrayList<>(players.subList(0, 6));
            bench    = new ArrayList<>(players.subList(6, players.size()));
        } else {
            starting = new ArrayList<>(players);
        }

        System.out.println("\nOtomatik kadro (en yüksek reyting):");
        starting.forEach(p -> System.out.println("  " + p.getFullName()
                + " | Genel: " + String.format("%.1f", p.calculateOverallRating())));

        tactic.setStartingLineup(starting);
        tactic.setSubstitutes(bench);
        return tactic;
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        System.out.println("\n--- SET " + periodNumber + " BİTTİ. SKOR: "
                + game.getHomeScore() + " - " + game.getAwayScore() + " ---");

        if (game.getHomeSubsLeft() <= 0 && team.equals(game.getHomeTeam())) {
            System.out.println("Değişiklik hakkınız kalmadı.");
            return;
        }
        if (game.getAwaySubsLeft() <= 0 && team.equals(game.getAwayTeam())) {
            System.out.println("Değişiklik hakkınız kalmadı.");
            return;
        }

        System.out.println("Değişiklik yapmak istiyor musunuz? (e/h): ");
        String answer = getScanner().nextLine().trim().toLowerCase();
        if (!answer.equals("e")) return;

        List<IPlayer> lineup = currentTactic.getStartingLineup();
        List<IPlayer> bench  = currentTactic.getSubstitutes();

        System.out.println("Sahadan çıkacak oyuncu (1-" + lineup.size() + "):");
        for (int i = 0; i < lineup.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + lineup.get(i).getFullName());
        }
        try {
            int outIdx = Integer.parseInt(getScanner().nextLine().trim()) - 1;
            System.out.println("Giren oyuncu (1-" + bench.size() + "):");
            for (int i = 0; i < bench.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + bench.get(i).getFullName());
            }
            int inIdx = Integer.parseInt(getScanner().nextLine().trim()) - 1;
            if (outIdx >= 0 && outIdx < lineup.size() && inIdx >= 0 && inIdx < bench.size()) {
                IPlayer out = lineup.remove(outIdx);
                IPlayer in  = bench.remove(inIdx);
                lineup.add(in);
                bench.add(out);
                game.addLogEntry("Değişiklik: " + out.getFullName() + " → " + in.getFullName());
            }
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz giriş, değişiklik yapılmadı.");
        }

        PositionsVolleyball.resolvePositionCollisions(currentTactic);
    }
}
