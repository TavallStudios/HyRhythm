package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.loader.context.FileLoadingContext;
import com.hypixel.hytale.server.worldgen.loader.prefab.PrefabPatternGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.prefab.WeightedPrefabMapJsonLoader;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPatternGenerator;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class PrefabContainerJsonLoader extends JsonLoader<SeedStringResource, PrefabContainer> {
   private final FileLoadingContext context;

   public PrefabContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, FileLoadingContext context) {
      super(seed.append(".PrefabContainer"), dataFolder, json);
      this.context = context;
   }

   @Nonnull
   public PrefabContainer load() {
      return new PrefabContainer(this.loadEntries());
   }

   @Nonnull
   protected PrefabContainer.PrefabContainerEntry[] loadEntries() {
      if (!this.has("Entries")) {
         return new PrefabContainer.PrefabContainerEntry[0];
      } else {
         JsonArray entryArray = this.get("Entries").getAsJsonArray();
         PrefabContainer.PrefabContainerEntry[] entries = new PrefabContainer.PrefabContainerEntry[entryArray.size()];

         for(int i = 0; i < entries.length; ++i) {
            try {
               entries[i] = (new PrefabContainerEntryJsonLoader(this.seed.append("-" + i), this.dataFolder, entryArray.get(i), this.context)).load();
            } catch (Throwable e) {
               throw new Error(String.format("Failed to load prefab container entry #%s.", i), e);
            }
         }

         return entries;
      }
   }

   public static class PrefabContainerEntryJsonLoader extends JsonLoader<SeedStringResource, PrefabContainer.PrefabContainerEntry> {
      private final FileLoadingContext context;

      public PrefabContainerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, FileLoadingContext context) {
         super(seed.append(".PrefabContainerEntry"), dataFolder, json);
         this.context = context;
      }

      @Nonnull
      public PrefabContainer.PrefabContainerEntry load() {
         IWeightedMap<WorldGenPrefabSupplier> prefabs = (new WeightedPrefabMapJsonLoader(this.seed, this.dataFolder, this.json, "Prefab", "Weight")).load();
         if (!this.has("Pattern")) {
            throw new IllegalArgumentException("Could not find prefab pattern. Keyword: Pattern");
         } else {
            PrefabPatternGenerator prefabPatternGenerator = (new PrefabPatternGeneratorJsonLoader(this.seed, this.dataFolder, this.get("Pattern"), this.context)).load();
            return new PrefabContainer.PrefabContainerEntry(prefabs, prefabPatternGenerator, this.loadEnvironment());
         }
      }

      protected int loadEnvironment() {
         int environment = -2147483648;
         if (this.has("Environment")) {
            String environmentId = this.get("Environment").getAsString();
            environment = Environment.getAssetMap().getIndex(environmentId);
            if (environment == -2147483648) {
               throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
            }
         }

         return environment;
      }
   }

   public interface Constants {
      String KEY_ENTRIES = "Entries";
      String KEY_ENTRY_PREFAB = "Prefab";
      String KEY_ENTRY_WEIGHT = "Weight";
      String KEY_ENTRY_PATTERN = "Pattern";
      String KEY_ENVIRONMENT = "Environment";
      String ERROR_FAIL_ENTRY = "Failed to load prefab container entry #%s.";
      String ERROR_LOADING_ENVIRONMENT = "Error while looking up environment \"%s\"!";
      String ERROR_ENTRY_NO_PATTERN = "Could not find prefab pattern. Keyword: Pattern";
   }
}
