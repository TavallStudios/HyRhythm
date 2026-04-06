package com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.responsecurve.config.ResponseCurve;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import javax.annotation.Nonnull;

public abstract class CurveCondition extends Condition {
   public static final BuilderCodec<CurveCondition> ABSTRACT_CODEC;
   protected String responseCurve;
   protected ResponseCurve.Reference responseCurveReference;

   protected CurveCondition() {
   }

   public String getResponseCurve() {
      return this.responseCurve;
   }

   public double calculateUtility(int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer, EvaluationContext context) {
      ResponseCurve curve = this.responseCurveReference.get();
      if (curve == null) {
         throw new IllegalStateException("No such response curve asset: " + this.responseCurve);
      } else {
         double input = this.getNormalisedInput(selfIndex, archetypeChunk, target, commandBuffer, context);
         return input == 1.7976931348623157E308 ? 0.0 : MathUtil.clamp(curve.computeY(input), 0.0, 1.0);
      }
   }

   public int getSimplicity() {
      return 20;
   }

   protected abstract double getNormalisedInput(int var1, ArchetypeChunk<EntityStore> var2, Ref<EntityStore> var3, CommandBuffer<EntityStore> var4, EvaluationContext var5);

   @Nonnull
   public String toString() {
      String var10000 = this.responseCurve;
      return "CurveCondition{responseCurve=" + var10000 + "} " + super.toString();
   }

   static {
      ABSTRACT_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(CurveCondition.class, BASE_CODEC).appendInherited(new KeyedCodec("Curve", Codec.STRING), (condition, s) -> condition.responseCurve = s, (condition) -> condition.responseCurve, (condition, parent) -> condition.responseCurve = parent.responseCurve).documentation("The response curve used to evaluate the condition.").addValidator(Validators.nonNull()).addValidator(ResponseCurve.VALIDATOR_CACHE.getValidator()).add()).afterDecode((condition) -> {
         if (condition.responseCurve != null) {
            int index = ResponseCurve.getAssetMap().getIndex(condition.responseCurve);
            condition.responseCurveReference = new ResponseCurve.Reference(index, (ResponseCurve)ResponseCurve.getAssetMap().getAsset(index));
         }

      })).build();
   }
}
