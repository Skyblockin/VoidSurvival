# Item Deserializer Specification
This document will cover every possible option for the item deserializer used in Tannslee's plugins

## Table of Contents
1. [Non-vanilla Types](#Non-vanilla-types)
	1. [RangedValue](#rangedvalue)
	2. [MiniMessage string](#minimessage-string)
2. [Vanilla Types](#vanilla-types)
	1. [ConsumeEffect](#consumeeffect)
	2. [PotionEffect](#potioneffect)
	3. [PotionEffectType](#potioneffecttype)
	4. [Attribute](#attribute)
	5. [AttributeModifier](#attributemodifier)
	6. [EquipmentSlotGroup](#equipmentslotgroup)
	7. [EquipmentSlot](#equipmentslot)
	8. [RegistryKeySet](#registrykeyset)
3. [Possible Item Fields](#possible-item-fields)
	1. [Item type](#item-type)
	2. [Item amount](#item-amount)
	3. [Item Durability](#item-durability)
	4. [Rarity](#Rarity)
	5. [Custom Name](#custom-name)
	6. [Lore](#lore)
	7. [Item Enchantments](#item-enchantments)
	8. [Stored Enchantments](#stored-enchantments)
	9. [Attribute Modifiers](#attribute-modifiers)
	10. [Tooltip Display](#tooltip-display)
	11. [Repair Cost](#repair-cost)
	12. [Enchantment Glint Override](#enchantment-glint-override)
	13. [Intangible Projectile](#intangible-projectile)
	14. [Food Properties](#food-properties)
	15. [Consumable Properties](#consumable-properties)
	16. [Use Remainder](#use-remainder)
	17. [Use Cooldown](#use-cooldown)
	18. [Damage Resistant](#damage-resistant)
	19. [Tool Properties](#tool-properties)
	20. [Enchantable](#enchantable)
	21. [Equippable](#equippable)
	22. [Repairable](#repairable)
	23. [Glider](#glider)
	24. [Tooltip Style](#tooltip-style)
	25. [Death Protection](#death-protection)
	26. [Blocks Attacks (Shield)](#blocks-attacks)
	27. [Dyed Item Color](#dyed-item-color)
	28. [Potion Contents](#potion-contents)
	29. [Writable Book Content](#writable-book-content)
	30. [Written Book Content](#written-book-content)
	31. [Armor Trim](#armor-trim)
	32. [Profile](#profile)
	33. [Item Container Contents](#item-container-contents)
	34. [Break Sound](#break-sound)
    35. [Entity Data](#entity-data)
    36. [Spawner Data](#spawner-data)
	37. [Custom Data](#custom-data) 




## Non-vanilla types
### RangedValue
This is a value that can either be a fixed integer or an integer range which the value is randomly chosen from upon item creation. A valued range will be in the form <[low, min]\> while a fixed value will be in the form \<value\>. 
<br>Example 1: `"amount": 1` is valid
<br>Example 2: `"amount": [1, 3]` is valid
<br>Example 3: `"amount": [1, 2, 3]` is NOT valid

### MiniMessage string
This is a string which will be parsed into a component upon deserialization. It has full support for the [MiniMessage format](https://docs.papermc.io/adventure/minimessage/format/), making colorful names and lore easy to create.

## Vanilla types
### ConsumeEffect
Term for four different effect types that can happen when consuming an item. The types are "apply_effects", "remove_effects", "teleport", "play_sound".

Apply effects example: 
```
{
    "type": "apply_effects",
    "effects": <list of potion effects>,
    "probability": <probability between 0 and 1> 
}
```

Remove effects example
```
{
    "type": "remove_effects",
    "effects": <list of potion effect types>
}
```

Teleport example:
```
{
    "type": "teleport",
    "diameter": <diameter to teleport in>
}
```

Play sound example:
```
{
    "type": "play_sound",
    "sound" <sound key>
}
```

### PotionEffect
An object representing a potion effect. Format:
```
{
    "type": "<potion effect type>",
    "duration": <duration in ticks, optional, defaults to -1 (infinite)>,
    "level": <potion effect level, optional, defaults to 1>,
    "ambient": boolean, optional, defaults to false,
    "particles": boolean, optional, defaults to true,
    "icon": boolean, optional, defaults to true
}
```

### PotionEffectType
A potion effect type. See https://minecraft.wiki/w/Effect#Effects for a list of effect identifiers. If the namespace is not defined, it should default to `minecraft`.

### Attribute
An attribute. See https://minecraft.wiki/w/Attribute for a list of attributes and their identifiers.

### AttributeModifier
An attribute modifier used to apply a modifier to an attribute. The modifier consists of an amount, an operation and an [EquipmentSlotGroup](#equipmentslotgroup). The possible operations are "add_number", "add_scalar" and "multiply_scalar_1". "Add number" adds the amount directly to the base amount, "add_scalar" multiplies the base amount by the amount and "multiply_scalar_1" multiplies the base amount by `1 + amount`
Format: 
```
{
    "name": <any unique name>,
    "amount": <floating point number>,
    "operation": <operation>,
    "slot": EquipmentSlotGroup
}
```

### EquipmentSlotGroup
A value representing a slot or a group of slots. Valid values for this are "any", "mainhand", "offhand", "hand", "feet", "legs", "chest", "head", "armor" and "body".

### EquipmentSlot
A value representing a slot. Valid values for this are "hand", "off_hand", "feet", "legs", "chest", "head".

### RegistryKeySet
A registry key set can either be a single registry key (for example minecraft:stone), a list of registry keys or a tag. For more information on what tags are,  see https://minecraft.wiki/w/Tag_(Java_Edition). However in short, tags are predefined lists of registry keys which can be referred to with `#<tag name>`.

### DamageReduction
An object giving instructions on how much resistance against what damage types should be given. For a list of damage types, see https://minecraft.wiki/w/Damage_type_tag_(Java_Edition)
Format:
```
{
    "damage_type": <damage type tag key>,
    "factor": <float>,
    "base": <float>,
    "horizontal_angle": <float>
}
```
The factor means the percent of damage reduction, the base value is the flat amount of damage reduction. The horizontal angle is the maximum angle between the direction of the player and the direction of the incoming attack.

### ItemDamageFunction
An object representing when an how much damage an item should take. The threshold is the minimum amount of damage that must be taken before the item takes damage. The base amount is the flat amount of damage applied to the item and the factor is a percentage of the incoming damage applied to the item.
Example:
```
{
    "base": <float>,
    "factor": <float>,
    "threshold": <float>
}
```

## Possible Item Fields

### Item type
The item type. The value is the exact item ID found in-game when hovering over an item with F3 + H enabled.
<br>Example 1: `"id": "minecraft:iron_ingot"`

### Item amount
The item amount. The value is a [RangedValue](#rangedvalue).
<br>Example 1: `"amount": 1`

### Item durability
You can set the maximum damage of an item with the "max_damage" key. The current damage of the item is set with the "damage" key. The current damage of the item is a [RangedValue](#rangedvalue) while the maximum damage is a fixed integer.
<br>Example 1: `"max_damage": 250`  gives the item a maximum damage value of 250. In practice this means maximum 250 durability, or what a vanilla iron pickaxe has.
<br>Example 2: `"damage": [1, 3]` gives the item a random damage value between 1 and 3. For an item with 250 maximum durability, this means the item will have 247 to 249 durability.
<br>Example 3: `"damage": 1`
<br>This creates an item with a fixed 1 damage, or with the above example, 249 durability.

To make an item unbreakable, you can simply set the "unbreakable" flag to true.
<br>Example 4:  `"unbreakable": true`

### Custom Name
A [MiniMessage string](#minimessage-string) for the item's display name.
<br>Example 1: `"name": "<red>Red Name"`

### Lore
A list of [MiniMessage string](#minimessage-string)s for the item lore.
<br>Example 1: 
```
"lore": [
    "<red>First line with red color",
    "<green>Second line with green color"
]
```

### Rarity
The item's vanilla rarity. It has 4 possible values, which are "common", "uncommon", "rare" and "epic". An invalid value will result in no rarity being set.
<br>Example 1: `"rarity": "common"`

### Item enchantments
A map of the enchantments on an item. The values are [RangedValue](#rangedvalue)s, thus allowing for items with random enchantment levels.
<br>Example 1:
```
"enchantments": {
    "minecraft:unbreaking": [1, 3],
    "voidsurvival:bleed": 1
}
```

### Stored Enchantments
The exact same as [Item enchantments](#item-enchantments), except for enchanted books so the enchantments on the book can be applied to other items on an anvil.
<br>Example 1:
```
"stored_enchantments": {
    "minecraft:unbreaking": 3,
    "minecraft:sharpness": 3
}
```

### Attribute Modifiers
This is possibly one of the most powerful fields. It is a map consisting of [Attribute](#attribute)s as keys and [AttributeModifier](#attributemodifier)s as values. The "name" field is optional, the server will generate a random UUID to replace it instead. The slot is technically optional and will default to "any", but it is still highly discouraged to leave it out and it will generate an error.
<br>Example:
```
"attributes": {
    "minecraft:movement_speed": {
        "name": "quick_feet",
        "amount": 1.2,
        "operation": "add_scalar",
        "slot": "feet"
    }
}
```

### Tooltip Display
A list of components to hide from the item's tooltip.
<br>Example 1:
```
"tooltip_display": [
    "unbreakable",
    "lore"
]
```
This would hide the lore and unbreakable tag of the item.

### Repair Cost
The additional cost to repair the item on an anvil. By default, this is 0.
<br>Example 1: `"repair_cost": 10`

### Enchantment Glint Override
A flag to make the item appear enchanted regardless of enchantments actually being present on the item.
<br>Example 1: `"glint" true`

### Intangible Projectile
A flag to make a projectile item intangible when fired. Basically this means the projectile can only be picked up in creative mode.
<br>Example 1: `"intangible_projectile": true`

### Food Properties
A component to set the food properties of the item. This component can make any item edible and restore hunger.
<br>Example: 
```
"food": {
    "nutrition": <integer of half hunger bars, defaults to 0>,
    "can_always_eat": boolean, defaults to false,
    "saturation": <float, defaults to 0> 
}
```

### Consumable Properties
A component to set the consumable properties of the item. This is similar to food, but this component allows the application of effects when the item is consumed.
<br>Example 1 (much simpler usage):
```
"consumable": {
    "effect_chance": <float from 0 to 1, defaults to 1>,
    "effects": <list of potion effects>
}
```

Example 2 (allows for more complex effects, see [ConsumeEffect](#consumeeffect)):
```
"consumable": {
    "effects": <list of consume effects>
}
```

### Use Remainder
A component to set the item this item turn's into when it is used. Examples of vanilla items are a water bucket when it is emptied or a bowl of soup when it is eaten. The key for this component is "use_remainder", and the value is another item, which can be defined the exact same way this guide is trying to explain.
<br>Example 1: 
```
"use_remainder": {
    "id": "minecraft:iron_ingot",
    "name": "<green>Remainder ingot"
}
``` 

### Use Cooldown
The use cooldown of this item. The group key can be anything as long as it is in the form namespace:key.
<br>Format: 
```
"use_cooldown": {
    "cooldown": <float, defaults to 0>
    "group": <group key, optional>
}
```

### Damage Resistant
A component to set the damage resistances of this item, such as making it fireproof. Simply a damage type tag. See https://minecraft.wiki/w/Damage_type_tag_(Java_Edition) for a list of damage type tags.

### Tool Properties
A component to set the tool properties of the item. Damage per block and default mining speed specify those values if there is no rule set for the block being broken. The [RegistryKeySet](#registrykeyset) is a set of registry keys which dictate what block types the specific rule applies to.

<br>Example: 
```
"tool": {
    "rules": [
        {
            "type": RegistryKeySet
            "default_mining_speed": float,
            "correct_tool": boolean
        }
    ],
    "damage_per_block": <integer, optional, defaults to 1>,
    "default_mining_speed": <float, optional, defaults to 1>
}
```

### Weapon
A component to set the weapon properties of the item. The current API available for this is very lacking and this component is not very useful.
<br>Example:
```
"weapon": {
    "attack_damage": <integer>,
    "disable_blocking_seconds" <float, optional, defaults to 0>
}
```

### Enchantable
Very strange component that somehow affects whether the item can be enchanted.
<br>Example: `"enchantable": 1`


### Equippable
A component that allows setting armor-like properties of the item. This has not yet been fully implemented. The valid slot is an [EquipmentSlot](#equipmentslot) and the equip sound is the registry key referring to a sound. A good way to discover good sounds to use is to play with the vanilla /playsound command in-game.
<br>Example of current functionality:
```
"equippable": {
    "valid_slot": <equipment slot>,
    "equip_sound": <sound key>
}
```

### Repairable
A [RegistryKeySet](#registrykeyset) of items the item can be repaired with.
<br>Example:
```
"repairable": [
    "minecraft:diamond",
    "minecraft:dirt"
]
```

### Glider
A flag to set whether or not the item is a glider. The only vanilla glider is the Elytra.
<br>Example: `"glider": true`

### Tooltip Style
A key for a tooltip style. I am unsure as of right now how this works, but I added it either way because it is a single key value. See https://minecraft.wiki/w/Data_component_format#tooltip_style for more information.
<br>Example: `"tooltip_style": <key>`

### Death Protection
Allows defining what the item does when the player holding it dies. It is a list of [ConsumeEffect](#consumeeffect)s.
<br>Example:
```
"death_protection": [
    {
        "type": "apply_effects",
        "effects": <list of potion effects>,
        "probability": <probability between 0 and 1> 
    }
]
```

### Blocks Attacks
Weirdly named component that allows that item to behave like a shield. For the damage reduction format, see [DamageReduction](#damagereduction). For the item damage function format, see [ItemDamageFunction](#itemdamagefunction). The disable cooldown scale is the multiplier applied to however long a weapon will disable the item blocking an attack.

<br>Format:
```
"blocks_attacks": {
    "damage_reductions": <list of damage reductions>,
    "item_damage": <item damage function>,
    "disable_cooldown_scale": <float, optional, defaults to 1>,
    "block_delay": <float, defaults to 0>,
    "block_sound": <sound key, optional>
    "disable_sound": <sound key, optional>
}
```

### Dyed Item Color
The color of leather armor.
<br>Example: `"color": "0xff000"`

### Potion Contents
A list of [PotionEffect](#potioneffect)s.
<br>Example: 
```
"potion_contents": [
    {
        "type": "minecraft:fire_resistance",
        "duration": 300
    }
]
```

### Writable Book Content
The raw text contained in a book that can still be written to.
<br>Format: 
```
"writable_cook_content": [
    "page 1 text",
    "page 2 text"
]
```

### Written Book Content
<br>Format:
```
"written_book_content": {
    "author": "Tannslee",
    "title": "Written Book",
    "generation": <integer, optional, defaults to 0>
    "resolved": <boolean, optional, defaults to true>
    "pages": <list of MiniMessage strings>
}
```

### Armor Trim
The armor trim on armor. See https://minecraft.wiki/w/Smithing_Template#Variants for information on identifiers for armor trim patterns and materials.

<br>Format:
```
"armor_trim": {
    "material": <trim material>,
    "pattern": <trim pattern>
}
```

### Profile
A player profile object mostly to allow the creation of custom player heads. There are many websites you can use to generate the base64 string required to fetch all the necessary information. One such website is https://minecraft-heads.com/tools/head-command-generator
<br>Example: `"base64": <base64 string>`

### Item Container Contents
A list of ItemData objects.
<br>Example:
```
"container_contents": [
    {
    	"id": "minecraft:iron_ingot",
	    "amount": [1, 3]
    },
    {
        "id": "minecraft:gold_ingot",
        "amount": 2
    }
]
```

### Break Sound
The sound to play when the item breaks. For more information on sound keys, see https://minecraft.wiki/w/Sound
<br>Format: `"break_sound": <sound key>`

### Entity Data
Format
```
{
    "persistent": boolean, optional, false
    "id": entity id,
    "customName": MiniMessage string,
    "attributes": attribute name -> float map (like vanilla),
    "equipment": EquipmentSlot -> ItemData map,
    "drop_chances": EquipmentSlot -> Float map
}
```


### Spawner Data
Format
```
{
    "min_spawn_delay": ticks, optional, default 200,
    "max_spawn_delay": ticks, optional, default 800,
    "spawn_delay": ticks, optional, default 0,
    "spawn_count": integer, optional, default 4,
    "max_nearby_entities": integer, optional, default 4,
    "required_player_range": integer, optional, default 16,
    "spawn_range": integer, optional, default 4,
    "entity_data": entity data object
}
```

### Custom Data
This is a component allowing you to store any arbitrary JSON data on the item. This is mostly useful for custom items that require a custom item ID to identify the item.
<br>Example:
```
"custom_data": {
    "item_id": "avaritia"
}
```




