package com.hypixel.hytale.codec.codecs.simple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.PrimitiveCodec;
import com.hypixel.hytale.codec.RawJsonCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDouble;
import org.bson.BsonValue;

public class FloatCodec implements Codec<Float>, RawJsonCodec<Float>, PrimitiveCodec {
   public static final String STRING_SCHEMA_PATTERN = "^(-?Infinity|NaN)$";

   @Nonnull
   public Float decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      return decodeFloat(bsonValue);
   }

   @Nonnull
   public BsonValue encode(Float t, ExtraInfo extraInfo) {
      return new BsonDouble((double)t);
   }

   @Nonnull
   public Float decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
      return readFloat(reader);
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context) {
      StringSchema stringSchema = new StringSchema();
      stringSchema.setPattern("^(-?Infinity|NaN)$");
      return Schema.anyOf(new NumberSchema(), stringSchema);
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context, @Nullable Float def) {
      StringSchema stringSchema = new StringSchema();
      stringSchema.setPattern("^(-?Infinity|NaN)$");
      NumberSchema numberSchema = new NumberSchema();
      if (def != null) {
         if (!def.isNaN() && !def.isInfinite()) {
            numberSchema.setDefault(def.doubleValue());
         } else {
            stringSchema.setDefault(def.toString());
         }
      }

      Schema schema = Schema.anyOf(numberSchema, stringSchema);
      schema.getHytale().setType("Number");
      return schema;
   }

   public static float decodeFloat(@Nonnull BsonValue value) {
      if (value.isString()) {
         switch (value.asString().getValue()) {
            case "NaN" -> {
               return 0.0F / 0.0F;
            }
            case "Infinity" -> {
               return 1.0F / 0.0F;
            }
            case "-Infinity" -> {
               return -1.0F / 0.0F;
            }
         }
      }

      return (float)value.asNumber().doubleValue();
   }

   public static float readFloat(@Nonnull RawJsonReader reader) throws IOException {
      if (reader.peekFor('"')) {
         float var10000;
         switch (reader.readString()) {
            case "NaN" -> var10000 = 0.0F / 0.0F;
            case "Infinity" -> var10000 = 1.0F / 0.0F;
            case "-Infinity" -> var10000 = -1.0F / 0.0F;
            default -> throw new IOException("Unexpected string: \"" + str + "\", expected NaN, Infinity, -Infinity");
         }

         return var10000;
      } else {
         return (float)reader.readDoubleValue();
      }
   }
}
