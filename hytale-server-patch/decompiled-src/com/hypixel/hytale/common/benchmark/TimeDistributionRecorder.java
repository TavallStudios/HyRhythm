package com.hypixel.hytale.common.benchmark;

import com.hypixel.hytale.math.util.MathUtil;
import java.util.Formatter;
import javax.annotation.Nonnull;

public class TimeDistributionRecorder extends TimeRecorder {
   protected int minLogRange;
   protected int maxLogRange;
   protected int logSteps;
   protected long[] valueBins;

   public TimeDistributionRecorder(double maxSecs, double minSecs, int logSteps) {
      if (!(maxSecs < 1.0E-6) && !(maxSecs > 0.1)) {
         if (!(minSecs < 1.0E-6) && !(minSecs > 0.1)) {
            if (maxSecs <= minSecs) {
               throw new IllegalArgumentException("Max seconds must be larger than min seconds");
            } else if (logSteps >= 2 && logSteps <= 10) {
               this.maxLogRange = MathUtil.ceil(Math.log10(maxSecs));
               this.minLogRange = MathUtil.floor(Math.log10(minSecs));
               this.logSteps = MathUtil.clamp(logSteps, 2, 10);
               this.valueBins = new long[(this.maxLogRange - this.minLogRange) * this.logSteps + 2];
               int i = 0;

               for(int length = this.valueBins.length; i < length; ++i) {
                  this.valueBins[i] = 0L;
               }

            } else {
               throw new IllegalArgumentException("LogSteps must be between 2 and 10");
            }
         } else {
            throw new IllegalArgumentException("Min seconds must be between 100 milli secs and 1 micro sec");
         }
      } else {
         throw new IllegalArgumentException("Max seconds must be between 100 milli secs and 1 micro sec");
      }
   }

   public TimeDistributionRecorder(double maxSecs, double minSecs) {
      this(maxSecs, minSecs, 5);
   }

   public TimeDistributionRecorder() {
      this(0.1, 1.0E-5);
   }

   public void reset() {
      super.reset();
      int i = 0;

      for(int length = this.valueBins.length; i < length; ++i) {
         this.valueBins[i] = 0L;
      }

   }

   public double recordNanos(long nanos) {
      double secs = super.recordNanos(nanos);
      int var10002 = this.valueBins[this.timeToIndex(secs)]++;
      return secs;
   }

   public int timeToIndex(double secs) {
      double logSecs = Math.log10(secs);
      double indexDbl = ((double)this.maxLogRange - logSecs) * (double)this.logSteps;
      int index = MathUtil.ceil(indexDbl);
      if (index < 0) {
         index = 0;
      } else if (index >= this.valueBins.length) {
         index = this.valueBins.length - 1;
      }

      return index;
   }

   public double indexToTime(int index) {
      if (index < 0) {
         index = 0;
      } else if (index >= this.valueBins.length) {
         index = this.valueBins.length - 1;
      }

      if (index == this.valueBins.length - 1) {
         return 0.0;
      } else {
         double exp = (double)this.maxLogRange - (double)index / (double)this.logSteps;
         return Math.pow(10.0, exp);
      }
   }

   public int size() {
      return this.valueBins.length;
   }

   public long get(int index) {
      return this.valueBins[index];
   }

   @Nonnull
   public String toString() {
      StringBuilder stringBuilder = new StringBuilder(12 * this.size());
      stringBuilder.append("Cnt=").append(this.getCount());

      for(int i = 0; i < this.size(); ++i) {
         stringBuilder.append(' ').append(formatTime(this.indexToTime(i))).append('=').append(this.get(i));
      }

      String var10000 = super.toString();
      return var10000 + " " + String.valueOf(stringBuilder);
   }

   public void formatHeader(@Nonnull Formatter formatter, @Nonnull String columnFormatHeader) {
      super.formatHeader(formatter, columnFormatHeader);

      for(int i = 0; i < this.size(); ++i) {
         formatter.format(columnFormatHeader, formatTime(this.indexToTime(i)));
      }

   }

   public void formatValues(@Nonnull Formatter formatter, @Nonnull String columnFormatValue) {
      this.formatValues(formatter, 0L, columnFormatValue);
   }

   public void formatValues(@Nonnull Formatter formatter, long normalValue) {
      this.formatValues(formatter, normalValue, "|%6.6s");
   }

   public void formatValues(@Nonnull Formatter formatter, long normalValue, @Nonnull String columnFormatValue) {
      super.formatValues(formatter, columnFormatValue);
      double norm = this.count > 0L && normalValue > 1L ? (double)normalValue / (double)this.count : 1.0;

      for(int i = 0; i < this.size(); ++i) {
         formatter.format(columnFormatValue, (int)Math.round((double)this.get(i) * norm));
      }

   }
}
