/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mbach231.demeter;

import com.mbach231.demeter.food.FoodHistoryManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 *
 */
public class Demeter extends JavaPlugin {

    FoodHistoryManager foodHistoryManager_;
    private static boolean logEnabled_;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        int maxFoodAmount = this.getConfig().getInt("max-food-amount");
        int minutesPerUpdate = this.getConfig().getInt("minutes-per-update");
        logEnabled_ = this.getConfig().getBoolean("log-debug");
        foodHistoryManager_ = new FoodHistoryManager(new File(this.getDataFolder(), "history.yml"), maxFoodAmount);

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                
                foodHistoryManager_.handleUpdateCycle();
            }
        }, minutesPerUpdate * 60 * 20L, minutesPerUpdate * 60 * 20L);

        this.getServer().getPluginManager().registerEvents(foodHistoryManager_, this);
    }

    @Override
    public void onDisable() {
        this.saveConfig();
        foodHistoryManager_.saveHistoryConfig();
    }
    
    public static void log(String string) {
        if(logEnabled_) {
            Bukkit.getLogger().info("[Demeter] " + string);
        }
    }

}
