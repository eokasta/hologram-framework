package com.github.eokasta.hologram;

import com.github.eokasta.hologram.protocol.DataWatcherHolder;
import com.github.eokasta.hologram.protocol.HologramProtocol;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class is responsible for abstracting the lines of holograms.
 *
 * @author Lucas Monteiro
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractHologramLine {

    private static int ENTITY_ID = 0;

    protected final Hologram hologram;
    protected final HologramSettings settings;
    protected final int entityId = --ENTITY_ID;
    protected final float height;

    @Setter(AccessLevel.PROTECTED)
    protected Location location;

    /**
     * Hides this hologram line.
     *
     * @param player player will no longer see this line.
     */
    protected void hide(@NotNull Player player) {
        HologramProtocol.sendDestroyPacket(entityId, player);
    }

    /**
     * Shows this hologram line.
     *
     * @param player player who will see this line.
     */
    protected void show(@NotNull Player player) {
        HologramProtocol.sendSpawnPacket(entityId, location, player, new DataWatcherHolder());
    }

    /**
     * Teleports this hologram line.
     *
     * @param player player who will receive the packet.
     * @param location the location where the entity will teleport to.
     */
    protected void teleportTo(@NotNull Player player, @NotNull Location location) {
        if (location.equals(this.location)) return;

        this.location = location;
        HologramProtocol.sendTeleportPacket(entityId, player, location);
    }

    /**
     * This method is called when this hologram line is updated.
     *
     * @param player player who will receive the update.
     * @see AbstractHologramLine#update(Player)
     */
    protected void onUpdate(@NotNull Player player) {

    }

    /**
     * Updates this hologram line.
     *
     * @param player player who will receive the update.
     */
    protected void update(@NotNull Player player) {
        HologramProtocol.sendMetadataCreatePacket(
              entityId,
              player,
              " ",
              settings.isVisibleCustomName(),
              settings.isVisibleArmorStand(),
              settings.isSmall(),
              settings.isArms(),
              settings.isNoBasePlate(),
              settings.isMarker()
        );

        onUpdate(player);
    }

}
