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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ban;

import java.util.List;

public class CommandSafe {
    private static final List<String> badCommands = List.of(
            "stop",
            "restart",
            "op",
            "deop",
            "whitelist",
            "ban-ip",
            "kick",
            "ban",
            "ip",
            "save-all",
            "unban",
            "luckperms",
            "lp",
            "ban",
            "pardon",
            "banlist",
            "unban",
            "jail",
            "unjail",
            "mute",
            "unmute",
            "sudo",
            "*",
            "all",
            "reset",
            "cleardata"
    );

    public static boolean isBadCommand(String command) {
        for (String bad : badCommands) {
            if (command.contains(bad)) {
                return true;
            }
        }
        return false;
    }
}
