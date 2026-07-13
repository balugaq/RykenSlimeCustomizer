package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.mocks;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class MockRegistry {
    public static void register() {
        MockObject.getMocks().put(
                Server.class,
                new MockObject.BanMocker<>(
                        Server.class,
                        List.of(
                                "banIP",
                                "clearRecipes",
                                "createWorld",
                                "dispatchCommand",
                                "getBanList",
                                "getIPBans",
                                "getWhitelistedPlayers",
                                "getWorlds",
                                "reload",
                                "resetRecipes",
                                "selectEntities",
                                "setDefaultGameMode",
                                "shutdown",
                                "unloadWorld")));

        MockObject.getMocks().put(
                Player.class,
                new MockObject.BanMocker<>(
                        Player.class,
                        List.of(
                                "ban",
                                "banIp",
                                "banPlayerFull",
                                "banPlayerIp",
                                "chat",
                                "getHAProxyAddress",
                                "kick",
                                "kickPlayer",
                                "performCommand",
                                "setGameMode",
                                "setOp",
                                "transfer")));

        MockObject.getMocks().put(PluginManager.class, new MockObject.PluginManagerMocker());

        List<Class<?>> listenInterfaces = List.of(Entity.class, CommandSender.class, Block.class, World.class);

        for (Class<?> clazz : listenInterfaces) {
            MockObject.getMocks().put(clazz, new MockObject.EmptyMocker<>(clazz));
        }

        MockObject.getMocks().put(PlayerProfile.class, new MockObject.InstantizableMocker<>(PlayerProfile.class, (clazz, delegate) ->
        {
            try {
                return clazz.getConstructor(OfflinePlayer.class, int.class)
                        .newInstance(delegate.getPlayer(), delegate.getBackpackCount());
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                throw MockObject.failedToMockObject(delegate, e);
            }
        }
        ));

        MockObject.getMocks().put(PlayerRightClickEvent.class, new MockObject.InstantizableMocker<>(PlayerRightClickEvent.class, (clazz, delegate) ->
        {
            try {
                return clazz.getConstructor(PlayerInteractEvent.class)
                        .newInstance(delegate.getInteractEvent());
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw MockObject.failedToMockObject(delegate, e);
            }
        }
        ));

        MockObject.getMocks().put(PlayerInteractEvent.class, new MockObject.InstantizableMocker<>(PlayerInteractEvent.class, (clazz, delegate) ->
        {
            try {
                return clazz.getConstructor(Player.class, Action.class, ItemStack.class, Block.class, BlockFace.class, EquipmentSlot.class, Location.class)
                        .newInstance(delegate.getPlayer(), delegate.getAction(), delegate.getItem(), delegate.getClickedBlock(), delegate.getBlockFace(), delegate.getHand(), delegate.getInteractionPoint());
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw MockObject.failedToMockObject(delegate, e);
            }
        }
        ));

        MockObject.getMocks().put(Location.class, new MockObject.InstantizableMocker<>(Location.class, (clazz, delegate) ->
        {
            try {
                return clazz.getConstructor(World.class, double.class, double.class, double.class, float.class, float.class)
                        .newInstance(delegate.getWorld(), delegate.getX(), delegate.getY(), delegate.getZ(), delegate.getPitch(), delegate.getYaw());
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw MockObject.failedToMockObject(delegate, e);
            }
        }
        ));

        MockObject.getMocks().put(BlockBreakEvent.class, new MockObject.InstantizableMocker<>(BlockBreakEvent.class, (clazz, delegate) ->
        {
            try {
                return clazz.getConstructor(Block.class, Player.class)
                        .newInstance(delegate.getBlock(), delegate.getPlayer());
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw MockObject.failedToMockObject(delegate, e);
            }
        }
        ));

        MockObject.getMocks().put(PlayerItemConsumeEvent.class, new MockObject.InstantizableMocker<>(PlayerItemConsumeEvent.class, (clazz, delegate) ->
        {
            try {
                return clazz.getConstructor(Player.class, ItemStack.class, EquipmentSlot.class)
                        .newInstance(delegate.getPlayer(), delegate.getItem(), delegate.getHand());
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw MockObject.failedToMockObject(delegate, e);
            }
        }
        ));

        Field MODIFIERS_FILED;
        Field MODIFIER_FUNCTIONS_FILED;
        try {
            MODIFIERS_FILED = EntityDamageEvent.class.getDeclaredField("modifiers");
            MODIFIERS_FILED.setAccessible(true);

            MODIFIER_FUNCTIONS_FILED = EntityDamageEvent.class.getDeclaredField("modifierFunctions");
            MODIFIER_FUNCTIONS_FILED.setAccessible(true);
            MockObject.getMocks().put(EntityDamageByEntityEvent.class, new MockObject.InstantizableMocker<>(EntityDamageByEntityEvent.class, (clazz, delegate) ->
            {
                try {
                    return clazz.getConstructor(Entity.class, Entity.class, EntityDamageEvent.DamageCause.class, Map.class, Map.class, boolean.class)
                            .newInstance(delegate.getDamager(), delegate.getEntity(), delegate.getCause(), MODIFIERS_FILED.get(delegate), MODIFIER_FUNCTIONS_FILED.get(delegate), delegate.isCritical());
                } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw MockObject.failedToMockObject(delegate, e);
                }
            }
            ));
        } catch (NoSuchFieldException e) {
            ExceptionHandler.handleError("No such field", e);
        }
    }
}
