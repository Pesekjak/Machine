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
public class PacketPlayInPlayerRotation extends PacketIn {

    private static final int ID = 0x16;

    static {
        register(PacketPlayInPlayerRotation.class, ID, PacketState.PLAY_IN,
                PacketPlayInPlayerRotation::new);
    }

    @NotNull
    private Location location;
    private boolean onGround;

    public PacketPlayInPlayerRotation(FriendlyByteBuf buf) {
        location = Location.of(0, 0, 0, buf.readFloat(), buf.readFloat(), null);
        onGround = buf.readBoolean();
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        return new FriendlyByteBuf()
                .writeFloat(location.getYaw())
                .writeFloat(location.getPitch())
                .writeBoolean(onGround)
                .bytes();
    }

    @Override
    public PacketIn clone() {
        return new PacketPlayInPlayerRotation(new FriendlyByteBuf(serialize()));
    }
}
