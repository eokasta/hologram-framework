package com.github.eokasta.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HologramProtocol {

    private static final int DEFAULT_ENTITY_TYPE_ID, LEGACY_ENTITY_TYPE_ID, MINECRAFT_MINOR_VERSION;
    private static final EntityType ENTITY_TYPE;

    protected static final ProtocolManager PROTOCOL_MANAGER;

    static {
        DEFAULT_ENTITY_TYPE_ID = 1;
        ENTITY_TYPE = EntityType.ARMOR_STAND;
        LEGACY_ENTITY_TYPE_ID = ENTITY_TYPE.getTypeId();
        MINECRAFT_MINOR_VERSION = MinecraftVersion.getCurrentVersion().getMinor();

        PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
    }

    public static void sendDestroyPacket(
          int entityId,
          @NotNull Player target
    ) {
        final PacketContainer packet =
              PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        if (isLegacyMinecraftVersion())
            packet.getIntegerArrays().write(0, new int[]{entityId});
        else
            packet.getIntLists().write(0, Collections.singletonList(entityId));

        sendPacket(packet, target);
    }

    public static void sendSpawnPacket(
          int entityId,
          @NotNull Location location,
          @NotNull Player target,
          @NotNull DataWatcherHolder dataWatcherHolder
    ) {
        final PacketContainer packet = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

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
            packet.getEntityTypeModifier().write(0, ENTITY_TYPE);

            packet.getIntegers().write(0, entityId)
                  .write(1, DEFAULT_ENTITY_TYPE_ID);

            packet.getUUIDs().write(0, UUID.randomUUID());

            packet.getDoubles().write(0, location.getX())
                  .write(1, location.getY())
                  .write(2, location.getZ());
        }

        sendPacket(packet, target);
    }

    public static void sendMetadataCreatePacket(
          int entityId,
          @NotNull String customName,
          @NotNull Player target,
          boolean visibleCustomName,
          boolean visibleArmorStand,
          boolean small,
          boolean gravity,
          boolean arms,
          boolean noBasePlate,
          boolean marker
    ) {
        final PacketContainer metadataPacket =
              ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, entityId);

        final WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        byte flags = 0;
        if (small)
            flags |= 0x01;

        if (gravity)
            flags |= 0x02;

        if (arms)
            flags |= 0x04;

        if (noBasePlate)
            flags |= 0x08;

        if (marker)
            flags |= 0x16;

        if (isLegacyMinecraftVersion()) {
            if (!visibleArmorStand)
                dataWatcher.setObject(0, (byte) 0x20);

            dataWatcher.setObject(2, customName);
            dataWatcher.setObject(3, (byte) (visibleCustomName ? 1 : 0));
            dataWatcher.setObject(10, flags);
        } else {
            dataWatcher.setObject(
                  new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)),
                  (byte) 0x20
            );

            dataWatcher.setObject(
                  new WrappedDataWatcher.WrappedDataWatcherObject(2,
                        WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
                  Optional.of(WrappedChatComponent.fromChatMessage(customName)[0].getHandle())
            );

            if (!visibleArmorStand)
                dataWatcher.setObject(
                      new WrappedDataWatcher.WrappedDataWatcherObject(3,
                            WrappedDataWatcher.Registry.get(Boolean.class)),
                      true
                );

            dataWatcher.setObject(
                  new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)),
                  flags
            );
        }
    }

    public static void sendMetadataTextUpdatePacket(
          int entityId,
          @NotNull String customName,
          @NotNull Player target
    ) {
        final PacketContainer metadataPacket =
              ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, entityId);

        final WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        if (isLegacyMinecraftVersion())
            dataWatcher.setObject(2, customName);
        else
            dataWatcher.setObject(
                  new WrappedDataWatcher.WrappedDataWatcherObject(2,
                        WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
                  Optional.of(WrappedChatComponent.fromChatMessage(customName)[0].getHandle())
            );

        metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        sendPacket(metadataPacket, target);
    }

    protected static boolean isLegacyMinecraftVersion() {
        return MINECRAFT_MINOR_VERSION < 9;
    }

    private static void sendPacket(PacketContainer packet, Player target) {
        try {
            PROTOCOL_MANAGER.sendServerPacket(target, packet);
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    static WrappedDataWatcher getDataWatcher(final Location location) {
        Location target = location;

        // use a dummy location if location wasn't set
        if (target == null) {
            final World world = Bukkit.getWorlds().get(0);
            target = new Location(world, 0, world.getMaxHeight(), 0, 0, 0);
        }

        return createDataWatcher(target);
    }

    private static WrappedDataWatcher createDataWatcher(@NotNull Location location) {
        final World world = Objects.requireNonNull(location.getWorld(), "The world of location is null.");
        final Entity entity = world.spawnEntity(location, EntityType.ARMOR_STAND);
        final WrappedDataWatcher wrappedDataWatcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        entity.remove();

        return wrappedDataWatcher;
    }

}
