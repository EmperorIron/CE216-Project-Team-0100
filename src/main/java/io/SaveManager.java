package core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SaveManager {

    private static final String SAVE_DIR = "saves/";

    // GSON nesnesini oluştururken "PrettyPrinting" açıyoruz ki
    // dosya tek satır çorba gibi değil, alt alta düzenli ve okunaklı yazılsın.
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // --- OYUNU KAYDETME METODU ---
    public static boolean saveGame(SaveData data, String fileName) {
        // Klasör yoksa oluştur
        File directory = new File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Dosyaya JSON olarak yaz
        try (FileWriter writer = new FileWriter(SAVE_DIR + fileName + ".json")) {
            gson.toJson(data, writer);
            System.out.println("Oyun başarıyla kaydedildi: " + fileName);
            return true;
        } catch (IOException e) {
            System.err.println("Kaydetme hatası: " + e.getMessage());
            return false;
        }
    }

    // --- OYUNU YÜKLEME METODU ---
    public static SaveData loadGame(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            // JSON dosyasını oku ve SaveData objesine çevir
            SaveData loadedData = gson.fromJson(reader, SaveData.class);
            System.out.println("Oyun başarıyla yüklendi: " + loadedData.getSaveName());
            return loadedData;
        } catch (Exception e) {
            System.err.println("Yükleme hatası, dosya bozuk olabilir: " + e.getMessage());
            return null;
        }
    }
}