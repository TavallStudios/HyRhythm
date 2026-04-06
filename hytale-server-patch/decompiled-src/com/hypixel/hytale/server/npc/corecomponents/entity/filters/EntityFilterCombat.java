package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.view.combat.CombatViewSystems;
import com.hypixel.hytale.server.npc.blackboard.view.combat.InterpretedCombatData;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterCombat;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class EntityFilterCombat extends EntityFilterBase {
   public static final int COST = 100;
   protected final String sequence;
   protected final double minTimeElapsed;
   protected final double maxTimeElapsed;
   protected final Mode combatMode;
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider();

   public EntityFilterCombat(@Nonnull BuilderEntityFilterCombat builder, @Nonnull BuilderSupport builderSupport) {
      this.sequence = builder.getSequence(builderSupport);
      double[] timeElapsedRange = builder.getTimeElapsedRange(builderSupport);
      this.minTimeElapsed = timeElapsedRange[0];
      this.maxTimeElapsed = timeElapsedRange[1];
      this.combatMode = builder.getCombatMode(builderSupport);
   }

   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      List<InterpretedCombatData> combatData = CombatViewSystems.getCombatData(targetRef, store);

      for(int i = 0; i < combatData.size(); ++i) {
         InterpretedCombatData data = (InterpretedCombatData)combatData.get(i);
         boolean var10000;
         switch (this.combatMode.ordinal()) {
            case 0:
               if (!data.getAttack().equals(this.sequence)) {
                  var10000 = false;
               } else {
                  float time = data.getCurrentElapsedTime();
                  var10000 = (double)time >= this.minTimeElapsed && (double)time <= this.maxTimeElapsed;
               }
               break;
            case 1:
               if (!data.isCharging()) {
                  var10000 = false;
               } else {
                  float currentTime = data.getCurrentElapsedTime();
                  var10000 = (double)currentTime >= this.minTimeElapsed && (double)currentTime <= this.maxTimeElapsed;
               }
               break;
            case 2:
               var10000 = data.isPerformingMeleeAttack() || data.isPerformingRangedAttack();
               break;
            case 3:
               var10000 = data.isPerformingMeleeAttack();
               break;
            case 4:
               var10000 = data.isPerformingRangedAttack();
               break;
            case 5:
               var10000 = data.isPerformingBlock();
               break;
            case 6:
               var10000 = true;
               break;
            case 7:
               var10000 = false;
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         boolean matches = var10000;
         if (matches) {
            return true;
         }
      }

      return this.combatMode == EntityFilterCombat.Mode.None;
   }

   public int cost() {
      return 100;
   }

   public static enum Mode implements Supplier<String> {
      Sequence("Combat sequence"),
      Charging("Weapon charging"),
      Attacking("Attacking"),
      Melee("Melee"),
      Ranged("Ranged"),
      Blocking("Blocking"),
      Any("Any"),
      None("None");

      private final String description;

      private Mode(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
