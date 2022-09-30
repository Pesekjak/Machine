package me.pesekjak.machine.network.packets.out;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class PacketPlayOutSetTablistHeaderAndFooter extends PacketOut {

    private static final int ID = 0x63;

    @Getter @Setter
    private Component header, footer;

    static {
        register(PacketPlayOutSetTablistHeaderAndFooter.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutSetTablistHeaderAndFooter::new);
    }

    public PacketPlayOutSetTablistHeaderAndFooter(FriendlyByteBuf buf) {
        header = buf.readComponent();
        footer = buf.readComponent();
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        return new FriendlyByteBuf()
                .writeComponent(header)
                .writeComponent(footer)
                .bytes();
    }

    @Override
    public PacketOut clone() {
        return new PacketPlayOutSetTablistHeaderAndFooter(new FriendlyByteBuf(serialize()));
    }

}
