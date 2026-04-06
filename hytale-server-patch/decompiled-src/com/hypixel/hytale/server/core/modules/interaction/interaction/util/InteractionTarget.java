package com.hypixel.hytale.server.core.modules.interaction.interaction.util;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum InteractionTarget {
   USER,
   OWNER,
   TARGET;

   @Nonnull
   public static final EnumCodec<InteractionTarget> CODEC = (new EnumCodec<InteractionTarget>(InteractionTarget.class)).documentKey(USER, "Causes the interaction to target the entity that triggered/owns the interaction chain.").documentKey(OWNER, "Causes the interaction to target the entity that owns the interaction chain.").documentKey(TARGET, "Causes the interaction to target the entity that is the target of the interaction chain.");

   @Nullable
   public Ref<EntityStore> getEntity(@Nonnull InteractionContext ctx, @Nonnull Ref<EntityStore> ref) {
      Ref var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = ctx.getEntity();
         case 1 -> var10000 = ctx.getOwningEntity();
         case 2 -> var10000 = ctx.getTargetEntity();
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.InteractionTarget toProtocol() {
      com.hypixel.hytale.protocol.InteractionTarget var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = com.hypixel.hytale.protocol.InteractionTarget.User;
         case 1 -> var10000 = com.hypixel.hytale.protocol.InteractionTarget.Owner;
         case 2 -> var10000 = com.hypixel.hytale.protocol.InteractionTarget.Target;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }
}
