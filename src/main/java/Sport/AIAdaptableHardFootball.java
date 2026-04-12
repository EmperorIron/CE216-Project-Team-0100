package Sport;

import Interface.IGame;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

import java.util.*;
import java.util.stream.Collectors;

public class AIAdaptableHardFootball extends AIFootball {

    public AIAdaptableHardFootball(ITeam team) {
        super(team);
    }

    @Override
    public ITactic generateStartingTactic() {
        List<String> candidateFormations = new ArrayList<>(TacticFootball.BALANCED_FORMATIONS);
        candidateFormations.add(getCoachPreferredFormation());

        return findBestTactic(candidateFormations, getAvailablePlayers(), "BALANCED");
    }

    @Override
    public void handlePeriodBreak(IGame game, ITactic currentTactic, int periodNumber) {
        adaptTactics(game, currentTactic);
        makeSubstitutions(game, currentTactic);
    }

    @Override
    protected void makeSubstitutions(IGame game, ITactic currentTactic) {
        boolean isHome = game.getHomeTeam().equals(this.team);
        int subsLeft = isHome ? game.getHomeSubsLeft() : game.getAwaySubsLeft();
        if (subsLeft <= 0) return;

        int myScore = isHome ? game.getHomeScore() : game.getAwayScore();
        int oppScore = isHome ? game.getAwayScore() : game.getHomeScore();
        
        List<IPlayer> substitutes = new ArrayList<>(currentTactic.getSubstitutes());
        if (substitutes.isEmpty()) return;

        if (myScore < oppScore) { // Losing, need to attack
            IPlayer playerOut = findWorstPerformer(currentTactic.getStartingLineup(), "DEFENDER");
            if (playerOut == null) playerOut = findWorstPerformer(currentTactic.getStartingLineup(), "MIDFIELDER");
            IPlayer playerIn = findBestAvailable(substitutes, "FORWARD");
            if (playerOut != null && playerIn != null) {
                performSubstitution(currentTactic, playerOut, playerIn, game, "AI seeking a goal");
                if (isHome) game.setHomeSubsLeft(subsLeft - 1); else game.setAwaySubsLeft(subsLeft - 1);
            }
        } else if (myScore > oppScore) { // Winning, need to defend
            IPlayer playerOut = findWorstPerformer(currentTactic.getStartingLineup(), "FORWARD");
            IPlayer playerIn = findBestAvailable(substitutes, "DEFENDER");
            if (playerOut != null && playerIn != null) {
                performSubstitution(currentTactic, playerOut, playerIn, game, "AI protecting the lead");
                if (isHome) game.setHomeSubsLeft(subsLeft - 1); else game.setAwaySubsLeft(subsLeft - 1);
            }
        }
    }

    private void performSubstitution(ITactic tactic, IPlayer out, IPlayer in, IGame game, String reason) {
        tactic.getStartingLineup().remove(out);
        tactic.getSubstitutes().remove(in);
        tactic.getStartingLineup().add(in);
        tactic.getSubstitutes().add(out);
        game.addLogEntry("AI Taktik (" + team.getName() + "): " + reason + ". Giren: " + in.getFullName() + ", Çıkan: " + out.getFullName());
    }

    private IPlayer findWorstPerformer(List<IPlayer> players, String role) {
        return players.stream()
                .filter(p -> getRole(p).equals(role))
                .min(Comparator.comparingDouble(IPlayer::calculateOverallRating))
                .orElse(null);
    }

    private IPlayer findBestAvailable(List<IPlayer> players, String role) {
        return players.stream()
                .filter(p -> getRole(p).equals(role) && !p.isInjured())
                .max(Comparator.comparingDouble(IPlayer::calculateOverallRating))
                .orElse(null);
    }

    @Override
    protected void adaptTactics(IGame game, ITactic currentTactic) {
        boolean isHome = game.getHomeTeam().equals(this.team);
        int myScore = isHome ? game.getHomeScore() : game.getAwayScore();
        int oppScore = isHome ? game.getAwayScore() : game.getHomeScore();

        if (currentTactic instanceof TacticFootball tacticFootball) {
            if (myScore < oppScore) {
                ITactic bestTactic = findBestTactic(TacticFootball.ATTACKING_FORMATIONS, getAvailablePlayers(), "ATTACK");
                tacticFootball.setStartingLineup(bestTactic.getStartingLineup());
                tacticFootball.applyTacticStyle(TacticFootball.ALL_OUT_ATTACK);
                game.addLogEntry("AI Taktik (" + team.getName() + "): Geriye düşüldüğü için 'All Out Attack' taktiğine ve " + bestTactic.getFormation() + " dizilişine geçildi.");
            } else if (myScore > oppScore) {
                ITactic bestTactic = findBestTactic(TacticFootball.DEFENSIVE_FORMATIONS, getAvailablePlayers(), "DEFEND");
                tacticFootball.setStartingLineup(bestTactic.getStartingLineup());
                tacticFootball.applyTacticStyle(TacticFootball.PARK_THE_BUS);
                game.addLogEntry("AI Taktik (" + team.getName() + "): Skoru korumak için 'Park the Bus' savunma taktiğine ve " + bestTactic.getFormation() + " dizilişine geçildi.");
            } else {
                tacticFootball.applyTacticStyle(TacticFootball.BALANCED);
            }
        }
    }

    private ITactic findBestTactic(List<String> formations, List<IPlayer> players, String mode) {
        ITactic bestTactic = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (String formationStr : new HashSet<>(formations)) { // Use HashSet to avoid duplicates
            ITactic candidateTactic = new TacticFootball(formationStr);
            List<IPlayer> lineup = selectBestLineupForFormation(formationStr, players);
            if (lineup.size() < 11) continue;

            candidateTactic.setStartingLineup(lineup);
            float xg = candidateTactic.getTotalXGMultiplier();
            float xga = candidateTactic.getTotalXGAMultiplier();
            
            double score = switch (mode) {
                case "ATTACK" -> xg;
                case "DEFEND" -> -xga;
                default -> xg - xga;
            };

            if (score > bestScore) {
                bestScore = score;
                bestTactic = candidateTactic;
            }
        }

        if (bestTactic == null) { // Fallback
            bestTactic = new TacticFootball("1-4-4-2");
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