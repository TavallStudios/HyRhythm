package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonValue;

public class ArraySchema extends Schema {
   public static final BuilderCodec<ArraySchema> CODEC;
   private Object items;
   private Integer minItems;
   private Integer maxItems;
   private Boolean uniqueItems;

   public ArraySchema() {
   }

   public ArraySchema(Schema item) {
      this.setItem(item);
   }

   @Nullable
   public Object getItems() {
      return this.items;
   }

   public void setItem(Schema items) {
      this.items = items;
   }

   public void setItems(Schema... items) {
      this.items = items;
   }

   @Nullable
   public Integer getMinItems() {
      return this.minItems;
   }

   public void setMinItems(Integer minItems) {
      this.minItems = minItems;
   }

   @Nullable
   public Integer getMaxItems() {
      return this.maxItems;
   }

   public void setMaxItems(Integer maxItems) {
      this.maxItems = maxItems;
   }

   public boolean getUniqueItems() {
      return this.uniqueItems;
   }

   public void setUniqueItems(boolean uniqueItems) {
      this.uniqueItems = uniqueItems;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            ArraySchema that = (ArraySchema)o;
            if (this.items != null) {
               if (!this.items.equals(that.items)) {
                  return false;
               }
            } else if (that.items != null) {
               return false;
            }

            if (this.minItems != null) {
               if (!this.minItems.equals(that.minItems)) {
                  return false;
               }
            } else if (that.minItems != null) {
               return false;
            }

            if (this.maxItems != null) {
               if (!this.maxItems.equals(that.maxItems)) {
                  return false;
               }
            } else if (that.maxItems != null) {
               return false;
            }

            return this.uniqueItems != null ? this.uniqueItems.equals(that.uniqueItems) : that.uniqueItems == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.items != null ? this.items.hashCode() : 0);
      result = 31 * result + (this.minItems != null ? this.minItems.hashCode() : 0);
      result = 31 * result + (this.maxItems != null ? this.maxItems.hashCode() : 0);
      result = 31 * result + (this.uniqueItems != null ? this.uniqueItems.hashCode() : 0);
      return result;
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ArraySchema.class, ArraySchema::new, Schema.BASE_CODEC).addField(new KeyedCodec("items", new ItemOrItems(), false, true), (o, i) -> o.items = i, (o) -> o.items)).addField(new KeyedCodec("minItems", Codec.INTEGER, false, true), (o, i) -> o.minItems = i, (o) -> o.minItems)).addField(new KeyedCodec("maxItems", Codec.INTEGER, false, true), (o, i) -> o.maxItems = i, (o) -> o.maxItems)).addField(new KeyedCodec("uniqueItems", Codec.BOOLEAN, false, true), (o, i) -> o.uniqueItems = i, (o) -> o.uniqueItems)).build();
   }

   /** @deprecated */
   @Deprecated
   private static class ItemOrItems implements Codec<Object> {
      @Nonnull
      private ArrayCodec<Schema> array;

      private ItemOrItems() {
         this.array = new ArrayCodec<Schema>(Schema.CODEC, (x$0) -> new Schema[x$0]);
      }

      public Object decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
         return bsonValue.isArray() ? this.array.decode(bsonValue, extraInfo) : Schema.CODEC.decode(bsonValue, extraInfo);
      }

      public BsonValue encode(Object o, ExtraInfo extraInfo) {
         return o instanceof Schema[] ? this.array.encode((Schema[])o, extraInfo) : Schema.CODEC.encode((Schema)o, extraInfo);
      }

      @Nonnull
      public Schema toSchema(@Nonnull SchemaContext context) {
         return Schema.anyOf(Schema.CODEC.toSchema(context), this.array.toSchema(context));
      }
   }
}
