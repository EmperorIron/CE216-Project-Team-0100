package Sport.Football;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.*;
import java.util.stream.Collectors;

public class AIAttackHardFootball extends AIFootball {

    public AIAttackHardFootball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        ITactic bestTactic = findBestTactic(Arrays.asList(getCoachPreferredFormation()), getAvailablePlayers());
        if (bestTactic instanceof TacticFootball tacticFootball) {
            tacticFootball.applyTacticStyle(TacticFootball.AVAILABLE_STYLES.get(1).name());
        }
        return bestTactic;
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        adaptTactics(game, currentTactic);
        makeSubstitutions(game, currentTactic);
    }

    @Override
    protected void makeSubstitutions(IGame game, ITactic currentTactic) {
        // Hard AI could sub tired attackers for fresh ones.
        boolean isHome = game.getHomeTeam().equals(this.team);
        int subsLeft = isHome ? game.getHomeSubsLeft() : game.getAwaySubsLeft();
        if (subsLeft <= 0) return;

        // Example: Sub a tired forward for a fresh one from the bench
    }

    @Override
    protected void adaptTactics(IGame game, ITactic currentTactic) {
        if (currentTactic instanceof TacticFootball tacticFootball) {
            String styleName = TacticFootball.AVAILABLE_STYLES.get(1).name();
            tacticFootball.applyTacticStyle(styleName);
            game.addLogEntry("AI Tactic (" + team.getName() + "): Continuing with '" + styleName + "' tactic.");
        }
    }

    private ITactic findBestTactic(List<String> formations, List<IPlayer> players) {
        ITactic bestTactic = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (String formationStr : new HashSet<>(formations)) {
            ITactic candidateTactic = new TacticFootball(formationStr);
            List<IPlayer> lineup = selectBestLineupForFormation(formationStr, players);
            if (lineup.size() < 11) continue;

            candidateTactic.setStartingLineup(lineup);
            
            float xg = candidateTactic.getTotalXGMultiplier();
            if (xg > bestScore) {
                bestScore = xg;
                bestTactic = candidateTactic;
            }
        }

        if (bestTactic == null) { // Fallback
            bestTactic = new TacticFootball("1-4-3-3");
            players.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());
            bestTactic.setStartingLineup(new ArrayList<>(players.subList(0, 11)));
        }
        
        List<IPlayer> starters = bestTactic.getStartingLineup();
        List<IPlayer> subs = new ArrayList<>(players);
        subs.removeAll(starters);
        bestTactic.setSubstitutes(subs);
        return bestTactic;
    }

    private List<IPlayer> selectBestLineupForFormation(String formationStr, List<IPlayer> availablePlayers) {
        Map<String, Integer> required = parseFormationCounts(formationStr);
        List<IPlayer> lineup = new ArrayList<>();
        List<IPlayer> pool = new ArrayList<>(availablePlayers);
        pool.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());

        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            String role = entry.getKey();
            int count = entry.getValue();
            List<IPlayer> picked = pool.stream().filter(p -> getRole(p).equals(role)).limit(count).collect(Collectors.toList());
            lineup.addAll(picked);
            pool.removeAll(picked);
        }
        return lineup;
    }

    private String getRole(IPlayer player) {
        int posId = player.getPrimaryPositionId();
        if (PositionsFootball.isGoalkeeperPosition(posId)) return "GOALKEEPER";
        if (PositionsFootball.isDefenderPosition(posId)) return "DEFENDER";
        if (PositionsFootball.isForwardPosition(posId)) return "FORWARD";
        return "MIDFIELDER";
    }

    private Map<String, Integer> parseFormationCounts(String formationStr) {
        Map<String, Integer> counts = new HashMap<>();
        String[] parts = formationStr.split("-");
        
        int offset = 0;
        if (parts.length > 0 && parts[0].equals("1")) {
            counts.put("GOALKEEPER", 1);
            offset = 1;
        } else {
            counts.put("GOALKEEPER", 1);
        }
        
        int remainingLines = parts.length - offset;
        if (remainingLines == 3) {
            counts.put("DEFENDER", Integer.parseInt(parts[offset]));
            counts.put("MIDFIELDER", Integer.parseInt(parts[offset + 1]));
            counts.put("FORWARD", Integer.parseInt(parts[offset + 2]));
        } else if (remainingLines == 4) {
            counts.put("DEFENDER", Integer.parseInt(parts[offset]));
            counts.put("MIDFIELDER", Integer.parseInt(parts[offset + 1]) + Integer.parseInt(parts[offset + 2]));
            counts.put("FORWARD", Integer.parseInt(parts[offset + 3]));
        } else if (remainingLines == 2) {
            counts.put("DEFENDER", Integer.parseInt(parts[offset]));
            counts.put("MIDFIELDER", Integer.parseInt(parts[offset + 1]));
            counts.put("FORWARD", 1);
        }
        return counts;
    }
}