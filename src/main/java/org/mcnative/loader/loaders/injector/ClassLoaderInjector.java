package org.mcnative.loader.loaders.injector;

import org.mcnative.loader.GuestPluginExecutor;

import java.io.File;

public interface ClassLoaderInjector {

    ClassLoader loadMcNativeClasses(File location);

    ClassLoader getClassLoader(GuestPluginExecutor executor,File location);

    void handleEnable(GuestPluginExecutor executor);

    void handleDisable(GuestPluginExecutor executor);

}
