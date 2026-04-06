package com.hypixel.hytale.builtin.adventure.objectives.historydata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import java.time.Instant;
import javax.annotation.Nonnull;

public abstract class CommonObjectiveHistoryData {
   @Nonnull
   public static final CodecMapCodec<CommonObjectiveHistoryData> CODEC = new CodecMapCodec<CommonObjectiveHistoryData>("Type");
   @Nonnull
   public static final BuilderCodec<CommonObjectiveHistoryData> BASE_CODEC;
   protected String id;
   protected int timesCompleted;
   protected Instant lastCompletionTimestamp;
   protected String category;

   public CommonObjectiveHistoryData(String id, String category) {
      this.id = id;
      this.timesCompleted = 1;
      this.lastCompletionTimestamp = Instant.now();
      this.category = category;
   }

   protected CommonObjectiveHistoryData() {
   }

   public String getId() {
      return this.id;
   }

   public int getTimesCompleted() {
      return this.timesCompleted;
   }

   public Instant getLastCompletionTimestamp() {
      return this.lastCompletionTimestamp;
   }

   public String getCategory() {
      return this.category;
   }

   protected void completed() {
      ++this.timesCompleted;
      this.lastCompletionTimestamp = Instant.now();
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "CommonObjectiveHistoryData{id='" + var10000 + "', timesCompleted=" + this.timesCompleted + ", lastCompletionTimestamp=" + String.valueOf(this.lastCompletionTimestamp) + ", category='" + this.category + "'}";
   }

   static {
      BASE_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(CommonObjectiveHistoryData.class).append(new KeyedCodec("Id", Codec.STRING), (commonObjectiveHistoryData, s) -> commonObjectiveHistoryData.id = s, (commonObjectiveHistoryData) -> commonObjectiveHistoryData.id).add()).append(new KeyedCodec("TimesCompleted", Codec.INTEGER), (commonObjectiveHistoryData, integer) -> commonObjectiveHistoryData.timesCompleted = integer, (commonObjectiveHistoryData) -> commonObjectiveHistoryData.timesCompleted).add()).append(new KeyedCodec("LastCompletionTimestamp", Codec.LONG), (o, i) -> o.lastCompletionTimestamp = Instant.ofEpochMilli(i), (o) -> o.lastCompletionTimestamp == null ? null : o.lastCompletionTimestamp.toEpochMilli()).add()).append(new KeyedCodec("Category", Codec.STRING), (commonObjectiveHistoryData, s) -> commonObjectiveHistoryData.category = s, (commonObjectiveHistoryData) -> commonObjectiveHistoryData.category).add()).build();
   }
}
