package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.codec.PairCodec;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JumpToRandomIndex extends SequenceBrushOperation {
   public static final BuilderCodec<JumpToRandomIndex> CODEC;
   @Nullable
   public IWeightedMap<String> variableNameArg = null;

   public JumpToRandomIndex() {
      super("Jump to Random Stored Index", "Jump the stack execution to a random location in the stack using the specified weights and saved index names", false);
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.variableNameArg != null) {
         String indexName = (String)this.variableNameArg.get(brushConfig.getRandom());
         brushConfigCommandExecutor.loadOperatingIndex(indexName);
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(JumpToRandomIndex.class, JumpToRandomIndex::new).append(new KeyedCodec("WeightedListOfIndexNames", new ArrayCodec(PairCodec.IntegerStringPair.CODEC, (x$0) -> new PairCodec.IntegerStringPair[x$0])), (op, val) -> {
         if (val != null && val.length != 0) {
            WeightedMap.Builder<String> builder = WeightedMap.<String>builder(new String[0]);

            for(PairCodec.IntegerStringPair pair : val) {
               builder.put(pair.getRight(), pair.getLeft().doubleValue());
            }

            op.variableNameArg = builder.build();
         } else {
            op.variableNameArg = null;
         }

      }, (op) -> {
         if (op.variableNameArg == null) {
            return new PairCodec.IntegerStringPair[0];
         } else {
            ArrayList<PairCodec.IntegerStringPair> pairs = new ArrayList();
            op.variableNameArg.forEachEntry((str, weight) -> pairs.add(PairCodec.IntegerStringPair.fromPair(Pair.of((int)weight, str))));
            return (PairCodec.IntegerStringPair[])pairs.toArray(new PairCodec.IntegerStringPair[0]);
         }
      }).documentation("A weighted list of weights and their corresponding index names").add()).documentation("Jump the stack execution to a random location in the stack using the specified weights and saved index names")).build();
   }
}
