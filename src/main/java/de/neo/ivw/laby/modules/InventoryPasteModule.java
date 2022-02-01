package de.neo.ivw.laby.modules;

import com.google.gson.JsonObject;
import de.neo.ivw.laby.InventoryWebViewClient;
import de.neo.ivw.laby.util.InventoryUtil;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.UUID;

public class InventoryPasteModule implements IModule {

    @Override
    public String getCommand() {
        return "invpaste";
    }

    @Override
    public String getShortDescription() {
        return "Paste your inventory to the server";
    }

    @Override
    public String getHelpPage() {
        return getShortDescription();
    }

    @Override
    public void execute(ClientPlayerEntity p, String[] args) {
        JsonObject inv;
        if(args.length == 0) {
            sendMessage(p, "Pasting your inventory to the server...");
            inv = InventoryUtil.serializeInventory(((StringTextComponent)p.getName()).getText() + "s inventory", p.inventory);
        }else if (args.length == 1) {
            if(args[0].equals("inv")) {
                sendMessage(p, "Pasting your inventory to the server...");
                inv = InventoryUtil.serializeInventory(((StringTextComponent)p.getName()).getText() + "s inventory", p.inventory);
            }else if(args[0].equals("next")) {
                sendMessage(p, "Waiting for the next inventory...");
                InventoryWebViewClient.getInstance().setWaitingForInventory(true);
                return;
            }else if(args[0].equals("clear")) {
                sendMessage(p, "Canceling the next inventory...");
                InventoryWebViewClient.getInstance().setWaitingForInventory(false);
                return;
            }else if (args[0].equals("")) {
                sendMessage(p, "Pasting your inventory to the server...");
                inv = InventoryUtil.serializeInventory(((StringTextComponent)p.getName()).getText() + "s inventory", p.inventory);
            } else {
                sendMessage(p, "Unknown argument: " + args[0]);
                return;
            }
        }else {
            sendMessage(p, "Too many arguments");
            return;
        }
        InventoryUtil.paste(inv).thenAccept(result -> {
            if(!result.get("success").getAsBoolean()) {
                sendMessage(p, "Error while pasting your inventory to the server: " + result.get("error").getAsString());
                return;
            }
            HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ITextComponent.getTextComponentOrEmpty("Click to open the inventory"));
            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, result.get("url").getAsString());
            StringTextComponent msg = new StringTextComponent("§aPasted your inventory to " + result.get("url").getAsString());
            msg.setStyle(Style.EMPTY.setHoverEvent(hover).setClickEvent(click));
            p.sendStatusMessage(msg, false);
        });
    }

}
