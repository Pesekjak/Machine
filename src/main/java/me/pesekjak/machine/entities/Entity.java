package me.pesekjak.machine.entities;

import lombok.Getter;
import lombok.Setter;
import me.pesekjak.machine.Machine;
import me.pesekjak.machine.server.NBTSerializable;
import me.pesekjak.machine.server.ServerProperty;
import me.pesekjak.machine.utils.EntityUtils;
import me.pesekjak.machine.utils.NBTUtils;
import me.pesekjak.machine.utils.UUIDUtils;
import me.pesekjak.machine.world.Location;
import me.pesekjak.machine.world.World;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class Entity implements Identity, ServerProperty, NBTSerializable, Nameable {

    @Getter
    private final Machine server;

    @Getter
    private final EntityType entityType;
    @Getter
    private UUID uuid;
    @Getter
    private final int entityId;

    @Getter
    private boolean active;

    private final Set<String> tags = new HashSet<>();
    @Getter @Setter
    private boolean silent;
    @Getter @Setter
    private boolean noGravity;
    @Getter @Setter
    private boolean glowing;
    @Getter @Setter
    private boolean hasVisualFire;
    @Getter @Setter
    private int ticksFrozen;
    @Getter
    private Location location;
    @Getter
    private Location previousLocation;
    @Getter @Setter
    private float fallDistance;
    @Getter @Setter
    private short remainingFireTicks;
    @Getter @Setter
    private boolean onGround;
    @Getter @Setter
    private boolean invulnerable;
    @Getter @Setter
    private int portalCooldown;

    public Entity(Machine server, EntityType entityType, UUID uuid) {
        this.server = server;
        this.entityType = entityType;
        this.uuid = uuid;
        entityId = EntityUtils.getEmptyID();
        location = new Location(0, 0, 0, getServer().getDefaultWorld());
        active = false;
    }

    @Override @NotNull
    public UUID uuid() {
        return uuid;
    }

    public String getName() {
        return entityType.getTypeName();
    }

    @Override
    public Component getCustomName() {
        // TODO return custom name metadata
        return null;
    }

    @Override
    public void setCustomName(Component customName) {
        // TODO set custom name metadata
    }


    public boolean isCustomNameVisible() {
        // TODO metadata
        return false;
    }

    public void setCustomNameVisible(boolean customNameVisible) {
        // TODO metadata
    }

    public World getWorld() {
        return location.getWorld();
    }

    public void setLocation(Location location) {
        if (location.getWorld() == null)
            location.setWorld(getWorld());
        this.previousLocation = this.location;
        this.location = location;
    }

    public Set<String> getTags() {
        return Set.copyOf(tags);
    }

    public boolean addTag(String tag) {
        return tags.size() < 1024 && tags.add(tag);
    }

    public boolean removeTag(String tag) {
        return tags.remove(tag);
    }

    protected void init() throws IOException {
        if (active)
            throw new IllegalStateException(this + " is already initiated");
        active = true;
        getServer().getEntityManager().addEntity(this);
    }

    public void remove() {
        if (!active)
            throw new IllegalStateException(this + " is not active");
        active = false;
        getServer().getEntityManager().removeEntity(this);
    }

    @Override
    public NBTCompound toNBT() {
        return NBT.Compound(nbt -> {
            nbt.set("Pos", NBTUtils.doubleList(location.getX(), location.getY(), location.getZ()));
            nbt.set("Motion", NBTUtils.doubleList(0, 0, 0)); // TODO implement motion
            nbt.set("Rotation", NBTUtils.floatList(location.getYaw(), location.getPitch()));
            nbt.setFloat("FallDistance", fallDistance);
            nbt.setShort("Fire", remainingFireTicks);
            nbt.setShort("Air", (short) 0);
            nbt.setByte("OnGround", (byte) (onGround ? 1 : 0));
            nbt.setByte("Invulnerable", (byte) (invulnerable ? 1 : 0));
            nbt.setInt("PortalCooldown", portalCooldown);
            nbt.setIntArray("UUID", UUIDUtils.uuidToIntArray(uuid));
            nbt.setLong("WorldUUIDLeast", getWorld().getUuid().getLeastSignificantBits());
            nbt.setLong("WorldUUIDMost", getWorld().getUuid().getMostSignificantBits());

            if (getCustomName() != null)
                nbt.setString("CustomName", GsonComponentSerializer.gson().serialize(getCustomName()));

            if (isCustomNameVisible())
                nbt.setByte("CustomNameVisible", (byte) (isCustomNameVisible() ? 1 : 0));

            if (silent)
                nbt.setByte("Silent", (byte) (silent ? 1 : 0));

            if (noGravity)
                nbt.setByte("NoGravity", (byte) (noGravity ? 1 : 0));

            if (glowing)
                nbt.setByte("Glowing", (byte) (glowing ? 1 : 0));

            if (ticksFrozen > 0)
                nbt.setInt("TicksFrozen", (byte) ticksFrozen);

            if (hasVisualFire)
                nbt.setByte("HasVisualFire", (byte) (hasVisualFire ? 1 : 0));

            if (!tags.isEmpty())
                nbt.set("Tags", new NBTList<>(NBTType.TAG_String, tags.stream()
                        .map(NBTString::new)
                        .toList()));
        });
    }

    @SuppressWarnings("ConstantConditions")
    protected void load(NBTCompound nbtCompound) {
        NBTList<NBTDouble> pos = nbtCompound.getList("Pos");
        NBTList<NBTDouble> motion = nbtCompound.getList("Motion");
        NBTList<NBTFloat> rotation = nbtCompound.getList("Rotation");

        if (pos == null)
            pos = NBTUtils.doubleList(0, 0, 0);
        if (motion == null)
            motion = NBTUtils.doubleList(0, 0, 0);
        if (rotation == null)
            rotation = NBTUtils.floatList(0, 0);

        getLocation().setX(pos.get(0).getValue());
        getLocation().setY(pos.get(1).getValue());
        getLocation().setZ(pos.get(2).getValue());
        getLocation().setYaw(rotation.get(0).getValue());
        getLocation().setPitch(rotation.get(1).getValue());

        fallDistance = nbtCompound.contains("FallDistance") ? nbtCompound.getAsFloat("FallDistance") : 0;
        remainingFireTicks = nbtCompound.contains("Fire") ? nbtCompound.getAsShort("Fire") : 0;
        onGround = nbtCompound.contains("OnGround") ? nbtCompound.getBoolean("OnGround") : false;
        invulnerable = nbtCompound.contains("Invulnerable") ? nbtCompound.getBoolean("Invulnerable") : false;
        portalCooldown = nbtCompound.contains("PortalCooldown") ? nbtCompound.getAsInt("PortalCooldown") : 0;
        if (nbtCompound.contains("UUID"))
            uuid = UUIDUtils.uuidFromIntArray(nbtCompound.getIntArray("UUID").copyArray());
        if (nbtCompound.contains("CustomName")) {
            String string = nbtCompound.getString("CustomName");
            setCustomName(GsonComponentSerializer.gson().deserialize(string));
        }
        setCustomNameVisible(nbtCompound.contains("CustomNameVisible") ? nbtCompound.getBoolean("CustomNameVisible") : false);
        silent = nbtCompound.contains("Silent") ? nbtCompound.getBoolean("Silent") : false;
        noGravity = nbtCompound.contains("NoGravity") ? nbtCompound.getBoolean("NoGravity") : false;
        glowing = nbtCompound.contains("Glowing") ? nbtCompound.getBoolean("Glowing") : false;
        ticksFrozen = nbtCompound.contains("TicksFrozen") ? nbtCompound.getInt("TicksFrozen") : 0;
        hasVisualFire = (nbtCompound.contains("HasVisualFire") ? nbtCompound.getBoolean("HasVisualFire") : false);
        if (nbtCompound.contains("Tags")) {
            tags.clear();
            NBTList<NBTString> nbtStrings = nbtCompound.getList("Tags");
            int i = Math.min(nbtStrings.getSize(), 1024);
            for (int j = 0; j < i; j++)
                tags.add(nbtStrings.get(j).getValue());
        }
    }

}
