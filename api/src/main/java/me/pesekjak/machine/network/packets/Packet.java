package me.pesekjak.machine.network.packets;

import lombok.Getter;
import org.jetbrains.annotations.*;

import java.util.Set;

/**
 * Represents a server packet.
 */
public interface Packet extends Cloneable {

    /**
     * @return mapped id of the packet
     */
    int getId();

    /**
     * @return packet state used by the packet
     */
    @NotNull PacketState getPacketState();

    /**
     * Serializes the packet data, doesn't contain packet size and id.
     * @return serialized packet data
     */
    byte @NotNull [] serialize();

    /**
     * Serializes the full packet including size and id.
     * @return serialized packet
     */
    byte @NotNull [] rawSerialize();

    /**
     * Returns the size of the packet, including both id and data.
     * @return size of the packet
     */
    int getSize();

    /**
     * Serializes the full compress packet.
     * @param threshold threshold
     * @return serialized compressed packet
     */
    byte @NotNull [] rawCompressedSerialize(int threshold);

    /**
     * Returns the size of compressed packet, including both id and data.
     * @return compressed size of the packet
     */
    int getCompressedSize();

    @NotNull Packet clone();

    /**
     * Represents different groups of packets.
     */
    enum PacketState {

        HANDSHAKING_IN (0b000),
        HANDSHAKING_OUT(0b001),
        STATUS_IN      (0b010),
        STATUS_OUT     (0b011),
        LOGIN_IN       (0b100),
        LOGIN_OUT      (0b101),
        PLAY_IN        (0b110),
        PLAY_OUT       (0b111);

        public static final int OFFSET = 12;

        @Getter
        private final int mask;

        PacketState(int mask) {
            this.mask = mask << OFFSET;
        }

        /**
         * Returns a packet state from its mask.
         * @param mask mask of the packet state
         * @return packet state with given mask
         */
        public static @Nullable PacketState fromMask(@Range(from = 0, to = 0b111) int mask) {
            for(PacketState state : values()) {
                if(state.mask == mask) return state;
            }
            return null;
        }

        /**
         * @return unmodifiable set of packet states for packets from client to server
         */
        public static @Unmodifiable @NotNull Set<PacketState> in() {
            return Set.of(HANDSHAKING_IN, STATUS_IN, LOGIN_IN, PLAY_IN);
        }

        /**
         * @return unmodifiable set of packet states for packets from server to client
         */
        public static @Unmodifiable @NotNull Set<PacketState> out() {
            return Set.of(HANDSHAKING_OUT, STATUS_OUT, LOGIN_OUT, PLAY_OUT);
        }

    }

}
