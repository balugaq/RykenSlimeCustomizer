package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ban.CommandSafe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.Optional;

public interface BaseRSCItemGroup {
    default ItemGroup getSelf() {
        return (ItemGroup) this;
    }

    void register(SlimefunAddon plugin);

    void addContent(String action);

    void addContent(ItemGroup itemGroup);

    default void addContent(BaseRSCItemGroup base) {
        addContent(base.getSelf());
    }

    static BaseRSCItemGroup create(NamespacedKey key, ItemStack item, int tier, ProjectAddon addon, GroupType type, Visible visible, boolean forceHidden, boolean hasParent) {
        if (RykenSlimefunCustomizer.jeg) {
            return new RSCItemGroupJEG(key, item, tier, addon, type, visible, forceHidden, hasParent);
        } else {
            return new RSCItemGroupLegacy(key, item, tier, addon, type, visible, forceHidden, hasParent);
        }
    }

    NamespacedKey getKey();

    default void readAction(String action, SlimefunGuideMode mode, Player p, int slot, ItemStack clickedItem, ClickAction clickAction) {
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
                File file = new File(getProjectAddon().getScriptsFolder(), content + ".js");
                if (!file.exists()) {
                    ExceptionHandler.handleWarning(
                        "在" + getKey().getKey() + "物品组按钮中发现执行脚本时遇到了问题: " + "找不到脚本文件 " + file.getName());
                } else {
                    eval = JavaScriptEval.create(file, getProjectAddon());
                }

                if (eval != null) {
                    eval.evalFunction("onButtonGroupClick", p, slot, clickedItem, clickAction, mode);
                }
            }
            default -> ExceptionHandler.handleWarning("在" + getKey().getKey() + "物品组按钮中发现未知的操作类型: " + action);
        }
    }

    ProjectAddon getProjectAddon();

    static void addItemToGroup(ItemGroup itemGroup, SlimefunItem sf) {
        if (itemGroup instanceof RSCItemGroupLegacy group) {
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

    default boolean isContentVisibleInGroup(Object content, Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        switch (content) {
            case RSCItemGroupJEG itemGroup -> { return itemGroup.isVisibleInNested(p, profile, mode); }
            case RSCItemGroupLegacy itemGroup -> { return itemGroup.isVisibleInNested(p, profile, mode); }
            case SlimefunItem sf -> { return !sf.isDisabledIn(p.getWorld()); }
            case String action -> { return true; }
            default -> {
                ExceptionHandler.handleError("物品组 " + getKey().getKey() + " 中存在未知内容: " + content);
                return false;
            }
        }
    };
}
