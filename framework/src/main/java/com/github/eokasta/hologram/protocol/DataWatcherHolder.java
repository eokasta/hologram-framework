package com.github.eokasta.hologram.protocol;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;

/**
 * Class to holder a {@link WrappedDataWatcher} instance.
 *
 * @author Lucas Monteiro
 */
public class DataWatcherHolder {

    private WrappedDataWatcher wrappedDataWatcher;

    /**
     * Gets existent {@link WrappedDataWatcher} or create new instance.
     *
     * @return an existing {@link WrappedDataWatcher} or create new instance.
     */
    final WrappedDataWatcher getDataWatcher() {
        if (wrappedDataWatcher == null && HologramProtocol.isLegacyMinecraftVersion())
            wrappedDataWatcher = HologramProtocol.getDataWatcher();

        return wrappedDataWatcher;
    }

}
