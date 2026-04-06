package com.hypixel.hytale.server.core.modules.time;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.UpdateTime;
import com.hypixel.hytale.protocol.packets.world.UpdateTimeSettings;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.ecs.MoonPhaseChangeEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

public class WorldTimeResource implements Resource<EntityStore> {
   public static final long NANOS_PER_DAY;
   public static final int SECONDS_PER_DAY;
   public static final int HOURS_PER_DAY;
   public static final int DAYS_PER_YEAR;
   public static final Instant ZERO_YEAR;
   public static final Instant MAX_TIME;
   public static final ZoneOffset ZONE_OFFSET;
   public static final float SUN_HEIGHT = 2.0F;
   public static final boolean USE_SHADOW_MAPPING_SAFE_ANGLE = true;
   public static final float DAYTIME_PORTION_PERCENTAGE = 0.6F;
   public static final int DAYTIME_SECONDS;
   public static final int NIGHTTIME_SECONDS;
   public static final int SUNRISE_SECONDS;
   public static final float SHADOW_MAPPING_SAFE_ANGLE_LERP = 0.35F;
   @Nonnull
   private final UpdateTime currentTimePacket = new UpdateTime();
   private Instant gameTime;
   private LocalDateTime _gameTimeLocalDateTime;
   private int currentHour;
   private double sunlightFactor;
   private double scaledTime;
   private int moonPhase;
   @Nonnull
   private final UpdateTimeSettings currentSettings = new UpdateTimeSettings();
   @Nonnull
   private final UpdateTimeSettings tempSettings = new UpdateTimeSettings();

   @Nonnull
   public static ResourceType<EntityStore, WorldTimeResource> getResourceType() {
      return TimeModule.get().getWorldTimeResourceType();
   }

   public static double getSecondsPerTick(World world) {
      int daytimeDurationSeconds = world.getDaytimeDurationSeconds();
      int nighttimeDurationSeconds = world.getNighttimeDurationSeconds();
      int totalDurationSeconds = daytimeDurationSeconds + nighttimeDurationSeconds;
      return (double)SECONDS_PER_DAY / (double)totalDurationSeconds;
   }

   public void tick(float dt, @Nonnull Store<EntityStore> store) {
      World world = ((EntityStore)store.getExternalData()).getWorld();
      if (!updateTimeSettingsPacket(this.tempSettings, world).equals(this.currentSettings)) {
         boolean wasTimePausedChanged = this.currentSettings.timePaused != this.tempSettings.timePaused;
         updateTimeSettingsPacket(this.currentSettings, world);
         PlayerUtil.broadcastPacketToPlayers(store, (ToClientPacket)this.currentSettings);
         if (wasTimePausedChanged) {
            this.broadcastTimePacket(store);
         }
      }

      if (!world.getWorldConfig().isGameTimePaused()) {
         int secondsOfDay = this._gameTimeLocalDateTime.get(ChronoField.SECOND_OF_DAY);
         int daytimeDurationSeconds = world.getDaytimeDurationSeconds();
         int nighttimeDurationSeconds = world.getNighttimeDurationSeconds();
         int totalDurationSeconds = daytimeDurationSeconds + nighttimeDurationSeconds;
         double daytimeRate = (double)DAYTIME_SECONDS / (double)daytimeDurationSeconds;
         double nighttimeRate = (double)NIGHTTIME_SECONDS / (double)nighttimeDurationSeconds;
         double x0;
         if (secondsOfDay >= SUNRISE_SECONDS && secondsOfDay < SUNRISE_SECONDS + DAYTIME_SECONDS) {
            x0 = (double)(secondsOfDay - SUNRISE_SECONDS) / daytimeRate;
         } else {
            x0 = (double)daytimeDurationSeconds + MathUtil.floorMod((double)(secondsOfDay - SUNRISE_SECONDS - DAYTIME_SECONDS), (double)SECONDS_PER_DAY) / nighttimeRate;
         }

         double x1 = x0 + (double)dt;
         long whole = (long)Math.floor(x1 / (double)totalDurationSeconds) - (long)Math.floor(x0 / (double)totalDurationSeconds);
         double m0 = MathUtil.floorMod(x0, (double)totalDurationSeconds);
         double m1 = MathUtil.floorMod(x1, (double)totalDurationSeconds);
         double f0 = m0 <= (double)daytimeDurationSeconds ? daytimeRate * m0 : (double)DAYTIME_SECONDS + nighttimeRate * (m0 - (double)daytimeDurationSeconds);
         double f1 = m1 <= (double)daytimeDurationSeconds ? daytimeRate * m1 : (double)DAYTIME_SECONDS + nighttimeRate * (m1 - (double)daytimeDurationSeconds);
         double advance = (double)(whole * (long)SECONDS_PER_DAY) + (f1 - f0);
         Instant temp = this.gameTime.plusNanos((long)(advance * 1.0E9));
         if (temp.isBefore(ZERO_YEAR)) {
            temp = MAX_TIME.minusSeconds(ZERO_YEAR.getEpochSecond() - this.gameTime.getEpochSecond()).minusNanos((long)(ZERO_YEAR.getNano() - this.gameTime.getNano()));
         }

         if (temp.isAfter(MAX_TIME)) {
            temp = ZERO_YEAR.plusSeconds(MAX_TIME.getEpochSecond() - this.gameTime.getEpochSecond()).plusNanos((long)(MAX_TIME.getNano() - this.gameTime.getNano()));
         }

         this.setGameTime0(temp);
         this.updateMoonPhase(world, store);
      }
   }

   public int getMoonPhase() {
      return this.moonPhase;
   }

   public void setMoonPhase(int moonPhase, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (moonPhase != this.moonPhase) {
         MoonPhaseChangeEvent event = new MoonPhaseChangeEvent(moonPhase);
         componentAccessor.invoke(event);
      }

      this.moonPhase = moonPhase;
   }

   public void updateMoonPhase(@Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      WorldConfig worldGameplayConfig = world.getGameplayConfig().getWorldConfig();
      int totalMoonPhases = worldGameplayConfig.getTotalMoonPhases();
      double dayProgress = (double)this.currentHour / (double)HOURS_PER_DAY;
      int currentDay = this._gameTimeLocalDateTime.getDayOfYear();
      int weekDay = (currentDay - 1) % totalMoonPhases;
      if (dayProgress < 0.5) {
         if (weekDay == 0) {
            this.setMoonPhase(totalMoonPhases - 1, componentAccessor);
         } else {
            this.setMoonPhase(weekDay - 1, componentAccessor);
         }
      } else {
         this.setMoonPhase(weekDay, componentAccessor);
      }

   }

   public boolean isMoonPhaseWithinRange(@Nonnull World world, int minMoonPhase, int maxMoonPhase) {
      WorldConfig worldGameplayConfig = world.getGameplayConfig().getWorldConfig();
      int totalMoonPhases = worldGameplayConfig.getTotalMoonPhases();
      if (minMoonPhase <= maxMoonPhase) {
         return MathUtil.within((double)this.moonPhase, (double)minMoonPhase, (double)maxMoonPhase);
      } else {
         return MathUtil.within((double)this.moonPhase, (double)minMoonPhase, (double)totalMoonPhases) || MathUtil.within((double)this.moonPhase, 0.0, (double)maxMoonPhase);
      }
   }

   public void setGameTime0(@Nonnull Instant gameTime) {
      this.gameTime = gameTime;
      this._gameTimeLocalDateTime = LocalDateTime.ofInstant(gameTime, ZONE_OFFSET);
      this.updateTimePacket(this.currentTimePacket);
      this.currentHour = this._gameTimeLocalDateTime.getHour();
      int dayProgress = this._gameTimeLocalDateTime.get(ChronoField.SECOND_OF_DAY);
      float dayDuration = 0.6F * (float)SECONDS_PER_DAY;
      float nightDuration = (float)SECONDS_PER_DAY - dayDuration;
      float halfNight = nightDuration * 0.5F;
      this.updateSunlightFactor(dayProgress, halfNight);
      this.updateScaledTime((float)dayProgress, dayDuration, halfNight);
   }

   private void updateSunlightFactor(int dayProgress, float halfNight) {
      float dawnRelativeProgress = ((float)dayProgress - halfNight) / (float)SECONDS_PER_DAY;
      this.sunlightFactor = MathUtil.clamp((double)TrigMathUtil.sin(6.2831855F * dawnRelativeProgress) + 0.2, 0.0, 1.0);
   }

   private void updateScaledTime(float dayProgress, float dayDuration, float halfNight) {
      if (dayProgress <= halfNight) {
         this.scaledTime = (double)MathUtil.lerp(0.0F, 0.25F, dayProgress / halfNight);
      } else {
         dayProgress -= halfNight;
         if (dayProgress <= dayDuration) {
            this.scaledTime = (double)MathUtil.lerp(0.25F, 0.75F, dayProgress / dayDuration);
         } else {
            dayProgress -= dayDuration;
            this.scaledTime = (double)MathUtil.lerp(0.75F, 1.0F, dayProgress / halfNight);
         }
      }
   }

   public Instant getGameTime() {
      return this.gameTime;
   }

   public LocalDateTime getGameDateTime() {
      return this._gameTimeLocalDateTime;
   }

   public double getSunlightFactor() {
      return this.sunlightFactor;
   }

   public void setGameTime(@Nonnull Instant gameTime, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      this.setGameTime0(gameTime);
      this.updateMoonPhase(world, store);
      this.broadcastTimePacket(store);
   }

   public void setDayTime(double dayTime, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (!(dayTime < 0.0) && !(dayTime > 1.0)) {
         Instant oldGameTime = this.gameTime;
         Instant dayStart = oldGameTime.truncatedTo(ChronoUnit.DAYS);
         Instant newGameTime = dayStart.plusNanos((long)(dayTime * (double)NANOS_PER_DAY));
         if (newGameTime.isBefore(oldGameTime)) {
            this.setGameTime(newGameTime.plus(1L, ChronoUnit.DAYS), world, store);
         } else {
            this.setGameTime(newGameTime, world, store);
         }

      } else {
         throw new IllegalArgumentException("Day time must be between 0 and 1");
      }
   }

   public void broadcastTimePacket(@Nonnull Store<EntityStore> store) {
      PlayerUtil.broadcastPacketToPlayers(store, (ToClientPacket)this.currentTimePacket);
   }

   public void sendTimePackets(@Nonnull PlayerRef playerRef) {
      playerRef.getPacketHandler().write((ToClientPacket)this.currentSettings);
      playerRef.getPacketHandler().write((ToClientPacket)this.currentTimePacket);
   }

   public boolean isDayTimeWithinRange(double minTime, double maxTime) {
      double dayProgress = (double)this._gameTimeLocalDateTime.getHour() / (double)HOURS_PER_DAY;
      if (!(minTime > maxTime)) {
         return MathUtil.within(dayProgress, minTime, maxTime);
      } else {
         return MathUtil.within(dayProgress, minTime, 1.0) || MathUtil.within(dayProgress, 0.0, maxTime);
      }
   }

   public void updateTimePacket(@Nonnull UpdateTime currentTimePacket) {
      if (currentTimePacket.gameTime == null) {
         currentTimePacket.gameTime = new InstantData();
      }

      currentTimePacket.gameTime.seconds = this.gameTime.getEpochSecond();
      currentTimePacket.gameTime.nanos = this.gameTime.getNano();
   }

   @Nonnull
   public static UpdateTimeSettings updateTimeSettingsPacket(@Nonnull UpdateTimeSettings settings, @Nonnull World world) {
      WorldConfig worldGameplayConfig = world.getGameplayConfig().getWorldConfig();
      settings.daytimeDurationSeconds = world.getDaytimeDurationSeconds();
      settings.nighttimeDurationSeconds = world.getNighttimeDurationSeconds();
      settings.totalMoonPhases = (byte)worldGameplayConfig.getTotalMoonPhases();
      settings.timePaused = world.getWorldConfig().isGameTimePaused();
      return settings;
   }

   public boolean isScaledDayTimeWithinRange(double minTime, double maxTime) {
      if (!(minTime > maxTime)) {
         return MathUtil.within(this.scaledTime, minTime, maxTime);
      } else {
         return MathUtil.within(this.scaledTime, minTime, 1.0) || MathUtil.within(this.scaledTime, 0.0, maxTime);
      }
   }

   public boolean isYearWithinRange(double minTime, double maxTime) {
      return false;
   }

   public int getCurrentHour() {
      return this.currentHour;
   }

   public float getDayProgress() {
      return (float)this._gameTimeLocalDateTime.get(ChronoField.SECOND_OF_DAY) / (float)SECONDS_PER_DAY;
   }

   @Nonnull
   public Vector3f getSunDirection() {
      float dayTime = this.getDayProgress() * (float)HOURS_PER_DAY;
      float daylightDuration = 0.6F * (float)HOURS_PER_DAY;
      float nightDuration = (float)HOURS_PER_DAY - daylightDuration;
      float halfNightDuration = nightDuration * 0.5F;
      float sunAngle;
      if (dayTime < halfNightDuration) {
         float inverseAllNightDay = 1.0F / (nightDuration * 2.0F);
         sunAngle = MathUtil.wrapAngle((dayTime * inverseAllNightDay - halfNightDuration * inverseAllNightDay) * 6.2831855F);
      } else if (dayTime > (float)HOURS_PER_DAY - halfNightDuration) {
         float inverseAllNightDay = 1.0F / (nightDuration * 2.0F);
         sunAngle = MathUtil.wrapAngle((dayTime * inverseAllNightDay - ((float)HOURS_PER_DAY + halfNightDuration) * inverseAllNightDay) * 6.2831855F);
      } else {
         float halfDaylightDuration = daylightDuration * 0.5F;
         float inverseAllDaylightDay = 1.0F / (daylightDuration * 2.0F);
         sunAngle = MathUtil.wrapAngle((dayTime * inverseAllDaylightDay - ((float)HOURS_PER_DAY * 0.5F - halfDaylightDuration) * inverseAllDaylightDay) * 6.2831855F);
      }

      Vector3f sunPosition = new Vector3f(TrigMathUtil.cos(sunAngle), TrigMathUtil.sin(sunAngle) * 2.0F, TrigMathUtil.sin(sunAngle));
      sunPosition.normalize();
      float tweakedSunHeight = sunPosition.y + 0.2F;
      if (tweakedSunHeight > 0.0F) {
         sunPosition.scale(-1.0F);
      }

      sunPosition.x = MathUtil.lerp(sunPosition.x, Vector3f.DOWN.x, 0.35F);
      sunPosition.y = MathUtil.lerp(sunPosition.y, Vector3f.DOWN.y, 0.35F);
      sunPosition.z = MathUtil.lerp(sunPosition.z, Vector3f.DOWN.z, 0.35F);
      return sunPosition;
   }

   @Nonnull
   public static InstantData instantToInstantData(@Nonnull Instant instant) {
      return new InstantData(instant.getEpochSecond(), instant.getNano());
   }

   @Nonnull
   public static Instant instantDataToInstant(@Nonnull InstantData instantData) {
      return Instant.ofEpochSecond(instantData.seconds, (long)instantData.nanos);
   }

   @Nonnull
   public Resource<EntityStore> clone() {
      WorldTimeResource worldTimeComponent = new WorldTimeResource();
      worldTimeComponent.gameTime = this.gameTime;
      worldTimeComponent._gameTimeLocalDateTime = this._gameTimeLocalDateTime;
      worldTimeComponent.currentHour = this.currentHour;
      worldTimeComponent.sunlightFactor = this.sunlightFactor;
      worldTimeComponent.scaledTime = this.scaledTime;
      return worldTimeComponent;
   }

   @Nonnull
   public String toString() {
      return "WorldTimeResource{, gameTime=" + String.valueOf(this.gameTime) + "}";
   }

   static {
      NANOS_PER_DAY = ChronoUnit.DAYS.getDuration().toNanos();
      SECONDS_PER_DAY = (int)ChronoUnit.DAYS.getDuration().getSeconds();
      HOURS_PER_DAY = (int)ChronoUnit.DAYS.getDuration().toHours();
      DAYS_PER_YEAR = (int)ChronoUnit.YEARS.getDuration().toDays();
      ZERO_YEAR = Instant.parse("0001-01-01T00:00:00.00Z");
      MAX_TIME = Instant.ofEpochSecond(31553789759L, 99999999L);
      ZONE_OFFSET = ZoneOffset.UTC;
      DAYTIME_SECONDS = (int)((float)SECONDS_PER_DAY * 0.6F);
      NIGHTTIME_SECONDS = (int)((float)SECONDS_PER_DAY * 0.39999998F);
      SUNRISE_SECONDS = NIGHTTIME_SECONDS / 2;
   }
}
