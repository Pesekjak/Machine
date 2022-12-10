package me.pesekjak.machine.network;

import lombok.Getter;
import me.pesekjak.machine.Machine;
import me.pesekjak.machine.network.packets.Packet;
import me.pesekjak.machine.network.packets.PacketFactory;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.server.ServerProperty;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Connection of the Machine server
 */
public class ServerConnectionImpl extends Thread implements ServerConnection {

    public final static int READ_IDLE_TIMEOUT = 30000;
    public final static int KEEP_ALIVE_FREQ = 20000;

    @Getter
    private final Machine server;
    private final Set<ClientConnection> clients = new CopyOnWriteArraySet<>();
    @Getter
    private final String ip;
    @Getter
    private final int port;
    @Getter
    private ServerSocket socket;
    private boolean running;

    public ServerConnectionImpl(Machine server) {
        if(server.getConnection() != null)
            throw new IllegalStateException();
        this.server = server;
        ip = server.getProperties().getServerIp();
        port = server.getProperties().getServerPort();
        start();
    }

    /**
     * Starts listening to the clients.
     */
    @Override
    public void run() {
        try {
            socket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            server.getConsole().info("Server is listening on '" + socket.getInetAddress().getHostName() + ":" + socket.getLocalPort() + "'");
            running = true;
            while(running) {
                try {
                    Socket connection = socket.accept();
                    ClientConnection sc = new ClientConnection(server, connection);
                    clients.add(sc);
                    sc.start();
                } catch (Exception exception) {
                    if(server.isRunning()) // preventing socket exception when closed
                        server.getExceptionHandler().handle(exception);
                }
            }
        } catch (IOException exception) {
            server.getExceptionHandler().handle(exception);
            System.exit(2);
        }
    }

    /**
     * Closes the server connection.
     */
    @Override
    public void close() {
        if(!running)
            throw new IllegalStateException("Server connection isn't running");
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) { }
    }

    /**
     * @return list of all connected clients
     */
    public @NotNull Set<PlayerConnection> getClients() {
        return Collections.unmodifiableSet(clients);
    }

    /**
     * Sends a packet to all clients with state matching the packet's state.
     * @param packet packet that will be sent
     */
    public void broadcastPacket(@NotNull Packet packet) throws IOException {
        final Set<ClientConnection.ClientState> states = Arrays.stream(PlayerConnection.ClientState.fromState(packet.getPacketState()))
                .collect(Collectors.toSet());
        for(ClientConnection client : clients) {
            if(states.contains(client.getClientState())) client.sendPacket(packet);
        }
    }

    /**
     * Disconnects the client connection.
     * @param connection client connection to disconnect
     */
    protected void disconnect(@NotNull ClientConnection connection) {
        if(connection.getClientState() != ClientConnection.ClientState.DISCONNECTED)
            connection.disconnect();
        clients.remove(connection);
    }

}