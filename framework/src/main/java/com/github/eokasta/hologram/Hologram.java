package com.github.eokasta.hologram;

import com.github.eokasta.hologram.protocol.HologramProtocol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is the main composition of holographic lines.
 *
 * @author Lucas Monteiro
 */
@RequiredArgsConstructor
@Getter
public class Hologram {

    private final Set<Player> invisibleTo = new HashSet<>();
    private final Set<Player> hiddenTo = new HashSet<>();
    private final List<AbstractHologramLine> lines;

    @Setter
    private HologramInteractHandler interactHandler;

    @Setter
    private Location location;
    private boolean spawned;
    private boolean destroyed;

    /**
     * Gets the unmodifiable lines from hologram.
     *
     * @return unmodifiable lines from this hologram.
     */
    @NotNull
    public List<AbstractHologramLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Gets the specific line by index.
     *
     * @param index index of the line.
     * @return the hologram line at the specified position.
     */
    @Nullable
    public AbstractHologramLine getLine(int index) {
        return lines.get(index);
    }

    /**
     * Shows all lines of the hologram to the player.
     *
     * @param player player who will see the hologram.
     */
    public void show(@NotNull Player player) {
        if (!spawned) return;

        this.invisibleTo.remove(player);
        this.hiddenTo.remove(player);

        this.lines.forEach(line -> line.show(player));
    }

    /**
     * Hides the hologram from the player.
     *
     * @param player player who will no longer see the hologram.
     */
    public void hide(@NotNull Player player) {
        this.lines.forEach(line -> line.hide(player));
        this.hiddenTo.add(player);
    }

    /**
     * Sets invisibility of the hologram to the player.
     *
     * @param player player who will no longer see the hologram.
     */
    public void invisible(@NotNull Player player) {
        this.lines.forEach(line -> line.hide(player));
        this.invisibleTo.add(player);
    }

    /**
     * Checks if the hologram is visible to the player.
     *
     * @param player the player to be checked.
     * @return <b>true</b> if the hologram is visible to the player or <b>false</b> if not visible.
     */
    public boolean isVisibleTo(@NotNull Player player) {
        return !invisibleTo.contains(player);
    }

    /**
     * Checks if the hologram is hidden from the player.
     *
     * @param player the player to be checked.
     * @return <b>true</b> if the hologram is hidden from the player or <b>false</b> if not hidden.
     */
    public boolean isHiddenTo(@NotNull Player player) {
        return hiddenTo.contains(player);
    }

    /**
     * Checks if the player can see this hologram.
     *
     * @param player the player to be checked.
     * @return <b>true</b> if the player can see the hologram or <b>false</b> if cannot see.
     */
    public boolean canSee(@NotNull Player player) {
        if (!spawned) return false;

        Objects.requireNonNull(location, "Hologram location cannot be null.");

        return isInRange(player) && isVisibleTo(player);
    }

    /**
     * Checks if the player is within the range to see the hologram.
     *
     * @param player the player to be checked.
     * @return <b>true</b> if the player is within the range or <b>false</b> if not.
     */
    public boolean isInRange(@NotNull Player player) {
        Objects.requireNonNull(location, "Hologram location cannot be null.");
        final World locationWorld =
              Objects.requireNonNull(location.getWorld(), "Hologram world cannot be null.");

        if (!locationWorld.equals(player.getWorld())) return false;

        final int viewDistance = HologramProtocol.isLegacyMinecraftVersion() ?
              Bukkit.getViewDistance() * 500 :
              locationWorld.getViewDistance() * 500;

        return location.distanceSquared(player.getLocation()) <= viewDistance;
    }

    /**
     * Updates the hologram for all players, checking whether or not they can see it.
     */
    public void update() {
        if (!spawned) return;

        Objects.requireNonNull(location, "Hologram location cannot be null.");

        for (Player player : getPlayersOnWorld()) {
            final boolean inRange = isInRange(player);
            final boolean visible = isVisibleTo(player);
            final boolean hiddenTo = isHiddenTo(player);

            if (!inRange && visible && !hiddenTo) {
                hide(player);
                continue;
            }

            if (inRange && visible && hiddenTo) {
                show(player);
                continue;
            }

            if (inRange)
                update(player);
        }
    }

    /**
     * Updates the hologram for a specific player, checking whether or not they can see it.
     */
    public void update(Player player) {
        if (!spawned || !canSee(player)) return;

        lines.forEach(line -> line.update(player));
    }

    /**
     * Spawns the hologram to a location and shows it to all players who can see it.
     *
     * @param location the location to be spawned.
     */
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

    /**
     * Completely destroys the hologram, removing it for all players.
     */
    public void destroy() {
        if (destroyed)
            throw new IllegalStateException("Hologram already destroyed.");

        getPlayersOnWorld().stream()
              .filter(this::canSee)
              .forEach(this::hide);

        this.destroyed = true;
        this.spawned = false;
    }

    /**
     * Teleports the hologram to a new location and updates it for all players who can see it.
     *
     * @param location the location the hologram will teleport to.
     */
    public void teleportTo(@NotNull Location location) {
        if (!spawned)
            throw new IllegalStateException("Hologram needs to be spawned to teleport.");

        setLocation(location);
        final Location clonedLocation = location.clone();

        getPlayersOnWorld().stream()
              .filter(this::canSee)
              .forEach(player -> {
                  for (AbstractHologramLine line : lines)
                      line.teleportTo(player, clonedLocation.add(0.0f, line.getHeight(), 0.0f).clone());
              });
    }

    /**
     * Completely invalidates the player from this hologram.
     *
     * @param player the player who will be invalidated.
     */
    protected void invalidatePlayer(@NotNull Player player) {
        invisibleTo.remove(player);
        hiddenTo.remove(player);
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
