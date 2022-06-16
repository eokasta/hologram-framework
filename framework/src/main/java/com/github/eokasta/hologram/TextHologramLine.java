package com.github.eokasta.hologram;

import com.github.eokasta.hologram.protocol.HologramProtocol;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TextHologramLine extends DynamicHologramLine {

    @Getter
    @Setter
    private String text;

    public TextHologramLine(Hologram hologram) {
        super(hologram, HologramSettings.builder()
                    .visibleArmorStand(false)
                    .visibleCustomName(true)
                    .build(),
              0.26f);
    }

    @Override
    protected void show(@NotNull Player player) {
        super.show(player);
        updateLine(player);
    }

    @Override
    protected void update(@NotNull Player player) {
        updateLine(player);
    }

    private void updateLine(@NotNull Player player) {
        final String text =
              Objects.requireNonNull(applyOrGetValue(this.text, player), "Hologram line text cannot be null.");

        HologramProtocol.sendMetadataCreatePacket(
              entityId,
              player,
              text,
              settings.isVisibleCustomName(),
              settings.isVisibleArmorStand(),
              settings.isSmall(),
              settings.isArms(),
              settings.isNoBasePlate(),
              settings.isMarker()
        );
    }

}
