package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import javax.annotation.Nonnull;

public class WorldTransactionRecord extends TransactionRecord {
   public void revert() {
   }

   public void complete() {
   }

   public void unload() {
   }

   public boolean shouldBeSerialized() {
      return false;
   }

   @Nonnull
   public String toString() {
      return "WorldTransactionRecord{} " + super.toString();
   }
}
