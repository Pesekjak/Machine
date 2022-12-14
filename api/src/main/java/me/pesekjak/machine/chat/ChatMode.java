package me.pesekjak.machine.chat;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Represents players chat mode option.
 */
public enum ChatMode {

    ENABLED,
    COMMANDS_ONLY,
    HIDDEN;

    /**
     * @return numeric id of the chat mode used by Minecraft protocol.
     */
    public @Range(from = 0, to = 2) int getID() {
        return ordinal();
    }

    /**
     * Returns chat mode from its numeric id.
     * @param id id of the chat mode
     * @return chat mode for given id
     */
    public static @NotNull ChatMode fromID(@Range(from = 0, to = 2) int id) {
        Preconditions.checkArgument(id < values().length, "Unsupported ChatMode type");
        return values()[id];
    }

}
