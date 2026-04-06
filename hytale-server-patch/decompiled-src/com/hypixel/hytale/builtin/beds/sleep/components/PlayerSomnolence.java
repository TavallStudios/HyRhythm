package com.hypixel.hytale.builtin.beds.sleep.components;

import com.hypixel.hytale.builtin.beds.BedsPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSomnolence implements Component<EntityStore> {
   @Nonnull
   public static PlayerSomnolence AWAKE;
   @Nonnull
   private PlayerSleep state;

   public static ComponentType<EntityStore, PlayerSomnolence> getComponentType() {
      return BedsPlugin.getInstance().getPlayerSomnolenceComponentType();
   }

   public PlayerSomnolence() {
      this.state = PlayerSleep.FullyAwake.INSTANCE;
   }

   public PlayerSomnolence(@Nonnull PlayerSleep state) {
      this.state = PlayerSleep.FullyAwake.INSTANCE;
      this.state = state;
   }

   @Nonnull
   public PlayerSleep getSleepState() {
      return this.state;
   }

   @Nullable
   public Component<EntityStore> clone() {
      PlayerSomnolence clone = new PlayerSomnolence();
      clone.state = this.state;
      return clone;
   }

   static {
      AWAKE = new PlayerSomnolence(PlayerSleep.FullyAwake.INSTANCE);
   }
}
