package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.ValidationResults;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RequiredBlockFaceSupportValidator implements LegacyValidator<Map<BlockFace, RequiredBlockFaceSupport[]>> {
   static final RequiredBlockFaceSupportValidator INSTANCE = new RequiredBlockFaceSupportValidator();

   public void accept(@Nullable Map<BlockFace, RequiredBlockFaceSupport[]> support, @Nonnull ValidationResults results) {
      if (support != null) {
         for(Map.Entry<BlockFace, RequiredBlockFaceSupport[]> entry : support.entrySet()) {
            BlockFace blockFace = (BlockFace)entry.getKey();
            RequiredBlockFaceSupport[] requiredBlockFaceSupports = (RequiredBlockFaceSupport[])entry.getValue();

            for(int i = 0; i < requiredBlockFaceSupports.length; ++i) {
               RequiredBlockFaceSupport blockFaceSupport = requiredBlockFaceSupports[i];
               if (blockFaceSupport == null) {
                  String var10001 = String.valueOf(blockFace);
                  results.fail("Value for 'Support." + var10001 + "[" + i + "]' can't be null!");
               } else {
                  boolean noRequirements = blockFaceSupport.getFaceType() == null && blockFaceSupport.getBlockSetId() == null && blockFaceSupport.getBlockTypeId() == null && blockFaceSupport.getFluidId() == null && blockFaceSupport.getMatchSelf() == RequiredBlockFaceSupport.Match.IGNORED && blockFaceSupport.getTagId() == null;
                  if (blockFaceSupport.getSupport() != RequiredBlockFaceSupport.Match.IGNORED && noRequirements) {
                     String var10 = String.valueOf(blockFace);
                     results.warn("Value for 'Support." + var10 + "[" + i + "]' doesn't define any requirements and should be removed!");
                  }

                  if (blockFaceSupport.getSupport() == RequiredBlockFaceSupport.Match.IGNORED && !blockFaceSupport.allowsSupportPropagation()) {
                     String var11 = String.valueOf(blockFace);
                     results.warn("Value for 'Support." + var11 + "[" + i + "]' doesn't allow support or support propagation so should be removed!");
                  }
               }
            }
         }

      }
   }
}
