package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class GatherObjectiveTaskAsset extends CountObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<GatherObjectiveTaskAsset> CODEC;
   protected BlockTagOrItemIdField blockTagOrItemIdField;

   public GatherObjectiveTaskAsset(String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, int count, BlockTagOrItemIdField blockTagOrItemIdField) {
      super(descriptionId, taskConditions, mapMarkers, count);
      this.blockTagOrItemIdField = blockTagOrItemIdField;
   }

   protected GatherObjectiveTaskAsset() {
   }

   @Nonnull
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER_AND_MARKER;
   }

   public BlockTagOrItemIdField getBlockTagOrItemIdField() {
      return this.blockTagOrItemIdField;
   }

   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (!super.matchesAsset0(task)) {
         return false;
      } else {
         return !(task instanceof GatherObjectiveTaskAsset) ? false : ((GatherObjectiveTaskAsset)task).blockTagOrItemIdField.equals(this.blockTagOrItemIdField);
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.blockTagOrItemIdField);
      return "GatherObjectiveTaskAsset{blockTagOrItemIdTask=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(GatherObjectiveTaskAsset.class, GatherObjectiveTaskAsset::new, CountObjectiveTaskAsset.CODEC).append(new KeyedCodec("BlockTagOrItemId", BlockTagOrItemIdField.CODEC), (gatherObjectiveTaskAsset, blockTagOrItemIdField) -> gatherObjectiveTaskAsset.blockTagOrItemIdField = blockTagOrItemIdField, (gatherObjectiveTaskAsset) -> gatherObjectiveTaskAsset.blockTagOrItemIdField).addValidator(Validators.nonNull()).add()).build();
   }
}
