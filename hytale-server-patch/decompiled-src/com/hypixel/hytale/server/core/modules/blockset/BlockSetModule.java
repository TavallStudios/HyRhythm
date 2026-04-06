package com.hypixel.hytale.server.core.modules.blockset;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.blockset.commands.BlockSetCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @deprecated */
@Deprecated(
   forRemoval = true
)
public class BlockSetModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(BlockSetModule.class).build();
   private static BlockSetModule INSTANCE;
   @Nonnull
   private Int2ObjectMap<IntSet> flattenedBlockSets = new Int2ObjectOpenHashMap();
   @Nonnull
   private Int2ObjectMap<IntSet> unmodifiableFlattenedBlockSets;
   private BlockSetLookupTable blockSetLookupTable;

   public BlockSetModule(@Nonnull JavaPluginInit module) {
      super(module);
      this.unmodifiableFlattenedBlockSets = Int2ObjectMaps.unmodifiable(this.flattenedBlockSets);
      INSTANCE = this;
   }

   protected void setup() {
      this.getCommandRegistry().registerCommand(new BlockSetCommand(this));
      this.getEventRegistry().register((Class)LoadedAssetsEvent.class, BlockType.class, this::onBlockTypesChanged);
      this.getEventRegistry().register((Class)LoadedAssetsEvent.class, BlockSet.class, this::onBlockSetsChanged);
   }

   private void onBlockTypesChanged(@Nonnull LoadedAssetsEvent<String, BlockType, BlockTypeAssetMap<String, BlockType>> event) {
      this.blockSetLookupTable = new BlockSetLookupTable(((BlockTypeAssetMap)event.getAssetMap()).getAssetMap());
      this.flattenedBlockSets = this.flattenBlockSets(this.blockSetLookupTable);
      this.unmodifiableFlattenedBlockSets = Int2ObjectMaps.unmodifiable(this.flattenedBlockSets);
   }

   private void onBlockSetsChanged(LoadedAssetsEvent<String, BlockSet, DefaultAssetMap<String, BlockSet>> event) {
      this.blockSetLookupTable = new BlockSetLookupTable(BlockType.getAssetMap().getAssetMap());
      this.flattenedBlockSets = this.flattenBlockSets(this.blockSetLookupTable);
      this.unmodifiableFlattenedBlockSets = Int2ObjectMaps.unmodifiable(this.flattenedBlockSets);
   }

   @Nonnull
   private Int2ObjectMap<IntSet> flattenBlockSets(@Nonnull BlockSetLookupTable lookupTable) {
      Int2ObjectOpenHashMap<IntSet> flattenedSets = new Int2ObjectOpenHashMap();
      if (!lookupTable.isEmpty()) {
         BlockSet.getAssetMap().getAssetMap().forEach((s, blockSet) -> {
            int index = BlockSet.getAssetMap().getIndex(s);
            if (index == -2147483648) {
               throw new IllegalArgumentException("Unknown key! " + s);
            } else {
               IntSet tIntSet = (IntSet)flattenedSets.get(index);
               if (tIntSet == null) {
                  IntOpenHashSet set = this.createSet((BlockSet)blockSet, lookupTable, flattenedSets);
                  set.trim();
                  flattenedSets.put(index, set);
               }

            }
         });
      }

      return Int2ObjectMaps.unmodifiable(flattenedSets);
   }

   @Nonnull
   private IntOpenHashSet createSet(@Nonnull BlockSet blockSet, @Nonnull BlockSetLookupTable lookupTable, @Nonnull Int2ObjectMap<IntSet> flattenedSets) {
      IntOpenHashSet result = new IntOpenHashSet();
      String parent = blockSet.getParent();
      if (parent != null && !parent.isEmpty()) {
         int parentIndex = BlockSet.getAssetMap().getIndex(parent);
         if (parentIndex == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + parent);
         }

         result.addAll((IntCollection)flattenedSets.computeIfAbsent(parentIndex, (s) -> {
            IntOpenHashSet set = this.createSet(parent, lookupTable, flattenedSets);
            set.trim();
            return set;
         }));
      }

      if (blockSet.isIncludeAll()) {
         lookupTable.addAll(result);
      }

      String[] var10001 = blockSet.getIncludeBlockTypes();
      Object2ObjectMap var10002 = lookupTable.getBlockNameIdMap();
      Objects.requireNonNull(result);
      this.consume(var10001, var10002, "block name", result::addAll);
      var10001 = blockSet.getIncludeBlockGroups();
      var10002 = lookupTable.getGroupNameIdMap();
      Objects.requireNonNull(result);
      this.consume(var10001, var10002, "group name", result::addAll);
      var10001 = blockSet.getIncludeHitboxTypes();
      var10002 = lookupTable.getHitboxNameIdMap();
      Objects.requireNonNull(result);
      this.consume(var10001, var10002, "hitbox name", result::addAll);
      var10001 = blockSet.getExcludeBlockTypes();
      var10002 = lookupTable.getBlockNameIdMap();
      Objects.requireNonNull(result);
      this.consume(var10001, var10002, "block name", result::removeAll);
      var10001 = blockSet.getExcludeBlockGroups();
      var10002 = lookupTable.getGroupNameIdMap();
      Objects.requireNonNull(result);
      this.consume(var10001, var10002, "group name", result::removeAll);
      var10001 = blockSet.getExcludeHitboxTypes();
      var10002 = lookupTable.getHitboxNameIdMap();
      Objects.requireNonNull(result);
      this.consume(var10001, var10002, "hitbox name", result::removeAll);
      var10001 = blockSet.getIncludeCategories();
      Objects.requireNonNull(result);
      this.consume(var10001, lookupTable, result::addAll);
      var10001 = blockSet.getExcludeCategories();
      Objects.requireNonNull(result);
      this.consume(var10001, lookupTable, result::removeAll);
      return result;
   }

   private void consume(@Nullable String[] values, @Nonnull Map<String, IntSet> map, String typeString, @Nonnull Consumer<IntSet> addAll) {
      if (values != null) {
         for(String s : values) {
            this.consumeEntry(s, addAll, map, typeString);
         }
      }

   }

   private void consume(@Nullable String[][] values, @Nonnull BlockSetLookupTable lookupTable, @Nonnull Consumer<IntSet> addAll) {
      if (values != null) {
         for(String[] s : values) {
            this.consumeCategory(s, addAll, lookupTable);
         }
      }

   }

   @Nonnull
   private IntOpenHashSet createSet(String name, @Nonnull BlockSetLookupTable lookupTable, @Nonnull Int2ObjectMap<IntSet> flattenedSets) {
      Map<String, BlockSet> blockSets = BlockSet.getAssetMap().getAssetMap();
      BlockSet blockSet = (BlockSet)blockSets.get(name);
      if (blockSet == null) {
         this.getLogger().at(Level.WARNING).log("Creating block sets: Failed to find block set '%s'", name);
         return new IntOpenHashSet();
      } else {
         return this.createSet(blockSet, lookupTable, flattenedSets);
      }
   }

   private void consumeCategory(@Nullable String[] categories, @Nonnull Consumer<IntSet> predicate, @Nonnull BlockSetLookupTable lookupTable) {
      if (categories != null && categories.length != 0) {
         Map<String, IntSet> categoryIdMap = lookupTable.getCategoryIdMap();
         IntSet catSet = (IntSet)categoryIdMap.get(categories[0]);
         if (catSet == null) {
            this.getLogger().at(Level.WARNING).log("Creating block sets: '%s' does not match any block category", categories[0]);
         } else if (categories.length == 1) {
            predicate.accept(catSet);
         } else {
            IntSet andSet = new IntOpenHashSet(catSet);

            for(int i = 1; i < categories.length; ++i) {
               catSet = (IntSet)categoryIdMap.get(categories[i]);
               if (catSet == null) {
                  this.getLogger().at(Level.WARNING).log("Creating block sets: '%s' does not match any block category", categories[i]);
                  return;
               }

               andSet.removeAll(catSet);
               if (andSet.isEmpty()) {
                  return;
               }
            }

            predicate.accept(andSet);
         }
      }
   }

   private void consumeEntry(@Nonnull String name, @Nonnull Consumer<IntSet> predicate, @Nonnull Map<String, IntSet> nameIdMap, String typeString) {
      if (StringUtil.isGlobPattern(name)) {
         boolean[] found = new boolean[]{false};
         nameIdMap.forEach((s, tIntSet) -> {
            if (StringUtil.isGlobMatching(name, s)) {
               predicate.accept(tIntSet);
               found[0] = true;
            }

         });
         if (!found[0]) {
            this.getLogger().at(Level.FINE).log("Creating block sets: '%s' does not match any %s", name, typeString);
         }
      } else {
         IntSet ids = (IntSet)nameIdMap.get(name);
         if (ids == null) {
            this.getLogger().at(Level.WARNING).log("Creating block sets: Failed to find %s '%s'", typeString, name);
         } else {
            predicate.accept(ids);
         }
      }

   }

   @Nonnull
   public Int2ObjectMap<IntSet> getBlockSets() {
      return this.unmodifiableFlattenedBlockSets;
   }

   public boolean blockInSet(int set, int blockId) {
      IntSet s = (IntSet)this.flattenedBlockSets.get(set);
      return s != null && s.contains(blockId);
   }

   public boolean blockInSet(int set, @Nullable BlockType blockType) {
      return blockType != null && this.blockInSet(set, blockType.getId());
   }

   public boolean blockInSet(int set, @Nullable String blockTypeKey) {
      if (blockTypeKey == null) {
         return false;
      } else {
         IntSet s = (IntSet)this.flattenedBlockSets.get(set);
         if (s == null) {
            return false;
         } else {
            int index = BlockType.getAssetMap().getIndex(blockTypeKey);
            if (index == -2147483648) {
               throw new IllegalArgumentException("Unknown key! " + blockTypeKey);
            } else {
               return s.contains(index);
            }
         }
      }
   }

   public static BlockSetModule getInstance() {
      return INSTANCE;
   }
}
