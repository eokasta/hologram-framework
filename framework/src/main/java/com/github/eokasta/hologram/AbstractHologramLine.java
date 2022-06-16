package com.github.eokasta.hologram;

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

    protected abstract void update(@NotNull Player player);

}
