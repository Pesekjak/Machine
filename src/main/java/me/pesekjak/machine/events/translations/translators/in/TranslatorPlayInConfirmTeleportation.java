package me.pesekjak.machine.events.translations.translators.in;

import me.pesekjak.machine.entities.Player;
import me.pesekjak.machine.events.translations.PacketTranslator;
import me.pesekjak.machine.network.ClientConnection;
import me.pesekjak.machine.network.packets.in.play.PacketPlayInConfirmTeleportation;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class TranslatorPlayInConfirmTeleportation extends PacketTranslator<PacketPlayInConfirmTeleportation> {

    @Override
    public boolean translate(ClientConnection connection, PacketPlayInConfirmTeleportation packet) {
        Player player = connection.getOwner();
        if (player == null)
            return false;
        if (!player.isTeleporting() || player.getTeleportId() != packet.getTeleportId()) {
            player.setTeleporting(false);
            connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return false;
        }
        player.setTeleporting(false);
        player.setLocation(player.getTeleportLocation());
        player.setTeleportLocation(null);
        return true;
    }

    @Override
    public void translateAfter(ClientConnection connection, PacketPlayInConfirmTeleportation packet) {}

    @Override
    public @NotNull Class<PacketPlayInConfirmTeleportation> packetClass() {
        return PacketPlayInConfirmTeleportation.class;
    }

}
