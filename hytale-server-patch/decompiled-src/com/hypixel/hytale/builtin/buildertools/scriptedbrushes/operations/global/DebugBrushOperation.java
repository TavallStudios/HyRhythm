package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.global;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.GlobalBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DebugBrushOperation extends GlobalBrushOperation {
   public static final BuilderCodec<DebugBrushOperation> CODEC;
   @Nonnull
   private Boolean printOperations = false;
   @Nonnull
   private Boolean stepThrough = false;
   @Nonnull
   private Boolean enableBreakpoints = false;
   @Nonnull
   private BrushConfigCommandExecutor.DebugOutputTarget outputTarget;
   @Nonnull
   private Boolean breakOnError;

   public DebugBrushOperation() {
      super("Debug Step-Through", "Debug options for scripted brushes");
      this.outputTarget = BrushConfigCommandExecutor.DebugOutputTarget.Chat;
      this.breakOnError = false;
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      brushConfigCommandExecutor.setInDebugSteppingMode(this.stepThrough);
      brushConfigCommandExecutor.setPrintOperations(this.printOperations);
      brushConfigCommandExecutor.setEnableBreakpoints(this.enableBreakpoints);
      brushConfigCommandExecutor.setDebugOutputTarget(this.outputTarget);
      brushConfigCommandExecutor.setBreakOnError(this.breakOnError);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(DebugBrushOperation.class, DebugBrushOperation::new).append(new KeyedCodec("PrintOperations", Codec.BOOLEAN), (op, val) -> op.printOperations = val, (op) -> op.printOperations).documentation("Prints the index and name of each operation as it executes").add()).append(new KeyedCodec("StepThrough", Codec.BOOLEAN), (op, val) -> op.stepThrough = val, (op) -> op.stepThrough).documentation("Enables manual step-through mode (pause after each operation)").add()).append(new KeyedCodec("EnableBreakpoints", Codec.BOOLEAN), (op, val) -> op.enableBreakpoints = val, (op) -> op.enableBreakpoints).documentation("Master toggle for breakpoint operations").add()).append(new KeyedCodec("OutputTarget", new EnumCodec(BrushConfigCommandExecutor.DebugOutputTarget.class)), (op, val) -> op.outputTarget = val, (op) -> op.outputTarget).documentation("Where debug messages are sent (Chat, Console, or Both)").add()).append(new KeyedCodec("BreakOnError", Codec.BOOLEAN), (op, val) -> op.breakOnError = val, (op) -> op.breakOnError).documentation("Pause on error instead of terminating execution").add()).documentation("Debug options for scripted brushes")).build();
   }
}
