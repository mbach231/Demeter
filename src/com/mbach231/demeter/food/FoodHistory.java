/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mbach231.demeter.food;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;

/**
 *
 *
 */
public class FoodHistory {

    private final Map<Material, Integer> foodHistoryMap_;

    public FoodHistory() {
        foodHistoryMap_ = new ConcurrentHashMap();
    }
    
    public FoodHistory(Map<Material, Integer> historyMap) {
        foodHistoryMap_ = historyMap;
    }

    public void consumedMaterial(Material mat) {
        if (Food.isFood(mat)) {

            int currentValue = foodHistoryMap_.containsKey(mat) ? foodHistoryMap_.get(mat) : 0;
            foodHistoryMap_.put(mat, currentValue + 1);

        }
    }

    public int getFoodAmount(Material mat) {
        return foodHistoryMap_.containsKey(mat) ? foodHistoryMap_.get(mat) : 0;
    }

    public void lowerHistoryAmounts() {
        Set<Entry<Material,Integer>> entrySet = foodHistoryMap_.entrySet();
        for (Map.Entry<Material, Integer> entry : entrySet) {

            if (entry.getKey() != null && entry.getValue() != null) {
                Material mat = entry.getKey();
                int amount = entry.getValue();

                amount--;

                if (amount <= 0) {
                    foodHistoryMap_.remove(mat);
                } else {
                    foodHistoryMap_.put(mat, amount);
                }
            }

        }
    }

    // Used for saving to history on player exit
    public Set<Entry<Material, Integer>> getEntrySet() {
        return foodHistoryMap_.entrySet();
    }

}
