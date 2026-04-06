package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.saveandload;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PersistentDataOperation extends SequenceBrushOperation {
   public static final BuilderCodec<PersistentDataOperation> CODEC;
   @Nonnull
   public String variableNameArg = "Undefined";
   @Nonnull
   public ArgTypes.IntegerOperation operationArg;
   @Nonnull
   public Integer modifierArg;

   public PersistentDataOperation() {
      super("Persistent Data", "Store and operate on data that sticks around between executions", false);
      this.operationArg = ArgTypes.IntegerOperation.SET;
      this.modifierArg = 0;
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      int persistentVariable = brushConfigCommandExecutor.getPersistentVariableOrDefault(this.variableNameArg, 0);
      System.out.println(this.variableNameArg + ": " + persistentVariable);
      int newValue = this.operationArg.operate(persistentVariable, this.modifierArg);
      brushConfigCommandExecutor.setPersistentVariable(this.variableNameArg, newValue);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PersistentDataOperation.class, PersistentDataOperation::new).append(new KeyedCodec("StoredName", Codec.STRING), (op, val) -> op.variableNameArg = val, (op) -> op.variableNameArg).documentation("The name of the variable to modify").add()).append(new KeyedCodec("Operation", new EnumCodec(ArgTypes.IntegerOperation.class)), (op, val) -> op.operationArg = val, (op) -> op.operationArg).documentation("The operation to perform on the variable using the modifier").add()).append(new KeyedCodec("Modifier", Codec.INTEGER), (op, val) -> op.modifierArg = val, (op) -> op.modifierArg).documentation("The value to modify the variable by").add()).documentation("Store and operate on data that sticks around between executions")).build();
   }
}
