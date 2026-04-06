package com.hypixel.hytale.server.core.universe.world.meta;

import com.hypixel.hytale.registry.Registration;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

public class BlockStateRegistration extends Registration {
   private final Class<? extends BlockState> blockStateClass;

   public BlockStateRegistration(Class<? extends BlockState> blockStateClass, BooleanSupplier isEnabled, Runnable unregister) {
      super(isEnabled, unregister);
      this.blockStateClass = blockStateClass;
   }

   public BlockStateRegistration(@Nonnull BlockStateRegistration registration, BooleanSupplier isEnabled, Runnable unregister) {
      super(isEnabled, unregister);
      this.blockStateClass = registration.blockStateClass;
   }

   public Class<? extends BlockState> getBlockStateClass() {
      return this.blockStateClass;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.blockStateClass);
      return "BlockStateRegistration{blockStateClass=" + var10000 + ", " + super.toString() + "}";
   }
}
