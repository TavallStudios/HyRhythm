package com.hypixel.hytale.builtin.adventure.worldlocationcondition;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldlocationcondition.WorldLocationCondition;
import javax.annotation.Nonnull;

public class WorldLocationConditionPlugin extends JavaPlugin {
   public WorldLocationConditionPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      WorldLocationCondition.CODEC.register((String)"NeighbourBlockTags", NeighbourBlockTagsLocationCondition.class, NeighbourBlockTagsLocationCondition.CODEC);
   }
}
