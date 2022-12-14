package me.pesekjak.machine.network.packets;

import org.jetbrains.annotations.NotNull;

/**
 * Packet sent from server to client.
 */
public abstract class PacketOut extends PacketImpl {

    /**
     * Creates mapping and creator for the packet. Each PacketOut has to call this in static block.
     * @param packetClass class reference of the packet
     * @param id mapped id by Mojang
     * @param creator PacketCreator
     */
    protected static void register(@NotNull Class<? extends PacketOut> packetClass, int id, @NotNull PacketState state, PacketCreator<? extends PacketOut> creator) {
        id = id | state.getMask();
        PacketFactory.OUT_MAPPING.put(packetClass, id);
        PacketFactory.CREATORS.put(packetClass, creator);
    }

    public abstract @NotNull PacketOut clone();

}
