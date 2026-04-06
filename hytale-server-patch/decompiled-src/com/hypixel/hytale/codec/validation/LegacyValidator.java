package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;

/** @deprecated */
@Deprecated(
   forRemoval = true
)
public interface LegacyValidator<T> extends Validator<T> {
   void accept(T var1, ValidationResults var2);

   default void updateSchema(SchemaContext context, Schema target) {
      System.err.println("updateSchema: " + this.getClass().getSimpleName());
   }
}
