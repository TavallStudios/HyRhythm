package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.scanners.AreaScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class AreaScannerAsset extends ScannerAsset {
   @Nonnull
   public static final BuilderCodec<AreaScannerAsset> CODEC;
   private int resultCap = 1;
   private AreaScanner.ScanShape scanShape;
   private int scanRange;
   private ScannerAsset childScannerAsset;

   public AreaScannerAsset() {
      this.scanShape = AreaScanner.ScanShape.CIRCLE;
      this.scanRange = 0;
      this.childScannerAsset = new OriginScannerAsset();
   }

   @Nonnull
   public Scanner build(@Nonnull ScannerAsset.Argument argument) {
      return (Scanner)(!super.skip() && this.childScannerAsset != null ? new AreaScanner(this.resultCap, this.scanShape, this.scanRange, this.childScannerAsset.build(argument)) : Scanner.noScanner());
   }

   public void cleanUp() {
      this.childScannerAsset.cleanUp();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(AreaScannerAsset.class, AreaScannerAsset::new, ScannerAsset.ABSTRACT_CODEC).append(new KeyedCodec("ResultCap", Codec.INTEGER, true), (t, k) -> t.resultCap = k, (k) -> k.resultCap).addValidator(Validators.greaterThanOrEqual(0)).add()).append(new KeyedCodec("ScanShape", AreaScanner.ScanShape.CODEC, false), (t, k) -> t.scanShape = k, (t) -> t.scanShape).add()).append(new KeyedCodec("ScanRange", Codec.INTEGER, false), (t, k) -> t.scanRange = k, (t) -> t.scanRange).addValidator(Validators.greaterThan(-1)).add()).append(new KeyedCodec("ChildScanner", ScannerAsset.CODEC, false), (t, k) -> t.childScannerAsset = k, (t) -> t.childScannerAsset).add()).build();
   }
}
