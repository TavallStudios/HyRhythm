package com.hypixel.hytale.server.core.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

public class ValueCodec<T> implements Codec<Value<T>> {
   public static final ValueCodec<Object> REFERENCE_ONLY = new ValueCodec<Object>((Codec)null);
   public static final ValueCodec<String> STRING;
   public static final ValueCodec<LocalizableString> LOCALIZABLE_STRING;
   public static final ValueCodec<Integer> INTEGER;
   public static final ValueCodec<PatchStyle> PATCH_STYLE;
   protected Codec<T> codec;

   ValueCodec(Codec<T> codec) {
      this.codec = codec;
   }

   public Value<T> decode(BsonValue bsonValue, ExtraInfo extraInfo) {
      throw new UnsupportedOperationException();
   }

   public BsonValue encode(@Nonnull Value<T> r, ExtraInfo extraInfo) {
      return (BsonValue)(r.getValue() != null ? this.codec.encode(r.getValue(), extraInfo) : (new BsonDocument()).append("$Document", new BsonString(r.getDocumentPath())).append("@Value", new BsonString(r.getValueName())));
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context) {
      return this.codec.toSchema(context);
   }

   static {
      STRING = new ValueCodec<String>(Codec.STRING);
      LOCALIZABLE_STRING = new ValueCodec<LocalizableString>(LocalizableString.CODEC);
      INTEGER = new ValueCodec<Integer>(Codec.INTEGER);
      PATCH_STYLE = new ValueCodec<PatchStyle>(PatchStyle.CODEC);
   }
}
