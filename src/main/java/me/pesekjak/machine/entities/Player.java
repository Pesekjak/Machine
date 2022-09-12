package me.pesekjak.machine.entities;

import lombok.Getter;
import lombok.Setter;
import me.pesekjak.machine.Machine;
import me.pesekjak.machine.network.ClientConnection;
import me.pesekjak.machine.network.packets.out.PacketPlayOutChangeDifficulty;
import me.pesekjak.machine.network.packets.out.PacketPlayOutGameEvent;
import me.pesekjak.machine.network.packets.out.PacketPlayOutLogin;
import me.pesekjak.machine.network.packets.out.PacketPlayOutPluginMessage;
import me.pesekjak.machine.network.packets.out.PacketPlayOutWorldSpawnPosition;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import me.pesekjak.machine.world.BlockPosition;
import me.pesekjak.machine.world.Difficulty;
import me.pesekjak.machine.world.World;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Player extends LivingEntity {

    @Getter
    private final String username;
    @Getter
    private final ClientConnection connection;

    @Getter
    private Gamemode gamemode = Gamemode.CREATIVE; // for now

    public Player(Machine server, @NotNull UUID uuid, @NotNull String username, @NotNull ClientConnection connection) {
        super(server, EntityType.PLAYER, uuid);
        this.username = username;
        if(connection.getOwner() != null)
            throw new UnsupportedOperationException("There can't be multiple players with the same ClientConnection");
        this.connection = connection;
        try {
            init();
        } catch (IOException e) {
            connection.disconnect(Component.text("Failed initialization."));
        }
    }

    @Override
    protected void init() throws IOException {
        super.init();
        NBTCompound nbt = NBT.Compound(Map.of(
                "minecraft:dimension_type", getServer().getDimensionTypeManager().toNBT(),
                "minecraft:worldgen/biome", getServer().getBiomeManager().toNBT()));
        List<String> worlds = new ArrayList<>();
        for(World world : getServer().getWorldManager().getWorlds())
            worlds.add(world.getName().toString());
        FriendlyByteBuf playLoginBuf = new FriendlyByteBuf()
                .writeInt(1)
                .writeBoolean(false)
                .writeByte((byte) gamemode.getID())
                .writeByte((byte) -1)
                .writeStringList(worlds, StandardCharsets.UTF_8)
                .writeNBT("", nbt)
                .writeString(getWorld().getDimensionType().getName().toString(), StandardCharsets.UTF_8)
                .writeString(getWorld().getName().toString(), StandardCharsets.UTF_8)
                .writeLong(getWorld().getSeed())
                .writeVarInt(getServer().getProperties().getMaxPlayers())
                .writeVarInt(8) // TODO Server Properties - View Distance
                .writeVarInt(8) // TODO Server Properties - Simulation Distance
                .writeBoolean(false) // TODO Server Properties - Reduced Debug Screen
                .writeBoolean(true)
                .writeBoolean(false)
                .writeBoolean(false) // TODO World - Is Spawn World Flat
                .writeBoolean(false);
        connection.sendPacket(new PacketPlayOutLogin(playLoginBuf));

        // TODO Add this as option in server properties
        connection.sendPacket(PacketPlayOutPluginMessage.getBrandPacket("Machine server"));

        sendDifficultyChange(getWorld().getDifficulty());
        sendWorldSpawnChange(new BlockPosition(0, 0, 0), 0.0F);
        sendGamemodeChange(gamemode);
    }

    public void setGamemode(Gamemode gamemode) {
        try {
            this.gamemode = gamemode;
            sendGamemodeChange(gamemode);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendDifficultyChange(Difficulty difficulty) throws IOException {
        FriendlyByteBuf buf = new FriendlyByteBuf()
                .writeByte((byte) difficulty.getId())
                .writeBoolean(true);
        connection.sendPacket(new PacketPlayOutChangeDifficulty(buf));
    }

    private void sendWorldSpawnChange(BlockPosition position, float angle) throws IOException {
        FriendlyByteBuf buf = new FriendlyByteBuf()
                .writeBlockPos(position)
                .writeFloat(angle);
        connection.sendPacket(new PacketPlayOutWorldSpawnPosition(buf));
    }

    private void sendGamemodeChange(Gamemode gamemode) throws IOException {
        FriendlyByteBuf buf = new FriendlyByteBuf()
                .writeByte((byte) 3)
                .writeFloat(gamemode.getID());
        connection.sendPacket(new PacketPlayOutGameEvent(buf));
    }
}
