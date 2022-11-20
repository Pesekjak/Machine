package me.pesekjak.machine.network.packets.in.play;

import lombok.Getter;
import lombok.Setter;
import me.pesekjak.machine.network.packets.PacketIn;
import me.pesekjak.machine.utils.FriendlyByteBuf;

public class PacketPlayInConfirmTeleportation extends PacketIn {

    private static final int ID = 0x00;

    static {
        register(PacketPlayInConfirmTeleportation.class, ID, PacketState.PLAY_IN,
                PacketPlayInConfirmTeleportation::new);
    }

    @Getter @Setter
    private int teleportId;

    public PacketPlayInConfirmTeleportation(FriendlyByteBuf buf) {
        teleportId = buf.readVarInt();
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        return new FriendlyByteBuf()
                .writeVarInt(teleportId)
                .bytes();
    }

    @Override
    public PacketIn clone() {
        return new PacketPlayInConfirmTeleportation(new FriendlyByteBuf(serialize()));
    }
}
