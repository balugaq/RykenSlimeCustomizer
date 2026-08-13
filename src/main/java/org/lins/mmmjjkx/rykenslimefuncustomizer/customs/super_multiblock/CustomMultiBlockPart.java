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
package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.super_multiblock;

import org.bukkit.Location;
import org.graalvm.polyglot.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.script.JavaScriptEval;

public class CustomMultiBlockPart implements MultiBlockPart {
    private final JavaScriptEval eval;
    private final DisplayDescriptor descriptor;

    public CustomMultiBlockPart(@NonNull JavaScriptEval eval, @Nullable DisplayDescriptor descriptor) {
        this.eval = eval;
        this.descriptor = descriptor;
    }

    @Override
    public boolean isOfPart(@NonNull SuperMultiBlock superMultiBlockInstance, @NonNull Location partLocation) {
        Value result = eval.evalFunction("isOfPart", partLocation, superMultiBlockInstance);
        return result != null && result.asBoolean();
    }

    @Override
    @Nullable
    public DisplayDescriptor getDisplayDescriptor(@NonNull SuperMultiBlock superMultiBlockInstance, @NonNull Location partLocation) {
        if (descriptor != null) return descriptor;
        Value result = eval.evalFunction("getDisplayDescriptor", partLocation, superMultiBlockInstance);
        if (result == null) return null;
        return result.as(DisplayDescriptor.class);
    }
}