package me.pesekjak.machine.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.pesekjak.machine.Machine;
import me.pesekjak.machine.chunk.ChunkUtils;
import me.pesekjak.machine.entities.Entity;
import me.pesekjak.machine.entities.ServerPlayer;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.network.packets.out.play.PacketPlayOutChangeDifficulty;
import me.pesekjak.machine.network.packets.out.play.PacketPlayOutWorldSpawnPosition;
import me.pesekjak.machine.utils.NamespacedKey;
import me.pesekjak.machine.world.blocks.BlockType;
import me.pesekjak.machine.world.blocks.WorldBlock;
import me.pesekjak.machine.world.dimensions.DimensionType;
import me.pesekjak.machine.world.region.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a world, which may contain entities, chunks and blocks.
 */
@RequiredArgsConstructor
@Getter
public abstract class WorldImpl implements World {

    private final @NotNull Machine server;

    protected final @NotNull AtomicReference<WorldManager> managerReference = new AtomicReference<>();

    private final @NotNull NamespacedKey name;
    private final @NotNull UUID uuid;
    private final @NotNull DimensionType dimensionType;
    private final @NotNull WorldType worldType;
    private final long seed;
    private @NotNull Difficulty difficulty = Difficulty.DEFAULT_DIFFICULTY;
    private @NotNull Location worldSpawn = Location.of(0, 0, 0, this);
    protected boolean loaded = false;

    @Override
    public void setBlock(@NotNull BlockType blockType, @NotNull BlockPosition position, @Nullable BlockType.CreateReason reason, @Nullable BlockType.DestroyReason replaceReason, @Nullable Entity source) {
        getChunk(position).setBlock(
                ChunkUtils.getSectionRelativeCoordinate(position.getX()),
                position.getY() - dimensionType.getMinY(),
                ChunkUtils.getSectionRelativeCoordinate(position.getZ()),
                blockType, reason, replaceReason, source);
    }

    @Override
    public @NotNull WorldBlock getBlock(@NotNull BlockPosition position) {
        return getChunk(position).getBlock(
                ChunkUtils.getSectionRelativeCoordinate(position.getX()),
                position.getY() - dimensionType.getMinY(),
                ChunkUtils.getSectionRelativeCoordinate(position.getZ()));
    }

    @Override
    public void setDifficulty(@NotNull Difficulty difficulty) {
        this.difficulty = difficulty;
        PacketOut packet = new PacketPlayOutChangeDifficulty(difficulty);
        for(Entity entity : getEntities()) {
            if(!(entity instanceof ServerPlayer player)) continue;
            player.sendPacket(packet);
        }
    }

    @Override
    public void setWorldSpawn(@NotNull Location location) {
        this.worldSpawn = location;
        PacketOut packet = new PacketPlayOutWorldSpawnPosition(location);
        for(Entity entity : getEntities()) {
            if(!(entity instanceof ServerPlayer player)) continue;
            player.sendPacket(packet);
        }
    }

    /**
     * @param regionX x coordinate of the region
     * @param regionZ z coordinate of the region
     * @return region at given coordinates
     */
    public abstract @NotNull Region getRegion(int regionX, int regionZ);

    /**
     * Saves region at given coordinates.
     * @param regionX x coordinate of the region
     * @param regionZ z coordinate of the region
     * @throws IOException if an I/O error occurs during unloading
     */
    public abstract void saveRegion(int regionX, int regionZ) throws IOException;

    /**
     * Saves the given region.
     * @param region region to save
     * @throws IOException if an I/O error occurs during unloading
     */
    public void saveRegion(@NotNull Region region) throws IOException {
        if(region.getWorld() != this) throw new IllegalStateException();
        saveRegion(region.getX(), region.getZ());
    }

}
