package com.skyblockin.voidsurvival.recipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.util.FileUtil;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RecipeManager implements Listener {

    private final HashMap<NamespacedKey, CustomRecipe> recipeMap = new HashMap<>();

    @EventHandler
    public void onNewItemFound(PlayerInventorySlotChangeEvent event) {

        Player player = event.getPlayer();
        ItemStack newStack = event.getNewItemStack();
        ItemStack oldStack = event.getOldItemStack();

        if (!newStack.isEmpty() && newStack.getType() != oldStack.getType()) {
            recipeMap.forEach((key, recipe) -> {
                if (recipe.isUnlockedBy(newStack.getType().asItemType()) && !player.hasDiscoveredRecipe(key)) {
                    player.discoverRecipe(key);
                }
            });
        }
    }

    public void loadRecipes() {

        int count = 0;

        for (File recipeFile : FileUtil.listFiles("recipes")) {

            try {

                JsonNode node = Json.readFromFile(recipeFile);;

                this.recipeMap.clear();

                for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {

                    Map.Entry<String, JsonNode> entry = it.next();

                    NamespacedKey key = new NamespacedKey(VoidSurvival.getInstance(), entry.getKey());

                    CustomRecipe recipe = CustomRecipe.fromJson(key, entry.getValue());

                    this.recipeMap.put(key, recipe);

                    Bukkit.removeRecipe(key);
                    Bukkit.addRecipe(recipe.getRecipe());

                    count++;
                }

            } catch (Exception ex) {
                VoidSurvival.logError("Failed to load recipes from file '" + recipeFile.getName() + "'", ex);
            }

        }

        Bukkit.updateRecipes();

        VoidSurvival.logInfo("Loaded %d custom recipes", count);
    }

}
