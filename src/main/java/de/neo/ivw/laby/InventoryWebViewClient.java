package de.neo.ivw.laby;

import de.neo.ivw.laby.modules.HelpModule;
import de.neo.ivw.laby.modules.InventoryPasteModule;
import de.neo.ivw.laby.modules.ModuleManager;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Material;

import java.util.List;

public class InventoryWebViewClient extends LabyModAddon {

    private String invPasteUrl = "https://paste.neo8.de/inv/";

    private static InventoryWebViewClient instance;
    public static InventoryWebViewClient getInstance() {
        return instance;
    }

    private boolean waitingForInventory;

    public InventoryWebViewClient() {
        instance = this;
        waitingForInventory = false;
    }

    @Override
    public void onEnable() {
        registerModules();
    }

    @Override
    public void loadConfig() {
        if(!getConfig().has("paste.inv.url")) {
            getConfig().addProperty("paste.inv.url", "https://paste.neo8.de/inv/");
        }
        invPasteUrl = getConfig().get("paste.inv.url").getAsString();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new StringElement(
                "Inventorypaste URL",
                this,
                new IconData(Material.COMPARATOR),
                "paste.inv.url",
                "https://paste.neo8.de/inv/"));
    }

    private void registerModules() {
        ModuleManager.getInstance().registerModule(new HelpModule());
        ModuleManager.getInstance().registerModule(new InventoryPasteModule());
    }

    public void setWaitingForInventory(boolean waiting) {
        this.waitingForInventory = waiting;
    }

    public boolean isWaitingForInventory() {
        return waitingForInventory;
    }
}
