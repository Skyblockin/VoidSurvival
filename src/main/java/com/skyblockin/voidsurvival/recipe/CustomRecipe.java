package com.skyblockin.voidsurvival.recipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.ItemData;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.util.Functions;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class CustomRecipe {

    public static CustomRecipe fromJson(NamespacedKey key, JsonNode node) {

        RegistryKeySet<@NotNull ItemType> unlockedBy = null;
        Recipe recipe = parseRecipe(key, node);

        if (node.has("unlocked_by")) {
            unlockedBy = Functions.parseRegistryKeySet(RegistryKey.ITEM, node.get("unlocked_by"));
        }

        return new CustomRecipe(recipe, unlockedBy);
    }

    private static Recipe parseRecipe(NamespacedKey key, JsonNode node) {

        String type = node.get("type").asText();
        ItemData result = Json.convert(node.get("result"), ItemData.class);

        if ("shaped".equalsIgnoreCase(type)) {

            ShapedRecipe shapedRecipe = new ShapedRecipe(key, result.createItem());

            JsonNode shape = node.get("shape");
            JsonNode ingredients = node.get("ingredients");

            shapedRecipe.shape(
                shape.get(0).asText(),
                shape.get(1).asText(),
                shape.get(2).asText()
            );

            ingredients.fields().forEachRemaining(entry -> {

                char ingredientKey = entry.getKey().charAt(0);
                JsonNode value = entry.getValue();

                try {
                    shapedRecipe.setIngredient(ingredientKey, Json.convert(value, ItemData.class).createItem());
                } catch (Exception ex) {
                    VoidSurvival.logError(String.format("Failed to parse recipe '%s' ingredient '%c'", key, ingredientKey), ex);
                }

            });

            return shapedRecipe;

        } else {

            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result.createItem());

            JsonNode ingredients = node.get("ingredients");

            ingredients.elements().forEachRemaining(ingredient ->
                shapelessRecipe.addIngredient(Json.convert(ingredient, ItemData.class).createItem())
            );

            return shapelessRecipe;
        }

    }

    private final Recipe recipe;
    private final RegistryKeySet<@NotNull ItemType> unlockedBy;

    public CustomRecipe(Recipe recipe, RegistryKeySet<@NotNull ItemType> unlockedBy) {
        this.recipe = recipe;
        this.unlockedBy = unlockedBy;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public boolean isUnlockedBy(ItemType type) {
        if (unlockedBy == null) return true;
        return unlockedBy.contains(TypedKey.create(RegistryKey.ITEM, type.getKey().getKey()));
    }

}
