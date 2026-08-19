package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import com.balugaq.jeg.utils.GuideUtil;
import com.balugaq.jeg.utils.clickhandler.OnDisplay;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

public class ClickableDisplay {
    public static void display(Player player, ChestMenu inv, int slot, ItemStack stack) {
        if (RykenSlimefunCustomizer.jeg) {
            OnDisplay.Item.display(player, stack, OnDisplay.Item.DisplayType.Normal, GuideUtil.getLastGuide(player))
                .at(inv, slot, 1);
        } else {
            inv.addItem(slot, stack, ChestMenuUtils.getEmptyClickHandler());
        }
    }
}
