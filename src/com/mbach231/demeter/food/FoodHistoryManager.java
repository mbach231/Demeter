/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mbach231.demeter.food;

import com.mbach231.demeter.Demeter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 *
 */
public class FoodHistoryManager implements Listener {

    File historyFile_;
    Map<Player, FoodHistory> historyMap_;
    FileConfiguration historyConfig_;

    int maxFoodAmount_;

    public FoodHistoryManager(File file, int maxFoodAmount) {
        historyFile_ = file;
        historyConfig_ = YamlConfiguration.loadConfiguration(historyFile_);
        historyMap_ = new ConcurrentHashMap();
        maxFoodAmount_ = maxFoodAmount;
    }

    public void handleUpdateCycle() {
        //log("Handling update cycle!");

        Set<Entry<Player, FoodHistory>> entrySet = historyMap_.entrySet();
        for (Map.Entry<Player, FoodHistory> entry : entrySet) {
            Player player = entry.getKey();
            FoodHistory history = entry.getValue();
            history.lowerHistoryAmounts();
            historyMap_.put(player, history);
        }
    }

    public void saveHistoryConfig() {
        try {
            for (Map.Entry<Player, FoodHistory> entry : historyMap_.entrySet()) {
                savePlayer(entry.getKey());
                Demeter.log("Saving player: " + entry.getKey().getName());
            }

            historyConfig_.save(historyFile_);
        } catch (IOException ex) {
            Logger.getLogger(Demeter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void savePlayer(Player player) {
        if (historyMap_.containsKey(player)) {

            String uuidStr = player.getUniqueId().toString();
            FoodHistory foodHistory = historyMap_.get(player);

            Set<Entry<Material, Integer>> entrySet = foodHistory.getEntrySet();
            
            historyConfig_.set(uuidStr, null);
            
            if (entrySet.size() > 0) {

                for (Entry<Material, Integer> entry : entrySet) {
                    String materialStr = entry.getKey().toString();
                    
                    historyConfig_.set(uuidStr + "." + materialStr, entry.getValue());
                    Demeter.log("Saving - " + materialStr + ", " + entry.getValue());
                    //saveMap.put(materialStr, entry.getValue());
                }

                //historyConfig_.set(uuidStr, saveMap);
                Demeter.log("Saved Food History for: " + uuidStr);
            } else {
                Demeter.log("Could not save player, setting config to null");
            }
        } else {
            Demeter.log("Player did not exist in historyMap, ignoring");
        }
    }

    @EventHandler
    public void onPlayerEat(FoodLevelChangeEvent event) {

        if (event.getEntity() instanceof Player) {

            Player player = (Player) event.getEntity();
            int playerFoodLevel = player.getFoodLevel();

            int healAmount = event.getFoodLevel() - player.getFoodLevel();

            if (healAmount <= 0) {
                return;
            }

            Material consumedType = player.getItemInHand().getType();

            if (Food.isFood(consumedType) && historyMap_.containsKey(player)) {

                FoodHistory foodHistory = historyMap_.get(player);

                int numFoodConsumed = foodHistory.getFoodAmount(consumedType);

                // If at max, do not change foodHistory
                if (numFoodConsumed >= maxFoodAmount_) {
                    numFoodConsumed = maxFoodAmount_;
                } else {
                    foodHistory.consumedMaterial(consumedType);
                }

                int newHealAmount = Math.round(healAmount * (maxFoodAmount_ - numFoodConsumed) / maxFoodAmount_);
                Demeter.log("Player is consuming food - Old heal: " + healAmount + "\tNew Heal: " + newHealAmount);
                event.setFoodLevel(playerFoodLevel + newHealAmount);

                historyMap_.put(player, foodHistory);

            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        String uuidStr = player.getUniqueId().toString();

        Demeter.log("PlayerJoinEvent!");
        if (historyConfig_.contains(uuidStr)) {
            Demeter.log("Contains UUID!");

            Map<Material, Integer> configSectionMap = new HashMap();
            Set<String> materialStrSet = historyConfig_.getConfigurationSection(uuidStr).getKeys(false);

            Material material;
            int amount;
            for (String materialStr : materialStrSet) {
                material = Material.getMaterial(materialStr);

                if (material != null) {
                    amount = historyConfig_.getInt(uuidStr + "." + materialStr);
                    configSectionMap.put(material, amount);
                    Demeter.log(uuidStr + ": " + materialStr + ", " + amount);
                }
            }
            //ConfigurationSection section = historyConfig_.getConfigurationSection(uuidStr);
            //Map<String, Object> configSectionMap = section.getValues(true);
            historyMap_.put(player, new FoodHistory(configSectionMap));
        } else {
            Demeter.log("Could not find UUID!");
            historyMap_.put(player, new FoodHistory());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        savePlayer(player);
        historyMap_.remove(player);
    }


}
