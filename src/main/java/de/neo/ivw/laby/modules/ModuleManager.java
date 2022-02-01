package de.neo.ivw.laby.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModuleManager {

    private static ModuleManager instance;

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    private final static HashMap<String, IModule> modules = new HashMap<>();

    private ModuleManager() {
    }

    public void registerModule(IModule module) {
        modules.put(module.getCommand(), module);
        System.out.println("Registered module: " + module.getCommand());
    }

    public IModule getModule(String command) {
        IModule module = modules.get(command);
        if (module == null) {
            module = modules.get("help");
        }
        return module;
    }

    public List<IModule> getModules() {
        return new ArrayList<>(modules.values());
    }
}
