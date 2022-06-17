package com.github.eokasta.hologram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 *
 * @author Lucas Monteiro
 */
@RequiredArgsConstructor
@Getter
public class HologramInteractContext {

    private final Player player;
    private final AbstractHologramLine handler;
    private final HologramInteractAction action;

}
