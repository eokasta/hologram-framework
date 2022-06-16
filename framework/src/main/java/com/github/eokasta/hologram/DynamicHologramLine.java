package com.github.eokasta.hologram;

import lombok.AccessLevel;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

abstract class DynamicHologramLine extends AbstractHologramLine {

    @Setter
    protected BiFunction<AbstractHologramLine, Player, Object> function;

    public DynamicHologramLine(@NotNull Hologram hologram, @NotNull HologramSettings settings, float height) {
        super(hologram, settings, height);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    protected final <T> T applyOrGetValue(@NotNull T value, @NotNull Player player) {
        if (function == null)
            return value;

        return (T) function.apply(this, player);
    }

}
