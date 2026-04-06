package com.hypixel.hytale.server.core.asset.type.item.config.container;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;

public class MultipleItemDropContainer extends ItemDropContainer {
   public static final BuilderCodec<MultipleItemDropContainer> CODEC;
   protected ItemDropContainer[] containers;
   protected int minCount = 1;
   protected int maxCount = 1;

   public MultipleItemDropContainer(ItemDropContainer[] containers, double chance, int minCount, int maxCount) {
      super(chance);
      this.containers = containers;
      this.minCount = minCount;
      this.maxCount = maxCount;
   }

   public MultipleItemDropContainer() {
   }

   protected void populateDrops(List<ItemDrop> drops, @Nonnull DoubleSupplier chanceProvider, Set<String> droplistReferences) {
      int count = (int)MathUtil.fastRound(chanceProvider.getAsDouble() * (double)(this.maxCount - this.minCount) + (double)this.minCount);

      for(int i = 0; i < count; ++i) {
         for(ItemDropContainer container : this.containers) {
            if (container.getWeight() >= chanceProvider.getAsDouble() * 100.0) {
               container.populateDrops(drops, chanceProvider, droplistReferences);
            }
         }
      }

   }

   public List<ItemDrop> getAllDrops(List<ItemDrop> list) {
      for(ItemDropContainer container : this.containers) {
         container.getAllDrops(list);
      }

      return list;
   }

   @Nonnull
   public String toString() {
      double var10000 = this.weight;
      return "MultipleItemDropContainer{weight=" + var10000 + ", containers=" + Arrays.toString(this.containers) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(MultipleItemDropContainer.class, MultipleItemDropContainer::new, ItemDropContainer.DEFAULT_CODEC).addField(new KeyedCodec("MinCount", Codec.INTEGER), (multipleItemDropContainer, integer) -> multipleItemDropContainer.minCount = integer, (multipleItemDropContainer) -> multipleItemDropContainer.minCount)).addField(new KeyedCodec("MaxCount", Codec.INTEGER), (multipleItemDropContainer, integer) -> multipleItemDropContainer.maxCount = integer, (multipleItemDropContainer) -> multipleItemDropContainer.maxCount)).addField(new KeyedCodec("Containers", new ArrayCodec(ItemDropContainer.CODEC, (x$0) -> new ItemDropContainer[x$0])), (multipleItemDropContainer, o) -> multipleItemDropContainer.containers = o, (multipleItemDropContainer) -> multipleItemDropContainer.containers)).build();
   }
}
