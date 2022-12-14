package me.pesekjak.machine.world.biomes;

import lombok.Builder;
import lombok.Getter;
import me.pesekjak.machine.utils.NamespacedKey;
import me.pesekjak.machine.world.particles.Particle;
import mx.kenzie.nbt.NBTCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Default biome effects implementation.
 */
@Getter
@Builder
public class BiomeEffectsImpl implements BiomeEffects {

    /**
     * Creates the default biome effects.
     * @return newly created biome effects
     */
    public static @NotNull BiomeEffects createDefault() {
        return BiomeEffectsImpl.builder()
                .build();
    }

    @Builder.Default private final int fogColor = 0xC0D8FF;
    @Builder.Default private final int skyColor = 0x78A7FF;
    @Builder.Default private final int waterColor = 0x3F76E4;
    @Builder.Default private final int waterFogColor = 0x50533;
    private final @Nullable Integer foliageColor;
    private final @Nullable Integer grassColor;
    private final @Nullable BiomeEffects.GrassColorModifier grassColorModifier;
    private final @Nullable NamespacedKey ambientSound;
    private final @Nullable MoodSound moodSound;
    private final @Nullable AdditionsSound additionsSound;
    private final @Nullable Music music;
    private final @Nullable Float biomeParticleProbability;
    private final @Nullable Particle biomeParticle;

    @Override
    public @NotNull NBTCompound toNBT() {
        NBTCompound compound = new NBTCompound(Map.of(
                "fog_color", fogColor,
                "sky_color", skyColor,
                "water_color", waterColor,
                "water_fog_color", waterFogColor
        ));
        if (foliageColor != null)
            compound.set("foliage_color", foliageColor);
        if (grassColor != null)
            compound.set("grass_color", grassColor);
        if (grassColorModifier != null)
            compound.set("grass_color_modifier", grassColorModifier.name().toLowerCase());
        if (ambientSound != null)
            compound.set("ambient_sound", ambientSound.toString());
        if (moodSound != null)
            compound.set("mood_sound", moodSound.toNBT());
        if (additionsSound != null)
            compound.set("additions_sound", additionsSound.toNBT());
        if (music != null)
            compound.set("music", music.toNBT());
        if(biomeParticle != null && biomeParticleProbability != null)
            compound.set("particle", new NBTCompound(Map.of(
                    "probability", biomeParticleProbability,
                    "options", biomeParticle.toNBT())));
        return compound;
    }

    /**
     * Sound playing in a biome
     */
    public record MoodSoundImpl(@NotNull NamespacedKey sound, int tickDelay, int blockSearchExtent, double offset) implements MoodSound {
        @Override
        public @NotNull NBTCompound toNBT() {
            return new NBTCompound(Map.of(
                    "sound", sound.toString(),
                    "tick_delay", tickDelay,
                    "block_search_extent", blockSearchExtent,
                    "offset", offset));
        }
    }

    /**
     * Additional sound playing in a biome
     */
    public record AdditionsSoundImpl(@NotNull NamespacedKey sound, double tickChance) implements AdditionsSound {
        @Override
        public @NotNull NBTCompound toNBT() {
            return new NBTCompound(Map.of(
                    "sound", sound.toString(),
                    "tick_chance", tickChance));
        }
    }

    /**
     * Music playing in a biome
     */
    public record MusicImpl(@NotNull NamespacedKey sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) implements Music {
        @Override
        public @NotNull NBTCompound toNBT() {
            return new NBTCompound(Map.of(
                    "sound", sound.toString(),
                    "min_delay", minDelay,
                    "max_delay", maxDelay,
                    "replace_current_music", replaceCurrentMusic));
        }
    }

}
