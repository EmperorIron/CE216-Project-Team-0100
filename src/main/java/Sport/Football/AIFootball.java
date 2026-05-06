package Sport.Football;

import Classes.AI;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AIFootball extends AI {

    public AIFootball(ITeam team) {
        super(team);
    }

    protected String getCoachPreferredFormation() {
        if (!team.getCoaches().isEmpty()) {
            var headCoach = team.getCoaches().get(0);
            if (headCoach instanceof CoachFootball coachFootball) {
                return coachFootball.getPreferredFormation();
            }
        }
        return "1-4-4-2"; // Default fallback
    }

    protected List<IPlayer> getAvailablePlayers() {
        return team.getPlayers().stream().filter(p -> !p.isInjured()).collect(Collectors.toList());
    }

    // ─── TEMPLATE METHOD PATTERN ──────────────────────────────────────────────

    protected ITactic findBestTactic(List<String> formations, List<IPlayer> players) {
        ITactic bestTactic = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (String formationStr : new HashSet<>(formations)) {
            ITactic candidateTactic = new TacticFootball(formationStr);
            List<IPlayer> lineup = selectBestLineupForFormation(formationStr, players);
            if (lineup.size() < 11) continue;

            candidateTactic.setStartingLineup(lineup);
            
            double score = evaluateTactic(candidateTactic); // <--- Hook Method
            if (score > bestScore) {
                bestScore = score;
                bestTactic = candidateTactic;
            }
        }

        if (bestTactic == null) { 
            bestTactic = new TacticFootball("1-4-3-3");
            players.sort(Comparator.comparingDouble(IPlayer::calculateOverallRating).reversed());
            bestTactic.setStartingLineup(new ArrayList<>(players.subList(0, Math.min(11, players.size()))));
        }
        
        List<IPlayer> starters = bestTactic.getStartingLineup();
        List<IPlayer> subs = new ArrayList<>(players);
        subs.removeAll(starters);
        bestTactic.setSubstitutes(subs);
        return bestTactic;
    }

    protected double evaluateTactic(ITactic candidateTactic) {
        return 0.0; // Overridden by specific Hard AI subclasses
    }

    protected List<IPlayer> selectBestLineupForFormation(String formationStr, List<IPlayer> availablePlayers) {
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

    protected String getRole(IPlayer player) {
        int posId = player.getPrimaryPositionId();
        if (PositionsFootball.isGoalkeeperPosition(posId)) return "GOALKEEPER";
        if (PositionsFootball.isDefenderPosition(posId)) return "DEFENDER";
        if (PositionsFootball.isForwardPosition(posId)) return "FORWARD";
        return "MIDFIELDER";
    }

    protected Map<String, Integer> parseFormationCounts(String formationStr) {
        Map<String, Integer> counts = new HashMap<>();
        String[] parts = formationStr.split("-");
        int offset = (parts.length > 0 && parts[0].equals("1")) ? 1 : 0;
        counts.put("GOALKEEPER", 1);
        
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
