package tests;

import Classes.GameContext;
import Sport.Football.FootballFactory;
import Sport.Volleyball.VolleyballFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_GameContext {

    @Test
    void testSingletonInstance() {
        GameContext context1 = GameContext.getInstance();
        GameContext context2 = GameContext.getInstance();
        
        assertNotNull(context1, "GameContext instance should not be null.");
        assertSame(context1, context2, "GameContext should strictly adhere to the Singleton pattern and return the exact same memory instance.");
    }

    @Test
    void testActiveSportSwitching() {
        GameContext ctx = GameContext.getInstance();
        
        ctx.setActiveSport("FOOTBALL");
        ctx.setSportFactory(new FootballFactory());
        assertEquals("FOOTBALL", ctx.getActiveSport(), "Active sport name should match 'FOOTBALL'.");
        assertTrue(ctx.getSportFactory() instanceof FootballFactory, "Factory should dynamically cast to FootballFactory.");
        
        ctx.setActiveSport("VOLLEYBALL");
        ctx.setSportFactory(new VolleyballFactory());
        assertEquals("VOLLEYBALL", ctx.getActiveSport(), "Active sport name should seamlessly switch to 'VOLLEYBALL'.");
        assertTrue(ctx.getSportFactory() instanceof VolleyballFactory, "Factory should dynamically cast to VolleyballFactory.");
    }
    
    @Test
    void testMatchDayAndTacticFlags() {
        GameContext ctx = GameContext.getInstance();
        
        ctx.setMatchDay(true);
        assertTrue(ctx.isMatchDay(), "Match day flag should be active.");
        ctx.setMatchDay(false);
        assertFalse(ctx.isMatchDay(), "Match day flag should be inactive.");
        
        ctx.setTacticConfirmedForMatch(true);
        assertTrue(ctx.isTacticConfirmedForMatch(), "Tactic confirmed flag should be active.");
    }
}