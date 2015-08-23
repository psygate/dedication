/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author florian
 */
public class FileBackend implements PlayerDedicationBackend {

    private final Path basepath;
    private final Charset charset = Charset.forName("UTF-8");
    private final Plugin parent;

    public FileBackend(Plugin parent) throws IOException {
        this.parent = parent;
        basepath = Paths.get(parent.getDataFolder().getAbsolutePath(), "playerdata");
        if (!Files.exists(basepath)) {
            Files.createDirectories(basepath);
        }
    }

    @Override
    public void addPlaytime(Player player, long timedelta) {
        Path playerfile = basepath.resolve(player.getUniqueId().toString());
        if (!Files.exists(playerfile)) {
//            try (FileChannel channel = FileChannel.open(playerfile, StandardOpenOption.WRITE)) {
//                channel.write(ByteBuffer.wrap(Long.toString(timedelta).getBytes(charset)));
//            } catch (IOException ex) {
//                parent.getLogger().log(Level.WARNING, "Unable to log data for " + player.getName(), ex);
//            }
            addPlayer(player);
        }

        try (FileChannel channel = FileChannel.open(playerfile, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            channel.read(buffer);
            buffer.limit(buffer.position());
            buffer.rewind();

//            parent.getLogger().log(Level.WARNING, "Time loaded: {0} - Array: {1}", new Object[]{new String(asByteArray(buffer), charset), Arrays.toString(asByteArray(buffer))});

            long val = Long.parseLong(new String(asByteArray(buffer), charset));
            channel.position(0);
            channel.write(ByteBuffer.wrap(Long.toString(timedelta + val).getBytes(charset)));
        } catch (IOException ex) {
            parent.getLogger().log(Level.WARNING, "Unable to update log data for " + player.getName(), ex);
        }

    }

    private byte[] asByteArray(ByteBuffer buffer) {
        byte[] data = new byte[buffer.limit() - buffer.position()];

        for (int i = 0; i < data.length; i++) {
            data[i] = buffer.get();
        }

        return data;
    }

    @Override
    public boolean hasPlayer(Player player) {
        Path playerfile = basepath.resolve(player.getUniqueId().toString());
        return Files.exists(playerfile);
    }

    @Override
    public void addPlayer(Player player) {
        Path playerfile = basepath.resolve(player.getUniqueId().toString());

        try (FileChannel channel = FileChannel.open(playerfile, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
//            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
//            channel.read(buffer);

            channel.position(0);

            channel.write(ByteBuffer.wrap(Long.toString(0).getBytes(charset)));
        } catch (IOException ex) {
            parent.getLogger().log(Level.WARNING, "Unable create log data for " + player.getName(), ex);
        }
    }

    @Override
    public long getPlayerPlaytime(Player player) {
        Path playerfile = basepath.resolve(player.getUniqueId().toString());

        if (hasPlayer(player)) {
            try (FileChannel channel = FileChannel.open(playerfile, StandardOpenOption.READ)) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                buffer.limit((int) Files.size(playerfile));
                channel.read(buffer);
                buffer.limit(buffer.position());
                buffer.rewind();
//                parent.getLogger().log(Level.WARNING, "Time loaded: {0} - Array: {1}", new Object[]{new String(asByteArray(buffer), charset), Arrays.toString(asByteArray(buffer))});
                long val = Long.parseLong(new String(asByteArray(buffer), charset));
                return val;
            } catch (IOException ex) {
                parent.getLogger().log(Level.WARNING, "Unable to read log data for " + player.getName(), ex);
                return 0;
            }
        } else {
            return 0;
        }
    }
}
