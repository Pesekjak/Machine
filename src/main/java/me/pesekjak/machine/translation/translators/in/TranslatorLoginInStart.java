package me.pesekjak.machine.translation.translators.in;

import me.pesekjak.machine.auth.OnlineServer;
import me.pesekjak.machine.entities.ServerPlayer;
import me.pesekjak.machine.entities.player.PlayerProfile;
import me.pesekjak.machine.entities.player.PlayerProfileImpl;
import me.pesekjak.machine.translation.PacketTranslator;
import me.pesekjak.machine.network.ClientConnection;
import me.pesekjak.machine.network.packets.in.login.PacketLoginInStart;
import me.pesekjak.machine.network.packets.out.login.PacketLoginOutEncryptionRequest;
import me.pesekjak.machine.network.packets.out.login.PacketLoginOutSuccess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TranslatorLoginInStart extends PacketTranslator<PacketLoginInStart> {

    @Override
    public boolean translate(@NotNull ClientConnection connection, @NotNull PacketLoginInStart packet) {
        return true;
    }

    @Override
    public void translateAfter(@NotNull ClientConnection connection, @NotNull PacketLoginInStart packet) {
        connection.setLoginUsername(packet.getUsername());
        if(!connection.getServer().isOnline()) {
            final PlayerProfile profile = PlayerProfileImpl.offline(packet.getUsername());
            try {
                connection.sendPacket(new PacketLoginOutSuccess(profile.getUuid(), profile.getUsername(), profile.getTextures()));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            if(connection.getClientState() == ClientConnection.ClientState.DISCONNECTED)
                return;
            connection.setClientState(ClientConnection.ClientState.PLAY);
            ServerPlayer.spawn(connection.getServer(), profile, connection);
            return;
        }
        OnlineServer onlineServer = connection.getServer().getOnlineServer();
        if(onlineServer == null) {
            connection.disconnect();
            throw new IllegalStateException("Online server hasn't been initialized");
        }
        byte[] publicKey = onlineServer.getKey().getPublic().getEncoded();
        byte[] verifyToken = onlineServer.nextVerifyToken();

        connection.setPublicKeyData(packet.getPublicKeyData());
        try {
            connection.sendPacket(new PacketLoginOutEncryptionRequest(publicKey, verifyToken));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public @NotNull Class<PacketLoginInStart> packetClass() {
        return PacketLoginInStart.class;
    }

}
