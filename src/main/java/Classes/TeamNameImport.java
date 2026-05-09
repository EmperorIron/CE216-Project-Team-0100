package Classes;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class TeamNameImport {
    private static List<String> customTeamNames = new ArrayList<>();
    private static List<String> availableNames = new ArrayList<>();
    private static boolean useCustomNames = false;

    public static boolean isUseCustomNames() { return useCustomNames; }
    public static void setUseCustomNames(boolean use) { useCustomNames = use; }
    public static List<String> getCustomTeamNames() { return new ArrayList<>(customTeamNames); }

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

    public static void saveToDisk() {
        try {
            File dir = new File(io.SaveManager.getSaveDirectory());
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "custom_teams.txt");
            if (useCustomNames && !customTeamNames.isEmpty()) {
                java.nio.file.Files.write(file.toPath(), customTeamNames);
            } else {
                if (file.exists()) file.delete();
            }
        } catch (Exception e) {
            ErrorHandler.logError("Failed to save custom team names: " + e.getMessage());
        }
    }

    public static void loadFromDisk() {
        try {
            File file = new File(io.SaveManager.getSaveDirectory(), "custom_teams.txt");
            if (file.exists()) {
                List<String> names = java.nio.file.Files.readAllLines(file.toPath());
                setCustomNames(names);
            }
        } catch (Exception e) { }
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
