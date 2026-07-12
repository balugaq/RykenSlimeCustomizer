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
            "banlist",
            "pardon",
            "kick",
            "ban",
            "pardon-ip",
            "save-all",
            "unban",
            "luckperms",
            "lp",
            "cmi:ban",
            "cmi:pardon",
            "cmi:banlist",
            "cmi:unban",
            "cmi:jail",
            "cmi:unjail",
            "cmi:mute",
            "cmi:unmute",
            "cmi:sudo",
            "essentials:ban",
            "essentials:pardon",
            "essentials:banlist",
            "essentials:unban",
            "essentials:mute",
            "essentials:unmute",
            "essentials:jail",
            "essentials:unjail",
            "essentials:sudo");

    public static boolean isBadCommand(String command) {
        return badCommands.contains(command.toLowerCase());
    }
}
