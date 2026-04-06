package com.hypixel.hytale.protocol.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class NoopPacketStatsRecorder implements PacketStatsRecorder {
   private static final PacketStatsRecorder.PacketStatsEntry EMPTY_ENTRY = new PacketStatsRecorder.PacketStatsEntry() {
      public int getPacketId() {
         return 0;
      }

      @Nullable
      public String getName() {
         return null;
      }

      public boolean hasData() {
         return false;
      }

      public int getSentCount() {
         return 0;
      }

      public long getSentUncompressedTotal() {
         return 0L;
      }

      public long getSentCompressedTotal() {
         return 0L;
      }

      public long getSentUncompressedMin() {
         return 0L;
      }

      public long getSentUncompressedMax() {
         return 0L;
      }

      public long getSentCompressedMin() {
         return 0L;
      }

      public long getSentCompressedMax() {
         return 0L;
      }

      public double getSentUncompressedAvg() {
         return 0.0;
      }

      public double getSentCompressedAvg() {
         return 0.0;
      }

      @Nonnull
      public PacketStatsRecorder.RecentStats getSentRecently() {
         return PacketStatsRecorder.RecentStats.EMPTY;
      }

      public int getReceivedCount() {
         return 0;
      }

      public long getReceivedUncompressedTotal() {
         return 0L;
      }

      public long getReceivedCompressedTotal() {
         return 0L;
      }

      public long getReceivedUncompressedMin() {
         return 0L;
      }

      public long getReceivedUncompressedMax() {
         return 0L;
      }

      public long getReceivedCompressedMin() {
         return 0L;
      }

      public long getReceivedCompressedMax() {
         return 0L;
      }

      public double getReceivedUncompressedAvg() {
         return 0.0;
      }

      public double getReceivedCompressedAvg() {
         return 0.0;
      }

      @Nonnull
      public PacketStatsRecorder.RecentStats getReceivedRecently() {
         return PacketStatsRecorder.RecentStats.EMPTY;
      }
   };

   public void recordSend(int packetId, int uncompressedSize, int compressedSize) {
   }

   public void recordReceive(int packetId, int uncompressedSize, int compressedSize) {
   }

   @Nonnull
   public PacketStatsRecorder.PacketStatsEntry getEntry(int packetId) {
      return EMPTY_ENTRY;
   }
}
