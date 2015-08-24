/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication.runnables;

import com.wezro.dedication.Dedication;

/**
 *
 * @author florian
 */
public class SafetyStorage implements Runnable {

    @Override
    public void run() {
        Dedication.forceSave();
    }
}
