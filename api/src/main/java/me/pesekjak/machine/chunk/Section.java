package me.pesekjak.machine.chunk;

import me.pesekjak.machine.chunk.palette.Palette;
import me.pesekjak.machine.utils.Writable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a 16 blocks tall section of a chunk.
 */
public interface Section extends Writable, Cloneable {

    /**
     * @return block palette used by this section
     */
    @NotNull Palette getBlockPalette();

    /**
     * @return biome palette used by this section
     */
    @NotNull Palette getBiomePalette();

    /**
     * @return sky light data of this section
     */
    byte @NotNull [] getSkyLight();

    /**
     * @return block light data of this section
     */
    byte @NotNull [] getBlockLight();

    /**
     * Clears the section.
     */
    void clear();

    Section clone();

}
