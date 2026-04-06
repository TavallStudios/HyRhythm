package com.hypixel.hytale.builtin.crafting;

import com.hypixel.hytale.protocol.BenchRequirement;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BenchRecipeRegistry {
   private final String benchId;
   @Nonnull
   private final Map<String, Set<String>> categoryMap = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<String, Set<String>> itemToIncomingRecipe = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Set<String> uncategorizedRecipes = new ObjectOpenHashSet();
   @Nonnull
   private final Set<String> allMaterialIds = new ObjectOpenHashSet();
   @Nonnull
   private final Set<String> allMaterialResourceType = new ObjectOpenHashSet();

   public BenchRecipeRegistry(String benchId) {
      this.benchId = benchId;
   }

   @Nonnull
   public Iterable<String> getIncomingRecipesForItem(@Nonnull String itemId) {
      Set<String> recipes = (Set)this.itemToIncomingRecipe.get(itemId);
      return recipes == null ? Collections.emptySet() : recipes;
   }

   public void removeRecipe(@Nonnull String id) {
      this.uncategorizedRecipes.remove(id);

      for(Map.Entry<String, Set<String>> entry : this.categoryMap.entrySet()) {
         ((Set)entry.getValue()).remove(id);
      }

   }

   public void addRecipe(@Nonnull BenchRequirement benchRequirement, @Nonnull CraftingRecipe recipe) {
      if (benchRequirement.categories != null && benchRequirement.categories.length != 0) {
         for(String category : benchRequirement.categories) {
            ((Set)this.categoryMap.computeIfAbsent(category, (k) -> new ObjectOpenHashSet())).add(recipe.getId());
         }
      } else {
         this.uncategorizedRecipes.add(recipe.getId());
      }

   }

   public CraftingRecipe[] getAllRecipes() {
      Set<String> allRecipeIds = new ObjectOpenHashSet(this.uncategorizedRecipes);

      for(Set<String> recipes : this.categoryMap.values()) {
         allRecipeIds.addAll(recipes);
      }

      List<CraftingRecipe> allRecipes = new ObjectArrayList(allRecipeIds.size());

      for(String recipeId : allRecipeIds) {
         CraftingRecipe recipe = (CraftingRecipe)CraftingRecipe.getAssetMap().getAsset(recipeId);
         if (recipe != null) {
            allRecipes.add(recipe);
         }
      }

      return (CraftingRecipe[])allRecipes.toArray((x$0) -> new CraftingRecipe[x$0]);
   }

   @Nullable
   public Set<String> getRecipesForCategory(@Nonnull String benchCategoryId) {
      return (Set)this.categoryMap.get(benchCategoryId);
   }

   public void recompute() {
      this.allMaterialIds.clear();
      this.allMaterialResourceType.clear();
      this.itemToIncomingRecipe.clear();

      for(Set<String> recipes : this.categoryMap.values()) {
         this.extractMaterialFromRecipes(recipes);
      }

      this.extractMaterialFromRecipes(this.uncategorizedRecipes);
   }

   private void extractMaterialFromRecipes(@Nonnull Set<String> recipes) {
      for(String recipeId : recipes) {
         CraftingRecipe recipe = (CraftingRecipe)CraftingRecipe.getAssetMap().getAsset(recipeId);
         if (recipe != null) {
            BenchRequirement[] benchRequirements = recipe.getBenchRequirement();
            if (benchRequirements != null) {
               boolean matchesRegistry = false;

               for(BenchRequirement requirement : benchRequirements) {
                  if (requirement.id.equals(this.benchId)) {
                     matchesRegistry = true;
                     break;
                  }
               }

               if (matchesRegistry) {
                  for(MaterialQuantity material : recipe.getInput()) {
                     if (material.getItemId() != null) {
                        this.allMaterialIds.add(material.getItemId());
                     }

                     if (material.getResourceTypeId() != null) {
                        this.allMaterialResourceType.add(material.getResourceTypeId());
                     }
                  }

                  for(MaterialQuantity output : recipe.getOutputs()) {
                     ((Set)this.itemToIncomingRecipe.computeIfAbsent(output.getItemId(), (k) -> new ObjectOpenHashSet())).add(recipeId);
                  }
               }
            }
         }
      }

   }

   public boolean isValidCraftingMaterial(@Nonnull ItemStack itemStack) {
      if (this.allMaterialIds.contains(itemStack.getItemId())) {
         return true;
      } else {
         ItemResourceType[] resourceTypeId = itemStack.getItem().getResourceTypes();
         if (resourceTypeId != null) {
            for(ItemResourceType resTypeId : resourceTypeId) {
               if (this.allMaterialResourceType.contains(resTypeId.id)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean equals(@Nullable Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         BenchRecipeRegistry that = (BenchRecipeRegistry)o;
         return Objects.equals(this.benchId, that.benchId) && Objects.equals(this.categoryMap, that.categoryMap) && Objects.equals(this.uncategorizedRecipes, that.uncategorizedRecipes) && Objects.equals(this.allMaterialIds, that.allMaterialIds) && Objects.equals(this.allMaterialResourceType, that.allMaterialResourceType);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.benchId, this.categoryMap, this.uncategorizedRecipes, this.allMaterialIds, this.allMaterialResourceType});
   }

   @Nonnull
   public String toString() {
      String var10000 = this.benchId;
      return "BenchRecipeRegistry{benchId='" + var10000 + "', categoryMap=" + String.valueOf(this.categoryMap) + ", uncategorizedRecipes=" + String.valueOf(this.uncategorizedRecipes) + ", allMaterialIds=" + String.valueOf(this.allMaterialIds) + ", allMaterialResourceType=" + String.valueOf(this.allMaterialResourceType) + "}";
   }
}
