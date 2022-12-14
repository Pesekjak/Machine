package me.pesekjak.machine.world.blocks;

import lombok.*;
import me.pesekjak.machine.entities.Entity;
import me.pesekjak.machine.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Default implementation of block type.
 */
@AllArgsConstructor
@Getter
public class BlockTypeImpl implements BlockType {

    private final @NotNull NamespacedKey name;
    private final @NotNull BlockProperties properties;
    protected @NotNull BlockVisualizer visualizer;

    @Override
    public void create(@NotNull WorldBlock block, @NotNull CreateReason reason, @Nullable Entity source) {

    }

    @Override
    public void destroy(@NotNull WorldBlock block, @NotNull DestroyReason reason, @Nullable Entity source) {

    }

    @Override
    public void update(@NotNull WorldBlock block) {

    }

    /**
     * Default block properties implementation.
     */
    @Data
    @Builder
    public static class BlockProperties implements BlockType.BlockProperties {

        @Builder.Default private @NotNull Color color = Color.BLACK;
        @Builder.Default private boolean hasCollision = true;
        private float resistance;
        private boolean isAir;
        private float blockHardness;
        @Builder.Default private boolean allowsSpawning = true;
        @Builder.Default private boolean solidBlock = true;
        private boolean transparent;
        private boolean dynamicShape;

    }

}
