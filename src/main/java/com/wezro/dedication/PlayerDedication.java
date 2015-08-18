/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author florian
 */
public class PlayerDedication extends FixedMetadataValue {

    public PlayerDedication(Plugin owningPlugin, long timestamp) {
        super(owningPlugin, timestamp);
    }

}
