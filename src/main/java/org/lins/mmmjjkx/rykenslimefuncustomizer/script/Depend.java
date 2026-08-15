package org.lins.mmmjjkx.rykenslimefuncustomizer.script;

import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddonLoader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.PluginStateCache;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public interface Depend {
    static List<? extends Depend> load(Type type, List<String> depends) {
        return type.load(depends);
    }

    String name();

    boolean enabled();

    @NullMarked
    enum Type {
        PLUGIN(list -> {
            List<PluginDepend> lst = new ArrayList<>();
            for (String name : list) {
                lst.add(new PluginDepend(name, PluginStateCache.isEnabled(name)));
            }
            return lst;
        }),
        ADDON(list -> {
            List<AddonDepend> lst = new ArrayList<>();
            for (String name : list) {
                lst.add(new AddonDepend(name, ProjectAddonLoader.isLoadedOrTryLoad(name)));
            }
            return lst;
        });

        private final DependReader reader;

        Type(DependReader reader) {
            this.reader = reader;
        }

        public List<? extends Depend> load(List<String> depends) {
            return this.reader.read(depends);
        }

        @NullMarked
        @FunctionalInterface
        public interface DependReader {
            List<? extends Depend> read(List<String> depends);
        }
    }

    record AddonDepend(String name, boolean enabled) implements Depend {
    }

    record PluginDepend(String name, boolean enabled) implements Depend {
    }
}
