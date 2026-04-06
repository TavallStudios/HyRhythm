package com.hypixel.hytale.builtin.blockspawner;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.codec.WeightedMapCodec;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSpawnerTable implements JsonAssetWithMap<String, DefaultAssetMap<String, BlockSpawnerTable>> {
   @Nonnull
   public static final AssetBuilderCodec<String, BlockSpawnerTable> CODEC;
   private static DefaultAssetMap<String, BlockSpawnerTable> ASSET_MAP;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected IWeightedMap<BlockSpawnerEntry> entries;

   public static DefaultAssetMap<String, BlockSpawnerTable> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (DefaultAssetMap)AssetRegistry.getAssetStore(BlockSpawnerTable.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   public BlockSpawnerTable(String id, @Nullable IWeightedMap<BlockSpawnerEntry> entries) {
      this.id = id;
      this.entries = entries == null ? WeightedMap.builder(BlockSpawnerEntry.EMPTY_ARRAY).build() : entries;
   }

   protected BlockSpawnerTable() {
   }

   public String getId() {
      return this.id;
   }

   public IWeightedMap<BlockSpawnerEntry> getEntries() {
      return this.entries;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BlockSpawnerTable table = (BlockSpawnerTable)o;
         if (this.id != null) {
            if (!this.id.equals(table.id)) {
               return false;
            }
         } else if (table.id != null) {
            return false;
         }

         return this.entries != null ? this.entries.equals(table.entries) : table.entries == null;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.id != null ? this.id.hashCode() : 0;
      result = 31 * result + (this.entries != null ? this.entries.hashCode() : 0);
      return result;
   }

   @Nonnull
   public String toString() {
      return "BlockSpawnerTable{id='" + this.id + "'}";
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(BlockSpawnerTable.class, BlockSpawnerTable::new, Codec.STRING, (blockSpawnerTable, id) -> blockSpawnerTable.id = id, (blockSpawnerTable) -> blockSpawnerTable.id, (asset, data) -> asset.data = data, (asset) -> asset.data).appendInherited(new KeyedCodec("Entries", new WeightedMapCodec(BlockSpawnerEntry.CODEC, BlockSpawnerEntry.EMPTY_ARRAY)), (blockSpawnerTable, o) -> blockSpawnerTable.entries = o, (blockSpawnerTable) -> blockSpawnerTable.entries, (blockSpawnerTable, parent) -> blockSpawnerTable.entries = WeightedMap.<BlockSpawnerEntry>builder(BlockSpawnerEntry.EMPTY_ARRAY).putAll(parent.entries).build()).metadata(UIDefaultCollapsedState.UNCOLLAPSED).add()).validator((asset, results) -> {
         for(BlockSpawnerEntry entry : (BlockSpawnerEntry[])asset.getEntries().internalKeys()) {
            if (BlockType.getAssetMap().getIndex(entry.getBlockName()) == -2147483648) {
               results.fail("BlockName \"" + entry.getBlockName() + "\" does not exist in BlockSpawnerEntry");
            }
         }

      })).build();
   }
}
