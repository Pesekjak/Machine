package me.pesekjak.machine.network.packets.out.play;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import me.pesekjak.machine.utils.ServerBuffer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@ToString
public class PacketPlayOutDisconnect extends PacketOut {

    private static final int ID = 0x19;

    @Getter @Setter
    private @NotNull Component reason;

    static {
        register(PacketPlayOutDisconnect.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutDisconnect::new);
    }

    public PacketPlayOutDisconnect(@NotNull ServerBuffer buf) {
        reason = buf.readComponent();
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
                .writeComponent(reason)
                .bytes();
    }

    @Override
    public @NotNull PacketOut clone() {
        return new PacketPlayOutDisconnect(new FriendlyByteBuf(serialize()));
    }

}
