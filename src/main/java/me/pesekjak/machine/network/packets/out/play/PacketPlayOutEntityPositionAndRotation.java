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
public class PacketPlayOutEntityPositionAndRotation extends PacketOut {

    private static final int ID = 0x29;

    static {
        register(PacketPlayOutEntityPositionAndRotation.class, ID, PacketState.PLAY_OUT,
                PacketPlayOutEntityPositionAndRotation::new);
    }

    private int entityId;
    private short deltaX, deltaY, deltaZ;
    private float yaw, pitch;
    private boolean onGround;

    public PacketPlayOutEntityPositionAndRotation(FriendlyByteBuf buf) {
        entityId = buf.readVarInt();
        deltaX = buf.readShort();
        deltaY = buf.readShort();
        deltaZ = buf.readShort();
        yaw = buf.readAngle();
        pitch = buf.readAngle();
        onGround = buf.readBoolean();
    }

    public PacketPlayOutEntityPositionAndRotation(int entityId, Location previousLocation, Location newLocation, boolean onGround) {
        this.entityId = entityId;
        this.deltaX = (short) ((newLocation.getX() * 32 - previousLocation.getX() * 32) * 128);
        this.deltaY = (short) ((newLocation.getY() * 32 - previousLocation.getY() * 32) * 128);
        this.deltaZ = (short) ((newLocation.getZ() * 32 - previousLocation.getZ() * 32) * 128);
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
                .writeShort(deltaX)
                .writeShort(deltaY)
                .writeShort(deltaZ)
                .writeAngle(yaw)
                .writeAngle(pitch)
                .writeBoolean(onGround)
                .bytes();
    }

    @Override
    public PacketOut clone() {
        return new PacketPlayOutEntityPositionAndRotation(new FriendlyByteBuf(serialize()));
    }
}
