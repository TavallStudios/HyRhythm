package com.hypixel.hytale.server.core.modules.i18n.event;

import com.hypixel.hytale.event.IEvent;
import java.util.Map;
import javax.annotation.Nonnull;

public class MessagesUpdated implements IEvent<Void> {
   private final Map<String, Map<String, String>> changedMessages;
   private final Map<String, Map<String, String>> removedMessages;

   public MessagesUpdated(Map<String, Map<String, String>> changedMessages, Map<String, Map<String, String>> removedMessages) {
      this.changedMessages = changedMessages;
      this.removedMessages = removedMessages;
   }

   public Map<String, Map<String, String>> getChangedMessages() {
      return this.changedMessages;
   }

   public Map<String, Map<String, String>> getRemovedMessages() {
      return this.removedMessages;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.changedMessages);
      return "MessagesUpdated{changedMessages=" + var10000 + ", removedMessages=" + String.valueOf(this.removedMessages) + "}";
   }
}
