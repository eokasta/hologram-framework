package com.github.eokasta.hologram;

import com.github.eokasta.hologram.protocol.HologramProtocol;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EmptyHologramLine extends DynamicHologramLine {

    public EmptyHologramLine(Hologram hologram) {
        super(hologram, HologramSettings.builder()
              .visibleArmorStand(false)
              .visibleCustomName(false)
              .build(), 0.26f);
    }

    @Override
    protected void show(@NotNull Player player) {
        super.show(player);
        this.update(player);
    }

    @Override
    protected void update(@NotNull Player player) {
        HologramProtocol.sendMetadataCreatePacket(
              entityId,
              player,
              "",
              settings.isVisibleCustomName(),
              settings.isVisibleArmorStand(),
              settings.isSmall(),
              settings.isArms(),
              settings.isNoBasePlate(),
              settings.isMarker()
        );
    }

}
