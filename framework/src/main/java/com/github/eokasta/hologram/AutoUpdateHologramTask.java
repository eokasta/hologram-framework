package com.github.eokasta.hologram;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

/**
 *
 * @author Lucas Monteiro
 */
@RequiredArgsConstructor
public class AutoUpdateHologramTask implements Runnable {

    private final HologramRegistry registry;

    @Override
    public void run() {
        registry.forEach(hologram -> {
            if (hologram.isDestroyed())
                registry.unregisterHologram(hologram);

            hologram.update();
        });
    }

    /**
     * Initializes the auto update holograms with the delay to start and the period between each update.
     *
     * @param delay the delay to start.
     * @param period the period between each update.
     */
    public void initialize(long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(registry.getPlugin(), this, delay, period);
    }

}
