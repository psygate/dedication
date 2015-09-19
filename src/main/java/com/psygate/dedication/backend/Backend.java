package com.psygate.dedication.backend;

import com.psygate.dedication.data.PlayerData;
import java.util.UUID;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public interface Backend {

    public boolean hasPlayer(UUID uuid);

    public PlayerData loadPlayerData(UUID uuid);

    public void savePlayerData(PlayerData data);

    public void removePlayerData(UUID uuid);
}
