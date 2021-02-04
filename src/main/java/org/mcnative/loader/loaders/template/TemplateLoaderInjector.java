package org.mcnative.loader.loaders.template;

import org.mcnative.loader.GuestPluginExecutor;

import java.io.File;

public interface TemplateLoaderInjector {

    ClassLoader getClassLoader(GuestPluginExecutor executor,File location);

    void handleEnable(GuestPluginExecutor executor);

}
