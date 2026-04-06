package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OggVorbisInfoCache {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Map<String, OggVorbisInfo> vorbisFiles = new ConcurrentHashMap();

   @Nonnull
   public static CompletableFuture<OggVorbisInfo> get(String name) {
      OggVorbisInfo info = (OggVorbisInfo)vorbisFiles.get(name);
      if (info != null) {
         return CompletableFuture.completedFuture(info);
      } else {
         CommonAsset asset = CommonAssetRegistry.getByName(name);
         return asset == null ? CompletableFuture.completedFuture((Object)null) : get0(asset);
      }
   }

   @Nonnull
   public static CompletableFuture<OggVorbisInfo> get(@Nonnull CommonAsset asset) {
      OggVorbisInfo info = (OggVorbisInfo)vorbisFiles.get(asset.getName());
      return info != null ? CompletableFuture.completedFuture(info) : get0(asset);
   }

   @Nullable
   public static OggVorbisInfo getNow(String name) {
      OggVorbisInfo info = (OggVorbisInfo)vorbisFiles.get(name);
      if (info != null) {
         return info;
      } else {
         CommonAsset asset = CommonAssetRegistry.getByName(name);
         return asset == null ? null : (OggVorbisInfo)get0(asset).join();
      }
   }

   public static OggVorbisInfo getNow(@Nonnull CommonAsset asset) {
      OggVorbisInfo info = (OggVorbisInfo)vorbisFiles.get(asset.getName());
      return info != null ? info : (OggVorbisInfo)get0(asset).join();
   }

   @Nonnull
   private static CompletableFuture<OggVorbisInfo> get0(@Nonnull CommonAsset asset) {
      String name = asset.getName();
      return CompletableFutureUtil.<OggVorbisInfo>_catch(asset.getBlob().thenApply((bytes) -> {
         ByteBuf b = Unpooled.wrappedBuffer(bytes);

         OggVorbisInfo var21;
         try {
            int len = b.readableBytes();
            int id = -1;
            int i = 0;

            for(int end = len - 7; i <= end; ++i) {
               i = b.indexOf(i, len - 7, (byte)1);
               if (i == -1) {
                  break;
               }

               if (b.getByte(i + 1) == 118 && b.getByte(i + 2) == 111 && b.getByte(i + 3) == 114 && b.getByte(i + 4) == 98 && b.getByte(i + 5) == 105 && b.getByte(i + 6) == 115) {
                  id = i;
                  break;
               }
            }

            if (id < 0 || id + 16 > len) {
               throw new IllegalArgumentException("Vorbis id header not found");
            }

            i = b.getUnsignedByte(id + 11);
            int sampleRate = b.getIntLE(id + 12);
            double duration = -1.0;
            if (sampleRate > 0) {
               for(int i = Math.max(0, len - 14); i >= 0; --i) {
                  i = b.indexOf(i, 0, (byte)79);
                  if (i == -1) {
                     break;
                  }

                  if (b.getByte(i + 1) == 103 && b.getByte(i + 2) == 103 && b.getByte(i + 3) == 83) {
                     int headerType = b.getUnsignedByte(i + 5);
                     if ((headerType & 4) != 0) {
                        long granule = b.getLongLE(i + 6);
                        if (granule >= 0L) {
                           duration = (double)granule / (double)sampleRate;
                        }
                        break;
                     }
                  }
               }
            }

            OggVorbisInfo info = new OggVorbisInfo(i, sampleRate, duration);
            vorbisFiles.put(name, info);
            var21 = info;
         } finally {
            b.release();
         }

         return var21;
      }));
   }

   public static void invalidate(String name) {
      vorbisFiles.remove(name);
   }

   public static class OggVorbisInfo {
      public final int channels;
      public final int sampleRate;
      public final double duration;

      OggVorbisInfo(int channels, int sampleRate, double duration) {
         this.channels = channels;
         this.sampleRate = sampleRate;
         this.duration = duration;
      }

      @Nonnull
      public String toString() {
         return "OggVorbisInfo{channels=" + this.channels + ", sampleRate=" + this.sampleRate + ", duration=" + this.duration + "}";
      }
   }
}
