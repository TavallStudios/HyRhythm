package com.hypixel.hytale.codec.schema.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringSchema extends Schema {
   public static final BuilderCodec<StringSchema> CODEC;
   private String pattern;
   private String[] enum_;
   private String const_;
   private String default_;
   private Integer minLength;
   private Integer maxLength;
   private CommonAsset hytaleCommonAsset;
   private String hytaleCosmeticAsset;

   public String getPattern() {
      return this.pattern;
   }

   public void setPattern(String pattern) {
      this.pattern = pattern;
   }

   public void setPattern(@Nonnull Pattern pattern) {
      if (pattern.flags() != 0) {
         throw new IllegalArgumentException("Pattern has flags set. Flags are not supported in schema.");
      } else {
         this.pattern = pattern.pattern();
      }
   }

   public Integer getMinLength() {
      return this.minLength;
   }

   public void setMinLength(int minLength) {
      this.minLength = minLength;
   }

   public Integer getMaxLength() {
      return this.maxLength;
   }

   public void setMaxLength(int maxLength) {
      this.maxLength = maxLength;
   }

   public String[] getEnum() {
      return this.enum_;
   }

   public void setEnum(String[] enum_) {
      this.enum_ = enum_;
   }

   public String getConst() {
      return this.const_;
   }

   public void setConst(String const_) {
      this.const_ = const_;
   }

   public String getDefault() {
      return this.default_;
   }

   public void setDefault(String default_) {
      this.default_ = default_;
   }

   public CommonAsset getHytaleCommonAsset() {
      return this.hytaleCommonAsset;
   }

   public void setHytaleCommonAsset(CommonAsset hytaleCommonAsset) {
      this.hytaleCommonAsset = hytaleCommonAsset;
   }

   public String getHytaleCosmeticAsset() {
      return this.hytaleCosmeticAsset;
   }

   public void setHytaleCosmeticAsset(String hytaleCosmeticAsset) {
      this.hytaleCosmeticAsset = hytaleCosmeticAsset;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            StringSchema that = (StringSchema)o;
            if (this.pattern != null) {
               if (!this.pattern.equals(that.pattern)) {
                  return false;
               }
            } else if (that.pattern != null) {
               return false;
            }

            if (!Arrays.equals(this.enum_, that.enum_)) {
               return false;
            } else {
               if (this.const_ != null) {
                  if (!this.const_.equals(that.const_)) {
                     return false;
                  }
               } else if (that.const_ != null) {
                  return false;
               }

               if (this.default_ != null) {
                  if (!this.default_.equals(that.default_)) {
                     return false;
                  }
               } else if (that.default_ != null) {
                  return false;
               }

               if (this.minLength != null) {
                  if (!this.minLength.equals(that.minLength)) {
                     return false;
                  }
               } else if (that.minLength != null) {
                  return false;
               }

               if (this.maxLength != null) {
                  if (!this.maxLength.equals(that.maxLength)) {
                     return false;
                  }
               } else if (that.maxLength != null) {
                  return false;
               }

               if (this.hytaleCommonAsset != null) {
                  if (!this.hytaleCommonAsset.equals(that.hytaleCommonAsset)) {
                     return false;
                  }
               } else if (that.hytaleCommonAsset != null) {
                  return false;
               }

               return this.hytaleCosmeticAsset != null ? this.hytaleCosmeticAsset.equals(that.hytaleCosmeticAsset) : that.hytaleCosmeticAsset == null;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.pattern != null ? this.pattern.hashCode() : 0);
      result = 31 * result + Arrays.hashCode(this.enum_);
      result = 31 * result + (this.const_ != null ? this.const_.hashCode() : 0);
      result = 31 * result + (this.default_ != null ? this.default_.hashCode() : 0);
      result = 31 * result + (this.minLength != null ? this.minLength.hashCode() : 0);
      result = 31 * result + (this.maxLength != null ? this.maxLength.hashCode() : 0);
      result = 31 * result + (this.hytaleCommonAsset != null ? this.hytaleCommonAsset.hashCode() : 0);
      result = 31 * result + (this.hytaleCosmeticAsset != null ? this.hytaleCosmeticAsset.hashCode() : 0);
      return result;
   }

   @Nonnull
   public static Schema constant(String c) {
      StringSchema s = new StringSchema();
      s.setConst(c);
      return s;
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(StringSchema.class, StringSchema::new, Schema.BASE_CODEC).addField(new KeyedCodec("pattern", Codec.STRING, false, true), (o, i) -> o.pattern = i, (o) -> o.pattern)).addField(new KeyedCodec("enum", Codec.STRING_ARRAY, false, true), (o, i) -> o.enum_ = i, (o) -> o.enum_)).addField(new KeyedCodec("const", Codec.STRING, false, true), (o, i) -> o.const_ = i, (o) -> o.const_)).addField(new KeyedCodec("default", Codec.STRING, false, true), (o, i) -> o.default_ = i, (o) -> o.default_)).addField(new KeyedCodec("minLength", Codec.INTEGER, false, true), (o, i) -> o.minLength = i, (o) -> o.minLength)).addField(new KeyedCodec("maxLength", Codec.INTEGER, false, true), (o, i) -> o.maxLength = i, (o) -> o.maxLength)).addField(new KeyedCodec("hytaleCommonAsset", StringSchema.CommonAsset.CODEC, false, true), (o, i) -> o.hytaleCommonAsset = i, (o) -> o.hytaleCommonAsset)).addField(new KeyedCodec("hytaleCosmeticAsset", Codec.STRING, false, true), (o, i) -> o.hytaleCosmeticAsset = i, (o) -> o.hytaleCosmeticAsset)).build();
   }

   public static class CommonAsset {
      public static final BuilderCodec<CommonAsset> CODEC;
      private String[] requiredRoots;
      private String requiredExtension;
      private boolean isUIAsset;

      public CommonAsset(String requiredExtension, boolean isUIAsset, String... requiredRoots) {
         this.requiredRoots = requiredRoots;
         this.requiredExtension = requiredExtension;
         this.isUIAsset = isUIAsset;
      }

      protected CommonAsset() {
      }

      public String[] getRequiredRoots() {
         return this.requiredRoots;
      }

      public String getRequiredExtension() {
         return this.requiredExtension;
      }

      public boolean isUIAsset() {
         return this.isUIAsset;
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(CommonAsset.class, CommonAsset::new).addField(new KeyedCodec("requiredRoots", Codec.STRING_ARRAY, false, true), (o, i) -> o.requiredRoots = i, (o) -> o.requiredRoots)).addField(new KeyedCodec("requiredExtension", Codec.STRING, false, true), (o, i) -> o.requiredExtension = i, (o) -> o.requiredExtension)).addField(new KeyedCodec("isUIAsset", Codec.BOOLEAN, false, true), (o, i) -> o.isUIAsset = i, (o) -> o.isUIAsset)).build();
      }
   }
}
