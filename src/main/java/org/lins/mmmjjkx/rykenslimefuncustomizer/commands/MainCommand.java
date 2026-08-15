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
package org.lins.mmmjjkx.rykenslimefuncustomizer.commands;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.ProjectAddonManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.PluginStateCache;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.SaveditemsGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.super_multiblock.SuperMultiBlock;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.super_multiblock.SuperMultiBlockManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainCommand implements TabExecutor {
    @Override
    public boolean onCommand(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sendHelp(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.reload")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                RykenSlimefunCustomizer.reload();
                sendMessage(sender, "&a重载成功！");
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.list")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                List<ProjectAddon> addons = RykenSlimefunCustomizer.addonManager.getAllAddons();
                List<String> nameWithId = addons.stream()
                        .map(a -> a.getAddonName() + "(id: " + a.getAddonId() + ")")
                        .toList();
                StringBuilder component = new StringBuilder("&a已加载的附属: ");
                for (String nwi : nameWithId) {
                    component.append("&a").append(nwi);
                    if (nameWithId.indexOf(nwi) != (nameWithId.size() - 1)) {
                        component.append("&6, ");
                    }
                }
                sender.sendMessage(component.toString());
                return true;
            } else if (args[0].equalsIgnoreCase("reloadPlugin")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.reloadPlugin")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                RykenSlimefunCustomizer.INSTANCE.reloadConfig();
                if (RykenSlimefunCustomizer.INSTANCE.getConfig().getBoolean("saveExample")) {
                    RykenSlimefunCustomizer.saveExample();
                }
                sendMessage(sender, "&a重载配置成功！");
                return true;
            } else if (args[0].equalsIgnoreCase("resaveitems")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.resaveitems")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                if (!(sender instanceof Player player)) {
                    sendMessage(sender, "&4只有玩家才能执行此命令！");
                    return false;
                }

                if (!PluginStateCache.isEnabled("JustEnoughGuide")) {
                    sendMessage(sender, "&4此命令需要服务器安装JustEnoughGuide才能正常使用");
                    return false;
                }

                sendMessage(player, "&c注意：为确保正常保存所有物品，请站在一个空旷平整的地面上，不要移动，并执行/rsc resaveitems start");
                sendMessage(player, "&c执行此指令后，会自动在您下方生成一些箱子，用于存放保存的物品");
                sendMessage(player, "&c接下来，您可以升级/降低服务器版本，箱子中的物品在世界升级时会自动被服务器修正");
                sendMessage(player, "&c在您重新进入世界后，输入/rsc resaveitems end 以自动重新保存物品");
                sendMessage(player, "&c保存会自动替换原文件，为避免保存失败，请做好plugins/RykenSlimefunCustomizer下的所有文件的备份");
            } else if (args[0].equalsIgnoreCase("clearScriptCache")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.clearscriptcache")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                RykenSlimefunCustomizer.clearScriptCache();
                sendMessage(sender, "&a清除脚本缓存成功！");
                return true;
            } else if (args[0].equalsIgnoreCase("cleardisplayprojectiles")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.cleardisplayprojectiles")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                RykenSlimefunCustomizer.clearDisplayProjectiles();
                sendMessage(sender, "&a清除多方块显示实体成功！");
                return true;
            } else if (args[0].equalsIgnoreCase("buildSuperMultiBlock")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.buildSuperMultiBlock")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                if (!(sender instanceof Player player)) {
                    sendMessage(sender, "&4只有玩家才能执行此命令！");
                    return false;
                }

                Block b = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
                if (b == null || b.getType().isAir()) {
                    sendMessage(player, "&4你必须要看向一个超大多方块才能执行此指令");
                    return false;
                }

                SuperMultiBlock smb = SuperMultiBlockManager.getInstance().getSuperMultiBlock(b.getLocation());
                if (smb == null) {
                    smb = SuperMultiBlockManager.getCoreStorage().get(b.getLocation());
                    if (smb == null) {
                        sendMessage(player, "&4你必须要看向一个超大多方块才能执行此指令");
                        return false;
                    }
                }

                smb.buildMultiBlock(player);
            } else {
                sendMessage(sender, "&4找不到此子指令！");
                return false;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("enable")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.enable")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                File file = new File(ProjectAddonManager.ADDONS_DIRECTORY, args[1]);

                if (!file.exists() || !file.isDirectory()) {
                    sendMessage(sender, "&4没有这个文件夹！");
                    return false;
                }

                if (RykenSlimefunCustomizer.addonManager.isLoaded(file)) {
                    sendMessage(sender, "&4此附属已经被加载了！");
                    return false;
                }

                if (RykenSlimefunCustomizer.addonManager.loadAddon(file)) {
                    sendMessage(sender, "&a加载附属成功！");
                } else {
                    sendMessage(sender, "&c附属加载失败！");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("disable")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.disable")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                String id = args[1];
                ProjectAddon addon = RykenSlimefunCustomizer.addonManager.get(id);
                if (addon == null) {
                    sendMessage(sender, "&4没有这个附属！");
                    return false;
                }

                RykenSlimefunCustomizer.addonManager.unloadAddon(addon);

                sendMessage(sender, "&a卸载此附属成功！");
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.info")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                String id = args[1];
                ProjectAddon addon = RykenSlimefunCustomizer.addonManager.get(id);
                if (addon == null) {
                    sendMessage(sender, "&4没有这个附属！");
                    return false;
                }

                String authors = addon.getAuthors().toString();
                String authorsRemoveBrackets = authors.substring(1, authors.length() - 1);

                StringBuilder builder = new StringBuilder()
                        .append("名称: &a")
                        .append(addon.getAddonName())
                        .append("\n&f")
                        .append("ID: &a")
                        .append(addon.getAddonId())
                        .append("\n&f")
                        .append("作者(们): &a")
                        .append(authorsRemoveBrackets)
                        .append("\n&f")
                        .append("版本: &a")
                        .append(addon.getAddonVersion())
                        .append("\n&f")
                        .append("依赖: &a")
                        .append(addon.getDepends())
                        .append("\n&f")
                        .append("插件依赖: &a")
                        .append(addon.getPluginDepends())
                        .append("\n&f")
                        .append("描述: &a")
                        .append(addon.getDescription());

                if (addon.getGitHubRepo() != null && !addon.getGitHubRepo().isBlank()) {
                    builder.append("\n&f").append("Github仓库: &e").append(addon.getGitHubRepo());
                }

                sendMessage(sender, builder.toString());
                return true;
            } else if (args[0].equalsIgnoreCase("menupreview")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.menupreview")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                String menuPresetId = args[1];
                BlockMenuPreset bmp = Slimefun.getRegistry().getMenuPresets().get(menuPresetId);
                if (bmp == null) {
                    sendMessage(sender, "&4没有这个菜单！");
                    return false;
                }
                if (sender instanceof Player p) {
                    bmp.open(p);
                    return true;
                } else {
                    sendMessage(sender, "&4你不能在控制台使用此指令！");
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.reload")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                String prjId = args[1];
                ProjectAddon addon = RykenSlimefunCustomizer.addonManager.get(prjId);
                if (addon == null) {
                    sendMessage(sender, "&4没有这个附属！");
                    return false;
                }

                if (RykenSlimefunCustomizer.addonManager.reloadAddon(addon)) {
                    sendMessage(sender, "&a重载成功！");
                } else {
                    sendMessage(sender, "&c重载失败！");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("resaveitems")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.resaveitems")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                if (!(sender instanceof Player player)) {
                    sendMessage(sender, "&4只有玩家才能执行此命令！");
                    return false;
                }

                if (!PluginStateCache.isEnabled("JustEnoughGuide")) {
                    sendMessage(sender, "&4此命令需要服务器安装JustEnoughGuide才能正常使用");
                    return false;
                }

                if (player.getLocation().toBlockLocation().getBlockY()
                        == player.getWorld().getMinHeight()) {
                    sendMessage(sender, "&4所处Y过低，请站高一些");
                    return false;
                }

                if (!player.isOnGround()) {
                    sendMessage(sender, "&4请站在地上");
                    return false;
                }

                if (args[1].equalsIgnoreCase("start")) {
                    List<ItemStack> itemStacks = SaveditemsGroup.instance.getObjects().stream()
                            .map(x -> (ItemStack) x)
                            .toList();

                    int cnt = 0;
                    for (int i = 0; i < itemStacks.size(); i++) {
                        Location chestLocation = player.getLocation().clone().add((int) (i / 27), -1, 0);
                        Block block = chestLocation.getBlock();
                        if (block.getType() != Material.CHEST) {
                            block.setType(Material.CHEST);
                        }
                        BlockState blockState = block.getState();
                        if (blockState instanceof InventoryHolder holder) {
                            holder.getInventory().setItem(i % 27, itemStacks.get(i));
                            cnt++;
                        }
                    }

                    sendMessage(player, "&a保存成功！共" + cnt + "个物品，请执行下一步操作");
                } else if (args[1].equalsIgnoreCase("end")) {
                    Bukkit.getScheduler().runTaskLater(RykenSlimefunCustomizer.INSTANCE, () -> {
                        int i = 0;
                        int cnt = 0;
                        int offsetY = -1;
                        while (true) {
                            Location chestLocation =
                                    player.getLocation().clone().add(i++, offsetY, 0);
                            Block block = chestLocation.getBlock();
                            if (block.getType() != Material.CHEST) {
                                if (offsetY == -1) {
                                    offsetY = 0;
                                    i = 0;
                                } else {
                                    sendMessage(player, "&a已重新保存成功！共" + cnt + "个文件");
                                    break;
                                }
                            }

                            BlockState blockState = block.getState();
                            if (!(blockState instanceof InventoryHolder holder)) continue;
                            for (int j = 0; j < 27; j++) {
                                ItemStack itemStack = holder.getInventory().getItem(j);
                                if (itemStack != null) {
                                    ItemStack clone = itemStack.clone();
                                    String source = clone.getItemMeta().getPersistentDataContainer()
                                        .get(SaveditemsGroup.SOURCE_KEY, PersistentDataType.STRING);
                                    if (source == null) continue;
                                    clone.editMeta(meta -> {
                                        meta.getPersistentDataContainer().remove(SaveditemsGroup.SOURCE_KEY);
                                    });

                                    try {
                                        // resave clone
                                        String prjId = source.split(";")[0];
                                        String filePath = source.split(";")[1];

                                        ProjectAddon addon =
                                                RykenSlimefunCustomizer.addonManager.get(prjId);

                                        CommonUtils.saveItem(itemStack, filePath, addon);
                                        sendMessage(player, "&a已重新保存 " + source);
                                        cnt++;
                                    } catch (Exception e) {
                                        Debug.error("&c保存" + source + "物品失败", e);
                                    }
                                }
                            }
                        }
                    },
                    1L);
                } else {
                    sendMessage(sender, "&4请输入正确的参数！ (start/end)");
                }
            } else {
                sendMessage(sender, "&4找不到此子指令！");
                return false;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("saveitem")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.saveitem")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                String prjId = args[1];
                String itemId = args[2];
                ProjectAddon addon = RykenSlimefunCustomizer.addonManager.get(prjId);
                if (addon == null) {
                    sendMessage(sender, "&4没有这个附属！");
                    return false;
                }
                if (sender instanceof Player p) {
                    ItemStack itemStack = p.getInventory().getItemInMainHand();
                    if (itemStack.getType() == Material.AIR) {
                        sendMessage(sender, "&4你不能保存空气！");
                        return false;
                    }
                    CommonUtils.saveItem(itemStack, itemId, addon);
                    sendMessage(sender, "&a保存成功！");
                    return true;
                } else {
                    sendMessage(sender, "&4你不能在控制台使用此指令！");
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("getsaveditem")) {
                if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.getsaveditem")) {
                    sendMessage(sender, "&4你没有权限去做这些！");
                    return false;
                }

                String prjId = args[1];
                String itemId = args[2];
                ProjectAddon addon = RykenSlimefunCustomizer.addonManager.get(prjId);
                if (addon == null) {
                    sendMessage(sender, "&4没有这个附属！");
                    return false;
                }

                File file = new File(
                        RykenSlimefunCustomizer.addonManager.getAddonFolder(prjId), "saveditems/" + itemId + ".yml");
                if (!file.exists() || file.length() == 0) {
                    sendMessage(sender, "&4指向的物品文件没有内容！");
                    return false;
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                ItemStack item = config.getItemStack("item");
                if (item == null) {
                    sendMessage(sender, "&4无法读取此物品文件！");
                    return false;
                }

                if (sender instanceof Player p) {
                    ItemStack itemStack = p.getInventory().getItemInMainHand();
                    if (itemStack.getType() == Material.AIR) {
                        p.getInventory().setItemInMainHand(item);
                        sendMessage(sender, "&a物品已放入你的手中！");
                        return true;
                    }
                    p.getInventory().addItem(item);
                    sendMessage(sender, "&a物品已放入你的背包中！");
                    return true;
                } else {
                    sendMessage(sender, "&4你不能在控制台使用此指令！");
                    return false;
                }
            } else {
                sendMessage(sender, "&4找不到此子指令！");
                return false;
            }
        } else {
            sendMessage(sender, "&4找不到此子指令！");
            return false;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        List<String> raw = onTabCompleteRaw(args);
        return StringUtil.copyPartialMatches(args[args.length - 1], raw, new ArrayList<>());
    }

    public @NonNull List<String> onTabCompleteRaw(@NonNull String[] args) {
        if (args.length == 1) {
            return List.of(
                    "list",
                    "reload",
                    "reloadPlugin",
                    "list",
                    "enable",
                    "disable",
                    "saveitem",
                    "menupreview",
                    "getsaveditem",
                    "resaveitems",
                    "clearScriptCache",
                    "buildSuperMultiBlock");
        } else if (args.length == 2) {
            return switch (args[0]) {
                case "enable" ->
                    Arrays.stream(Objects.requireNonNull(ProjectAddonManager.ADDONS_DIRECTORY.listFiles()))
                            .map(File::getName)
                            .toList();
                case "disable", "saveitem", "getsaveditem" ->
                    RykenSlimefunCustomizer.addonManager.getAllAddons().stream()
                            .map(ProjectAddon::getAddonId)
                            .toList();
                case "menupreview" ->
                    Slimefun.getRegistry().getMenuPresets().keySet().stream().toList();
                default -> new ArrayList<>();
            };
        }
        return new ArrayList<>();
    }

    private void sendHelp(CommandSender sender) {
        if (!sender.hasPermission("rsc.command") || !sender.hasPermission("rsc.command.help")) {
            sendMessage(sender, "&4你没有权限去做这些！");
            return;
        }
        sendMessage(sender, """
                        &aRykenSlimeCustomizer帮助
                        &e/rsc (help) 显示帮助
                        &e/rsc reload 重载插件及附属
                        &e/rsc reloadPlugin 重载插件
                        &e/rsc list 显示加载成功的附属
                        &e/rsc enable <addons里的文件夹名称> 加载某个附属
                        &e/rsc disable <附属ID> 卸载某个附属
                        &e/rsc saveitem <附属ID> <ID> 保存物品
                        &e/rsc menupreview <ID> 预览机器菜单
                        &e/rsc getsaveditem <附属ID> <ID> 获取保存的物品
                        &e/rsc resaveitems 重新保存所有保存物品
                        &e/rsc clearScriptCache 清除脚本失败缓存
                        &e/rsc cleardisplayprojectiles 清除多方块显示实体""");
    }

    public static void sendMessage(CommandSender sender, String s) {
        sender.sendMessage(CommonUtils.decorate(s));
    }
}
