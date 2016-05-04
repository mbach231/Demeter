/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mbach231.demeter.food;

import org.bukkit.Material;

/**
 *
 *
 */
public class Food {

    
    public static boolean isFood(Material mat) {
        return mat.isEdible();
    }
    

}
