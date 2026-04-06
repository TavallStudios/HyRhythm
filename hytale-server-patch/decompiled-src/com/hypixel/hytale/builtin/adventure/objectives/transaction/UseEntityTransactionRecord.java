package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import java.util.UUID;
import javax.annotation.Nonnull;

public class UseEntityTransactionRecord extends TransactionRecord {
   protected UUID objectiveUUID;
   protected String taskId;

   public UseEntityTransactionRecord(UUID objectiveUUID, String taskId) {
      this.objectiveUUID = objectiveUUID;
      this.taskId = taskId;
   }

   public void revert() {
      ObjectivePlugin.get().getObjectiveDataStore().removeEntityTask(this.objectiveUUID, this.taskId);
   }

   public void complete() {
      ObjectivePlugin.get().getObjectiveDataStore().removeEntityTask(this.objectiveUUID, this.taskId);
   }

   public void unload() {
      ObjectivePlugin.get().getObjectiveDataStore().removeEntityTask(this.objectiveUUID, this.taskId);
   }

   public boolean shouldBeSerialized() {
      return false;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.objectiveUUID);
      return "UseEntityTransactionRecord{objectiveUUID=" + var10000 + ", taskId='" + this.taskId + "'} " + super.toString();
   }
}
