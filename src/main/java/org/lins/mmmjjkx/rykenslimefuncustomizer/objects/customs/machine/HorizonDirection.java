package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine;

import org.bukkit.entity.Player;

public enum HorizonDirection {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public static HorizonDirection getFace(Player player) {
        return getDirectionFromYaw(player.getLocation().getYaw());
    }

    public static HorizonDirection getDirectionFromYaw(float yaw) {
        // 标准化角度到 0-360
        float normalized = yaw % 360;
        if (normalized < 0) normalized += 360;

        // 将 0° 对齐到正北（根据你的坐标系调整）
        // 假设：0° = 正北（-Z），90° = 正东（+X）
        if (normalized >= 315 || normalized < 45) return HorizonDirection.NORTH;
        if (normalized >= 45 && normalized < 135) return HorizonDirection.EAST;
        if (normalized >= 135 && normalized < 225) return HorizonDirection.SOUTH;
        if (normalized >= 225 && normalized < 315) return HorizonDirection.WEST;

        return HorizonDirection.NORTH; // fallback
    }
}
