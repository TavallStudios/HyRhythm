package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class JumpIfCompareOperation extends SequenceBrushOperation {
   public static final BuilderCodec<JumpIfCompareOperation> CODEC;
   @Nonnull
   public List<BrushConfigIntegerComparison> comparisonsArg = List.of();
   @Nonnull
   public String indexVariableNameArg = "Undefined";

   public JumpIfCompareOperation() {
      super("Jump If Int Comparison", "Jump stack execution to a stored index operation based on a specified conditional using the brush config data", false);
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      boolean success = true;

      for(BrushConfigIntegerComparison brushConfigIntegerComparison : this.comparisonsArg) {
         boolean result = brushConfigIntegerComparison.apply(brushConfig);
         if (!result) {
            success = false;
         }
      }

      if (success) {
         brushConfigCommandExecutor.loadOperatingIndex(this.indexVariableNameArg);
      }

   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(JumpIfCompareOperation.class, JumpIfCompareOperation::new).append(new KeyedCodec("Comparisons", new ArrayCodec(JumpIfCompareOperation.BrushConfigIntegerComparison.CODEC, (x$0) -> new BrushConfigIntegerComparison[x$0])), (op, val) -> op.comparisonsArg = val != null ? Arrays.asList(val) : List.of(), (op) -> (BrushConfigIntegerComparison[])op.comparisonsArg.toArray(new BrushConfigIntegerComparison[0])).documentation("The comparison(s) that will be executed using AND between them to see if you should jump or not").add()).append(new KeyedCodec("StoredIndexName", Codec.STRING), (op, val) -> op.indexVariableNameArg = val, (op) -> op.indexVariableNameArg).documentation("The labeled index to jump to, previous or future").add()).documentation("Jump stack execution to a stored index operation based on a specified conditional using the brush config data")).build();
   }

   public static class BrushConfigIntegerComparison implements Function<BrushConfig, Boolean> {
      public static final BuilderCodec<BrushConfigIntegerComparison> CODEC;
      private BrushConfig.DataGettingFlags dataGettingFlag;
      private ArgTypes.IntegerComparisonOperator integerComparisonOperator;
      private int valueToCompareTo;

      public BrushConfigIntegerComparison() {
      }

      public BrushConfigIntegerComparison(BrushConfig.DataGettingFlags dataGettingFlag, ArgTypes.IntegerComparisonOperator integerComparisonOperator, int valueToCompareTo) {
         this.dataGettingFlag = dataGettingFlag;
         this.integerComparisonOperator = integerComparisonOperator;
         this.valueToCompareTo = valueToCompareTo;
      }

      @Nonnull
      public Boolean apply(BrushConfig brushConfig) {
         return this.integerComparisonOperator.compare(this.dataGettingFlag.getValue(brushConfig), this.valueToCompareTo);
      }

      @Nonnull
      public String toString() {
         String var10000 = this.dataGettingFlag.name();
         return var10000 + " " + this.integerComparisonOperator.getStringRepresentation() + " " + this.valueToCompareTo;
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(BrushConfigIntegerComparison.class, BrushConfigIntegerComparison::new).append(new KeyedCodec("DataGettingFlag", new EnumCodec(BrushConfig.DataGettingFlags.class)), (comp, val) -> comp.dataGettingFlag = val, (comp) -> comp.dataGettingFlag).add()).append(new KeyedCodec("IntegerComparisonOperator", new EnumCodec(ArgTypes.IntegerComparisonOperator.class)), (comp, val) -> comp.integerComparisonOperator = val, (comp) -> comp.integerComparisonOperator).add()).append(new KeyedCodec("ValueToCompareTo", Codec.INTEGER), (comp, val) -> comp.valueToCompareTo = val, (comp) -> comp.valueToCompareTo).add()).build();
      }
   }
}
