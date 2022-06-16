package com.github.eokasta.hologram.protocol;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class DataWatcherHolder {

    private WrappedDataWatcher wrappedDataWatcher;

    final WrappedDataWatcher getDataWatcher() {
        if (wrappedDataWatcher == null && HologramProtocol.isLegacyMinecraftVersion())
            wrappedDataWatcher = HologramProtocol.getDataWatcher(null);

        return wrappedDataWatcher;
    }

}
