package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.UnionProp;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class UnionPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<UnionPropAsset> CODEC;
   private PropAsset[] propAssets = new PropAsset[0];

   @Nonnull
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return Prop.noProp();
      } else {
         ArrayList<Prop> chainedProps = new ArrayList(this.propAssets.length);

         for(PropAsset asset : this.propAssets) {
            chainedProps.add(asset.build(argument));
         }

         return new UnionProp(chainedProps);
      }
   }

   public void cleanUp() {
      for(PropAsset propAsset : this.propAssets) {
         propAsset.cleanUp();
      }

   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(UnionPropAsset.class, UnionPropAsset::new, PropAsset.ABSTRACT_CODEC).append(new KeyedCodec("Props", new ArrayCodec(PropAsset.CODEC, (x$0) -> new PropAsset[x$0]), true), (asset, v) -> asset.propAssets = v, (asset) -> asset.propAssets).add()).build();
   }
}
