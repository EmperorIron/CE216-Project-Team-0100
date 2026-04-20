
package io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import io.SaveGame;

import Classes.Formation;
import Classes.Game;
import Classes.GameRules;
import Interface.ICoach;
import Interface.IGame;
import Interface.IManager;
import Interface.IPlayer;
import Interface.ITactic;
import Interface.ITeam;

public class SaveManager {

    private static final String SAVE_DIR = "saves/";

    // Interface ve Abstract sınıfların (polimorfik yapıların) Gson tarafından 
    // hangi alt sınıfa ait olduğunu bilerek kaydedilip yüklenebilmesi için özel Adapter.
    private static class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
        @Override
        public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
            JsonObject wrapper = new JsonObject();
            wrapper.addProperty("className", object.getClass().getName());
            wrapper.add("data", context.serialize(object, object.getClass()));
            return wrapper;
        }

        @Override
        public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
            JsonObject wrapper = (JsonObject) elem;
            JsonElement typeName = wrapper.get("className");
            JsonElement data = wrapper.get("data");
            try {
                Type actualType = Class.forName(typeName.getAsString());
                return context.deserialize(data, actualType);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Class not found: " + typeName.getAsString(), e);
            }
        }
    }

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ITeam.class, new InterfaceAdapter<ITeam>())
            .registerTypeAdapter(IPlayer.class, new InterfaceAdapter<IPlayer>())
            .registerTypeAdapter(ICoach.class, new InterfaceAdapter<ICoach>())
            .registerTypeAdapter(IManager.class, new InterfaceAdapter<IManager>())
            .registerTypeAdapter(ITactic.class, new InterfaceAdapter<ITactic>())
            .registerTypeAdapter(IGame.class, new InterfaceAdapter<IGame>())
            .registerTypeAdapter(Game.class, new InterfaceAdapter<Game>())
            .registerTypeAdapter(Formation.class, new InterfaceAdapter<Formation>())
            .registerTypeAdapter(GameRules.class, new InterfaceAdapter<GameRules>())
            .create();

    // --- OYUNU KAYDETME METODU ---
    public static boolean saveGame(SaveGame data, String fileName) {
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
    public static SaveGame loadGame(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            // JSON dosyasını oku ve SaveGame objesine çevir
            SaveGame loadedData = gson.fromJson(reader, SaveGame.class);
            System.out.println("Oyun başarıyla yüklendi: " + loadedData.getSaveName());
            return loadedData;
        } catch (Exception e) {
            System.err.println("Yükleme hatası, dosya bozuk olabilir: " + e.getMessage());
            return null;
        }
    }
}