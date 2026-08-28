package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups;

import com.balugaq.jeg.utils.GuideUtil;
import com.balugaq.jeg.utils.clickhandler.OnDisplay;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

/**
 * 供 init_script 快速操作物品组菜单的帮助类。
 * 典型用法（脚本内）：
 * <pre>
 * function init_script(menu, handler, group) {
 *     handler.move(0).to(1);
 *     handler.whenJEG().copy(2).to(3);
 * }
 * </pre>
 */
@NullMarked
public class MenuHandler {
    private final ChestMenu menu;
    private final @Nullable Player player;
    private final boolean enabled;

    public MenuHandler(ChestMenu menu, @Nullable Player player) {
        this(menu, player, true);
    }

    private MenuHandler(ChestMenu menu, @Nullable Player player, boolean enabled) {
        this.menu = menu;
        this.player = player;
        this.enabled = enabled;
    }

    /** JustEnoughGuide 存在时，后续操作才会生效。 */
    public MenuHandler whenJEG() {
        return new MenuHandler(menu, player, RykenSlimefunCustomizer.jeg);
    }

    /** JustEnoughGuide 不存在时，后续操作才会生效。 */
    public MenuHandler whenNoJEG() {
        return new MenuHandler(menu, player, !RykenSlimefunCustomizer.jeg);
    }

    /** 移动槽位的物品和点击事件。 */
    public Operation move(int from) {
        return new Operation(OpType.MOVE, from, null);
    }

    /** 复制槽位的物品和点击事件（源槽位保留）。 */
    public Operation copy(int from) {
        return new Operation(OpType.COPY, from, null);
    }

    /** 仅移动槽位的物品。 */
    public Operation moveItem(int from) {
        return new Operation(OpType.MOVE_ITEM, from, null);
    }

    /** 仅移动槽位的点击事件。 */
    public Operation moveHandler(int from) {
        return new Operation(OpType.MOVE_HANDLER, from, null);
    }

    /** 添加子物品组的显示到目标槽位。 */
    public Operation apply(ItemGroup sub) {
        return new Operation(OpType.APPLY, -1, sub);
    }

    private enum OpType {
        MOVE,
        COPY,
        MOVE_ITEM,
        MOVE_HANDLER,
        APPLY
    }

    public class Operation {
        private final OpType type;
        private final int from;
        private final @Nullable ItemGroup sub;

        private Operation(OpType type, int from, @Nullable ItemGroup sub) {
            this.type = type;
            this.from = from;
            this.sub = sub;
        }

        public void to(int to) {
            if (!enabled) return;

            switch (type) {
                case MOVE -> {
                    ItemStack item = menu.getItemInSlot(from);
                    ChestMenu.MenuClickHandler handler = menu.getMenuClickHandler(from);
                    menu.addItem(to, item, handler);
                    clearSlot(from);
                }
                case COPY -> menu.addItem(to, menu.getItemInSlot(from), menu.getMenuClickHandler(from));
                case MOVE_ITEM -> {
                    menu.addItem(to, menu.getItemInSlot(from));
                    menu.addItem(from, null);
                }
                case MOVE_HANDLER -> {
                    menu.addMenuClickHandler(to, menu.getMenuClickHandler(from));
                    menu.addMenuClickHandler(from, ChestMenuUtils.getEmptyClickHandler());
                }
                case APPLY -> {
                    if (sub != null) {
                        applySub(sub, to);
                    }
                }
            }
        }
    }

    private void clearSlot(int slot) {
        menu.addItem(slot, null);
        menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
    }

    private void applySub(ItemGroup sub, int slot) {
        if (RykenSlimefunCustomizer.jeg && player != null) {
            OnDisplay.ItemGroup.display(player, sub, OnDisplay.ItemGroup.DisplayType.Normal, GuideUtil.getLastGuide(player))
                .at(menu, slot, 1);
            return;
        }

        if (player == null) {
            return;
        }

        menu.addItem(slot, sub.getItem(player), (pl, s, clickedItem, action) -> {
            if (pl == null) return false;
            PlayerProfile.find(pl).ifPresent(profile -> {
                SlimefunGuideMode mode = RykenSlimefunCustomizer.jeg
                    ? GuideUtil.getLastGuideMode(pl)
                    : SlimefunGuide.getDefaultMode();
                SlimefunGuide.openItemGroup(profile, sub, mode, 1);
            });
            return false;
        });
    }
}
