package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public interface Validator<T> extends BiConsumer<T, ValidationResults> {
   void accept(T var1, ValidationResults var2);

   void updateSchema(SchemaContext var1, Schema var2);

   @Nonnull
   default LateValidator<T> late() {
      return new LateValidator<T>() {
         public void accept(T t, ValidationResults results) {
         }

         public void acceptLate(T t, ValidationResults results, ExtraInfo extraInfo) {
            Validator.this.accept(t, results);
         }

         public void updateSchema(SchemaContext context, Schema target) {
            Validator.this.updateSchema(context, target);
         }
      };
   }
}
