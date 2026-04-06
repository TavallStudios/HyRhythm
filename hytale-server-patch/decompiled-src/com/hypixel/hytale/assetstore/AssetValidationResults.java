package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.logger.util.GithubMessageUtil;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetValidationResults extends ValidationResults {
   private Set<Class<? extends JsonAsset>> disabledMissingAssetClasses;

   public AssetValidationResults(ExtraInfo extraInfo) {
      super(extraInfo);
   }

   public void handleMissingAsset(String field, @Nonnull Class<? extends JsonAsset> assetType, Object assetId) {
      if (this.disabledMissingAssetClasses == null || !this.disabledMissingAssetClasses.contains(assetType)) {
         throw new MissingAssetException(field, assetType, assetId);
      }
   }

   public void handleMissingAsset(String field, @Nonnull Class<? extends JsonAsset> assetType, Object assetId, String extra) {
      if (this.disabledMissingAssetClasses == null || !this.disabledMissingAssetClasses.contains(assetType)) {
         throw new MissingAssetException(field, assetType, assetId, extra);
      }
   }

   public void disableMissingAssetFor(Class<? extends JsonAsset> assetType) {
      if (this.disabledMissingAssetClasses == null) {
         this.disabledMissingAssetClasses = new HashSet();
      }

      this.disabledMissingAssetClasses.add(assetType);
   }

   public void logOrThrowValidatorExceptions(@Nonnull HytaleLogger logger, @Nonnull String msg) {
      this.logOrThrowValidatorExceptions(logger, msg, (Path)null, 0);
   }

   public void logOrThrowValidatorExceptions(@Nonnull HytaleLogger logger, @Nonnull String msg, @Nullable Path path, int lineOffset) {
      if (GithubMessageUtil.isGithub() && this.validatorExceptions != null && !this.validatorExceptions.isEmpty()) {
         for(ValidationResults.ValidatorResultsHolder holder : this.validatorExceptions) {
            String file = "unknown";
            if (path == null) {
               ExtraInfo var9 = this.extraInfo;
               if (var9 instanceof AssetExtraInfo) {
                  AssetExtraInfo<?> assetExtraInfo = (AssetExtraInfo)var9;
                  path = assetExtraInfo.getAssetPath();
               }
            }

            if (path != null) {
               file = path.toString();
            }

            for(ValidationResults.ValidationResult result : holder.results()) {
               String var10000;
               switch (result.result()) {
                  case SUCCESS -> var10000 = "";
                  case WARNING -> var10000 = holder.line() == -1 ? GithubMessageUtil.messageWarning(file, result.reason()) : GithubMessageUtil.messageWarning(file, holder.line() + lineOffset, holder.column(), result.reason());
                  case FAIL -> var10000 = holder.line() == -1 ? GithubMessageUtil.messageError(file, result.reason()) : GithubMessageUtil.messageError(file, holder.line() + lineOffset, holder.column(), result.reason());
                  default -> throw new MatchException((String)null, (Throwable)null);
               }

               HytaleLoggerBackend.rawLog(var10000);
            }
         }
      }

      super.logOrThrowValidatorExceptions(logger, msg);
   }
}
