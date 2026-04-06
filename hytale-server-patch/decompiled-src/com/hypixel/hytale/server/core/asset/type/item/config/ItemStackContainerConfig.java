package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import javax.annotation.Nonnull;

public class ItemStackContainerConfig {
   public static final ItemStackContainerConfig DEFAULT = new ItemStackContainerConfig();
   public static final BuilderCodec<ItemStackContainerConfig> CODEC;
   protected short capacity = 0;
   protected FilterType globalFilter;
   protected String tag;
   protected volatile int tagIndex;

   public ItemStackContainerConfig() {
      this.globalFilter = FilterType.ALLOW_ALL;
      this.tagIndex = -2147483648;
   }

   public short getCapacity() {
      return this.capacity;
   }

   public FilterType getGlobalFilter() {
      return this.globalFilter;
   }

   public int getTagIndex() {
      return this.tagIndex;
   }

   @Nonnull
   public String toString() {
      short var10000 = this.capacity;
      return "ItemStackContainerConfig{capacity=" + var10000 + ", globalFilter=" + String.valueOf(this.globalFilter) + ", tag='" + this.tag + "'}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ItemStackContainerConfig.class, ItemStackContainerConfig::new).append(new KeyedCodec("Capacity", Codec.SHORT), (itemTool, s) -> itemTool.capacity = s, (itemTool) -> itemTool.capacity).add()).append(new KeyedCodec("GlobalFilter", FilterType.CODEC), (itemTool, s) -> itemTool.globalFilter = s, (itemTool) -> itemTool.globalFilter).add()).append(new KeyedCodec("ItemTag", Codec.STRING), (materialQuantity, s) -> materialQuantity.tag = s, (materialQuantity) -> materialQuantity.tag).add()).afterDecode((config, extraInfo) -> {
         if (config.tag != null) {
            config.tagIndex = AssetRegistry.getOrCreateTagIndex(config.tag);
         }

      })).build();
   }
}
