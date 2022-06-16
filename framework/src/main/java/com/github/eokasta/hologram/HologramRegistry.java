package com.github.eokasta.hologram;

import com.github.eokasta.hologram.protocol.HologramProtocol;
import com.github.eokasta.hologram.protocol.PlayerEntityUsePacketListener;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HologramRegistry implements Iterable<Hologram> {

    @Getter
    private final Plugin plugin;
    private final Set<Hologram> registeredHolograms = new HashSet<>();

    private final AutoUpdateHologramTask updateTask;

    public HologramRegistry(@NotNull Plugin plugin, long delay, long period) {
        this.plugin = plugin;

        this.updateTask = new AutoUpdateHologramTask(this);
        updateTask.initialize(delay, period);

        HologramProtocol.registerPacketListener(new PlayerEntityUsePacketListener(this));
    }

    public HologramRegistry(@NotNull Plugin plugin) {
        this(plugin, 20L, 20L);
    }

    @NotNull
    public Collection<Hologram> getHolograms() {
        return Collections.unmodifiableSet(registeredHolograms);
    }

    @Nullable
    public AbstractHologramLine getHologramLine(int entityId) {
        for (Hologram hologram : getHolograms())
            for (AbstractHologramLine line : hologram.getLines())
                if (line.getEntityId() == entityId) return line;

        return null;
    }

    public void registerHologram(@NotNull Hologram hologram) {
        this.registeredHolograms.add(hologram);
    }

    public void unregisterHologram(@NotNull Hologram hologram) {
        this.registeredHolograms.remove(hologram);
    }

    @NotNull
    @Override
    public Iterator<Hologram> iterator() {
        return getHolograms().iterator();
    }
}
