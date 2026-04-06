package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nullable;

public class BooleanSchema extends Schema {
   public static final BuilderCodec<BooleanSchema> CODEC;
   private Boolean default_;

   public Boolean getDefault() {
      return this.default_;
   }

   public void setDefault(Boolean default_) {
      this.default_ = default_;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            BooleanSchema that = (BooleanSchema)o;
            return this.default_ != null ? this.default_.equals(that.default_) : that.default_ == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.default_ != null ? this.default_.hashCode() : 0);
      return result;
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(BooleanSchema.class, BooleanSchema::new, Schema.BASE_CODEC).addField(new KeyedCodec("default", Codec.BOOLEAN, false, true), (o, i) -> o.default_ = i, (o) -> o.default_)).build();
   }
}
