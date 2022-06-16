package com.github.eokasta.hologram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class Hologram {

    private final Set<Player> invisibleTo = Collections.newSetFromMap(new WeakHashMap<>());
    private final List<AbstractHologramLine> lines;

    @Setter
    private Location location;
    private boolean spawned;
    private boolean destroyed;

    @NotNull
    public List<AbstractHologramLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Nullable
    public AbstractHologramLine getLine(int index) {
        return lines.get(index);
    }

    public void show(@NotNull Player player) {
        if (!spawned) return;

        this.invisibleTo.remove(player);

        this.lines.forEach(line -> line.show(player));
    }

    public void hide(@NotNull Player player) {
        this.lines.forEach(line -> line.hide(player));
        this.invisibleTo.add(player);
    }

    public boolean isVisibleTo(@NotNull Player player) {
        return !invisibleTo.contains(player);
    }

    public boolean canSee(@NotNull Player player) {
        if (!spawned) return false;

        Objects.requireNonNull(location, "Hologram location cannot be null.");

        return isInRange(player) && !invisibleTo.contains(player);
    }

    public boolean isInRange(@NotNull Player player) {
        Objects.requireNonNull(location, "Hologram location cannot be null.");
        final World locationWorld =
              Objects.requireNonNull(location.getWorld(), "Hologram world cannot be null.");

        if (!locationWorld.equals(player.getWorld())) return false;

        return location.distanceSquared(player.getLocation()) <= locationWorld.getViewDistance() * 500;
    }

    public void update() {
        if (!spawned) return;

        Objects.requireNonNull(location, "Hologram location cannot be null.");

        for (Player player : getPlayersOnWorld()) {
            final boolean inRange = isInRange(player);
            final boolean isInvisible = invisibleTo.contains(player);

            if (!inRange && !isInvisible) {
                hide(player);
                continue;
            }

            if (inRange && isInvisible) {
                show(player);
                continue;
            }

            if (inRange)
                update(player);
        }
    }

    public void update(Player player) {
        if (!spawned || !canSee(player)) return;

        lines.forEach(line -> line.update(player));
    }

    public void spawn(@NotNull Location location) {
        initializeLines(location);

        for (Player player : getPlayersOnWorld()) {
            if (!isInRange(player))
                continue;

            lines.forEach(line -> line.show(player));
        }

        this.location = location;
        this.spawned = true;
    }

    public void destroy() {
        if (destroyed)
            throw new IllegalStateException("Hologram already destroyed.");

        getPlayersOnWorld().stream()
              .filter(this::canSee)
              .forEach(this::hide);

        this.destroyed = true;
    }

    private void initializeLines(Location initialLocation) {
        setLocation(initialLocation);
        for (final AbstractHologramLine line : lines)
            line.setLocation(initialLocation.add(0.0f, line.getHeight(), 0.0f).clone());
    }

    private List<Player> getPlayersOnWorld() {
        final World world = Objects.requireNonNull(location.getWorld(), "Hologram world cannot be null.");

        return world.getEntities().stream()
              .filter(entity -> entity instanceof Player && !entity.hasMetadata("NPC"))
              .map(entity -> (Player) entity)
              .collect(Collectors.toList());
    }

}
