package me.pesekjak.machine.world.blocks;

import lombok.Getter;
import me.pesekjak.machine.Machine;
import me.pesekjak.machine.server.ServerProperty;
import me.pesekjak.machine.utils.NamespacedKey;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockManager implements ServerProperty {

    private final Map<NamespacedKey, BlockType> blocks = new ConcurrentHashMap<>();
    @Getter
    private final Machine server;

    public BlockManager(Machine server) {
        this.server = server;
    }

    public static BlockManager createDefault(Machine server) {
        BlockManager manager = new BlockManager(server);
        manager.addBlocks(
                new BlockType(NamespacedKey.minecraft("air"), BlockType.BlockProperties.builder()
                        .color(new Color(255, 255, 255, 0)).isAir(true).transparent(true).build())
        );
        return new BlockManager(server);
    }

    public void addBlock(BlockType blockType) {
        if(blocks.containsKey(blockType.name))
            throw new IllegalStateException("Block '" + blockType.name + "' is already registered");
        blocks.put(blockType.getName(), blockType);
    }

    public void addBlocks(BlockType... blockTypes) {
        Arrays.stream(blockTypes).forEach(this::addBlock);
    }

    public void removeBlock(BlockType blockType) {
        blocks.remove(blockType.name);
    }

    public void removeBlocks(BlockType... blockTypes) {
        Arrays.stream(blockTypes).forEach(this::removeBlock);
    }

    public boolean isRegistered(NamespacedKey name) {
        return blocks.containsKey(name);
    }

    public boolean isRegistered(BlockType blockType) {
        return blocks.containsValue(blockType);
    }

    public BlockType getBlockType(NamespacedKey name) {
        return blocks.get(name);
    }

    public Set<BlockType> getBlocks() {
        return Set.copyOf(blocks.values());
    }

}