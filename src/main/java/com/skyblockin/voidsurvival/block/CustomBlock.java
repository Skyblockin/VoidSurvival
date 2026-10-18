package com.skyblockin.voidsurvival.block;

import com.fasterxml.jackson.databind.JsonNode;
import com.skyblockin.voidsurvival.VoidSurvival;
import com.skyblockin.voidsurvival.config.Json;
import com.skyblockin.voidsurvival.item.ItemData;
import com.skyblockin.voidsurvival.util.FileUtil;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;

import java.io.File;
import java.util.HashMap;

public class CustomBlock {

    private static final CustomBlock[] LUT = new CustomBlock[27 * 2 * 7];
    private static final HashMap<String, CustomBlock> byIdMap = new HashMap<>();

    private static final int STRIDE_INSTRUMENT = 14;
    private static final int STRIDE_OCTAVE = 7;

    public static CustomBlock ofBlockData(BlockData data) {
        if (data instanceof NoteBlock noteBlock) {
            int instrumentOrdinal = noteBlock.getInstrument().ordinal();
            int noteOctave = noteBlock.getNote().getOctave();
            int noteTone = noteBlock.getNote().getTone().ordinal();
            return LUT[(instrumentOrdinal * STRIDE_INSTRUMENT) + (noteOctave * STRIDE_OCTAVE) + noteTone];
        }
        return null;
    }

    public static CustomBlock ofBlock(Block block) {
        return ofBlockData(block.getBlockData());
    }

    public static CustomBlock getById(String id) {
        return byIdMap.get(id);
    }

    private static CustomBlock register(String id, Instrument instrument, Note note) {
        CustomBlock block = new CustomBlock(id, instrument, note);
        byIdMap.put(id, block);
        LUT[(instrument.ordinal() * STRIDE_INSTRUMENT) + (note.getOctave() * STRIDE_OCTAVE) + note.getTone().ordinal()] = block;
        return block;
    }

    public static final CustomBlock DEUXITE_ORE = register("deuxite_ore",
        Instrument.PIANO, new Note(0)
    );
    public static final CustomBlock MITHRIL_ORE = register("mithril_ore",
        Instrument.PIANO, new Note(1)
    );
    public static final CustomBlock PIGNITE_ORE = register("pignite_ore",
        Instrument.PIANO, new Note(2)
    );

    public static void load() {

        try {

            int validLoads = 0;

            for (File file : FileUtil.listFiles("Custom Blocks")) {

                if (!file.getName().endsWith(".json")) {
                    continue;
                }

                // Leave out the .json
                String fileName = file.getName().substring(0, file.getName().length() - 5);
                CustomBlock block = getById(fileName);

                if (block == null) {
                    VoidSurvival.logError("Tried to access nonexistent custom block with id '%s'", fileName);
                    continue;
                }

                JsonNode node = Json.readFromFile(file);

                block.requiredToolTier = node.path("required_tool_tier").asInt(-1);
                block.requiredToolType = node.path("required_tool_type").asInt(-1);
                block.toughness = (float) node.get("toughness").asDouble();
                block.breakResult = Json.convert(node.get("reward"), ItemData.class);
                block.breakSound = node.path("break_sound").asText("minecraft:block.stone.break");

                validLoads++;
            }

            VoidSurvival.logInfo("Loaded stats for %d custom blocks", validLoads);

        } catch (Exception ex) {
            VoidSurvival.logError("Failed to load custom block data!", ex);
        }

    }

    // Using notes and instruments to identify blocks
    private final String id;
    private final Instrument instrument;
    private final Note note;

    public int requiredToolTier = -1;
    public int requiredToolType = -1;
    public float toughness = -1;
    public ItemData breakResult = new ItemData();
    public BlockData data = Material.OBSIDIAN.createBlockData();
    public String breakSound = "minecraft:block.stone.break";

    public CustomBlock(String id, Instrument instrument, Note note) {
        this.id = id;
        this.instrument = instrument;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Note getNote() {
        return note;
    }

}
