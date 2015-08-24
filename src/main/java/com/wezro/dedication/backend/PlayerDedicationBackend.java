/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication.backend;

import org.bukkit.entity.Player;

/**
 *
 * @author florian
 */
public interface PlayerDedicationBackend {

    public void addPlaytime(Player player, long timedelta);

    public boolean hasPlayer(Player player);

    public void addPlayer(Player player);

    public long getPlayerPlaytime(Player player);
}
