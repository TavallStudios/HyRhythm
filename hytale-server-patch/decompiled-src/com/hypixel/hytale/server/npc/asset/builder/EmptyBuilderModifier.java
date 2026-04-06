package com.hypixel.hytale.server.npc.asset.builder;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import java.util.Map;

public class EmptyBuilderModifier extends BuilderModifier {
   public static final EmptyBuilderModifier INSTANCE = new EmptyBuilderModifier();

   private EmptyBuilderModifier() {
      super(Object2ObjectMaps.EMPTY_MAP, (StatePair[])null, (StateMappingHelper)null, (String)null, (Map)null);
   }

   public boolean isEmpty() {
      return true;
   }

   public int exportedStateCount() {
      return 0;
   }

   public void applyComponentStateMap(BuilderSupport support) {
      throw new UnsupportedOperationException("applyComponentStateMap is not valid for EmptyBuilderModifier");
   }

   public void popComponentStateMap(BuilderSupport support) {
      throw new UnsupportedOperationException("popComponentStateMap is not valid for EmptyBuilderModifier");
   }
}
