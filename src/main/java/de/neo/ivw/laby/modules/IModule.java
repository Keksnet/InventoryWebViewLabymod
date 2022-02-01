package de.neo.ivw.laby.modules;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public interface IModule {

    String getCommand();

    String getShortDescription();

    String getHelpPage();

    void execute(ClientPlayerEntity p, String[] args);

    default void sendMessage(ClientPlayerEntity p, String message) {
        p.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(message), false);
    }

}
