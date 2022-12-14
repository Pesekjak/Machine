package me.pesekjak.machine.network.packets.out.play;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.pesekjak.machine.chunk.data.ChunkData;
import me.pesekjak.machine.chunk.data.LightData;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import me.pesekjak.machine.utils.ServerBuffer;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@ToString
@Getter @Setter
public class PacketPlayOutChunkData extends PacketOut {

    private static final int ID = 0x21;

    private int chunkX;
    private int chunkZ;
    private @NotNull ChunkData chunkData;
    private @NotNull LightData lightData;

    static {
        register(PacketPlayOutChunkData.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutChunkData::new);
    }

    public PacketPlayOutChunkData(@NotNull ServerBuffer buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        chunkData = new ChunkData(buf);
        lightData = new LightData(buf);
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public @NotNull PacketState getPacketState() {
        return PacketState.PLAY_OUT;
    }

    @Override
    public byte @NotNull [] serialize() {
        return new FriendlyByteBuf()
                .writeInt(chunkX)
                .writeInt(chunkZ)
                .write(chunkData)
                .write(lightData)
                .bytes();
    }

    @Override
    public @NotNull PacketOut clone() {
        return new PacketPlayOutChunkData(new FriendlyByteBuf(serialize()));
    }

}
