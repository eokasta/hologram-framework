package com.github.eokasta.hologram;

import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HologramRegistry {

    private final Plugin plugin;
    private final Set<Hologram> registeredHolograms = new HashSet<>();

    public HologramRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public Collection<Hologram> getHolograms() {
        return Collections.unmodifiableSet(registeredHolograms);
    }

}
