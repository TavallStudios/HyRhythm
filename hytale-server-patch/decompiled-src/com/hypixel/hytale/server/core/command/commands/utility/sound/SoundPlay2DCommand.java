package com.hypixel.hytale.server.core.command.commands.utility.sound;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundPlay2DCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<SoundEvent> soundEventArg;
   @Nonnull
   private final DefaultArg<SoundCategory> categoryArg;
   @Nonnull
   private final FlagArg allFlag;

   public SoundPlay2DCommand() {
      super("2d", "server.commands.sound.2d.desc");
      this.soundEventArg = this.withRequiredArg("sound", "server.commands.sound.play.sound.desc", ArgTypes.SOUND_EVENT_ASSET);
      this.categoryArg = this.withDefaultArg("category", "server.commands.sound.category.desc", ArgTypes.SOUND_CATEGORY, SoundCategory.SFX, "server.commands.sound.category.default");
      this.allFlag = this.withFlagArg("all", "server.commands.sound.all.desc");
      this.addAliases(new String[]{"play"});
   }

   protected void execute(@Nonnull CommandContext context, @Nullable Ref<EntityStore> sourceRef, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      SoundEvent soundEvent = (SoundEvent)this.soundEventArg.get(context);
      SoundCategory soundCategory = (SoundCategory)this.categoryArg.get(context);
      int soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEvent.getId());
      if (this.allFlag.provided(context)) {
         SoundUtil.playSoundEvent2d(soundEventIndex, soundCategory, store);
      } else {
         SoundUtil.playSoundEvent2d(ref, soundEventIndex, soundCategory, store);
      }

   }
}
