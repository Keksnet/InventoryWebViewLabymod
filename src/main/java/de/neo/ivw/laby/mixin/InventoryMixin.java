package de.neo.ivw.laby.mixin;

import com.google.gson.JsonObject;
import de.neo.ivw.laby.InventoryWebViewClient;
import de.neo.ivw.laby.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientPlayNetHandler.class)
public class InventoryMixin {

    @Inject(method = "handleWindowItems", at = @At("TAIL"))
    public void injected(SWindowItemsPacket packet, CallbackInfo ci) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (InventoryWebViewClient.getInstance().isWaitingForInventory()) {
            InventoryWebViewClient.getInstance().setWaitingForInventory(false);
            player.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Pasting inventory to the server..."), false);
            JsonObject inv = InventoryUtil.serializeInventory("Opened inventory of " +
                    ((StringTextComponent)player.getName()).getText(), player.getUniqueID().toString(), "OTHER",
                    packet.getItemStacks().subList(0, packet.getItemStacks().size() - 36));
            InventoryUtil.paste(inv).thenAccept(result -> {
                if(!result.get("success").getAsBoolean()) {
                    player.sendStatusMessage(
                            ITextComponent.getTextComponentOrEmpty("Error while pasting inventory to the server: "
                                    + result.get("error").getAsString()),
                            false);
                    return;
                }
                HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ITextComponent.getTextComponentOrEmpty("Click to open the inventory"));
                ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, result.get("url").getAsString());
                StringTextComponent msg = new StringTextComponent("§aPasted inventory to " + result.get("url").getAsString());
                msg.setStyle(Style.EMPTY.setHoverEvent(hover).setClickEvent(click));
                player.sendStatusMessage(msg, false);
            });
        }
    }


}
