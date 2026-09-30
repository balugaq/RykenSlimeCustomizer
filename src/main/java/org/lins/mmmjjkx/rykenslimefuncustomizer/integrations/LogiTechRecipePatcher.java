package org.lins.mmmjjkx.rykenslimefuncustomizer.integrations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import it.unimi.dsi.fastutil.ints.IntList;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.MachineRecipeUtils;
import me.matl114.logitech.utils.UtilClass.ItemClass.ProbItemStack;
import me.matl114.logitech.utils.UtilClass.RecipeClass.StackMachineRecipe;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.Recipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers.MachineTicker;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.InputWrapper;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.NoConsume;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ReflectionUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 修复逻辑工艺堆叠机器 (如终极堆叠配方机器) 模拟 RSC 机器时 noConsume 失效的问题。
 *
 * <p>根因：LogiTech 的 {@code RecipeSupporter.transferRSCRecipes} 通过
 * {@code getClass().getName().endsWith("CustomMachineRecipe")} 识别 RSC 配方，
 * {@code CustomTemplateMachineRecipe} 等子类无法命中，会被原样放行并走
 * {@code stackFromMachine} 生成不带 noConsumeInputs 的普通 StackMachineRecipe，
 * 于是模拟时输入物品全部被消耗。</p>
 *
 * <p>即使类名命中，LogiTech 反射读取的 {@code noConsume} IntList (fuckLogitech 产物)
 * 也是按原始条目前缀猜测的索引，而 {@code MachineRecipeUtils.stackFrom} 会把输入数组
 * 按物品种类合并，索引随之错位——所以不能靠修数据，只能绕过。
 * 这里在 LogiTech 收集完配方之后，用 RSC 自己的权威数据整表重建
 * {@code MACHINE_RECIPELIST} 条目，直接构造 LogiTech 的 StackMachineRecipe：</p>
 *
 * <ul>
 *   <li>输入：每个 InputWrapper 一个条目 (readInputs 已按物品种类合并)，
 *       noConsume 按「物品种类整体」标记——与 LogiTech 合并后的语义一致；</li>
 *   <li>输出：复刻 transferRSCRecipes 的处理 (chance → ProbItemStack，chooseOne → RandomItemStack)；</li>
 *   <li>MATERIAL_GENERATOR 类型的机器走 LogiTech 的 MGeneratorRecipe 体系，跳过。</li>
 * </ul>
 */
@NullMarked
public final class LogiTechRecipePatcher {
    private LogiTechRecipePatcher() {
    }

    public static void patchAll() {
        int machines = 0;
        int recipeCount = 0;
        for (SlimefunItem sf : Slimefun.getRegistry().getAllSlimefunItems()) {
            if (sf.getAddon() != org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer.INSTANCE) continue;
            if (!(sf instanceof AdvancedCustomMachine acm)) continue;
            MachineTicker ticker = acm.getTicker();
            if (ticker == null || ticker.getType() == MachineTicker.Type.MATERIAL_GENERATOR) continue;

            List<? extends Recipe> recipes = ticker.getRecipes();
            if (recipes.isEmpty()) continue;

            // 链式配方机器 (CustomLinkedMachineRecipe) 不是 CustomMachineRecipe 体系，保持 LogiTech 原样
            boolean allCustom = true;
            for (Recipe r : recipes) {
                if (!(r instanceof CustomMachineRecipe)) {
                    allCustom = false;
                    break;
                }
            }
            if (!allCustom) continue;

            List<MachineRecipe> rebuilt = new ArrayList<>(recipes.size());
            for (Recipe r : recipes) {
                CustomMachineRecipe cmr = (CustomMachineRecipe) r;
                // 与 transferRSCRecipes 的丢弃行为保持一致
                if (cmr.isForDisplayOnly()) continue;
                rebuilt.add(build(cmr));
                recipeCount++;
            }

            RecipeSupporter.MACHINE_RECIPELIST.put(sf, rebuilt);
            machines++;
        }
        Debug.info("已重建逻辑工艺堆叠机器配方: " + machines + " 台机器, " + recipeCount + " 条配方");
    }

    private static MachineRecipe build(CustomMachineRecipe r) {
        // input: 每 wrapper 一个条目；noConsume 只能按物品种类整体标记 (stackFrom 合并语义)
        List<ItemStack> input = new ArrayList<>();
        Set<Integer> noConsume = new HashSet<>();
        int idx = 0;
        for (InputWrapper wp : r.getInputs()) {
            if (wp.getAmount() <= 0) continue;
            NoConsume nc = wp.getNoConsume();
            int ncAmount = nc.getNoConsumeAmountExcludeLinked() + nc.getNoConsumeLinkedAmount();
            if (ncAmount >= wp.getAmount()) {
                noConsume.add(idx);
            }
            input.add(wp.getStack().asQuantity(wp.getAmount()));
            idx++;
        }

        // output: 复刻 transferRSCRecipes (chance → ProbItemStack, chooseOne → 随机取一)
        IntList chances = r.getChances();
        ItemStack[] output = r.getOutput();
        List<ItemStack> outs = new ArrayList<>(output.length);
        for (int i = 0; i < output.length; i++) {
            double p = i < chances.size()
                ? Math.min(100, Math.max(0, chances.getInt(i))) / 100.0
                : 1.0;
            outs.add(p > 0.99 ? output[i] : new ProbItemStack(output[i], p));
        }
        ItemStack[] outArr;
        if (r.isChooseOne()) {
            outArr = new ItemStack[] {AddUtils.eqRandItemStackFactory(outs)};
        } else {
            outArr = outs.toArray(new ItemStack[0]);
        }

        return (MachineRecipe) ReflectionUtil.invokeStaticMethod(MachineRecipeUtils.class, "stackFrom", r.getTicks(), input.toArray(new ItemStack[0]), outArr, noConsume);
    }
}
