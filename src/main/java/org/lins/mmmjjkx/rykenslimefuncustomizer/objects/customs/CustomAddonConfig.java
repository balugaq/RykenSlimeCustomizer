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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;

public record CustomAddonConfig(File configFile, YamlConfiguration config, ScriptEval onReloadHandler) {
    public CustomAddonConfig(File configFile, YamlConfiguration config, @Nullable ScriptEval onReloadHandler) {
        this.configFile = configFile;
        this.config = config;
        this.onReloadHandler = onReloadHandler;
    }

    public void tryReload() {
        try {
            config.load(configFile);
            if (onReloadHandler != null) {
                onReloadHandler.evalFunction("onConfigReload", config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
