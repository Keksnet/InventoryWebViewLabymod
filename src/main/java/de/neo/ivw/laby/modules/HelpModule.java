package de.neo.ivw.laby.modules;

import net.minecraft.client.entity.player.ClientPlayerEntity;

import java.util.List;

public class HelpModule implements IModule {

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public void execute(ClientPlayerEntity p, String[] args) {
        List<IModule> modules = ModuleManager.getInstance().getModules();
        if(args.length == 1) {
            if(args[0].equals("help")) {
                sendMessage(p, "§aHelp page:");
                sendMessage(p, "§3" + getShortDescription());
                sendMessage(p, "§3" + getHelpPage());
            }
            IModule module = ModuleManager.getInstance().getModule(args[0]);
            if(module.getCommand().equals("help")) {
                sendMessage(p, "§cUnknown module: " + args[0]);
                return;
            }
            sendMessage(p, "§aHelp page of module " + module.getCommand() + ":");
            return;
        }
        sendMessage(p, "§aAvailable modules:");
        for (IModule module : modules) {
            sendMessage(p, "§3" + module.getCommand() + " §a- §3" + module.getShortDescription());
        }
    }

    @Override
    public String getShortDescription() {
        return "Displays the help page";
    }

    @Override
    public String getHelpPage() {
        return "Displays the help page or the help page of a specific module";
    }
}
