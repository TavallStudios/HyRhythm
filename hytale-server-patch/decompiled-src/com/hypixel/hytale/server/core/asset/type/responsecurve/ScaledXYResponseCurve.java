package com.hypixel.hytale.server.core.asset.type.responsecurve;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.MathUtil;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ScaledXYResponseCurve extends ScaledXResponseCurve {
   public static final BuilderCodec<ScaledXYResponseCurve> CODEC;
   protected double[] yRange;

   public ScaledXYResponseCurve(String responseCurve, double[] xRange, double[] yRange) {
      super(responseCurve, xRange);
      this.yRange = DEFAULT_RANGE;
      this.yRange = yRange;
   }

   protected ScaledXYResponseCurve() {
      this.yRange = DEFAULT_RANGE;
   }

   public double[] getYRange() {
      return this.yRange;
   }

   public double computeY(double x) {
      double normalisedY = this.computeNormalisedY(x);
      double minY = this.yRange[0];
      double maxY = this.yRange[1];
      return MathUtil.clamp(minY + (maxY - minY) * normalisedY, minY, maxY);
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.yRange);
      return "ScaledXYResponseCurve{yRange=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ScaledXYResponseCurve.class, ScaledXYResponseCurve::new, ScaledXResponseCurve.CODEC).documentation("A response curve that is scaled on both the x and y axes.")).append(new KeyedCodec("YRange", Codec.DOUBLE_ARRAY), (curve, o) -> curve.yRange = o, (curve) -> new double[]{curve.yRange[0], curve.yRange[1]}).documentation("The range to map the y axis to. e.g. [ 0, 10 ]").addValidator(Validators.doubleArraySize(2)).addValidator(Validators.monotonicSequentialDoubleArrayValidator()).add()).build();
   }
}
