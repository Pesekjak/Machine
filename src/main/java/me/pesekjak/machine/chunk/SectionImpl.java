package me.pesekjak.machine.chunk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.machine.chunk.palette.AdaptivePalette;
import me.pesekjak.machine.chunk.palette.Palette;
import me.pesekjak.machine.utils.ServerBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of the section.
 */
@AllArgsConstructor
@Getter
public class SectionImpl implements Section {

    private final @NotNull Palette blockPalette;
    private final @NotNull Palette biomePalette;
    @Setter
    private byte @NotNull [] skyLight;
    @Setter
    private byte @NotNull [] blockLight;

    public SectionImpl() {
        this(AdaptivePalette.blocks(), AdaptivePalette.biomes(),
                new byte[0], new byte[0]);
    }

    @Override
    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
        this.skyLight = new byte[0];
        this.blockLight = new byte[0];
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull SectionImpl clone() {
        return new SectionImpl(blockPalette.clone(), biomePalette.clone(), skyLight.clone(), blockLight.clone());
    }

    @Override
    public void write(@NotNull ServerBuffer buf) {
        buf.writeShort((short) blockPalette.count());
        blockPalette.write(buf);
        biomePalette.write(buf);
    }

}
