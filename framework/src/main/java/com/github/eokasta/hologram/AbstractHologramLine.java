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

    protected void hide(@NotNull Player player) {
        HologramProtocol.sendDestroyPacket(entityId, player);
    }

    protected void show(@NotNull Player player) {
        HologramProtocol.sendSpawnPacket(entityId, location, player, new DataWatcherHolder());
    }

    protected void teleportTo(@NotNull Player player, @NotNull Location location) {
        if (location.equals(this.location)) return;

        this.location = location;
        HologramProtocol.sendTeleportPacket(entityId, player, location);
    }

    protected void onUpdate(@NotNull Player player) {

    }

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
