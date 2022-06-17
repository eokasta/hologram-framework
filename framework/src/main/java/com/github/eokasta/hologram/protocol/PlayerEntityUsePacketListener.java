package com.github.eokasta.hologram.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.github.eokasta.hologram.AbstractHologramLine;
import com.github.eokasta.hologram.HologramInteractAction;
import com.github.eokasta.hologram.HologramInteractContext;
import com.github.eokasta.hologram.HologramRegistry;

/**
 * This class is responsible for listening and filtering
 * interaction packets with holograms using <a href="https://github.com/dmulloy2/ProtocolLib/">ProtocolLib</a>.
 *
 * @author Lucas Monteiro
 */
public class PlayerEntityUsePacketListener extends PacketAdapter {

    private final HologramRegistry registry;

    public PlayerEntityUsePacketListener(HologramRegistry registry) {
        super(registry.getPlugin(), PacketType.Play.Client.USE_ENTITY);
        this.registry = registry;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        final PacketContainer packet = event.getPacket();
        final int entityId = packet.getIntegers().read(0);
        final AbstractHologramLine hologramLine = registry.getHologramLine(entityId);
        if (hologramLine == null) return;

        final WrappedEnumEntityUseAction useAction = packet.getEnumEntityUseActions().read(0);
        final HologramInteractAction action = useAction.getAction() == EnumWrappers.EntityUseAction.ATTACK ?
              HologramInteractAction.LEFT_CLICK :
              HologramInteractAction.RIGHT_CLICK;

        final HologramInteractContext context = new HologramInteractContext(event.getPlayer(), hologramLine, action);
        hologramLine.getHologram().getInteractHandler().call(action, context);
    }

}
