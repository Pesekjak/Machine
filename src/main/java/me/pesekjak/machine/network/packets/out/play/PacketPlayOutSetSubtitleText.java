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
public class PacketPlayOutSetSubtitleText extends PacketOut {

    private static final int ID = 0x5B;

    @Getter @Setter
    private @NotNull Component text;

    static {
        register(PacketPlayOutSetSubtitleText.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutSetSubtitleText::new);
    }

    public PacketPlayOutSetSubtitleText(@NotNull ServerBuffer buf) {
        text = buf.readComponent();
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
                .writeComponent(text)
                .bytes();
    }

    @Override
    public @NotNull PacketOut clone() {
        return new PacketPlayOutSetSubtitleText(new FriendlyByteBuf(serialize()));
    }

}
