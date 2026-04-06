package com.hypixel.hytale.server.core.asset.type.item.config.container;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.codec.WeightedMapCodec;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;

public class ChoiceItemDropContainer extends ItemDropContainer {
   public static final BuilderCodec<ChoiceItemDropContainer> CODEC;
   protected IWeightedMap<ItemDropContainer> containers;
   protected int rollsMin = 1;
   protected int rollsMax = 1;

   public ChoiceItemDropContainer(ItemDropContainer[] containers, double chance) {
      super(chance);
      this.containers = WeightedMap.<ItemDropContainer>builder(ItemDropContainer.EMPTY_ARRAY).putAll(containers, ItemDropContainer::getWeight).build();
   }

   public ChoiceItemDropContainer() {
   }

   protected void populateDrops(List<ItemDrop> drops, DoubleSupplier chanceProvider, Set<String> droplistReferences) {
      int count = this.rollsMin + (int)(chanceProvider.getAsDouble() * (double)(this.rollsMax - this.rollsMin + 1));

      for(int i = 0; i < count; ++i) {
         ItemDropContainer drop = (ItemDropContainer)this.containers.get(chanceProvider);
         drop.populateDrops(drops, chanceProvider, droplistReferences);
      }

   }

   public List<ItemDrop> getAllDrops(List<ItemDrop> list) {
      for(ItemDropContainer container : (ItemDropContainer[])this.containers.internalKeys()) {
         container.getAllDrops(list);
      }

      return list;
   }

   @Nonnull
   public String toString() {
      double var10000 = this.weight;
      return "ChoiceItemDropContainer{weight=" + var10000 + ", containers=" + String.valueOf(this.containers) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ChoiceItemDropContainer.class, ChoiceItemDropContainer::new, ItemDropContainer.DEFAULT_CODEC).addField(new KeyedCodec("Containers", new WeightedMapCodec(ItemDropContainer.CODEC, ItemDropContainer.EMPTY_ARRAY)), (choiceItemDropContainer, o) -> choiceItemDropContainer.containers = o, (choiceItemDropContainer) -> choiceItemDropContainer.containers)).addField(new KeyedCodec("RollsMin", Codec.INTEGER), (choiceItemDropContainer, i) -> choiceItemDropContainer.rollsMin = i, (choiceItemDropContainer) -> choiceItemDropContainer.rollsMin)).addField(new KeyedCodec("RollsMax", Codec.INTEGER), (choiceItemDropContainer, i) -> choiceItemDropContainer.rollsMax = i, (choiceItemDropContainer) -> choiceItemDropContainer.rollsMax)).build();
   }
}
