package com.github.eokasta.hologram;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

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

    public void initialize(long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(registry.getPlugin(), this, delay, period);
    }

}
