package me.pesekjak.machine.world.dimensions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.pesekjak.machine.Machine;
import me.pesekjak.machine.utils.NamespacedKey;
import mx.kenzie.nbt.NBTCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import static me.pesekjak.machine.chunk.Chunk.CHUNK_SECTION_SIZE;

/**
 * Default implementation of the dimension manager.
 */
@RequiredArgsConstructor
public class DimensionTypeManagerImpl implements DimensionTypeManager {

    protected final @NotNull AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private final static String CODEC_TYPE = "minecraft:dimension_type";

    private final @NotNull Set<DimensionType> dimensionTypes = new CopyOnWriteArraySet<>();
    @Getter
    private final @NotNull Machine server;

    /**
     * Creates dimension manager with default values.
     * @param server server
     * @return new manager
     */
    public static @NotNull DimensionTypeManager createDefault(@NotNull Machine server) {
        DimensionTypeManagerImpl manager = new DimensionTypeManagerImpl(server);
        manager.addDimension(DimensionTypeImpl.createDefault());
        return manager;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void addDimension(@NotNull DimensionType dimensionType) {
        if(dimensionType.getManagerReference().get() != null && dimensionType.getManagerReference().get() != this)
            throw new IllegalStateException("Dimension type '" + dimensionType.getName() + "' is already registered in a different DimensionManager");
        if(dimensionType.getMinY() % CHUNK_SECTION_SIZE != 0 || dimensionType.getHeight() % CHUNK_SECTION_SIZE != 0 || dimensionType.getLogicalHeight() % CHUNK_SECTION_SIZE != 0)
            throw new IllegalStateException("Dimension type height levels has to be multiple of 16");
        if(dimensionType.getHeight() < 0 || dimensionType.getHeight() > 4064)
            throw new IllegalStateException("Dimension type height has to be between -2032 and 2016");
        if(dimensionType.getHeight() < dimensionType.getLogicalHeight())
            throw new IllegalStateException("Logical height of dimension type can't be higher than its height");
        if(dimensionType.getMinY() < -2032 || dimensionType.getMinY() > 2016)
            throw new IllegalStateException("Dimension type minimal Y level has to be between -2032 and 2016");
        dimensionType.getManagerReference().set(this);
        dimensionType.getIdReference().set(ID_COUNTER.getAndIncrement());
        dimensionTypes.add(dimensionType);
    }

    @Override
    public boolean removeDimension(@NotNull DimensionType dimensionType) {
        if(dimensionType.getManagerReference().get() != this) return false;
        if(dimensionTypes.remove(dimensionType)) {
            dimensionType.getManagerReference().set(null);
            dimensionType.getIdReference().set(-1);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRegistered(@NotNull NamespacedKey name) {
        final DimensionType dimension = getDimension(name);
        if(dimension == null) return false;
        return isRegistered(dimension);
    }

    @Override
    public DimensionType getDimension(@NotNull NamespacedKey name) {
        for(DimensionType dimensionType : getDimensions()) {
            if(!(dimensionType.getName().equals(name))) continue;
            return dimensionType;
        }
        return null;
    }

    @Override
    public @Nullable DimensionType getById(int id) {
        for(DimensionType dimensionType : getDimensions()) {
            if (dimensionType.getIdReference().get() != id) continue;
            return dimensionType;
        }
        return null;
    }

    @Override
    public @NotNull Set<DimensionType> getDimensions() {
        return Collections.unmodifiableSet(dimensionTypes);
    }

    @Override
    public @NotNull String getCodecType() {
        return CODEC_TYPE;
    }

    @Override
    public @NotNull List<NBTCompound> getCodecElements() {
        return new ArrayList<>(dimensionTypes.stream()
                .map(DimensionType::toNBT)
                .toList());
    }

}
