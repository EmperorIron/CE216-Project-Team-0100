package Classes;

import java.util.ArrayList;
import java.util.List;

public class TeamNameImport {
    public static List<String> customTeamNames = new ArrayList<>();
    private static List<String> availableNames = new ArrayList<>();
    public static boolean useCustomNames = false;

    public static void setCustomNames(List<String> names) {
        customTeamNames.clear();
        availableNames.clear();
        if (names != null && !names.isEmpty()) {
            customTeamNames.addAll(names);
            availableNames.addAll(names);
            useCustomNames = true;
        } else {
            useCustomNames = false;
        }
    }
    
    public static String getNextCustomName() {
        if (useCustomNames) {
            if (availableNames.isEmpty() && !customTeamNames.isEmpty()) {
                availableNames.addAll(customTeamNames); // refill if we run out
            }
            if (!availableNames.isEmpty()) {
                return availableNames.remove(0);
            }
        }
        return null;
    }
}
