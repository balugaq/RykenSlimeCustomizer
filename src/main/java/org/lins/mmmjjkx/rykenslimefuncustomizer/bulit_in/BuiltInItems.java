package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in;

import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@NullMarked
public class BuiltInItems {
    private static final Map<String, ItemStack> stacks = new HashMap<>();

    static {
        stacks.put("slimefun_guide_survival", Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE).getItem());
        stacks.put("slimefun_guide_cheat", Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.CHEAT_MODE).getItem());
        stacks.put("background", ChestMenuUtils.getBackground());
        stacks.put("no_permission_item", ChestMenuUtils.getNoPermissionItem());
        stacks.put("not_researched", ChestMenuUtils.getNotResearchedItem());
        stacks.put("input_slot_texture", ChestMenuUtils.getInputSlotTexture());
        stacks.put("output_slot_texture", ChestMenuUtils.getOutputSlotTexture());
        stacks.put("wiki_button", ChestMenuUtils.getWikiButton());
    }

    @Nullable
    public static ItemStack createStack(String material) {
        var stack = stacks.get(material);
        if (stack != null) return stack.clone();
        return null;
    }
}
