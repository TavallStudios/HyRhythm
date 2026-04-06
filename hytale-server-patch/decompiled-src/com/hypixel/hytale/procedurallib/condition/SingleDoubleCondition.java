package com.hypixel.hytale.procedurallib.condition;

import javax.annotation.Nonnull;

public class SingleDoubleCondition implements IDoubleCondition {
   protected final double value;

   public SingleDoubleCondition(double value) {
      this.value = value;
   }

   public boolean eval(double value) {
      return value < this.value;
   }

   @Nonnull
   public String toString() {
      return "SingleDoubleCondition{value=" + this.value + "}";
   }
}
