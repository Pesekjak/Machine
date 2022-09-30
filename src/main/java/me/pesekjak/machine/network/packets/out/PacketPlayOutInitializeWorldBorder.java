package me.pesekjak.machine.network.packets.out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.utils.FriendlyByteBuf;

@AllArgsConstructor
public class PacketPlayOutInitializeWorldBorder extends PacketOut {

    private static final int ID = 0x1F;

    @Getter @Setter
    private double x, z, oldDiameter, newDiameter;
    @Getter @Setter
    private long speed;
    @Getter @Setter
    private int portalTeleportBoundary; // Usually 29999984
    @Getter @Setter
    private int warningBlocks, warningTime;

    static {
        register(PacketPlayOutInitializeWorldBorder.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutInitializeWorldBorder::new);
    }

    public PacketPlayOutInitializeWorldBorder(FriendlyByteBuf buf) {
        x = buf.readDouble();
        z = buf.readDouble();
        oldDiameter = buf.readDouble();
        newDiameter = buf.readDouble();
        speed = buf.readVarLong();
        portalTeleportBoundary = buf.readVarInt();
        warningBlocks = buf.readVarInt();
        warningTime = buf.readVarInt();
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        return new FriendlyByteBuf()
                .writeDouble(x)
                .writeDouble(z)
                .writeDouble(oldDiameter)
                .writeDouble(newDiameter)
                .writeVarLong(speed)
                .writeVarInt(portalTeleportBoundary)
                .writeVarInt(warningBlocks)
                .writeVarInt(warningTime)
                .bytes();
    }

    @Override
    public PacketOut clone() {
        return new PacketPlayOutInitializeWorldBorder(new FriendlyByteBuf(serialize()));
    }

}
