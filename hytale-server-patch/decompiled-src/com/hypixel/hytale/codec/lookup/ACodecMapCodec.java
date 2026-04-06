package com.hypixel.hytale.codec.lookup;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.InheritCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidatableCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public abstract class ACodecMapCodec<K, T, C extends Codec<? extends T>> implements Codec<T>, ValidatableCodec<T>, InheritCodec<T> {
   protected final String key;
   protected final Codec<K> keyCodec;
   protected final Map<K, C> idToCodec;
   protected final Map<Class<? extends T>, K> classToId;
   protected final Map<K, Class<? extends T>> idToClass;
   @Nonnull
   protected AtomicReference<CodecPriority<C>[]> codecs;
   protected final boolean allowDefault;
   protected final boolean encodeDefaultKey;

   public ACodecMapCodec(Codec<K> keyCodec) {
      this(keyCodec, false);
   }

   public ACodecMapCodec(Codec<K> keyCodec, boolean allowDefault) {
      this("Id", keyCodec, allowDefault);
   }

   public ACodecMapCodec(String id, Codec<K> keyCodec) {
      this(id, keyCodec, false);
   }

   public ACodecMapCodec(String key, Codec<K> keyCodec, boolean allowDefault) {
      this(key, keyCodec, allowDefault, true);
   }

   public ACodecMapCodec(String key, Codec<K> keyCodec, boolean allowDefault, boolean encodeDefaultKey) {
      this.idToCodec = new ConcurrentHashMap();
      this.classToId = new ConcurrentHashMap();
      this.idToClass = new ConcurrentHashMap();
      this.codecs = new AtomicReference(new CodecPriority[0]);
      this.key = key;
      this.allowDefault = allowDefault;
      this.encodeDefaultKey = encodeDefaultKey;
      this.keyCodec = keyCodec;
   }

   @Nonnull
   public ACodecMapCodec<K, T, C> register(K id, Class<? extends T> aClass, C codec) {
      this.register(Priority.NORMAL, id, aClass, codec);
      return this;
   }

   public ACodecMapCodec<K, T, C> register(@Nonnull Priority priority, K id, Class<? extends T> aClass, C codec) {
      this.idToCodec.put(id, codec);
      this.classToId.put(aClass, id);
      this.idToClass.put(id, aClass);
      if (codec instanceof ValidatableCodec) {
         ((ValidatableCodec)codec).validateDefaults(new ExtraInfo(), new HashSet());
      }

      if (!this.allowDefault && !priority.equals(Priority.NORMAL)) {
         throw new IllegalStateException("Defaults disallowed but non-normal priority provided");
      } else if (!this.allowDefault) {
         return this;
      } else {
         CodecPriority<C> codecPriority = new CodecPriority<C>(codec, priority);

         CodecPriority<C>[] current;
         CodecPriority<C>[] newCodecs;
         do {
            current = (CodecPriority[])this.codecs.get();
            int index = Arrays.binarySearch(current, codecPriority, Comparator.comparingInt((a) -> a.priority().getLevel()));
            int insertionPoint;
            if (index >= 0) {
               insertionPoint = index + 1;
            } else {
               insertionPoint = -(index + 1);
            }

            newCodecs = new CodecPriority[current.length + 1];
            System.arraycopy(current, 0, newCodecs, 0, insertionPoint);
            newCodecs[insertionPoint] = codecPriority;
            System.arraycopy(current, insertionPoint, newCodecs, insertionPoint + 1, current.length - insertionPoint);
         } while(!this.codecs.compareAndSet(current, newCodecs));

         return this;
      }
   }

   public void remove(Class<? extends T> aClass) {
      K id = (K)this.classToId.remove(aClass);
      C codec = (C)(this.idToCodec.remove(id));
      this.idToClass.remove(id);
      if (this.allowDefault) {
         CodecPriority<C>[] current;
         CodecPriority<C>[] newCodecs;
         do {
            current = (CodecPriority[])this.codecs.get();
            int index = -1;

            for(int i = 0; i < current.length; ++i) {
               CodecPriority<C> c = current[i];
               if (c.codec() == codec) {
                  index = i;
                  break;
               }
            }

            if (index == -1) {
               return;
            }

            newCodecs = new CodecPriority[current.length - 1];
            System.arraycopy(current, 0, newCodecs, 0, index);
            System.arraycopy(current, index + 1, newCodecs, index, current.length - index - 1);
         } while(!this.codecs.compareAndSet(current, newCodecs));

      }
   }

   @Nullable
   public C getDefaultCodec() {
      CodecPriority<C>[] c = (CodecPriority[])this.codecs.get();
      return (C)(c.length == 0 ? null : (Codec)c[0].codec());
   }

   public C getCodecFor(K key) {
      return (C)(this.idToCodec.get(key));
   }

   public C getCodecFor(Class<? extends T> key) {
      return (C)(this.idToCodec.get(this.classToId.get(key)));
   }

   public Class<? extends T> getClassFor(K key) {
      return (Class)this.idToClass.get(key);
   }

   public K getIdFor(Class<? extends T> key) {
      return (K)this.classToId.get(key);
   }

   public Set<K> getRegisteredIds() {
      return Collections.unmodifiableSet(this.idToCodec.keySet());
   }

   public T decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
      BsonDocument document = bsonValue.asDocument();
      BsonValue id = document.get(this.key);
      C codec = id == null ? null : (Codec)this.idToCodec.get(this.keyCodec.decode(id, extraInfo));
      if (codec == null) {
         C defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            String var10002 = this.key;
            throw new UnknownIdException("No codec registered with for '" + var10002 + "': " + String.valueOf(id));
         } else {
            return (T)defaultCodec.decode(document, extraInfo);
         }
      } else {
         return (T)codec.decode(document, extraInfo);
      }
   }

   @Nullable
   public T decodeAndInherit(@Nonnull BsonDocument document, T parent, ExtraInfo extraInfo) {
      BsonValue id = document.get(this.key);
      C codec = (C)(this.idToCodec.get(id == null ? null : id.asString().getValue()));
      if (codec == null) {
         C defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            String var10002 = this.key;
            throw new UnknownIdException("No codec registered with for '" + var10002 + "': " + String.valueOf(id));
         } else {
            return (T)(defaultCodec instanceof InheritCodec ? ((InheritCodec)defaultCodec).decodeAndInherit(document, parent, extraInfo) : defaultCodec.decode(document, extraInfo));
         }
      } else {
         return (T)(codec instanceof InheritCodec ? ((InheritCodec)codec).decodeAndInherit(document, parent, extraInfo) : codec.decode(document, extraInfo));
      }
   }

   public void decodeAndInherit(@Nonnull BsonDocument document, T t, T parent, ExtraInfo extraInfo) {
      BsonValue id = document.get(this.key);
      C codec = (C)(this.idToCodec.get(id == null ? null : id.asString().getValue()));
      if (codec == null) {
         C defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            String var10002 = this.key;
            throw new UnknownIdException("No codec registered with for '" + var10002 + "': " + String.valueOf(id));
         } else if (defaultCodec instanceof InheritCodec) {
            ((InheritCodec)defaultCodec).decodeAndInherit(document, t, parent, extraInfo);
         } else {
            throw new UnsupportedOperationException();
         }
      } else if (codec instanceof InheritCodec) {
         ((InheritCodec)codec).decodeAndInherit(document, t, parent, extraInfo);
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public BsonValue encode(@Nonnull T t, ExtraInfo extraInfo) {
      Class<? extends T> aClass = t.getClass();
      K id = (K)this.classToId.get(aClass);
      C defaultCodec = this.getDefaultCodec();
      if (id == null && defaultCodec == null) {
         String var9 = String.valueOf(aClass);
         throw new UnknownIdException("No id registered with for '" + var9 + "': " + String.valueOf(t));
      } else {
         C codec = (C)(this.idToCodec.get(id));
         if (codec == null) {
            if (defaultCodec == null) {
               String var10002 = String.valueOf(aClass);
               throw new UnknownIdException("No codec registered with for '" + var10002 + "': " + String.valueOf(t));
            }

            codec = defaultCodec;
         }

         BsonValue encode = codec.encode(t, extraInfo);
         if (id == null) {
            return encode;
         } else {
            BsonDocument document = new BsonDocument();
            if (this.encodeDefaultKey || codec != defaultCodec) {
               document.put(this.key, this.keyCodec.encode(id, extraInfo));
            }

            document.putAll(encode.asDocument());
            return document;
         }
      }
   }

   @Nullable
   public T decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.mark();
      K id = null;
      if (RawJsonReader.seekToKey(reader, this.key)) {
         id = this.keyCodec.decodeJson(reader, extraInfo);
      }

      reader.reset();
      extraInfo.ignoreUnusedKey(this.key);

      Object var6;
      try {
         C codec = id == null ? null : (Codec)this.idToCodec.get(id);
         if (codec != null) {
            Object var10 = codec.decodeJson(reader, extraInfo);
            return (T)var10;
         }

         C defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            String var10002 = this.key;
            throw new UnknownIdException("No codec registered with for '" + var10002 + "': " + String.valueOf(id));
         }

         var6 = defaultCodec.decodeJson(reader, extraInfo);
      } finally {
         extraInfo.popIgnoredUnusedKey();
      }

      return (T)var6;
   }

   @Nullable
   public T decodeAndInheritJson(@Nonnull RawJsonReader reader, @Nullable T parent, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.mark();
      K id = null;
      if (RawJsonReader.seekToKey(reader, this.key)) {
         id = this.keyCodec.decodeJson(reader, extraInfo);
      } else if (parent != null) {
         id = (K)this.getIdFor(parent.getClass());
      }

      reader.reset();
      extraInfo.ignoreUnusedKey(this.key);

      Object var7;
      try {
         C codec = id == null ? null : (Codec)this.idToCodec.get(id);
         if (codec != null) {
            if (codec instanceof InheritCodec) {
               Object var12 = ((InheritCodec)codec).decodeAndInheritJson(reader, parent, extraInfo);
               return (T)var12;
            }

            Object var11 = codec.decodeJson(reader, extraInfo);
            return (T)var11;
         }

         C defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            String var10002 = this.key;
            throw new UnknownIdException("No codec registered with for '" + var10002 + "': " + String.valueOf(id));
         }

         if (!(defaultCodec instanceof InheritCodec)) {
            var7 = defaultCodec.decodeJson(reader, extraInfo);
            return (T)var7;
         }

         var7 = ((InheritCodec)defaultCodec).decodeAndInheritJson(reader, parent, extraInfo);
      } finally {
         extraInfo.popIgnoredUnusedKey();
      }

      return (T)var7;
   }

   public void decodeAndInheritJson(@Nonnull RawJsonReader reader, T t, @Nullable T parent, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.mark();
      K id = null;
      if (RawJsonReader.seekToKey(reader, this.key)) {
         id = this.keyCodec.decodeJson(reader, extraInfo);
      } else if (parent != null) {
         id = (K)this.getIdFor(parent.getClass());
      }

      reader.reset();
      extraInfo.ignoreUnusedKey(this.key);

      try {
         C codec = id == null ? null : (Codec)this.idToCodec.get(id);
         if (codec != null) {
            if (codec instanceof InheritCodec) {
               ((InheritCodec)codec).decodeAndInheritJson(reader, t, parent, extraInfo);
               return;
            }

            throw new UnsupportedOperationException();
         }

         C defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            String var10002 = this.key;
            throw new UnknownIdException("No codec registered with for '" + var10002 + "': " + String.valueOf(id));
         }

         if (!(defaultCodec instanceof InheritCodec)) {
            throw new UnsupportedOperationException();
         }

         ((InheritCodec)defaultCodec).decodeAndInheritJson(reader, t, parent, extraInfo);
      } finally {
         extraInfo.popIgnoredUnusedKey();
      }

   }

   public void validate(@Nonnull T t, ExtraInfo extraInfo) {
      K id = (K)this.getIdFor(t.getClass());
      C codec = this.getCodecFor(id);
      if (this.keyCodec instanceof ValidatableCodec) {
         ((ValidatableCodec)this.keyCodec).validate(id, extraInfo);
      }

      if (codec instanceof ValidatableCodec) {
         ((ValidatableCodec)codec).validate(t, extraInfo);
      }

   }

   public void validateDefaults(ExtraInfo extraInfo, @Nonnull Set<Codec<?>> tested) {
      if (tested.add(this)) {
         ValidatableCodec.validateDefaults(this.keyCodec, extraInfo, tested);

         for(C codec : this.idToCodec.values()) {
            ValidatableCodec.validateDefaults(codec, extraInfo, tested);
         }

      }
   }

   @Nonnull
   public Schema toSchema(@Nonnull SchemaContext context) {
      List<Schema> options = new ObjectArrayList();
      Map.Entry<K, C>[] entries = (Map.Entry[])this.idToCodec.entrySet().toArray((x$0) -> new Map.Entry[x$0]);
      Arrays.sort(entries, Comparator.comparing((e) -> (Comparable)(e.getKey() instanceof Comparable ? (Comparable)e.getKey() : e.getKey().toString())));
      C def = this.allowDefault ? this.getDefaultCodec() : null;
      String defKey = null;

      for(Map.Entry<K, C> entry : entries) {
         C c = (C)(entry.getValue());
         if (c == def) {
            defKey = entry.getKey().toString();
         }

         Schema schema = context.refDefinition(c);
         if (schema.getRef() != null && c instanceof BuilderCodec<? extends T> bc) {
            Schema subSchema = context.getRawDefinition(bc);
            if (subSchema instanceof ObjectSchema objectSchema) {
               this.mutateChildSchema(entry.getKey().toString(), context, bc, objectSchema);
            }
         }

         options.add(schema);
      }

      if (options.isEmpty()) {
         ObjectSchema s = new ObjectSchema();
         s.setAdditionalProperties(false);
         return s;
      } else {
         Schema s = Schema.anyOf((Schema[])options.toArray((x$0) -> new Schema[x$0]));
         s.getHytale().setMergesProperties(true);
         s.setTitle("Type Selector");
         s.setHytaleSchemaTypeField(new Schema.SchemaTypeField(this.key, defKey, (String[])Arrays.stream(entries).map((e) -> e.getKey().toString()).toArray((x$0) -> new String[x$0])));
         return s;
      }
   }

   protected void mutateChildSchema(String key, @Nonnull SchemaContext context, BuilderCodec<? extends T> c, @Nonnull ObjectSchema objectSchema) {
      C def = null;
      if (this.allowDefault) {
         def = this.getDefaultCodec();
      }

      Schema keySchema = this.keyCodec.toSchema(context);
      if (def == c) {
         keySchema.setTypes(new String[]{"null", "string"});
         Schema origKey = keySchema;
         keySchema = new Schema();
         StringSchema enum_ = new StringSchema();
         enum_.setEnum((String[])this.idToCodec.entrySet().stream().filter((v) -> v.getValue() != c).map(Map.Entry::getKey).map(Object::toString).toArray((x$0) -> new String[x$0]));
         keySchema.setAllOf(origKey, Schema.not(enum_));
      } else {
         ((StringSchema)keySchema).setConst(key);
      }

      keySchema.setMarkdownDescription("This field controls the type, it must be set to the constant value \"" + key + "\" to function as this type.");
      LinkedHashMap<String, Schema> props = new LinkedHashMap();
      props.put(this.key, keySchema);
      Map<String, Schema> otherProps = objectSchema.getProperties();
      otherProps.remove(this.key);
      props.putAll(otherProps);
      objectSchema.setProperties(props);
   }

   public static class UnknownIdException extends CodecException {
      public UnknownIdException(String message) {
         super(message);
      }
   }

   private static record CodecPriority<C>(C codec, Priority priority) {
   }
}
