package org.mcnative.loader.loaders.template.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;
import org.mcnative.loader.loaders.GuestPluginLoader;

public class BungeeCordDummyPlugin extends Plugin {

    private GuestPluginLoader loader;

    public void setLoader(GuestPluginLoader loader) {
        this.loader = loader;
    }

    @Override
    public void onDisable() {
        loader.handlePluginDisable();
    }

    @Override
    public boolean equals(Object obj) {
        return loader.getInstance().equals(obj) || super.equals(obj);
    }
}
