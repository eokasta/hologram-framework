package com.github.eokasta.hologram;

import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author Lucas Monteiro
 */
@Builder
@Getter
public class HologramSettings {

    private final boolean visibleCustomName;
    private final boolean visibleArmorStand;
    private final boolean small;
    private final boolean arms;
    private final boolean noBasePlate;
    private final boolean marker;

}
