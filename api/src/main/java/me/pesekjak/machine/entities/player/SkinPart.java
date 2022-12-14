package me.pesekjak.machine.entities.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents different skin parts of player's skin.
 */
@RequiredArgsConstructor
public enum SkinPart {

    CAPE(0x01),
    JACKET(0x02),
    LEFT_SLEEVE(0x04),
    RIGHT_SLEEVE(0x08),
    LEFT_PANTS(0x10),
    RIGHT_PANTS(0x20),
    HAT(0x40);

    @Getter
    private final @Range(from = 1, to = 255) int mask;

    /**
     * Returns skin part of given name.
     * @param name name of the skin part
     * @return skin part with given name
     */
    public static @Nullable SkinPart getByName(@NotNull String name) {
        for (SkinPart value : values()) {
            if (value.name().equalsIgnoreCase(name)) return value;
        }
        return null;
    }

    /**
     * Creates a skin mask from given skin parts.
     * @param parts skin parts of the mask
     * @return created mask from given skin parts
     */
    public static @Range(from = 0, to = 255) int skinMask(SkinPart @NotNull ... parts) {
        int mask = 0;
        for(SkinPart part : parts)
            mask |= part.mask;
        return mask;
    }

    /**
     * Creates set of skin parts from given skin mask.
     * @param mask skin mask
     * @return set of skin parts of given skin mask
     */
    public static @NotNull Set<SkinPart> fromMask(@Range(from = 0, to = 127) int mask) {
        Set<SkinPart> set = new HashSet<>();
        for (SkinPart skinPart : values()) {
            if ((skinPart.mask & mask) == skinPart.mask) set.add(skinPart);
            if (skinPart.mask > mask) return set;
        }
        return set;
    }

}
