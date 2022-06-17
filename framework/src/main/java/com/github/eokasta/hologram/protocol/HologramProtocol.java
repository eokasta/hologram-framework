package com.github.eokasta.hologram.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is responsible for managing all sending of hologram
 * packets using <a href="https://github.com/dmulloy2/ProtocolLib/">ProtocolLib</a>.
 *
 * @author Lucas Monteiro
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HologramProtocol {

    private static final int DEFAULT_ENTITY_TYPE_ID, LEGACY_ENTITY_TYPE_ID, MINECRAFT_MINOR_VERSION;
    private static final EntityType ENTITY_TYPE;

    static {
        DEFAULT_ENTITY_TYPE_ID = 1;
        ENTITY_TYPE = EntityType.ARMOR_STAND;
        LEGACY_ENTITY_TYPE_ID = ENTITY_TYPE.getTypeId();
        MINECRAFT_MINOR_VERSION = MinecraftVersion.getCurrentVersion().getMinor();
    }

    /**
     * Sends a packet to destroy an entity.
     *
     * @param entityId identify of the entity to be destroyed.
     * @param target player who will receive the packet.
     */
    public static void sendDestroyPacket(
          int entityId,
          @NotNull Player target
    ) {
        final PacketContainer packet =
              ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        if (isLegacyMinecraftVersion())
            packet.getIntegerArrays().write(0, new int[]{entityId});
        else
            packet.getIntLists().write(0, Collections.singletonList(entityId));

        sendPacket(packet, target);
    }

    /**
     * Sends a packet to spawn an entity.
     *
     * @param entityId identify of the entity to be spawned.
     * @param location location where the entity will be spawned.
     * @param target player who will receive the packet.
     * @param dataWatcherHolder DataWatcher holder.
     */
    public static void sendSpawnPacket(
          int entityId,
          @NotNull Location location,
          @NotNull Player target,
          @NotNull DataWatcherHolder dataWatcherHolder
    ) {
        final PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        if (isLegacyMinecraftVersion()) {
            packet.getIntegers().write(0, entityId)
                  .write(1, LEGACY_ENTITY_TYPE_ID)
                  .write(2, (int) (location.getX() * 32))
                  .write(3, (int) (location.getY() * 32))
                  .write(4, (int) (location.getZ() * 32));

            packet.getBytes().write(0,
                  (byte) (location.getYaw() * 256.0F / 360.0F));

            packet.getBytes().write(1,
                  (byte) (location.getPitch() * 256.0F / 360.0F));

            packet.getDataWatcherModifier().write(0, dataWatcherHolder.getDataWatcher());
        } else {
            packet.getIntegers().write(0, LEGACY_ENTITY_TYPE_ID);

            packet.getIntegers().write(0, entityId)
                  .write(1, DEFAULT_ENTITY_TYPE_ID);

            packet.getUUIDs().write(0, UUID.randomUUID());

            packet.getDoubles().write(0, location.getX())
                  .write(1, location.getY())
                  .write(2, location.getZ());
        }

        sendPacket(packet, target);
    }

    /**
     * Sends a packet to create/edit an entity's metadata.
     *
     * @param entityId identify of the entity to be spawned.
     * @param target player who will receive the packet.
     * @param customName armor stand entity custom name.
     * @param visibleCustomName whether or not the armor stand will have a visible name.
     * @param visibleArmorStand whether the armor support will be visible.
     * @param small whether the armor stand will be small.
     * @param arms whether the armor stand will have arms.
     * @param noBasePlate whether the armor stand will have base plate removed.
     * @param marker whether the armor support will have marker.
     */
    public static void sendMetadataCreatePacket(
          int entityId,
          @NotNull Player target,
          @NotNull String customName,
          boolean visibleCustomName,
          boolean visibleArmorStand,
          boolean small,
          boolean arms,
          boolean noBasePlate,
          boolean marker
    ) {
        final PacketContainer packet =
              ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entityId);

        final WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        byte flags = 0;
        if (isLegacyMinecraftVersion()) {
            if (!visibleArmorStand)
                dataWatcher.setObject(0, (byte) 0x20);

            dataWatcher.setObject(2, customName);
            dataWatcher.setObject(3, (byte) (visibleCustomName ? 1 : 0));

            if (small)
                flags |= 0x01;

            if (arms)
                flags |= 0x04;

            if (noBasePlate)
                flags |= 0x08;

            if (marker)
                flags |= 0x16;

            dataWatcher.setObject(10, flags);
        } else {
            if (!visibleArmorStand)
                dataWatcher.setObject(
                      new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)),
                      (byte) 0x20
                );

            if (visibleCustomName) {
                dataWatcher.setObject(
                      new WrappedDataWatcher.WrappedDataWatcherObject(2,
                            WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
                      Optional.of(WrappedChatComponent.fromChatMessage(customName)[0].getHandle())
                );

                dataWatcher.setObject(
                      new WrappedDataWatcher.WrappedDataWatcherObject(3,
                            WrappedDataWatcher.Registry.get(Boolean.class)),
                      true
                );
            }

            if (small)
                flags |= 0x01;

            if (arms)
                flags |= 0x04;

            if (noBasePlate)
                flags |= 0x08;

            if (marker)
                flags |= 0x10;

            dataWatcher.setObject(
                  new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)),
                  flags
            );

            packet.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        }

        sendPacket(packet, target);
    }

    /**
     * Sends a packet to teleport an entity.
     *
     * @param entityId identify of the entity to be spawned.
     * @param target player who will receive the packet.
     * @param location the location where the entity will teleport to.
     */
    public static void sendTeleportPacket(
          int entityId,
          @NotNull Player target,
          @NotNull Location location
    ) {
        final PacketContainer packet =
              ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);

        packet.getIntegers().write(0, entityId);

        if (isLegacyMinecraftVersion()) {
            packet.getIntegers().write(1, (int) Math.floor(location.getX() * 32));
            packet.getIntegers().write(2, (int) Math.floor(location.getY() * 32));
            packet.getIntegers().write(3, (int) Math.floor(location.getZ() * 32));
        } else {
            packet.getDoubles().write(0, location.getX());
            packet.getDoubles().write(1, location.getY());
            packet.getDoubles().write(2, location.getZ());
        }

        packet.getBytes().write(0,
              (byte) (location.getYaw() * 256.0F / 360.0F));
        packet.getBytes().write(1,
              (byte) (location.getPitch() * 256.0F / 360.0F));

        packet.getBooleans().write(0, false);

        sendPacket(packet, target);
    }

    /**
     * Register a new {@link PacketAdapter}.
     *
     * @param packetAdapter the {@link PacketAdapter} to register
     */
    public static void registerPacketListener(@NotNull PacketAdapter packetAdapter) {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }

    /**
     * Checks if the server is on the legacy version.
     *
     * @return <b>true</b> if the server is running on a legacy version or <b>false</b> if running above 1.9.
     */
    protected static boolean isLegacyMinecraftVersion() {
        return MINECRAFT_MINOR_VERSION < 9;
    }

    /**
     * Creates a new {@link WrappedDataWatcher}.
     *
     * @return a {@link WrappedDataWatcher}
     * @see HologramProtocol#createDataWatcher(Location)
     */
    protected static WrappedDataWatcher getDataWatcher() {
        final World world = Bukkit.getWorlds().get(0);
        final Location target = new Location(world, 0, world.getMaxHeight(), 0, 0, 0);

        return createDataWatcher(target);
    }

    /**
     * Sends a packets to player.
     *
     * @param packet packet to be sent.
     * @param target player who will receive the packet.
     */
    private static void sendPacket(PacketContainer packet, Player target) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(target, packet);
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link WrappedDataWatcher}.
     *
     * @return a {@link WrappedDataWatcher}
     */
    private static WrappedDataWatcher createDataWatcher(@NotNull Location location) {
        final World world = Objects.requireNonNull(location.getWorld(), "The world of location is null.");
        final Entity entity = world.spawnEntity(location, EntityType.ARMOR_STAND);
        final WrappedDataWatcher wrappedDataWatcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        entity.remove();

        return wrappedDataWatcher;
    }

}
