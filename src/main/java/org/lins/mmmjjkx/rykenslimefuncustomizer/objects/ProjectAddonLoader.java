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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects;

import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.Validate;
import lombok.Data;
import net.bytebuddy.ByteBuddy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.ProjectAddonManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.events.AddonLoadEvent;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.listeners.ScriptableEventListener;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomAddonConfig;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.global.RecipeTypeMap;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.GenerationReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.ItemGroupReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.MenuReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.RecipeTypesReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.ResearchReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item.ArmorReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item.CapacitorsReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item.FoodReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item.GeoResourceReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item.ItemReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item.MobDropsReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.GeneratorReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.LinkedRecipeMachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.MachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.MaterialGeneratorReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.MultiBlockMachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.RecipeMachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.SimpleMachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.SolarGeneratorReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.SuperMultiBlockMachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.SuperReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.TemplateMachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.WorkbenchReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.update.GitHubUpdater;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Data
public class ProjectAddonLoader {
    private final Map<String, File> ids;
    private final File projectDir;
    private final String id;

    public ProjectAddonLoader(File projectDir, Map<String, File> ids, String id) {
        Validate.notNull(projectDir, "File cannot be null!");
        Validate.isTrue(projectDir.isDirectory(), "File must be a directory!");

        this.projectDir = projectDir;
        this.ids = ids;
        this.id = id;
    }

    @Nullable public ProjectAddon load() {
        Bukkit.getPluginManager().callEvent(new AddonLoadEvent(this));
        Debug.debug(() -> "Loading addon " + id);
        YamlConfiguration info = readYml(projectDir, Constants.INFO_FILE);

        Debug.info("开始读取文件夹 " + projectDir.getName() + " 中的项目信息...");

        String name = info.getString("name");
        String version = info.getString("version", "1.0.0");
        String id = info.getString("id"); // checked in ProjectAddonManager
        if (name == null || id == null) {
            Debug.error("在名称为 " + projectDir.getName() + " 的文件夹中有无效的项目信息，导致此附属无法加载！");
            return null;
        }
        String description = info.getString("description", "");
        String downloadZipName = info.getString("downloadZipName", "");
        List<String> depends = new ArrayList<>();
        List<String> pluginDepends = new ArrayList<>();
        List<String> authors = info.getStringList("authors");
        String repo = info.getString("repo");

        if (repo != null && RykenSlimefunCustomizer.allowUpdate(id)) {
            String[] split = repo.split("/");
            if (split.length == 2
                && GitHubUpdater.checkAndUpdate(version, split[0], split[1], id, projectDir.getName())
                // todo: downloaded - GitHubUpdater 阻塞了主线程！改到服务器 done 后再更新内容啊
                && !Objects.equals(readYml(projectDir, Constants.INFO_FILE).getString("version"), version)) {
                return load(); // reload
            }
        }

        if (name.isBlank()) {
            Debug.error("在名称为 " + projectDir.getName() + " 的文件夹中有无效的项目名称，导致此附属无法加载！");
            return null;
        }

        // todo: 这两个 depend 有重叠功能，后面重写一下
        if (info.contains("depends")) {
            depends = info.getStringList("depends");
            if (!RykenSlimefunCustomizer.addonManager.isLoaded(depends.toArray(new String[0]))) {
                boolean loadResult = loadDependencies(depends);
                if (!loadResult) {
                    Debug.error("在名称为 " + name + " 的附属(附属id: " + id + ") 中需要 RSC 附属依赖项 " + depends
                            + "，由于部分RSC附属依赖项在加载时出错或未安装，导致此附属无法加载！");
                    return null;
                }
            }
        }

        if (info.contains("pluginDepends")) {
            Set<String> unloadedPlugins = new HashSet<>();
            pluginDepends = info.getStringList("pluginDepends");
            for (String pluginDepend : pluginDepends) {
                if (!Bukkit.getPluginManager().isPluginEnabled(pluginDepend)) {
                    unloadedPlugins.add(pluginDepend);
                }
            }

            if (!unloadedPlugins.isEmpty()) {
                StringBuilder message = new StringBuilder("在名称为 " + name + " 的附属(附属id:" + id + ") 需要插件依赖项 ");
                for (String pluginDepend : pluginDepends) {
                    if (unloadedPlugins.contains(pluginDepend)) {
                        message.append("&c").append(pluginDepend).append("&r ");
                    } else {
                        message.append("&a").append(pluginDepend).append("&r ");
                    }
                }
                message.append("，由于部分插件依赖项在加载时出错或未安装，导致此附属无法加载！");
                Debug.error(message.toString());
                return null;
            }
        }

        List<String> loadStartTexts = info.getStringList("loadStartTexts");
        CMIChatColor.translate(loadStartTexts);

        ProjectAddon addon = new ProjectAddon(id, name, version, pluginDepends, depends, description, authors, projectDir);

        if (repo != null) {
            addon.setGitHubRepo("https://github.com/" + repo);
        }

        if (!downloadZipName.isBlank()) {
            addon.setDownloadZipName(downloadZipName);
        }

        loadListenerScript(info, addon);

        if (!loadStartTexts.isEmpty()) {
            for (String text : loadStartTexts) {
                Debug.info(text);
            }
        }

        loadAddonConfig(addon, projectDir, id);

        String idPattern = info.getString("idPattern");
        Debug.debug(() -> "Find id pattern: " + idPattern);
        if (idPattern != null && !idPattern.isBlank()) {
            if (idPattern.contains("%0")) {
                addon.setIdPattern(idPattern);
            } else {
                Debug.warn("在名称为 " + projectDir.getName() + "的文件夹中有无效的配置: idPattern | idPattern 必须包含 %0（原id） 已跳过");
            }
        }

        Debug.info("读取完成，开始加载附属 " + addon.getAddonId() + " 中的内容...");

        ItemGroupReader groupReader = new ItemGroupReader(projectDir, addon);
        addon.addTotalObjects(groupReader.getSize());
        addon.setItemGroups(groupReader.readAll());

        RecipeTypesReader recipeTypesReader = new RecipeTypesReader(projectDir, addon);
        addon.addTotalObjects(recipeTypesReader.getSize());
        addon.setRecipeTypes(recipeTypesReader.readAll());
        RecipeTypeMap.pushRecipeType(addon.getRecipeTypes());

        MobDropsReader mobDropsReader = new MobDropsReader(projectDir, addon);
        GeoResourceReader resourceReader = new GeoResourceReader(projectDir, addon);
        ItemReader itemReader = new ItemReader(projectDir, addon);
        ArmorReader armorReader = new ArmorReader(projectDir, addon);
        CapacitorsReader capacitorsReader = new CapacitorsReader(projectDir, addon);
        FoodReader foodReader = new FoodReader(projectDir, addon);
        MenuReader menuReader = new MenuReader(projectDir, addon);
        MachineReader machineReader = new MachineReader(projectDir, addon);
        GeneratorReader generatorReader = new GeneratorReader(projectDir, addon);
        SolarGeneratorReader solarGeneratorReader = new SolarGeneratorReader(projectDir, addon);
        MaterialGeneratorReader materialGeneratorReader = new MaterialGeneratorReader(projectDir, addon);
        RecipeMachineReader recipeMachineReader = new RecipeMachineReader(projectDir, addon);
        SimpleMachineReader simpleMachineReader = new SimpleMachineReader(projectDir, addon);
        MultiBlockMachineReader multiBlockMachineReader = new MultiBlockMachineReader(projectDir, addon);
        SuperReader superReader = new SuperReader(projectDir, addon);
        TemplateMachineReader templateMachineReader = new TemplateMachineReader(projectDir, addon);
        LinkedRecipeMachineReader linkedRecipeMachineReader = new LinkedRecipeMachineReader(projectDir, addon);
        WorkbenchReader workbenchReader = new WorkbenchReader(projectDir, addon);
        SuperMultiBlockMachineReader superMultiBlockMachineReader = new SuperMultiBlockMachineReader(projectDir, addon);
        GenerationReader generationReader = new GenerationReader(projectDir, addon);

        Debug.info("开始加载 " + projectDir.getName() + " 中的物品内容...");
        addon.addTotalObjects(mobDropsReader.getSize()
                + resourceReader.getSize()
                + itemReader.getSize()
                + armorReader.getSize()
                + capacitorsReader.getSize()
                + foodReader.getSize()
                + menuReader.getSize()
                + machineReader.getSize()
                + generatorReader.getSize()
                + solarGeneratorReader.getSize()
                + materialGeneratorReader.getSize()
                + recipeMachineReader.getSize()
                + simpleMachineReader.getSize()
                + multiBlockMachineReader.getSize()
                + superReader.getSize()
                + templateMachineReader.getSize()
                + linkedRecipeMachineReader.getSize()
                + workbenchReader.getSize()
                + superMultiBlockMachineReader.getSize()
                + generationReader.getSize());

        mobDropsReader.preload();
        resourceReader.preload();
        itemReader.preload();
        armorReader.preload();
        capacitorsReader.preload();
        foodReader.preload();
        machineReader.preload();
        generatorReader.preload();
        solarGeneratorReader.preload();
        materialGeneratorReader.preload();
        recipeMachineReader.preload();
        simpleMachineReader.preload();
        multiBlockMachineReader.preload();
        superReader.preload();
        templateMachineReader.preload();
        linkedRecipeMachineReader.preload();
        workbenchReader.preload();
        superMultiBlockMachineReader.preload();
        generationReader.preload();

        Debug.info("开始注册 " + projectDir.getName() + " 存放的内容...");

        addon.setMobDrops(mobDropsReader.readAll());
        addon.setGeoResources(resourceReader.readAll());
        addon.setItems(itemReader.readAll());
        addon.setArmors(armorReader.readAll());
        addon.setCapacitors(capacitorsReader.readAll());
        addon.setFoods(foodReader.readAll());
        addon.setMenus(menuReader.readAll());
        addon.setMachines(machineReader.readAll());
        addon.setGenerators(generatorReader.readAll());
        addon.setSolarGenerators(solarGeneratorReader.readAll());
        addon.setMaterialGenerators(materialGeneratorReader.readAll());
        addon.setRecipeMachines(recipeMachineReader.readAll());
        addon.setSimpleMachines(simpleMachineReader.readAll());
        addon.setMultiBlockMachines(multiBlockMachineReader.readAll());
        addon.setSupers(superReader.readAll());
        addon.setTemplateMachines(templateMachineReader.readAll());
        addon.setLinkedRecipeMachines(linkedRecipeMachineReader.readAll());
        addon.setWorkbenches(workbenchReader.readAll());
        addon.setSuperMultiBlockMachines(superMultiBlockMachineReader.readAll());
        addon.setGenerationInfos(generationReader.readAll());

        Debug.info("开始加载要求延迟加载的内容...");

        // late inits
        addon.getMobDrops().addAll(mobDropsReader.loadLateInits());
        addon.getGeoResources().addAll(resourceReader.loadLateInits());
        addon.getItems().addAll(itemReader.loadLateInits());
        addon.getArmors().addAll(armorReader.loadLateInits());
        addon.getCapacitors().addAll(capacitorsReader.loadLateInits());
        addon.getFoods().addAll(foodReader.loadLateInits());
        addon.getMenus().addAll(menuReader.loadLateInits());
        addon.getMachines().addAll(machineReader.loadLateInits());
        addon.getGenerators().addAll(generatorReader.loadLateInits());
        addon.getSolarGenerators().addAll(solarGeneratorReader.loadLateInits());
        addon.getMaterialGenerators().addAll(materialGeneratorReader.loadLateInits());
        addon.getRecipeMachines().addAll(recipeMachineReader.loadLateInits());
        addon.getSimpleMachines().addAll(simpleMachineReader.loadLateInits());
        addon.getMultiBlockMachines().addAll(multiBlockMachineReader.loadLateInits());
        addon.getSupers().addAll(superReader.loadLateInits());
        addon.getTemplateMachines().addAll(templateMachineReader.loadLateInits());
        addon.getLinkedRecipeMachines().addAll(linkedRecipeMachineReader.loadLateInits());
        addon.getWorkbenches().addAll(workbenchReader.loadLateInits());
        addon.getSuperMultiBlockMachines().addAll(superMultiBlockMachineReader.loadLateInits());

        ResearchReader researchReader = new ResearchReader(projectDir, addon);
        addon.addTotalObjects(researchReader.getSize());
        List<Research> researchesList = researchReader.readAll();
        researchesList.addAll(researchReader.loadLateInits());
        addon.setResearches(researchesList);

        List<String> enabledTexts = info.getStringList("enabledTexts");
        CMIChatColor.translate(enabledTexts);
        if (!enabledTexts.isEmpty()) {
            for (String text : enabledTexts) {
                Debug.info(text);
            }
        }

        Debug.info("加载附属 " + addon.getAddonId() + " 成功!");
        Debug.info("共 " + addon.getTotalObjects() + " 个配置项，加载成功 " + addon.getLoadedObjects() + " 个配置项");

        return addon;
    }

    private boolean loadDependencies(List<String> depends) {
        for (String dependency : depends) {
            if (RykenSlimefunCustomizer.addonManager.isLoaded(dependency)) continue;

            if (!ids.containsKey(dependency)) return false;

            File dependencyFile = ids.get(dependency);
            ProjectAddonLoader loader = new ProjectAddonLoader(dependencyFile, ids, dependency);

            ProjectAddon addon = loader.load();
            if (addon != null) {
                addon.setMarkedAsDepend(true);
                RykenSlimefunCustomizer.addonManager.addProjectAddon(addon);
            }
        }
        return true;
    }

    private static void loadAddonConfig(ProjectAddon addon, File projectDir, String id) {
        File customConfigFolder = new File(ProjectAddonManager.CONFIGS_DIRECTORY, id);
        File configFile = new File(projectDir, Constants.ADDON_CONFIG_FILE);
        YamlConfiguration originConfig = YamlConfiguration.loadConfiguration(configFile);
        if (originConfig.getKeys(false).isEmpty()) return;
        File existingConfig = new File(customConfigFolder, "config.yml");
        if (!customConfigFolder.exists()) {
            customConfigFolder.mkdirs();
        }

        if (!existingConfig.exists()) {
            try {
                Files.copy(configFile.toPath(), existingConfig.toPath());
            } catch (IOException e) {
                Debug.error("无法复制配置文件 " + configFile.getAbsolutePath() + " 到 " + customConfigFolder.getAbsolutePath()
                    + "，附属可能不按预期工作！", e);
            }
        }

        File scriptHandler = new File(addon.getScriptsFolder(), "configHandler.js");
        ScriptEval eval = scriptHandler.exists() ? JavaScriptEval.create(scriptHandler, addon) : null;
        if (eval != null) Debug.debug(() -> "Creating config handler script");
        CustomAddonConfig customConfigObj = new CustomAddonConfig(existingConfig, originConfig, eval);

        YamlConfiguration dest = YamlConfiguration.loadConfiguration(existingConfig);
        CommonUtils.completeFile(originConfig, dest);
        try {
            dest.save(existingConfig);
        } catch (IOException e) {
            Debug.error("无法保存配置文件: " + existingConfig.getAbsolutePath(), e);
        }

        addon.setConfig(customConfigObj);
        customConfigObj.tryReload();
    }

    private static void loadListenerScript(ConfigurationSection info, ProjectAddon addon) {
        String scriptListener = info.getString("scriptListener", "");
        if (scriptListener.isBlank()) return;

        File file = new File(addon.getScriptsFolder(), scriptListener + ".js");
        if (!file.exists()) {
            Debug.warn("无法找到附属 " + addon.getAddonId() + " 的对应监听脚本文件 " + file.getName());
            return;
        }

        Debug.debug(() -> "Creating script listener");
        JavaScriptEval eval = JavaScriptEval.create(file, addon);

        // First letter to uppercase
        String listenerName = scriptListener.replaceFirst(
            String.valueOf(scriptListener.charAt(0)),
            String.valueOf(Character.toUpperCase(scriptListener.charAt(0))));

        Class<? extends ScriptableEventListener> sel = new ByteBuddy()
            .subclass(ScriptableEventListener.class)
            .name("org.lins.mmmjjkx.rykenslimefuncustomizer." + addon.getAddonId().toLowerCase(Locale.ROOT) + ".listeners." + listenerName + "$$ByteBuddy")
            .make()
            .load(RykenSlimefunCustomizer.INSTANCE.getClass().getClassLoader())
            .getLoaded();

        try {
            var listenerObj = (ScriptableEventListener) sel.getConstructors()[0].newInstance(eval);
            Bukkit.getPluginManager().registerEvents(listenerObj, RykenSlimefunCustomizer.INSTANCE);

            addon.setEventListener(listenerObj);
            Debug.info("成功注册附属 " + addon.getAddonId() + " 的监听脚本: " + file.getName());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static YamlConfiguration readYml(File dir, String file) {
        File dest = new File(dir, file);
        if (!dest.exists()) return new YamlConfiguration();

        return YamlConfiguration.loadConfiguration(dest);
    }
}
