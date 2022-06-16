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

    public static void registerPacketListener(@NotNull PacketAdapter packetListener) {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
    }

    protected static boolean isLegacyMinecraftVersion() {
        return MINECRAFT_MINOR_VERSION < 9;
    }

    protected static WrappedDataWatcher getDataWatcher(final Location location) {
        Location target = location;

        if (target == null) {
            final World world = Bukkit.getWorlds().get(0);
            target = new Location(world, 0, world.getMaxHeight(), 0, 0, 0);
        }

        return createDataWatcher(target);
    }

    private static void sendPacket(PacketContainer packet, Player target) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(target, packet);
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static WrappedDataWatcher createDataWatcher(@NotNull Location location) {
        final World world = Objects.requireNonNull(location.getWorld(), "The world of location is null.");
        final Entity entity = world.spawnEntity(location, EntityType.ARMOR_STAND);
        final WrappedDataWatcher wrappedDataWatcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        entity.remove();

        return wrappedDataWatcher;
    }

}
