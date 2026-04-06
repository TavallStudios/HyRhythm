package com.hypixel.hytale.server.core.task;

import com.hypixel.hytale.registry.Registration;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

public class TaskRegistration extends Registration {
   private final Future<?> task;

   public TaskRegistration(@Nonnull Future<?> task) {
      super(() -> true, () -> task.cancel(false));
      this.task = task;
   }

   public TaskRegistration(@Nonnull TaskRegistration registration, BooleanSupplier isEnabled, Runnable unregister) {
      super(isEnabled, unregister);
      this.task = registration.task;
   }

   public Future<?> getTask() {
      return this.task;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.task);
      return "TaskRegistration{task=" + var10000 + ", " + super.toString() + "}";
   }
}
