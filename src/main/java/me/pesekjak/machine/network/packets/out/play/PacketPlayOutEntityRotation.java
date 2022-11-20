package me.pesekjak.machine.network.packets.out.play;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.utils.FriendlyByteBuf;
import me.pesekjak.machine.world.Location;

@AllArgsConstructor
@ToString
@Getter @Setter
public class PacketPlayOutEntityRotation extends PacketOut {

    private static final int ID = 0x2A;

    static {
        register(PacketPlayOutEntityRotation.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutEntityRotation::new);
    }

    private int entityId;
    private float yaw, pitch;
    private boolean onGround;

    public PacketPlayOutEntityRotation(FriendlyByteBuf buf) {
        entityId = buf.readVarInt();
        yaw = buf.readAngle();
        pitch = buf.readAngle();
        onGround = buf.readBoolean();
    }

    public PacketPlayOutEntityRotation(int entityId, Location newLocation, boolean onGround) {
        this.entityId = entityId;
        this.yaw = newLocation.getYaw();
        this.pitch = newLocation.getPitch();
        this.onGround = onGround;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public byte[] serialize() {
        return new FriendlyByteBuf()
                .writeVarInt(entityId)
                .writeAngle(yaw)
                .writeAngle(pitch)
                .writeBoolean(onGround)
                .bytes();
    }

    @Override
    public PacketOut clone() {
        return new PacketPlayOutEntityRotation(new FriendlyByteBuf(serialize()));
    }
}
