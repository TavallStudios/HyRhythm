package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.exception.CodecException;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonSerializationException;
import org.bson.BsonValue;

public class KeyedCodec<T> {
   @Nonnull
   private final String key;
   @Nonnull
   private final Codec<T> codec;
   private final boolean required;

   public KeyedCodec(@Nonnull String key, Codec<T> codec) {
      this(key, codec, false);
   }

   public KeyedCodec(@Nonnull String key, Codec<T> codec, boolean required) {
      this.key = (String)Objects.requireNonNull(key, "key parameter can't be null");
      this.codec = (Codec)Objects.requireNonNull(codec, "codec parameter can't be null");
      this.required = required;
      if (key.isEmpty()) {
         throw new IllegalArgumentException("Key must not be empty! Key: '" + key + "'");
      } else {
         char firstCharFromKey = key.charAt(0);
         if (Character.isLetter(firstCharFromKey) && !Character.isUpperCase(firstCharFromKey)) {
            throw new IllegalArgumentException("Key must start with an upper case character! Key: '" + key + "'");
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public KeyedCodec(@Nonnull String key, Codec<T> codec, boolean required, boolean bypassCaseCheck) {
      this.key = (String)Objects.requireNonNull(key, "key parameter can't be null");
      this.codec = (Codec)Objects.requireNonNull(codec, "codec parameter can't be null");
      this.required = required;
      if (key.isEmpty()) {
         throw new IllegalArgumentException("Key must not be empty! Key: '" + key + "'");
      } else {
         char firstCharFromKey = key.charAt(0);
         if (!bypassCaseCheck && Character.isLetter(firstCharFromKey) && !Character.isUpperCase(firstCharFromKey)) {
            throw new IllegalArgumentException("Key must start with an upper case character! Key: '" + key + "'");
         }
      }
   }

   @Nonnull
   public String getKey() {
      return this.key;
   }

   /** @deprecated */
   @Deprecated
   public T getNow(BsonDocument document) {
      return (T)this.getNow(document, EmptyExtraInfo.EMPTY);
   }

   public T getNow(BsonDocument document, @Nonnull ExtraInfo extraInfo) {
      return (T)this.get(document, extraInfo).orElseThrow(() -> new BsonSerializationException(this.key + " does not exist in document!"));
   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public T getOrNull(BsonDocument document) {
      return (T)this.getOrNull(document, EmptyExtraInfo.EMPTY);
   }

   @Nullable
   public T getOrNull(BsonDocument document, @Nonnull ExtraInfo extraInfo) {
      return (T)this.get(document, extraInfo).orElse((Object)null);
   }

   /** @deprecated */
   @Nonnull
   @Deprecated
   public Optional<T> get(BsonDocument document) {
      return this.get(document, EmptyExtraInfo.EMPTY);
   }

   @Nonnull
   public Optional<T> get(@Nullable BsonDocument document, @Nonnull ExtraInfo extraInfo) {
      extraInfo.pushKey(this.key);

      Optional var3;
      try {
         if (document != null) {
            BsonValue bsonValue = document.get(this.key);
            if (Codec.isNullBsonValue(bsonValue)) {
               Optional var11 = Optional.empty();
               return var11;
            }

            Optional var4 = Optional.ofNullable(this.decode(bsonValue, extraInfo));
            return var4;
         }

         var3 = Optional.empty();
      } catch (Exception e) {
         throw new CodecException("Failed decode", document, extraInfo, e);
      } finally {
         extraInfo.popKey();
      }

      return var3;
   }

   @Nullable
   public T getOrDefault(@Nullable BsonDocument document, @Nonnull ExtraInfo extraInfo, T def) {
      extraInfo.pushKey(this.key);

      Object var4;
      try {
         if (document != null) {
            BsonValue bsonValue = document.get(this.key);
            if (Codec.isNullBsonValue(bsonValue)) {
               Object var12 = def;
               return (T)var12;
            }

            Object var5 = this.codec.decode(bsonValue, extraInfo);
            return (T)var5;
         }

         var4 = def;
      } catch (Exception e) {
         throw new CodecException("Failed decode", document, extraInfo, e);
      } finally {
         extraInfo.popKey();
      }

      return (T)var4;
   }

   @Nonnull
   public Optional<T> getAndInherit(@Nullable BsonDocument document, T parent, @Nonnull ExtraInfo extraInfo) {
      extraInfo.pushKey(this.key);

      Optional var4;
      try {
         if (document != null) {
            BsonValue bsonValue = document.get(this.key);
            if (Codec.isNullBsonValue(bsonValue)) {
               Optional var12 = Optional.ofNullable(this.decodeAndInherit((BsonValue)null, parent, extraInfo));
               return var12;
            }

            Optional var5 = Optional.ofNullable(this.decodeAndInherit(bsonValue, parent, extraInfo));
            return var5;
         }

         var4 = Optional.ofNullable(this.decodeAndInherit((BsonValue)null, parent, extraInfo));
      } catch (Exception e) {
         throw new CodecException("Failed decode", document, extraInfo, e);
      } finally {
         extraInfo.popKey();
      }

      return var4;
   }

   /** @deprecated */
   @Deprecated
   public void put(@Nonnull BsonDocument document, T t) {
      this.put(document, t, EmptyExtraInfo.EMPTY);
   }

   public void put(@Nonnull BsonDocument document, @Nullable T t, @Nonnull ExtraInfo extraInfo) {
      if (t != null) {
         try {
            document.put(this.key, this.encode(t, extraInfo));
         } catch (Exception e) {
            throw new CodecException("Failed encode", t, extraInfo, e);
         }
      }

   }

   @Nullable
   protected T decode(BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
      if (!this.required && Codec.isNullBsonValue(bsonValue)) {
         return null;
      } else {
         try {
            return this.codec.decode(bsonValue, extraInfo);
         } catch (Exception e) {
            throw new CodecException("Failed to decode", bsonValue, extraInfo, e);
         }
      }
   }

   @Nullable
   protected T decodeAndInherit(@Nullable BsonValue bsonValue, T parent, @Nonnull ExtraInfo extraInfo) {
      if (!this.required && Codec.isNullBsonValue(bsonValue)) {
         return null;
      } else {
         try {
            return (T)(bsonValue != null && bsonValue.isDocument() && this.codec instanceof InheritCodec ? ((InheritCodec)this.codec).decodeAndInherit(bsonValue.asDocument(), parent, extraInfo) : this.codec.decode(bsonValue, extraInfo));
         } catch (Exception e) {
            throw new CodecException("Failed to decode", bsonValue, extraInfo, e);
         }
      }
   }

   protected BsonValue encode(T t, ExtraInfo extraInfo) {
      return this.codec.encode(t, extraInfo);
   }

   @Nonnull
   public Codec<T> getChildCodec() {
      return this.codec;
   }

   public boolean isRequired() {
      return this.required;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.key;
      return "KeyedCodec{key='" + var10000 + "', codec=" + String.valueOf(this.codec) + "}";
   }
}
