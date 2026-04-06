package com.hypixel.hytale.server.core.io;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.metrics.metric.AverageCollector;
import com.hypixel.hytale.protocol.PacketRegistry;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketStatsRecorderImpl implements PacketStatsRecorder {
   public static final MetricsRegistry<PacketStatsRecorderImpl> METRICS_REGISTRY;
   private final PacketStatsEntry[] entries = new PacketStatsEntry[512];

   public PacketStatsRecorderImpl() {
      for(int i = 0; i < this.entries.length; ++i) {
         this.entries[i] = new PacketStatsEntry(i);
      }

   }

   public void recordSend(int packetId, int uncompressedSize, int compressedSize) {
      if (packetId >= 0 && packetId < this.entries.length) {
         this.entries[packetId].recordSend(uncompressedSize, compressedSize);
      }
   }

   public void recordReceive(int packetId, int uncompressedSize, int compressedSize) {
      if (packetId >= 0 && packetId < this.entries.length) {
         this.entries[packetId].recordReceive(uncompressedSize, compressedSize);
      }
   }

   @Nonnull
   public PacketStatsEntry getEntry(int packetId) {
      return this.entries[packetId];
   }

   static {
      METRICS_REGISTRY = (new MetricsRegistry()).register("Packets", (recorder) -> {
         ArrayList<PacketStatsEntry> entries = new ArrayList();

         for(int i = 0; i < 512; ++i) {
            PacketStatsEntry entry = recorder.entries[i];
            if (entry.hasData()) {
               entries.add(entry);
            }
         }

         return (PacketStatsEntry[])entries.toArray((x$0) -> new PacketStatsEntry[x$0]);
      }, new ArrayCodec(PacketStatsRecorderImpl.PacketStatsEntry.METRICS_REGISTRY, (x$0) -> new PacketStatsEntry[x$0]));
   }

   public static class PacketStatsEntry implements PacketStatsRecorder.PacketStatsEntry {
      public static final MetricsRegistry<PacketStatsEntry> METRICS_REGISTRY;
      private final int packetId;
      private final AtomicInteger sentCount = new AtomicInteger();
      private final AtomicLong sentUncompressedTotal = new AtomicLong();
      private final AtomicLong sentCompressedTotal = new AtomicLong();
      private final AtomicLong sentUncompressedMin = new AtomicLong(9223372036854775807L);
      private final AtomicLong sentUncompressedMax = new AtomicLong();
      private final AtomicLong sentCompressedMin = new AtomicLong(9223372036854775807L);
      private final AtomicLong sentCompressedMax = new AtomicLong();
      private final AverageCollector sentUncompressedAvg = new AverageCollector();
      private final AverageCollector sentCompressedAvg = new AverageCollector();
      private final Queue<SizeRecord> sentRecently = new ConcurrentLinkedQueue();
      private final AtomicInteger receivedCount = new AtomicInteger();
      private final AtomicLong receivedUncompressedTotal = new AtomicLong();
      private final AtomicLong receivedCompressedTotal = new AtomicLong();
      private final AtomicLong receivedUncompressedMin = new AtomicLong(9223372036854775807L);
      private final AtomicLong receivedUncompressedMax = new AtomicLong();
      private final AtomicLong receivedCompressedMin = new AtomicLong(9223372036854775807L);
      private final AtomicLong receivedCompressedMax = new AtomicLong();
      private final AverageCollector receivedUncompressedAvg = new AverageCollector();
      private final AverageCollector receivedCompressedAvg = new AverageCollector();
      private final Queue<SizeRecord> receivedRecently = new ConcurrentLinkedQueue();

      public PacketStatsEntry(int packetId) {
         this.packetId = packetId;
      }

      void recordSend(int uncompressedSize, int compressedSize) {
         this.sentCount.incrementAndGet();
         this.sentUncompressedTotal.addAndGet((long)uncompressedSize);
         this.sentCompressedTotal.addAndGet((long)compressedSize);
         this.sentUncompressedMin.accumulateAndGet((long)uncompressedSize, Math::min);
         this.sentUncompressedMax.accumulateAndGet((long)uncompressedSize, Math::max);
         this.sentCompressedMin.accumulateAndGet((long)compressedSize, Math::min);
         this.sentCompressedMax.accumulateAndGet((long)compressedSize, Math::max);
         this.sentUncompressedAvg.add((double)uncompressedSize);
         this.sentCompressedAvg.add((double)compressedSize);
         long now = System.nanoTime();
         this.sentRecently.add(new SizeRecord(now, uncompressedSize, compressedSize));
         this.pruneOld(this.sentRecently, now);
      }

      void recordReceive(int uncompressedSize, int compressedSize) {
         this.receivedCount.incrementAndGet();
         this.receivedUncompressedTotal.addAndGet((long)uncompressedSize);
         this.receivedCompressedTotal.addAndGet((long)compressedSize);
         this.receivedUncompressedMin.accumulateAndGet((long)uncompressedSize, Math::min);
         this.receivedUncompressedMax.accumulateAndGet((long)uncompressedSize, Math::max);
         this.receivedCompressedMin.accumulateAndGet((long)compressedSize, Math::min);
         this.receivedCompressedMax.accumulateAndGet((long)compressedSize, Math::max);
         this.receivedUncompressedAvg.add((double)uncompressedSize);
         this.receivedCompressedAvg.add((double)compressedSize);
         long now = System.nanoTime();
         this.receivedRecently.add(new SizeRecord(now, uncompressedSize, compressedSize));
         this.pruneOld(this.receivedRecently, now);
      }

      private void pruneOld(Queue<SizeRecord> queue, long now) {
         long cutoff = now - TimeUnit.SECONDS.toNanos(30L);

         for(SizeRecord head = (SizeRecord)queue.peek(); head != null && head.nanos < cutoff; head = (SizeRecord)queue.peek()) {
            queue.poll();
         }

      }

      public boolean hasData() {
         return this.sentCount.get() > 0 || this.receivedCount.get() > 0;
      }

      public int getPacketId() {
         return this.packetId;
      }

      @Nullable
      public String getName() {
         PacketRegistry.PacketInfo info = (PacketRegistry.PacketInfo)PacketRegistry.all().get(this.packetId);
         return info != null ? info.name() : null;
      }

      public int getSentCount() {
         return this.sentCount.get();
      }

      public long getSentUncompressedTotal() {
         return this.sentUncompressedTotal.get();
      }

      public long getSentCompressedTotal() {
         return this.sentCompressedTotal.get();
      }

      public long getSentUncompressedMin() {
         return this.sentCount.get() > 0 ? this.sentUncompressedMin.get() : 0L;
      }

      public long getSentUncompressedMax() {
         return this.sentUncompressedMax.get();
      }

      public long getSentCompressedMin() {
         return this.sentCount.get() > 0 ? this.sentCompressedMin.get() : 0L;
      }

      public long getSentCompressedMax() {
         return this.sentCompressedMax.get();
      }

      public double getSentUncompressedAvg() {
         return this.sentUncompressedAvg.get();
      }

      public double getSentCompressedAvg() {
         return this.sentCompressedAvg.get();
      }

      public int getReceivedCount() {
         return this.receivedCount.get();
      }

      public long getReceivedUncompressedTotal() {
         return this.receivedUncompressedTotal.get();
      }

      public long getReceivedCompressedTotal() {
         return this.receivedCompressedTotal.get();
      }

      public long getReceivedUncompressedMin() {
         return this.receivedCount.get() > 0 ? this.receivedUncompressedMin.get() : 0L;
      }

      public long getReceivedUncompressedMax() {
         return this.receivedUncompressedMax.get();
      }

      public long getReceivedCompressedMin() {
         return this.receivedCount.get() > 0 ? this.receivedCompressedMin.get() : 0L;
      }

      public long getReceivedCompressedMax() {
         return this.receivedCompressedMax.get();
      }

      public double getReceivedUncompressedAvg() {
         return this.receivedUncompressedAvg.get();
      }

      public double getReceivedCompressedAvg() {
         return this.receivedCompressedAvg.get();
      }

      @Nonnull
      public PacketStatsRecorder.RecentStats getSentRecently() {
         return this.computeRecentStats(this.sentRecently);
      }

      @Nonnull
      public PacketStatsRecorder.RecentStats getReceivedRecently() {
         return this.computeRecentStats(this.receivedRecently);
      }

      private PacketStatsRecorder.RecentStats computeRecentStats(Queue<SizeRecord> queue) {
         int count = 0;
         long uncompressedTotal = 0L;
         long compressedTotal = 0L;
         int uncompressedMin = 2147483647;
         int uncompressedMax = 0;
         int compressedMin = 2147483647;
         int compressedMax = 0;

         for(SizeRecord record : queue) {
            ++count;
            uncompressedTotal += (long)record.uncompressedSize;
            compressedTotal += (long)record.compressedSize;
            uncompressedMin = Math.min(uncompressedMin, record.uncompressedSize);
            uncompressedMax = Math.max(uncompressedMax, record.uncompressedSize);
            compressedMin = Math.min(compressedMin, record.compressedSize);
            compressedMax = Math.max(compressedMax, record.compressedSize);
         }

         return count == 0 ? PacketStatsRecorder.RecentStats.EMPTY : new PacketStatsRecorder.RecentStats(count, uncompressedTotal, compressedTotal, uncompressedMin, uncompressedMax, compressedMin, compressedMax);
      }

      public void reset() {
         this.sentCount.set(0);
         this.sentUncompressedTotal.set(0L);
         this.sentCompressedTotal.set(0L);
         this.sentUncompressedMin.set(9223372036854775807L);
         this.sentUncompressedMax.set(0L);
         this.sentCompressedMin.set(9223372036854775807L);
         this.sentCompressedMax.set(0L);
         this.sentUncompressedAvg.clear();
         this.sentCompressedAvg.clear();
         this.sentRecently.clear();
         this.receivedCount.set(0);
         this.receivedUncompressedTotal.set(0L);
         this.receivedCompressedTotal.set(0L);
         this.receivedUncompressedMin.set(9223372036854775807L);
         this.receivedUncompressedMax.set(0L);
         this.receivedCompressedMin.set(9223372036854775807L);
         this.receivedCompressedMax.set(0L);
         this.receivedUncompressedAvg.clear();
         this.receivedCompressedAvg.clear();
         this.receivedRecently.clear();
      }

      static {
         METRICS_REGISTRY = (new MetricsRegistry()).register("PacketId", PacketStatsEntry::getPacketId, Codec.INTEGER).register("Name", PacketStatsEntry::getName, Codec.STRING).register("SentCount", PacketStatsEntry::getSentCount, Codec.INTEGER).register("SentUncompressedTotal", PacketStatsEntry::getSentUncompressedTotal, Codec.LONG).register("SentCompressedTotal", PacketStatsEntry::getSentCompressedTotal, Codec.LONG).register("SentUncompressedMin", PacketStatsEntry::getSentUncompressedMin, Codec.LONG).register("SentUncompressedMax", PacketStatsEntry::getSentUncompressedMax, Codec.LONG).register("SentCompressedMin", PacketStatsEntry::getSentCompressedMin, Codec.LONG).register("SentCompressedMax", PacketStatsEntry::getSentCompressedMax, Codec.LONG).register("ReceivedCount", PacketStatsEntry::getReceivedCount, Codec.INTEGER).register("ReceivedUncompressedTotal", PacketStatsEntry::getReceivedUncompressedTotal, Codec.LONG).register("ReceivedCompressedTotal", PacketStatsEntry::getReceivedCompressedTotal, Codec.LONG).register("ReceivedUncompressedMin", PacketStatsEntry::getReceivedUncompressedMin, Codec.LONG).register("ReceivedUncompressedMax", PacketStatsEntry::getReceivedUncompressedMax, Codec.LONG).register("ReceivedCompressedMin", PacketStatsEntry::getReceivedCompressedMin, Codec.LONG).register("ReceivedCompressedMax", PacketStatsEntry::getReceivedCompressedMax, Codec.LONG);
      }

      public static record SizeRecord(long nanos, int uncompressedSize, int compressedSize) {
      }
   }
}
