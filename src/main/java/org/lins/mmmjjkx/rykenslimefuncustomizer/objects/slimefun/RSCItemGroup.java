/*
 * RykenSlimefunCustomizer
 * Copyright (C) 2026 lijinhong11(mmmjjjkx) and balugaq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun;

import com.balugaq.jeg.utils.GuideUtil;
import com.balugaq.jeg.utils.formatter.Format;
import com.balugaq.jeg.utils.formatter.Formats;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ban.CommandSafe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class RSCItemGroup extends FlexItemGroup {
    private final List<Object> contents;
    private final ProjectAddon addon;
    private final GroupType type;
    private final Visible visible;
    private final boolean forceHidden;
    private final boolean hasParent;

    public RSCItemGroup(NamespacedKey key, ItemStack item, int tier, ProjectAddon addon, GroupType type, Visible visible, boolean forceHidden, boolean hasParent) {
        super(key, item, tier);

        ExceptionHandler.debugLog(() -> "创建物品组: " + key + " type=" + type.name());

        contents = new ArrayList<>();
        this.addon = addon;
        this.type = type;
        this.visible = visible;
        this.forceHidden = forceHidden;
        this.hasParent = hasParent;
    }

    public void addContent(SlimefunItem sf) {
        ExceptionHandler.debugLog(() -> "已添加物品 " + sf.getId() + " 至 " + getKey());
        contents.add(sf);
    }

    public void addContent(ItemGroup itemGroup) {
        ExceptionHandler.debugLog(() -> "已添加物品组 " + itemGroup.getKey().getKey() + " 至 " + getKey());
        contents.add(itemGroup);
    }

    public void addContent(String action) {
        ExceptionHandler.debugLog(() -> "已添加 Action " + action + " 至 " + getKey());
        contents.add(action);
    }

    @Override
    public boolean isVisible/*InMainMenu*/(@NonNull Player p, @NonNull PlayerProfile profile, @NonNull SlimefunGuideMode layout) {
        if (forceHidden || hasParent || type == GroupType.sub || type == GroupType.button) return false;
        if (type == GroupType.nested || type == GroupType.normal) {
            return true; // compatibility
        }
        // type == GroupType.seasonal && !hasParent
        return visible.apply(p, profile, layout);
    }

    public boolean isVisibleInNested(@NonNull Player p, @NonNull PlayerProfile profile, @NonNull SlimefunGuideMode layout) {
        if (forceHidden) return false;

        return visible.apply(p, profile, layout);
    }

    @Override
    public void open(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        setup(p, profile, mode, 1);
    }

    public boolean isContentVisibleInGroup(Object content, Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        switch (content) {
            case RSCItemGroup itemGroup -> { return itemGroup.isVisibleInNested(p, profile, mode); }
            case SlimefunItem sf -> { return !sf.isDisabledIn(p.getWorld()); }
            case String action -> { return true; }
            default -> {
                ExceptionHandler.handleError("物品组 " + getKey().getKey() + " 中存在未知内容: " + content);
                return false;
            }
        }
    }

    private void jegSetup(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        ChestMenu menu = new ChestMenu(GuideUtil.getGuideTitle(mode));
        Format format = type == GroupType.nested ? Formats.nested : Formats.sub;
        char c = type == GroupType.nested ? Formats.Char.ITEM_GROUP : Formats.Char.CONTENT;
        List<Object> validContent = this.contents.stream().filter(content -> isContentVisibleInGroup(content, p, profile, mode)).toList();
        int pages = (validContent.size() - 1) / format.getChars(c).size() + 1;
        GuideUtil.commonRender(menu, format, profile, p, this, page, pages, np -> {
            setup(p, profile, mode, np);
        });

        for (int i = 0; i < format.getChars(c).size(); i++) {
            int s = format.getChars(c).get(i);
            if (page * format.getChars(c).size() + i >= validContent.size()) {
                menu.addItem(s, null);
                menu.addMenuClickHandler(s, (clicker, slot, item, action) -> false);
                continue;
            }

            Object content = validContent.get((page - 1) * format.getChars(c).size() + i);
            handleContent(s, content, menu, p, profile, mode);
        }
    }

    protected void handleContent(int s, Object content, ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        switch (content) {
            case RSCItemGroup itemGroup -> {
                menu.addItem(s, itemGroup.getItem(p));
                menu.addMenuClickHandler(s, (pl, slot, item, action) -> {
                    if (itemGroup.type == GroupType.button) {
                        // Don't open the item group, but run the scripts
                        for (var o : itemGroup.contents) {
                            if (o instanceof String ac) {
                                readAction(ac, mode, pl, slot, item, action);
                            }
                        }
                        return false;
                    }
                    SlimefunGuide.openItemGroup(profile, itemGroup, mode, 1);
                    return false;
                });
            }
            case SlimefunItem sf -> {
                menu.addItem(s, sf.getItem());
                menu.addMenuClickHandler(s, (pl, slot, item, action) -> {
                    SlimefunGuide.displayItem(profile, sf, true);
                    return false;
                });
            }
            default -> throw new IllegalStateException("Unexpected value: " + content);
        }
    }

    private void setup(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        if (RykenSlimefunCustomizer.jeg) {
            jegSetup(p, profile, mode, page);
        } else {
            legacySetup(p, profile, mode, page);
        }
    }
    
    private void legacySetup(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        GuideHistory history = profile.getGuideHistory();
        if (mode == SlimefunGuideMode.SURVIVAL_MODE) {
            history.add(this, page);
        }

        ChestMenu menu = new ChestMenu(Slimefun.getLocalization().getMessage(p, "guide.title.main"));
        SurvivalSlimefunGuide guide =
            (SurvivalSlimefunGuide) Slimefun.getRegistry().getSlimefunGuide(mode);
        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(SoundEffect.GUIDE_BUTTON_CLICK_SOUND::playFor);
        guide.createHeader(p, profile, menu);
        menu.addItem(
            1,
            new CustomItemStack(ChestMenuUtils.getBackButton(
                p, "", ChatColor.GRAY + Slimefun.getLocalization().getMessage(p, "guide.back.guide"))));
        menu.addMenuClickHandler(1, (pl, s, is, action) -> {
            SlimefunGuide.openMainMenu(profile, mode, history.getMainMenuPage());
            return false;
        });

        int index = 9;
        int target = 36 * (page - 1) - 1;

        while (target < this.contents.size() - 1 && index < 45) {
            ++target;
            Object content = this.contents.get(target);
            switch (content) {
                case RSCItemGroup itemGroup -> {
                    if (itemGroup.isVisible(p, profile, mode)) {
                        menu.addItem(index, itemGroup.getItem(p));
                        menu.addMenuClickHandler(index, (pl, slot, item, action) -> {
                            // Don't open the item group, but run the scripts
                            for (var o : itemGroup.contents) {
                                if (o instanceof String ac) {
                                    readAction(ac, mode, pl, slot, item, action);
                                }
                            }
                            SlimefunGuide.openItemGroup(profile, itemGroup, mode, 1);
                            return false;
                        });
                        ++index;
                    }
                }
                case SlimefunItem sf -> {
                    if (!sf.isDisabledIn(p.getWorld())) {
                        menu.addItem(index, sf.getItem());
                        menu.addMenuClickHandler(index, (pl, slot, item, action) -> {
                            SlimefunGuide.displayItem(profile, sf, true);
                            return false;
                        });
                        ++index;
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + content);
            }
        }

        int validCount = (int) this.contents.stream().filter(content -> isContentVisibleInGroup(content, p, profile, mode)).count();
        int pages = target == validCount - 1 ? page : validCount / 36 + 1;
        menu.addItem(46, ChestMenuUtils.getPreviousButton(p, page, pages));
        menu.addMenuClickHandler(46, (pl, slot, item, action) -> {
            int next = page - 1;
            if (next > 0) {
                setup(p, profile, mode, next);
            }

            return false;
        });
        menu.addItem(52, ChestMenuUtils.getNextButton(p, page, pages));
        menu.addMenuClickHandler(52, (pl, slot, item, action) -> {
            int next = page + 1;
            if (next <= pages) {
                setup(p, profile, mode, next);
            }

            return false;
        });
        menu.open(p);
    }

    protected void readAction(String action, SlimefunGuideMode mode, Player p, int slot, ItemStack clickedItem, ClickAction clickAction) {
        if (action.split(" ").length < 2) {
            ExceptionHandler.handleWarning("在" + getKey().getKey() + "物品组按钮中发现未知的操作格式: " + action);
            return;
        }

        String type = action.split(" ")[0];
        String content = action.split(" ")[1];
        switch (type) {
            case "link" -> {
                p.sendMessage(CMIChatColor.translate("&e单击此处打开链接: "));
                TextComponent link = new TextComponent(content);
                link.setColor(net.md_5.bungee.api.ChatColor.GRAY);

                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(CMIChatColor.translate("&e" +content)));
                link.setHoverEvent(hoverEvent);

                ClickEvent spigotClickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, content);
                link.setClickEvent(spigotClickEvent);

                p.sendMessage(link);
            }
            case "console" -> {
                if (CommandSafe.isBadCommand(content)) {
                    ExceptionHandler.handleDanger(
                        "在" + getKey().getKey() + "物品组按钮中发现执行服务器高危操作,请联系附属对应作者进行处理！！！");
                    return;
                }
                content = action.replace(type + " ", "");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content.replaceAll("%player%", p.getName()));
            }
            case "open_itemgroup" -> {
                if (content.split(":").length < 2) {
                    ExceptionHandler.handleWarning(
                        "在" + getKey().getKey() + "物品组按钮中发现未知的物品组 NamespacedKey: " + content);
                    return;
                }
                String namespace = content.split(":")[0];
                String key = content.split(":")[1];
                int page = 1;
                if (content.split(":").length > 2) {
                    try {
                        page = Integer.parseInt(content.split(":")[2]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                Optional<PlayerProfile> Oprofile = PlayerProfile.find(p);
                if (Oprofile.isEmpty()) {
                    ExceptionHandler.handleWarning(
                        "在" + getKey().getKey() + "物品组按钮中发现无法获取 PlayerProfile: " + p.getName());
                    return;
                }
                PlayerProfile profile = Oprofile.get();
                for (ItemGroup group : Slimefun.getRegistry().getAllItemGroups()) {
                    if (group.getKey().getNamespace().equals(namespace)
                        && group.getKey().getKey().equals(key)) {
                        SlimefunGuideImplementation implementation =
                            Slimefun.getRegistry().getSlimefunGuide(mode);
                        implementation.openItemGroup(profile, group, page);
                    }
                }
            }
            case "display_slimefunitem" -> {
                Optional<PlayerProfile> Oprofile = PlayerProfile.find(p);
                if (Oprofile.isEmpty()) {
                    ExceptionHandler.handleWarning(
                        "在" + getKey().getKey() + "物品组按钮中发现无法获取 PlayerProfile: " + p.getName());
                    return;
                }
                SlimefunItem item = SlimefunItem.getById(content);
                if (item == null) {
                    ExceptionHandler.handleWarning(
                        "在" + getKey().getKey() + "物品组按钮中发现未知的 SlimefunItem ID: " + content);
                    return;
                }
                PlayerProfile profile = Oprofile.get();
                SlimefunGuideImplementation implementation =
                    Slimefun.getRegistry().getSlimefunGuide(mode);
                implementation.displayItem(profile, item, true);
            }
            case "script" -> {
                JavaScriptEval eval = null;
                File file = new File(addon.getScriptsFolder(), content + ".js");
                if (!file.exists()) {
                    ExceptionHandler.handleWarning(
                        "在" + getKey().getKey() + "物品组按钮中发现执行脚本时遇到了问题: " + "找不到脚本文件 " + file.getName());
                } else {
                    eval = JavaScriptEval.create(file, addon);
                }

                if (eval != null) {
                    eval.evalFunction("onButtonGroupClick", p, slot, clickedItem, clickAction, mode);
                }
            }
            default -> ExceptionHandler.handleWarning("在" + getKey().getKey() + "物品组按钮中发现未知的操作类型: " + action);
        }
    }

    public static void addItemToGroup(ItemGroup itemGroup, SlimefunItem sf) {
        if (itemGroup instanceof RSCItemGroup group) {
            ExceptionHandler.debugLog(() -> "添加物品 " + sf + " 到物品组 " + group.getKey());
            group.addContent(sf);
            return;
        }
        if (itemGroup instanceof FlexItemGroup) {
            ExceptionHandler.handleError("无法将物品 "+ sf + " 添加到 " + itemGroup.getKey() + " 因为是 FlexItemGroup!");
            return;
        }
        ExceptionHandler.debugLog(() -> "添加物品 " + sf + " 到物品组 " + itemGroup.getKey());
        itemGroup.add(sf);
    }
}
