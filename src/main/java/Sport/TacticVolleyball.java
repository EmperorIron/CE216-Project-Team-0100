package Sport;

import Classes.Tactic;


public class TacticVolleyball extends Tactic {

    public static final String SERVE_AND_RECEIVE = "Serve and Receive";
    public static final String POWER_ATTACK      = "Power Attack";
    public static final String DEFENSIVE_WALL    = "Defensive Wall";

    public TacticVolleyball(String formation) {
        super(new FormationVolleyball(formation));
        applyTacticStyle(SERVE_AND_RECEIVE);
    }

    public void applyTacticStyle(String style) {
        clearStyles();
        switch (style) {
            case POWER_ATTACK:
                addStyle(POWER_ATTACK, 1.30f, 1.10f);   // big attack boost, slight defence boost
                break;
            case DEFENSIVE_WALL:
                addStyle(DEFENSIVE_WALL, 0.80f, 0.70f); // lower attack, stronger wall
                break;
            case SERVE_AND_RECEIVE:
            default:
                addStyle(SERVE_AND_RECEIVE, 1.0f, 1.0f);
                break;
        }
    }
}
