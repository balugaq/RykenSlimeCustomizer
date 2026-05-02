package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in;

import com.balugaq.jeg.api.groups.BaseGroup;
import com.balugaq.jeg.api.groups.MixedGroup;
import com.balugaq.jeg.api.interfaces.JEGSlimefunGuideImplementation;
import com.balugaq.jeg.api.objects.enums.PatchScope;
import com.balugaq.jeg.api.objects.events.GuideEvents;
import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.balugaq.jeg.utils.EventUtil;
import com.balugaq.jeg.utils.GuideUtil;
import com.balugaq.jeg.utils.clickhandler.OnClick;
import com.balugaq.jeg.utils.clickhandler.OnDisplay;
import com.balugaq.jeg.utils.formatter.Formats;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.chat.ChatInput;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;

import java.util.List;

public class SaveditemsGroup extends MixedGroup<@NotNull SaveditemsGroup> {
    public static SaveditemsGroup instance;
    public static final NamespacedKey SOURCE_KEY = new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, "source");
    public SaveditemsGroup(final NamespacedKey key, final ItemStack item) {
        super(key, item);
        this.pageMap.put(1, this);
    }

    @Override
    public boolean isVisible(final Player player, final @NotNull PlayerProfile playerProfile, final SlimefunGuideMode slimefunGuideMode) {
        return player.isOp();
    }

    @Override
    public ChestMenu generateMenu(Player player, PlayerProfile playerProfile, SlimefunGuideMode slimefunGuideMode) {
        ChestMenu chestMenu = new ChestMenu(ItemStackHelper.getDisplayName(this.getItem(player)));
        OnClick.preset(chestMenu);
        SlimefunGuideImplementation implementation = GuideUtil.getSlimefunGuide(slimefunGuideMode);

        for(int ss : Formats.sub.getChars('b')) {
            chestMenu.addItem(ss, PatchScope.Back.patch(player, ChestMenuUtils.getBackButton(player, new String[0])));
            chestMenu.addMenuClickHandler(ss, (pl, s, is, action) -> EventUtil.callEvent(new GuideEvents.BackButtonClickEvent(pl, is, s, action, chestMenu, implementation)).ifSuccess(() -> {
                GuideHistory guideHistory = playerProfile.getGuideHistory();
                if (action.isShiftClicked()) {
                    SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
                } else {
                    GuideUtil.goBack(guideHistory);
                }

                return false;
            }));
        }

        for(int ss : Formats.sub.getChars('S')) {
            chestMenu.addItem(ss, PatchScope.Search.patch(player, ChestMenuUtils.getSearchButton(player)));
            chestMenu.addMenuClickHandler(ss, (pl, slot, item, action) -> EventUtil.callEvent(new GuideEvents.SearchButtonClickEvent(pl, item, slot, action, chestMenu, implementation)).ifSuccess(() -> {
                pl.closeInventory();
                Slimefun.getLocalization().sendMessage(pl, "guide.search.message");
                ChatInput.waitForPlayer(JustEnoughGuide.getInstance(), pl, (msg) -> implementation.openSearch(playerProfile, msg, true));
                return false;
            }));
        }

        for(int ss : Formats.sub.getChars('P')) {
            chestMenu.addItem(ss, PatchScope.PreviousPage.patch(player, ChestMenuUtils.getPreviousButton(player, this.page, (this.objects.size() - 1) / Formats.sub.getChars('i').size() + 1)));
            chestMenu.addMenuClickHandler(ss, (p, slot, item, action) -> EventUtil.callEvent(new GuideEvents.PreviousButtonClickEvent(p, item, slot, action, chestMenu, implementation)).ifSuccess(() -> {
                GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
                BaseGroup<?> customGroup = this.getByPage(Math.max(this.page - 1, 1));
                customGroup.open(player, playerProfile, slimefunGuideMode);
                return false;
            }));
        }

        for(int ss : Formats.sub.getChars('N')) {
            chestMenu.addItem(ss, PatchScope.NextPage.patch(player, ChestMenuUtils.getNextButton(player, this.page, (this.objects.size() - 1) / Formats.sub.getChars('i').size() + 1)));
            chestMenu.addMenuClickHandler(ss, (p, slot, item, action) -> EventUtil.callEvent(new GuideEvents.NextButtonClickEvent(p, item, slot, action, chestMenu, implementation)).ifSuccess(() -> {
                GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
                BaseGroup<?> customGroup = this.getByPage(Math.min(this.page + 1, (this.objects.size() - 1) / Formats.sub.getChars('i').size() + 1));
                customGroup.open(player, playerProfile, slimefunGuideMode);
                return false;
            }));
        }

        for(int ss : Formats.sub.getChars('B')) {
            chestMenu.addItem(ss, PatchScope.Background.patch(player, ChestMenuUtils.getBackground()));
            chestMenu.addMenuClickHandler(ss, ChestMenuUtils.getEmptyClickHandler());
        }

        List<Integer> contentSlots = Formats.sub.getChars('i');

        for(int i = 0; i < contentSlots.size(); ++i) {
            int index = i + this.page * contentSlots.size() - contentSlots.size();
            if (index < this.objects.size()) {
                Object o = this.objects.get(index);
                if (o instanceof SlimefunItem) {
                    SlimefunItem slimefunItem = (SlimefunItem)o;
                    OnDisplay.Item.display(player, slimefunItem.getItem(), OnDisplay.Item.Normal, implementation).at(chestMenu, (Integer)contentSlots.get(i), this.page);
                } else if (o instanceof ItemGroup) {
                    ItemGroup itemGroup = (ItemGroup)o;
                    SlimefunGuideImplementation var14 = GuideUtil.getGuide(player, GuideUtil.getLastGuideMode(player));
                    if (var14 instanceof JEGSlimefunGuideImplementation) {
                        JEGSlimefunGuideImplementation guide = (JEGSlimefunGuideImplementation)var14;
                        guide.showItemGroup0(chestMenu, player, playerProfile, itemGroup, (Integer)contentSlots.get(i));
                    }
                } else if (o instanceof ItemStack) {
                    ItemStack itemStack = ((ItemStack) o).clone();
                    String source = itemStack.getItemMeta().getPersistentDataContainer().get(SOURCE_KEY, PersistentDataType.STRING);
                    CommonUtils.addLore(itemStack, true, "源: " + source);
                    OnDisplay.Item.display(player, itemStack, OnDisplay.Item.Normal, implementation).at(chestMenu, (Integer)contentSlots.get(i), this.page);

                    chestMenu.addMenuClickHandler(contentSlots.get(i), (p, s, ik, a) -> {
                        if (p.isOp()) {
                            p.getInventory().addItem(itemStack.clone());
                        }
                        return false;
                    });
                }
            }
        }

        GuideUtil.addRTSButton(chestMenu, player, playerProfile, Formats.sub, slimefunGuideMode, implementation);
        if (implementation instanceof JEGSlimefunGuideImplementation jeg) {
            GuideUtil.addBookMarkButton(chestMenu, player, playerProfile, Formats.sub, jeg, this);
            GuideUtil.addItemMarkButton(chestMenu, player, playerProfile, Formats.sub, jeg, this);
        }

        Formats.sub.renderCustom(chestMenu);
        return chestMenu;
    }
}
