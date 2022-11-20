package me.pesekjak.machine.events.translations.translators.in.movement;

import me.pesekjak.machine.entities.Player;
import me.pesekjak.machine.events.translations.PacketTranslator;
import me.pesekjak.machine.network.ClientConnection;
import me.pesekjak.machine.network.packets.in.play.PacketPlayInPlayerOnGround;
import org.jetbrains.annotations.NotNull;

public class TranslatorPlayInPlayerOnGround extends PacketTranslator<PacketPlayInPlayerOnGround> {

    private Player player;

    @Override
    public boolean translate(ClientConnection connection, PacketPlayInPlayerOnGround packet) {
        player = connection.getOwner();
        return player != null;
    }

    @Override
    public void translateAfter(ClientConnection connection, PacketPlayInPlayerOnGround packet) {
        player.handleOnGround(packet.isOnGround());
    }

    @Override
    public @NotNull Class<PacketPlayInPlayerOnGround> packetClass() {
        return PacketPlayInPlayerOnGround.class;
    }

}
