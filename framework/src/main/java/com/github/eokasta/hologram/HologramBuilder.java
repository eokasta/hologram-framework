package com.github.eokasta.hologram;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is the {@link Hologram} builder, making it easy to create.
 */
@NoArgsConstructor
public class HologramBuilder {

    private final List<LineType> lines = new ArrayList<>();
    private final HologramInteractHandler hologramInteractHandler = new HologramInteractHandler();

    /**
     * Adds a new interaction with the hologram.
     *
     * @param action the {@link HologramInteractAction} type.
     * @param consumer the consumer that will happen.
     * @return this constructor.
     * @see HologramInteractAction
     * @see HologramInteractContext
     * @see HologramInteractHandler
     */
    public HologramBuilder addAction(
          @NotNull HologramInteractAction action,
          @NotNull Consumer<HologramInteractContext> consumer
    ) {
        hologramInteractHandler.addAction(action, consumer);
        return this;
    }

    /**
     * Adds a new left click interaction with the hologram.
     *
     * @param consumer the consumer that will happen.
     * @return this constructor.
     * @see HologramBuilder#addAction(HologramInteractAction, Consumer)
     */
    public HologramBuilder addLeftClickAction(@NotNull Consumer<HologramInteractContext> consumer) {
        return addAction(HologramInteractAction.LEFT_CLICK, consumer);
    }

    /**
     * Adds a new right click interaction with the hologram.
     *
     * @param consumer the consumer that will happen.
     * @return this constructor.
     * @see HologramBuilder#addAction(HologramInteractAction, Consumer)
     */
    public HologramBuilder addRightClickAction(@NotNull Consumer<HologramInteractContext> consumer) {
        return addAction(HologramInteractAction.RIGHT_CLICK, consumer);
    }

    /**
     * Adds a line of text to the hologram.
     *
     * @param text the text to be added.
     * @return this constructor.
     * @see TextHologramLine
     */
    public HologramBuilder addLine(@NotNull String text) {
        addLine(text, String.class);
        return this;
    }

    /**
     * Adds a dynamic line to the hologram.
     *
     * @param function the dynamic line function for each player.
     * @return this constructor.
     * @see DynamicHologramLine
     */
    public HologramBuilder addDynamicTextLine(@NotNull Function<Player, String> function) {
        return addLine(function, String.class);
    }

    /**
     * Adds a empty line to the hologram.
     *
     * @return this constructor.
     * @see EmptyHologramLine
     */
    public HologramBuilder addEmptyLine() {
        return addLine(null, null);
    }

    /**
     * Builds a new hologram with all settings.
     *
     * @return a {@link Hologram} instance.
     */
    public Hologram build() {
        final List<AbstractHologramLine> lines = new ArrayList<>();
        final Hologram hologram = new Hologram(lines);
        hologram.setInteractHandler(hologramInteractHandler);

        this.lines.stream()
              .map(lineType -> resolveLineType(hologram, lineType))
              .forEach(line -> lines.add(0, line));

        return hologram;
    }

    /**
     * Builds a new hologram with all settings and register on {@link HologramRegistry}.
     *
     * @return a {@link Hologram} instance.
     */
    public Hologram build(@NotNull HologramRegistry registry) {
        final Hologram hologram = build();
        registry.registerHologram(hologram);

        return hologram;
    }

    private HologramBuilder addLine(@Nullable Object value, @Nullable Class<?> type) {
        this.lines.add(new LineType(value, type));
        return this;
    }

    @SuppressWarnings("unchecked")
    private AbstractHologramLine resolveLineType(
          @NotNull Hologram hologram,
          @NotNull LineType lineType
    ) {
        final Object value = lineType.value;

        if (value instanceof BiFunction)
            return resolveDynamicFunctionType(hologram,
                  (BiFunction<AbstractHologramLine, Player, Object>) value,
                  lineType.type
            );

        if (value instanceof Function)
            return resolveDynamicFunctionType(
                  hologram,
                  ($, player) -> ((Function<Player, Object>) value).apply(player),
                  lineType.type
            );

        return createStaticLine(hologram, value);
    }

    private AbstractHologramLine resolveDynamicFunctionType(
          @NotNull Hologram hologram,
          final BiFunction<AbstractHologramLine, Player, Object> function,
          final Class<?> type
    ) {
        DynamicHologramLine dynamicHologramLine;
        if (type.equals(String.class))
            dynamicHologramLine = new TextHologramLine(hologram);
        else
            throw new IllegalArgumentException("Unsupported hologram type: " + type);

        dynamicHologramLine.setFunction(function);
        return dynamicHologramLine;
    }

    private AbstractHologramLine createStaticLine(
          @NotNull Hologram hologram,
          Object value
    ) {
        if (value == null) {
            return new EmptyHologramLine(hologram);
        }

        if (value instanceof String) {
            final TextHologramLine line = new TextHologramLine(hologram);
            line.setText((String) value);
            return line;
        }

        throw new IllegalArgumentException("Unsupported hologram type: " + value.getClass().getName());
    }

    @RequiredArgsConstructor
    private static final class LineType {

        private final Object value;
        private final Class<?> type;

    }

}
