package com.psygate.dedication.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.MaterialTarget;
import com.psygate.dedication.data.NumericTarget;
import com.psygate.dedication.data.PlayerData;
import com.psygate.dedication.data.Target;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Material;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class FileBackend implements Backend {

    private final static String POSTFIX = ".dedication.dat";
    private Path basefolder;
    private GsonBuilder gsonbuilder = new GsonBuilder();

    public FileBackend(File datadirectory) {
        this.basefolder = datadirectory.toPath();
        gsonbuilder.setPrettyPrinting();
        gsonbuilder.registerTypeAdapter(Target.class, new TargetSerializer());
    }

    @Override
    public boolean hasPlayer(UUID uuid) {
        return Files.exists(playerFile(uuid));
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid) {
        checkPreconditions();
        if (!hasPlayer(uuid)) {
            throw new IllegalArgumentException("Player does not exist.");
        }
        try (FileReader in = new FileReader(playerFile(uuid).toFile())) {
            return gsonbuilder.create().fromJson(in, PlayerData.class);
        } catch (IOException e) {
            Dedication.logger().log(Level.SEVERE, "Cannot load player data. (" + basefolder.toAbsolutePath() + ")", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void savePlayerData(PlayerData data) {
        checkPreconditions();
        checkData(data);
//        Dedication.logger().log(Level.FINE, "Saving: {0}", data);
        Gson gson = gsonbuilder.create();
        try (FileWriter out = new FileWriter(playerFile(data.getPlayer()).toFile())) {
            gson.toJson(data, out);
        } catch (IOException e) {
            Dedication.logger().log(Level.SEVERE, "Cannot save player data. (" + basefolder.toAbsolutePath() + ")[" + data + "]", e);
        }
    }

    @Override
    public void removePlayerData(UUID uuid) {
        try {
            Files.deleteIfExists(playerFile(uuid));
        } catch (IOException ex) {
            Dedication.logger().log(Level.SEVERE, "Cannot delete player data. (" + basefolder.toAbsolutePath() + ")[" + uuid + "]", ex);
        }
    }

    @Override
    public List<PlayerData> loadAll() {
        LinkedList<PlayerData> loaded = new LinkedList<>();

        try {
            for (Path p : listPlayerFiles()) {
                try (FileReader in = new FileReader(p.toFile())) {
                    loaded.add(gsonbuilder.create().fromJson(in, PlayerData.class));
                } catch (IOException e) {
                    Dedication.logger().log(Level.SEVERE, "Cannot load player data. (" + basefolder.toAbsolutePath() + ")", e);
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException ex) {
            Dedication.logger().log(Level.SEVERE, "Cannot load all player data. (" + basefolder.toAbsolutePath() + ")", ex);
        }

        return loaded;
    }

    private void checkPreconditions() {
        try {
            if (!Files.exists(basefolder)) {
                Files.createDirectories(basefolder);
            } else if (!Files.isDirectory(basefolder)) {
                throw new IOException("Inaccessible base folder.");
            }
        } catch (IOException e) {
            Dedication.logger().log(Level.SEVERE, "Cannot create data folder. (" + basefolder.toAbsolutePath() + ")", e);
        }
    }

    private Path playerFile(UUID uuid) {
        return basefolder.resolve(uuid.toString() + POSTFIX);
    }

    private List<Path> listPlayerFiles() throws IOException {
        final List<Path> paths = new LinkedList<>();
        Files.walkFileTree(basefolder, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(POSTFIX)) {
                    System.out.println(file.getFileName());

                    paths.add(file);
                    return FileVisitResult.CONTINUE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return paths;
    }

    private void checkData(PlayerData data) {
        Iterator<Target> it = data.getTargets().iterator();
        for (Target tgt = it.next(); it.hasNext(); tgt = it.next()) {
            if (tgt == null) {
                it.remove();
            } else if (tgt.getClass().isInterface() || Modifier.isAbstract(tgt.getClass().getModifiers())) {
                Dedication.logger().log(Level.WARNING, "Abstract class {0} detected. Removing from queue.", tgt.getClass());
                it.remove();
            }
        }
    }

    private class TargetSerializer implements JsonSerializer<Target>, JsonDeserializer<Target> {

        @Override
        public JsonElement serialize(Target src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject base = new JsonObject();
            base.add("class", new JsonPrimitive(src.getClass().getName()));
            base.add("type", new JsonPrimitive(src.getType().name()));
            base.add("uuid", new JsonPrimitive(src.getUUID().toString()));
            if (src instanceof NumericTarget) {
                NumericTarget tgt = (NumericTarget) src;
                base.add("target", new JsonPrimitive(tgt.getTarget()));
                base.add("value", new JsonPrimitive(tgt.getValue()));
            }

            if (src instanceof MaterialTarget) {
                MaterialTarget mat = (MaterialTarget) src;
                base.add("material", new JsonPrimitive(mat.getMaterial().name()));
                base.add("acceptAny", new JsonPrimitive(mat.isAcceptAny()));
            }

            return base;
        }

        @Override
        public Target deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jobj = json.getAsJsonObject();
            try {
                Class<?> clazz = Class.forName(jobj.get("class").getAsString());
                Target instance = (Target) clazz.getConstructor().newInstance();
                instance.setUUID(UUID.fromString(jobj.get("uuid").getAsString()));
                if (instance instanceof NumericTarget) {
                    NumericTarget tgt = (NumericTarget) instance;
                    tgt.setValue(jobj.get("value").getAsLong());
                    tgt.setTarget(jobj.get("target").getAsLong());
                }

                if (instance instanceof MaterialTarget) {
                    MaterialTarget tgt = (MaterialTarget) instance;
                    tgt.setMaterial(Material.valueOf(jobj.get("material").getAsString()));
                    tgt.setAcceptAny(jobj.get("acceptAny").getAsBoolean());
                }

                return instance;
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
                throw new JsonParseException(ex);
            }
        }

    }
}
