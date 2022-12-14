package me.pesekjak.machine.network.packets.in.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.pesekjak.machine.auth.PublicKeyData;
import me.pesekjak.machine.network.packets.PacketIn;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import me.pesekjak.machine.utils.ServerBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@AllArgsConstructor
@ToString
@Getter @Setter
public class PacketLoginInStart extends PacketIn {

    private static final int ID = 0x00;

    private @NotNull String username;
    private @Nullable PublicKeyData publicKeyData;
    private @Nullable UUID uuid;

    static {
        register(PacketLoginInStart.class, ID, PacketState.LOGIN_IN,
                PacketLoginInStart::new
        );
    }

    public PacketLoginInStart(@NotNull ServerBuffer buf) {
        username = buf.readString(StandardCharsets.UTF_8);
        if (buf.readBoolean())
            publicKeyData = buf.readPublicKey();
        if (buf.readBoolean())
            uuid = buf.readUUID();
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public @NotNull PacketState getPacketState() {
        return PacketState.LOGIN_IN;
    }

    @Override
    public byte @NotNull [] serialize() {
        FriendlyByteBuf buf = new FriendlyByteBuf()
                .writeString(username, StandardCharsets.UTF_8)
                .writeBoolean(publicKeyData != null);
        if (publicKeyData != null)
            buf.write(publicKeyData);
        buf.writeBoolean(uuid != null);
        if (uuid != null)
            buf.writeUUID(uuid);
        return buf.bytes();
    }

    @Override
    public @NotNull PacketIn clone() {
        return new PacketLoginInStart(new FriendlyByteBuf(serialize()));
    }

}
