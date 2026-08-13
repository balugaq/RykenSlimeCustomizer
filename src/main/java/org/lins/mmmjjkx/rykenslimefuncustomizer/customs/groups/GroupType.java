package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum GroupType {
    nested("parent"),
    sub,
    seasonal,
    button("action"),
    locked,
    normal;

    private final List<String> aliases = new ArrayList<>();

    GroupType() {}

    GroupType(String... aliases) {
        this.aliases.addAll(List.of(aliases));
    }

    @Nullable
    public static GroupType getType(String s) {
        for (GroupType type : values()) {
            if (type.name().equalsIgnoreCase(s)) return type;
            for (String alias : type.aliases) {
                if (alias.equalsIgnoreCase(s)) return type;
            }
        }
        return null;
    }
}
