package com.github.eokasta.hologram;

import com.github.eokasta.hologram.protocol.HologramProtocol;
import com.github.eokasta.hologram.protocol.PlayerEntityUsePacketListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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

    public HologramRegistry(@NotNull Plugin plugin, long delay, long period) {
        this.plugin = plugin;

        new AutoUpdateHologramTask(this).initialize(delay, period);

        HologramProtocol.registerPacketListener(new PlayerEntityUsePacketListener(this));

        Bukkit.getPluginManager().registerEvents(new PlayerHologramListener(), plugin);
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

    final class PlayerHologramListener implements Listener {

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            final Player player = event.getPlayer();

            for (Hologram hologram : getHolograms()) {
                if (!hologram.canSee(player)) continue;

                hologram.show(player);
            }
        }

        @EventHandler
        public void onWorldChange(PlayerChangedWorldEvent event) {
            final Player player = event.getPlayer();

            for (Hologram hologram : getHolograms()) {
                if (!hologram.canSee(player)) continue;

                hologram.show(player);
            }
        }

    }

}
