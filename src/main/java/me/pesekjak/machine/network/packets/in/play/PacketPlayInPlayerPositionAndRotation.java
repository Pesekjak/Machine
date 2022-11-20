package me.pesekjak.machine.network.packets.in.play;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.pesekjak.machine.network.packets.PacketIn;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import me.pesekjak.machine.world.Location;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@ToString
@Getter @Setter
public class PacketPlayInPlayerPositionAndRotation extends PacketIn {

    private static final int ID = 0x15;

    static {
        register(PacketPlayInPlayerPositionAndRotation.class, ID, PacketState.PLAY_IN,
                PacketPlayInPlayerPositionAndRotation::new);
    }

    @NotNull
    private Location location;
    private boolean onGround;

    public PacketPlayInPlayerPositionAndRotation(FriendlyByteBuf buf) {
        location = Location.of(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), null);
        onGround = buf.readBoolean();
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        FriendlyByteBuf buf = new FriendlyByteBuf();
        location.writePos(buf);
        return buf.writeFloat(location.getYaw())
                .writeFloat(location.getPitch())
                .writeBoolean(onGround)
                .bytes();
    }

    @Override
    public PacketIn clone() {
        return new PacketPlayInPlayerPositionAndRotation(new FriendlyByteBuf(serialize()));
    }
}
