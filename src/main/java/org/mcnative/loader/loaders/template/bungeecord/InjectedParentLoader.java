package org.mcnative.loader.loaders.template.bungeecord;

import net.md_5.bungee.api.ProxyServer;

public class InjectedParentLoader extends ClassLoader{

    private final String plugin;

    public InjectedParentLoader(String plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("LOADING "+plugin+" -> "+name);
        return ProxyServer.class.getClassLoader().loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("LOADING "+plugin+" -> "+name);
        return ProxyServer.class.getClassLoader().loadClass(name);
    }
}
