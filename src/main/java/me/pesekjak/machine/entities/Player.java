package me.pesekjak.machine.entities;

import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.machine.Machine;
import me.pesekjak.machine.chat.ChatColor;
import me.pesekjak.machine.chat.ChatMode;
import me.pesekjak.machine.chat.ChatUtils;
import me.pesekjak.machine.entities.player.Gamemode;
import me.pesekjak.machine.entities.player.Hand;
import me.pesekjak.machine.entities.player.PlayerProfile;
import me.pesekjak.machine.entities.player.SkinPart;
import me.pesekjak.machine.network.ClientConnection;
import me.pesekjak.machine.network.packets.PacketOut;
import me.pesekjak.machine.network.packets.out.play.*;
import me.pesekjak.machine.network.packets.out.play.PacketPlayOutGameEvent.Event;
import me.pesekjak.machine.network.packets.out.play.PacketPlayOutSynchronizePlayerPosition.TeleportFlags;
import me.pesekjak.machine.server.NBTSerializable;
import me.pesekjak.machine.server.PlayerManager;
import me.pesekjak.machine.server.codec.Codec;
import me.pesekjak.machine.world.Difficulty;
import me.pesekjak.machine.world.Location;
import me.pesekjak.machine.world.World;
import me.pesekjak.machine.world.WorldType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Player extends LivingEntity implements Audience, NBTSerializable {

    @Getter
    private final ClientConnection connection;
    @Getter @Setter
    private PlayerProfile profile;

    @Getter
    private Gamemode gamemode = Gamemode.CREATIVE; // for now
    @Getter @Nullable
    private Gamemode previousGamemode = null;

    @Getter @Setter
    private String locale;
    @Getter @Setter
    private byte viewDistance;
    @Getter @Setter
    private ChatMode chatMode;
    @Getter @Setter
    private Set<SkinPart> displayedSkinParts;
    @Getter @Setter
    private Hand mainHand;
    @Getter @Setter
    private int latency = 0;
    @Getter
    private Component displayName;
    @Getter
    private Component playerListName;

    @Getter
    private int teleportId = 0;
    @Getter @Setter
    private boolean teleporting = false;
    @Getter @Setter
    private Location teleportLocation;

    private Player(Machine server, @NotNull PlayerProfile profile, @NotNull ClientConnection connection) {
        super(server, EntityType.PLAYER, profile.getUuid());
        this.profile = profile;
        if(connection.getOwner() != null)
            throw new IllegalStateException("There can't be multiple players with the same ClientConnection");
        if(connection.getClientState() != ClientConnection.ClientState.PLAY)
            throw new IllegalStateException("Player's connection has to be in play state");
        connection.setOwner(this);
        connection.startKeepingAlive();
        this.connection = connection;
        this.displayName = Component.text(getName());
        playerListName = displayName;
    }

    public static Player spawn(Machine server, @NotNull PlayerProfile profile, @NotNull ClientConnection connection) {
        final PlayerManager manager = server.getPlayerManager();
        if(connection.getClientState() != ClientConnection.ClientState.PLAY) {
            throw new IllegalStateException("Player can't be initialized if their connection isn't in play state");
        }
        if(manager.getPlayer(profile.getUsername()) != null || manager.getPlayer(profile.getUuid()) != null) {
            connection.disconnect(Component.translatable("disconnect.loginFailed"));
            throw new IllegalStateException("Session is already active");
        }
        Player player = new Player(server, profile, connection);
        try {
            final NBTCompound nbtCompound = server.getPlayerDataContainer().getPlayerData(player.getUuid());
            if(nbtCompound != null)
                player.load(nbtCompound);
        } catch (Exception ignored) {
            server.getConsole().warning("Failed to load player data for " + player.getName() + " (" + player.getUuid() + ")");
        }
        try {
            manager.addPlayer(player);
            final Component joinMessage = Component.translatable("multiplayer.player.joined", Component.text(player.getName())).style(ChatColor.YELLOW.asStyle());
            manager.getPlayers().forEach(serverPlayer -> serverPlayer.sendMessage(joinMessage));
            server.getConsole().info(ChatColor.YELLOW + ChatUtils.componentToString(player.getDisplayName()) + " joined the game");
            player.init();
            return player;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void init() throws IOException {
        super.init();

        NBTCompound codec = new Codec(
                getServer().getDimensionTypeManager(),
                getServer().getBiomeManager(),
                getServer().getMessenger()
        ).toNBT();

        List<String> worlds = new ArrayList<>();
        for(World world : getServer().getWorldManager().getWorlds())
            worlds.add(world.getName().toString());

        //noinspection UnstableApiUsage
        sendPacket(new PacketPlayOutLogin(
                getEntityId(),
                false,
                gamemode,
                previousGamemode,
                worlds,
                codec,
                getWorld().getDimensionType().getName(),
                getWorld().getName(),
                Hashing.sha256().hashLong(getWorld().getSeed()).asLong(),
                getServer().getProperties().getMaxPlayers(),
                getServer().getProperties().getViewDistance(),
                getServer().getProperties().getSimulationDistance(),
                getServer().getProperties().isReducedDebugScreen(),
                true,
                false,
                getWorld().getWorldType() == WorldType.FLAT,
                false,
                null,
                null
        ));

        sendPacket(PacketPlayOutPluginMessage.getBrandPacket(getServer().getProperties().getServerBrand()));

        // Spawn Sequence: https://wiki.vg/Protocol_FAQ#What.27s_the_normal_login_sequence_for_a_client.3F
        sendDifficultyChange(getWorld().getDifficulty());
        // Player Abilities (Optional)
        // Set Carried Item
        // Update Recipes
        // Update Tags
        // Entity Event (for the OP permission level)
        // Commands
        // Recipe
        // Player Position
        getServer().getConnection().broadcastPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.Action.ADD_PLAYER, this));
        for (Player player : getServer().getEntityManager().getEntitiesOfClass(Player.class)) {
            if (player == this)
                continue;
            sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.Action.ADD_PLAYER, player));
        }
        // Set Chunk Cache Center
        // Light Update (One sent for each chunk in a square centered on the player's position)
        // Level Chunk With Light (One sent for each chunk in a square centered on the player's position)
        // World Border (Once the world is finished loading)
        sendWorldSpawnChange(getWorld().getWorldSpawn());
        getServer().getConnection().broadcastPacket(new PacketPlayOutSpawnPlayer(getEntityId(), getUuid(), getLocation()), connection -> {
            if (connection.getOwner() != null && !getConnection().equals(connection))
                return connection.getOwner().getWorld().equals(getWorld());
            return false;
        });
        getServer().getConnection().broadcastPacket(new PacketPlayOutHeadRotation(getEntityId(), getLocation().getYaw()), connection -> {
            if (connection.getOwner() != null && !getConnection().equals(connection))
                return connection.getOwner().getWorld().equals(getWorld());
            return false;
        });
        synchronizePosition(getLocation(), Collections.emptySet(), false);
        for (Player player : getServer().getEntityManager().getEntitiesOfClass(Player.class)) {
            if (player == this)
                continue;
            sendPacket(new PacketPlayOutSpawnPlayer(player.getEntityId(), player.getUuid(), player.getLocation()));
            sendPacket(new PacketPlayOutHeadRotation(player.getEntityId(), player.getLocation().getYaw()));
        }
        // Inventory, entities, etc
        sendGamemodeChange(gamemode);
        getWorld().loadPlayer(this);
    }

    @Override
    public void remove() {
        try {
            if(connection.getClientState() != ClientConnection.ClientState.DISCONNECTED)
                throw new IllegalStateException("You can't remove player from server until the connection is closed");
            super.remove();
            getServer().getConnection().broadcastPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.Action.REMOVE_PLAYER, this));
            getServer().getPlayerManager().removePlayer(this);
            final Component leaveMessage = Component.translatable("multiplayer.player.left", Component.text(getName())).style(ChatColor.YELLOW.asStyle());
            getServer().getPlayerManager().getPlayers().forEach(serverPlayer -> serverPlayer.sendMessage(leaveMessage));
            getServer().getConsole().info(ChatColor.YELLOW + ChatUtils.componentToString(getDisplayName()) + " left the game");
            save();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPacket(PacketOut packet) {
        try {
            connection.sendPacket(packet);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public String getName() {
        return profile.getUsername();
    }

    public String getUsername() {
        return profile.getUsername();
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName == null ? Component.text(getName()) : displayName;
    }

    public void setPlayerListName(Component playerListName) {
        if (playerListName == null)
            playerListName = Component.text(getName());
        this.playerListName = playerListName;
        try {
            getServer().getConnection().broadcastPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.Action.UPDATE_DISPLAY_NAME, this));
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setGamemode(Gamemode gamemode) {
        previousGamemode = this.gamemode;
        this.gamemode = gamemode;
        sendGamemodeChange(gamemode);
    }

    @Override
    public void sendMessage(final @NotNull Identity source, final @NotNull Component message, final @NotNull MessageType type) {
        getServer().getMessenger().sendMessage(this, message, type);
    }

    private void sendDifficultyChange(Difficulty difficulty) {
        sendPacket(new PacketPlayOutChangeDifficulty(difficulty));
    }

    private void sendWorldSpawnChange(Location location) {
        sendPacket(new PacketPlayOutWorldSpawnPosition(location));
    }

    private void sendGamemodeChange(Gamemode gamemode) {
        sendPacket(new PacketPlayOutGameEvent(Event.CHANGE_GAMEMODE, gamemode.getId()));
    }

    public void synchronizePosition(Location location, Set<TeleportFlags> flags, boolean dismountVehicle) {
        teleporting = true;

        double x = location.getX() - (flags.contains(TeleportFlags.X) ? getLocation().getX() : 0d);
        double y = location.getY() - (flags.contains(TeleportFlags.Y) ? getLocation().getY() : 0d);
        double z = location.getZ() - (flags.contains(TeleportFlags.Z) ? getLocation().getZ() : 0d);
        float yaw = location.getYaw() - (flags.contains(TeleportFlags.YAW) ? getLocation().getYaw() : 0f);
        float pitch = location.getPitch() - (flags.contains(TeleportFlags.PITCH) ? getLocation().getPitch() : 0f);

        teleportLocation = new Location(x, y, z, yaw, pitch, null);
        if (++teleportId == Integer.MAX_VALUE)
            teleportId = 0;

        sendPacket(new PacketPlayOutSynchronizePlayerPosition(location, flags, teleportId, dismountVehicle));
    }

    public void handleMovement(Location location, boolean onGround) throws IOException {
        if (teleporting)
            return;

        handleOnGround(onGround);
        Location currentLocation = getLocation();

        double deltaX = Math.abs(location.getX() - currentLocation.getX());
        double deltaY = Math.abs(location.getY() - currentLocation.getY());
        double deltaZ = Math.abs(location.getZ() - currentLocation.getZ());
        float deltaYaw = Math.abs(location.getYaw() - currentLocation.getYaw());
        float deltaPitch = Math.abs(location.getPitch() - currentLocation.getPitch());

        boolean positionChange = (deltaX + deltaY + deltaZ) > 0;
        boolean rotationChange = (deltaYaw + deltaPitch) > 0;
        if (!(positionChange || rotationChange))
            return;

        if (positionChange) {
            if (deltaX > 8 || deltaY > 8 || deltaZ > 8)
                getServer().getConnection().broadcastPacket(new PacketPlayOutTeleportEntity(getEntityId(), location, onGround), clientConnection -> clientConnection != getConnection());

            if (rotationChange)
                getServer().getConnection().broadcastPacket(new PacketPlayOutEntityPositionAndRotation(getEntityId(), currentLocation, location, onGround), clientConnection -> clientConnection != getConnection());
            else
                getServer().getConnection().broadcastPacket(new PacketPlayOutEntityPosition(getEntityId(), currentLocation, location, onGround), clientConnection -> clientConnection != getConnection());

        } else {
            getServer().getConnection().broadcastPacket(new PacketPlayOutEntityRotation(getEntityId(), location, onGround), clientConnection -> clientConnection != getConnection());
        }

        if (rotationChange) {
            getServer().getConnection().broadcastPacket(new PacketPlayOutHeadRotation(getEntityId(), location.getYaw()), clientConnection -> clientConnection != getConnection());
        }

        setLocation(location);
    }

    public void handleOnGround(boolean onGround) {
        setOnGround(onGround);
    }

    @Override
    public NBTCompound toNBT() {
        MutableNBTCompound nbtCompound = super.toNBT().toMutableCompound();
        nbtCompound.setInt("playerGameType", gamemode.getId());
        if (previousGamemode != null)
            nbtCompound.setInt("previousPlayerGameType", previousGamemode.getId());
        return nbtCompound.toCompound();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void load(NBTCompound nbtCompound) {
        super.load(nbtCompound);
        gamemode = Gamemode.fromID(nbtCompound.contains("playerGameType") ? nbtCompound.getInt("playerGameType") : Gamemode.SURVIVAL.getId()); // TODO replace with default gamemode from server.properties
        previousGamemode = nbtCompound.contains("previousPlayerGameType") ? Gamemode.fromID(nbtCompound.getInt("previousPlayerGameType")) : null;
    }

    protected void save() {
        getServer().getPlayerDataContainer().savePlayerData(this);
    }
}
