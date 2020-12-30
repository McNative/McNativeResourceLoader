package org.mcnative.loader.loaders;

public interface GuestPluginLoader {

    Object getInstance();

    default boolean isInstanceAvailable(){
        return getInstance() != null;
    }

    String getLoadedVersion();

    void handlePluginLoad();

    void handlePluginEnable();

    void handlePluginDisable();

}
