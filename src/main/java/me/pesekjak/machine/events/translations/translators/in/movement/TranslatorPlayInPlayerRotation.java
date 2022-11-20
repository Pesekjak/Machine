package me.pesekjak.machine.events.translations.translators.in.movement;

import me.pesekjak.machine.entities.Player;
import me.pesekjak.machine.events.translations.PacketTranslator;
import me.pesekjak.machine.network.ClientConnection;
import me.pesekjak.machine.network.packets.in.play.PacketPlayInPlayerRotation;
import me.pesekjak.machine.world.Location;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TranslatorPlayInPlayerRotation extends PacketTranslator<PacketPlayInPlayerRotation> {

    private Player player;
    private Location location;

    @Override
    public boolean translate(ClientConnection connection, PacketPlayInPlayerRotation packet) {
        player = connection.getOwner();
        if (player == null)
            return false;
        location = packet.getLocation()
                .withX(player.getLocation().getX())
                .withY(player.getLocation().getY())
                .withZ(player.getLocation().getZ());
        if (Location.isInvalid(location)) {
            connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return false;
        }
        return true;
    }

    @Override
    public void translateAfter(ClientConnection connection, PacketPlayInPlayerRotation packet) {
        try {
            player.handleMovement(location, packet.isOnGround());
        }
        catch (IOException e) {
            connection.getServer().getExceptionHandler().handle(e);
        }
    }

    @Override
    public @NotNull Class<PacketPlayInPlayerRotation> packetClass() {
        return PacketPlayInPlayerRotation.class;
    }

}
