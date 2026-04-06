package com.hypixel.hytale.metrics;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class MetricResults {
   public static final Codec<MetricResults> CODEC = new MetricResultsCodec();
   public static final Codec<MetricResults[]> ARRAY_CODEC;
   private final BsonDocument bson;

   protected MetricResults(BsonDocument bson) {
      this.bson = bson;
   }

   protected BsonDocument getBson() {
      return this.bson;
   }

   static {
      ARRAY_CODEC = new ArrayCodec<MetricResults[]>(CODEC, (x$0) -> new MetricResults[x$0]);
   }

   private static class MetricResultsCodec implements Codec<MetricResults> {
      @Nullable
      public MetricResults decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
         return Codec.isNullBsonValue(bsonValue) ? null : new MetricResults(bsonValue.asDocument());
      }

      public BsonValue encode(@Nullable MetricResults metricResults, ExtraInfo extraInfo) {
         return metricResults == null ? null : metricResults.bson;
      }

      @Nullable
      public MetricResults decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
         BsonValue bsonValue = RawJsonReader.readBsonValue(reader);
         return Codec.isNullBsonValue(bsonValue) ? null : new MetricResults(bsonValue.asDocument());
      }

      @Nonnull
      public Schema toSchema(@Nonnull SchemaContext context) {
         return new ObjectSchema();
      }
   }
}
