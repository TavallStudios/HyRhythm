package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectSchema extends Schema {
   public static final BuilderCodec<ObjectSchema> CODEC;
   private Map<String, Schema> properties;
   @Nullable
   private Object additionalProperties;
   private StringSchema propertyNames;
   private Schema unevaluatedProperties;

   public Map<String, Schema> getProperties() {
      return this.properties;
   }

   public void setProperties(Map<String, Schema> properties) {
      this.properties = properties;
   }

   @Nullable
   public Object getAdditionalProperties() {
      return this.additionalProperties;
   }

   public void setAdditionalProperties(boolean additionalProperties) {
      this.additionalProperties = additionalProperties;
   }

   public void setAdditionalProperties(Schema additionalProperties) {
      this.additionalProperties = additionalProperties;
   }

   public StringSchema getPropertyNames() {
      return this.propertyNames;
   }

   public void setPropertyNames(StringSchema propertyNames) {
      this.propertyNames = propertyNames;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            ObjectSchema that = (ObjectSchema)o;
            if (this.properties != null) {
               if (!this.properties.equals(that.properties)) {
                  return false;
               }
            } else if (that.properties != null) {
               return false;
            }

            if (this.additionalProperties != null) {
               if (!this.additionalProperties.equals(that.additionalProperties)) {
                  return false;
               }
            } else if (that.additionalProperties != null) {
               return false;
            }

            if (this.propertyNames != null) {
               if (!this.propertyNames.equals(that.propertyNames)) {
                  return false;
               }
            } else if (that.propertyNames != null) {
               return false;
            }

            return this.unevaluatedProperties != null ? this.unevaluatedProperties.equals(that.unevaluatedProperties) : that.unevaluatedProperties == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.properties != null ? this.properties.hashCode() : 0);
      result = 31 * result + (this.additionalProperties != null ? this.additionalProperties.hashCode() : 0);
      result = 31 * result + (this.propertyNames != null ? this.propertyNames.hashCode() : 0);
      result = 31 * result + (this.unevaluatedProperties != null ? this.unevaluatedProperties.hashCode() : 0);
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.properties);
      return "ObjectSchema{properties=" + var10000 + ", additionalProperties=" + String.valueOf(this.additionalProperties) + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ObjectSchema.class, ObjectSchema::new, Schema.BASE_CODEC).addField(new KeyedCodec("properties", new MapCodec(Schema.CODEC, LinkedHashMap::new), false, true), (o, i) -> o.properties = i, (o) -> o.properties)).addField(new KeyedCodec("additionalProperties", new Schema.BooleanOrSchema(), false, true), (o, i) -> o.additionalProperties = i, (o) -> o.additionalProperties)).addField(new KeyedCodec("propertyNames", StringSchema.CODEC, false, true), (o, i) -> o.propertyNames = i, (o) -> o.propertyNames)).build();
   }
}
