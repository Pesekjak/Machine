package me.pesekjak.machine.network.packets.out.play;

import lombok.*;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import me.pesekjak.machine.world.Location;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

@AllArgsConstructor
@ToString
@Getter @Setter
public class PacketPlayOutSynchronizePlayerPosition extends PacketOut {

    private static final int ID = 0x39;

    static {
        register(PacketPlayOutSynchronizePlayerPosition.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutSynchronizePlayerPosition::new);
    }

    private double x, y, z;
    private float yaw, pitch;
    @NotNull
    private Set<TeleportFlags> flags;
    private int teleportId;
    private boolean dismountVehicle;

    public PacketPlayOutSynchronizePlayerPosition(FriendlyByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        flags = TeleportFlags.unpack(buf.readByte());
        teleportId = buf.readVarInt();
        dismountVehicle = buf.readBoolean();
    }

    public PacketPlayOutSynchronizePlayerPosition(Location location, Set<TeleportFlags> flags, int teleportId, boolean dismountVehicle) {
        this(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), flags, teleportId, dismountVehicle);
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        return new FriendlyByteBuf()
                .writeDouble(x)
                .writeDouble(y)
                .writeDouble(z)
                .writeFloat(yaw)
                .writeFloat(pitch)
                .writeByte((byte) TeleportFlags.pack(flags))
                .writeVarInt(teleportId)
                .writeBoolean(dismountVehicle)
                .bytes();
    }

    @Override
    public PacketOut clone() {
        return new PacketPlayOutSynchronizePlayerPosition(new FriendlyByteBuf(serialize()));
    }

    @RequiredArgsConstructor
    public enum TeleportFlags {
        X(0),
        Y(1),
        Z(2),
        PITCH(3),
        YAW(4);

        private final int bit;

        private int getMask() {
            return 1 << bit;
        }

        private boolean isSet(int flag) {
            return (flag & getMask()) == getMask();
        }

        public static Set<TeleportFlags> unpack(int flag) {
            Set<TeleportFlags> set = EnumSet.noneOf(TeleportFlags.class);
            for (TeleportFlags value : values()) {
                if (value.isSet(flag))
                    set.add(value);
            }
            return set;
        }

        public static int pack(Set<TeleportFlags> flags) {
            int flag = 0;

            for (TeleportFlags teleportFlag : flags)
                flag |= teleportFlag.getMask();

            return flag;
        }
    }
}
