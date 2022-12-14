package me.pesekjak.machine.chunk;

import me.pesekjak.machine.entities.Entity;
import me.pesekjak.machine.entities.Player;
import me.pesekjak.machine.server.ServerProperty;
import me.pesekjak.machine.world.World;
import me.pesekjak.machine.world.biomes.Biome;
import me.pesekjak.machine.world.blocks.BlockType;
import me.pesekjak.machine.world.blocks.BlockVisual;
import me.pesekjak.machine.world.blocks.WorldBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents a chunk (16x16 area) in a world.
 */
public interface Chunk extends ServerProperty {

    int CHUNK_SIZE_X = 16,
            CHUNK_SIZE_Z = 16,
            CHUNK_SECTION_SIZE = 16;

    int CHUNK_SIZE_BITS = 4;

    /**
     * @return world the chunk is in
     */
    @NotNull World getWorld();

    /**
     * @return x coordinate of the chunk
     */
    int getChunkX();

    /**
     * @return z coordinate of the chunk
     */
    int getChunkZ();

    /**
     * @return index of the bottom section
     */
    int getMinSection();

    /**
     * @return index of the top section
     */
    int getMaxSection();

    /**
     * @return if the chunk is loaded
     */
    boolean isLoaded();

    /**
     * Returns a world block at given location in this chunk.
     * @param x x coordinate of the block in this chunk
     * @param y y coordinate of the block in this chunk
     * @param z z coordinate of the block in this chunk
     * @return world block at given location
     */
    @NotNull WorldBlock getBlock(int x, int y, int z);

    /**
     * Sets a new block type for a world block at given location in this chunk.
     * @param x x coordinate of the block in this chunk
     * @param y y coordinate of the block in this chunk
     * @param z z coordinate of the block in this chunk
     * @param blockType new block type
     * @param reason reason of the change
     * @param replaceReason reason of the replacement of the old block type
     * @param source source of the change
     * @return world block that has been changed
     */
    @NotNull WorldBlock setBlock(int x, int y, int z, @NotNull BlockType blockType, @Nullable BlockType.CreateReason reason, @Nullable BlockType.DestroyReason replaceReason, @Nullable Entity source);

    /**
     * Changes the visual of a block at given location in this chunk.
     * @param x x coordinate of the block in this chunk
     * @param y y coordinate of the block in this chunk
     * @param z z coordinate of the block in this chunk
     * @param visual new visual for the world block
     */
    void setVisual(int x, int y, int z, @NotNull BlockVisual visual);

    /**
     * Returns a biome at given location in this chunk.
     * @param x x coordinate of the biome
     * @param y y coordinate of the biome
     * @param z z coordinate of the biome
     * @return biome at given location
     */
    @NotNull Biome getBiome(int x, int y, int z);

    /**
     * Sets a new biome at the given location.
     * @param x x coordinate of the biome
     * @param y y coordinate of the biome
     * @param z z coordinate of the biome
     * @param biome new biome
     */
    void setBiome(int x, int y, int z, @NotNull Biome biome);

    /**
     * Returns unmodifiable list of all sections.
     * @return all sections of this chunk
     */
    @Unmodifiable @NotNull List<Section> getSections();

    /**
     * Returns section with given index.
     * @param section index of the section
     * @return section with given index
     */
    @NotNull Section getSection(int section);

    /**
     * Returns section at given y coordinate in the world.
     * @param blockY y coordinate of the section
     * @return section at given y coordinate
     */
    default @NotNull Section getSectionAt(int blockY) {
        return getSection(blockY >> CHUNK_SIZE_BITS);
    }

    /**
     * Sends a chunk to a player.
     * @param player player to send chunk for
     */
    void sendChunk(@NotNull Player player);

    /**
     * Unloads the chunk for the player.
     * @param player player to unload chunk for
     */
    void unloadChunk(@NotNull Player player);

    /**
     * Creates a copy of this chunk in a given world at given coordinates.
     * @param world world to create the chunk for
     * @param chunkX x coordinate of the copied chunk
     * @param chunkZ z coordinate of the copied chunk
     * @return copy of this chunk
     */
    @NotNull Chunk copy(@NotNull World world, int chunkX, int chunkZ);

    /**
     * Resets the chunk and its data.
     */
    void reset();

}
