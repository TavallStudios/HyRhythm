package com.hyrhythm.support;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CommandTestSender implements CommandSender {
    private final UUID uuid;
    private final String displayName;
    private final List<String> messages = new ArrayList<>();

    public CommandTestSender(UUID uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
    }

    @Override
    public void sendMessage(Message message) {
        messages.add(message.getRawText());
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        return true;
    }

    public List<String> messages() {
        return messages;
    }
}
