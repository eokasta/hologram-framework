package com.github.eokasta.hologram;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author Lucas Monteiro
 */
public class HologramInteractHandler {

    private final Map<HologramInteractAction, Consumer<HologramInteractContext>> actions = new HashMap<>();

    public synchronized void call(@NotNull HologramInteractAction action, @NotNull HologramInteractContext context) {
        final Consumer<HologramInteractContext> consumer = actions.get(action);
        if (consumer != null)
            consumer.accept(context);
    }

    public synchronized void addAction(@NotNull HologramInteractAction action, @NotNull Consumer<HologramInteractContext> consumer) {
        actions.put(action, consumer);
    }

    public synchronized void removeAction(@NotNull HologramInteractAction action) {
        actions.remove(action);
    }

    public synchronized Consumer<HologramInteractContext> getAction(HologramInteractAction action) {
        return actions.get(action);
    }

}
