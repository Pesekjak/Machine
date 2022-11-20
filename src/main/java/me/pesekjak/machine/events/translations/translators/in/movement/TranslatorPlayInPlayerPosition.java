package me.pesekjak.machine.events.translations.translators.in.movement;

import me.pesekjak.machine.entities.Player;
import me.pesekjak.machine.events.translations.PacketTranslator;
import me.pesekjak.machine.network.ClientConnection;
import me.pesekjak.machine.network.packets.in.play.PacketPlayInPlayerPosition;
import me.pesekjak.machine.world.Location;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TranslatorPlayInPlayerPosition extends PacketTranslator<PacketPlayInPlayerPosition> {

    private Player player;
    private Location location;

    @Override
    public boolean translate(ClientConnection connection, PacketPlayInPlayerPosition packet) {
        player = connection.getOwner();
        if (player == null)
            return false;
        location = packet.getLocation()
                .withYaw(player.getLocation().getYaw())
                .withPitch(player.getLocation().getPitch());
        if (Location.isInvalid(location)) {
            connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return false;
        }
        return true;
    }

    @Override
    public void translateAfter(ClientConnection connection, PacketPlayInPlayerPosition packet) {
        try {
            player.handleMovement(location, packet.isOnGround());
        }
        catch (IOException e) {
            connection.getServer().getExceptionHandler().handle(e);
        }
    }

    @Override
    public @NotNull Class<PacketPlayInPlayerPosition> packetClass() {
        return PacketPlayInPlayerPosition.class;
    }

}
