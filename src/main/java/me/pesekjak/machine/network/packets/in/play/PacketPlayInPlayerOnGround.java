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
public class PacketPlayInPlayerOnGround extends PacketIn {

    private static final int ID = 0x17;

    static {
        register(PacketPlayInPlayerOnGround.class, ID, PacketState.PLAY_IN,
                PacketPlayInPlayerOnGround::new);
    }

    private boolean onGround;

    public PacketPlayInPlayerOnGround(FriendlyByteBuf buf) {
        onGround = buf.readBoolean();
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        return new FriendlyByteBuf()
                .writeBoolean(onGround)
                .bytes();
    }

    @Override
    public PacketIn clone() {
        return new PacketPlayInPlayerOnGround(new FriendlyByteBuf(serialize()));
    }
}
