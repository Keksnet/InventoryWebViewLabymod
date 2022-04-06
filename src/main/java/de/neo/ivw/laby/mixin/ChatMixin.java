package de.neo.ivw.laby.mixin;

import de.neo.ivw.laby.modules.IModule;
import de.neo.ivw.laby.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(ClientPlayerEntity.class)
public abstract class ChatMixin {

    @Shadow
    public abstract void sendChatMessage(String message);

    private boolean ignore = false;

    @Inject(method="sendChatMessage", at=@At("HEAD"), cancellable = true)
    public void injected(String message, CallbackInfo info) {
        System.out.println(message);
        if (ignore) return;
        if(message.startsWith("#")) {
            if(message.startsWith("##")) {
                message = message.substring(1);
                sendChatMessage(message);
                info.cancel();
                ignore = true;
                return;
            }
            System.out.println("C");
            info.cancel();
            System.out.println("D");
            IModule module = ModuleManager.getInstance().getModule(message.replace("#", "").split(" ")[0]);
            System.out.println("E");
            String[] args = message.replace("#" + module.getCommand() + " ", "")
                    .replace("#" + module.getCommand(), "").trim().split(" ");
            System.out.println("F");
            System.out.println(Arrays.toString(args));
            System.out.println("G");
            module.execute(Minecraft.getInstance().player, args);
            System.out.println("H");
        }
    }

}
