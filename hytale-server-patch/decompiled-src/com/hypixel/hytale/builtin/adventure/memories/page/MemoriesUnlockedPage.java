package com.hypixel.hytale.builtin.adventure.memories.page;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MemoriesUnlockedPage extends InteractiveCustomUIPage<PageEventData> {
   private final BlockPosition blockPosition;

   public MemoriesUnlockedPage(@Nonnull PlayerRef playerRef, BlockPosition blockPosition) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, MemoriesUnlockedPage.PageEventData.CODEC);
      this.blockPosition = blockPosition;
   }

   public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
      MemoriesPlugin memoriesPlugin = MemoriesPlugin.get();
      commandBuilder.append("Pages/Memories/MemoriesUnlocked.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#DiscoverMemoriesButton", (new EventData()).append("Action", (Enum)MemoriesUnlockedPage.PageAction.DiscoverMemories));
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageEventData data) {
      Player player = (Player)store.getComponent(ref, Player.getComponentType());

      assert player != null;

      if (data.action == MemoriesUnlockedPage.PageAction.DiscoverMemories) {
         player.getPageManager().openCustomPage(ref, store, new MemoriesPage(this.playerRef, this.blockPosition));
      }

   }

   public static class PageEventData {
      @Nonnull
      public static final String KEY_ACTION = "Action";
      @Nonnull
      public static final BuilderCodec<PageEventData> CODEC;
      public PageAction action;

      static {
         CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(PageEventData.class, PageEventData::new).append(new KeyedCodec("Action", MemoriesUnlockedPage.PageAction.CODEC), (pageEventData, pageAction) -> pageEventData.action = pageAction, (pageEventData) -> pageEventData.action).add()).build();
      }
   }

   public static enum PageAction {
      DiscoverMemories;

      @Nonnull
      public static final Codec<PageAction> CODEC = new EnumCodec<PageAction>(PageAction.class);
   }
}
