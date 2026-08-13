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
package org.lins.mmmjjkx.rykenslimefuncustomizer.customs;

import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ToolUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.WeaponUseHandler;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.script.ScriptEval;

public class CustomUnplaceableItem extends CustomItem implements NotPlaceable {
    public CustomUnplaceableItem(YamlReader.BaseResult base, @Nullable ScriptEval eval) {
        super(base);

        if (eval == null) return;
        eval.doInit();

        this.addItemHandler((ItemUseHandler) e -> {
            eval.evalFunction("onUse", e);
            e.cancel();
        });

        this.addItemHandler((WeaponUseHandler) (e, p, it) -> {
            eval.evalFunction("onWeaponHit", e, p, it);
        });
        this.addItemHandler((ToolUseHandler) (e, it, i, drops) -> eval.evalFunction("onToolUse", e, it, i, drops));
    }
}
