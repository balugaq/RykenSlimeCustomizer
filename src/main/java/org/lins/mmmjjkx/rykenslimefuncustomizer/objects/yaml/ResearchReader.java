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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class ResearchReader extends YamlReader<Research> {
    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    @Override
    public String getFileName() {
        return Constants.RESEARCHES_FILE;
    }

    public ResearchReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public Research readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;

        if (!VALID_KEY.matcher(s).matches()) {
            Debug.error(file, section, "研究注册 ID 无效，不满足 " + VALID_KEY + " 的匹配");
            return null;
        }

        int researchId = section.getInt("id");
        String name = section.getString("name");
        int cost = section.getInt("levelCost");
        List<String> items = section.getStringList("items");

        if (researchId <= 0) {
            Debug.error(file, section, "缺少或配置错误 '研究数字 ID' (id)", 1, Integer.MAX_VALUE);
            return null;
        }
        if (cost <= 0) {
            Debug.error(file, section, "缺少或配置错误 '研究等级花费' (levelCost)", 1, Integer.MAX_VALUE);
            return null;
        }
        if (name == null || name.isBlank()) {
            Debug.error(file, section, "缺少或配置错误 '名称' (name)");
            return null;
        }

        name = CMIChatColor.translate(name);

        boolean hasCurrency = section.contains("currencyCost");
        double currency = 0;
        if (hasCurrency) {
            currency = section.getDouble("currencyCost");
            if (currency < 0) {
                Debug.warning(file, section, "缺少或配置错误 '研究货币花费' (currencyCost)", 1, Integer.MAX_VALUE);
                hasCurrency = false;
            }
        }

        Research research;
        if (hasCurrency) {
            research = new Research(
                    new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, s), researchId, name, cost, currency);
        } else {
            research = new Research(new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, s), researchId, name, cost);
        }

        for (String item : items) {
            SlimefunItem sfItem = SlimefunItem.getById(item);
            if (sfItem == null) {
                Debug.warning(file, section, "不是粘液物品 (item): " + item);
                continue;
            }
            research.addItems(sfItem);
        }

        research.register();

        return research;
    }

    // 研究不需要预加载物品
    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return List.of();
    }
}
