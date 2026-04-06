package com.hypixel.hytale.server.core.modules.time.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.commands.worldconfig.WorldConfigPauseTimeCommand;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import javax.annotation.Nonnull;

public class TimeCommand extends AbstractWorldCommand {
   public TimeCommand() {
      super("time", "server.commands.time.get.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases(new String[]{"daytime"});
      this.addUsageVariant(new SetTimeHourCommand());

      for(TimeOfDay value : TimeCommand.TimeOfDay.values()) {
         this.addSubCommand(new SetTimePeriodCommand(value));
      }

      this.addSubCommand(new TimeSetSubCommand());
      this.addSubCommand(new TimePauseCommand());
      this.addSubCommand(new TimeDilationCommand());
   }

   public void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      WorldTimeResource worldTimeResource = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
      LocalDateTime gameDateTime = worldTimeResource.getGameDateTime();
      Message pausedMessage = Message.translation(world.getWorldConfig().isGameTimePaused() ? "server.commands.time.paused" : "server.commands.time.unpaused");
      Message message = Message.translation("server.commands.time.info").param("worldName", world.getName()).param("timePaused", pausedMessage);
      context.sendMessage(message.param("time", worldTimeResource.getGameTime().toString()).param("dayOfWeek", FormatUtil.addNumberSuffix(gameDateTime.get(ChronoField.DAY_OF_WEEK))).param("weekOfMonth", FormatUtil.addNumberSuffix(gameDateTime.get(ChronoField.ALIGNED_WEEK_OF_MONTH))).param("weekOfYear", FormatUtil.addNumberSuffix(gameDateTime.get(ChronoField.ALIGNED_WEEK_OF_YEAR))).param("dayOfYear", FormatUtil.addNumberSuffix(gameDateTime.getDayOfYear())).param("moonPhase", FormatUtil.addNumberSuffix(worldTimeResource.getMoonPhase() + 1)));
   }

   public static enum TimeOfDay {
      Dawn((hoursOfDaylight) -> ((float)WorldTimeResource.HOURS_PER_DAY - hoursOfDaylight) / 2.0F, new String[]{"day", "morning"}),
      Midday((hoursOfDaylight) -> (float)WorldTimeResource.HOURS_PER_DAY / 2.0F, new String[]{"noon"}),
      Dusk((hoursOfDaylight) -> ((float)WorldTimeResource.HOURS_PER_DAY - hoursOfDaylight) / 2.0F + hoursOfDaylight, new String[]{"night"}),
      Midnight((hoursOfDaylight) -> 0.0F, new String[0]);

      @Nonnull
      private final Float2FloatFunction periodFunc;
      private final String[] aliases;

      private TimeOfDay(@Nonnull final Float2FloatFunction periodFunc, String... aliases) {
         this.periodFunc = periodFunc;
         this.aliases = aliases;
      }
   }

   private static class TimePauseCommand extends AbstractWorldCommand {
      public TimePauseCommand() {
         super("pause", "server.commands.pausetime.desc");
         this.setPermissionGroup((GameMode)null);
         this.addAliases(new String[]{"stop"});
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         WorldConfigPauseTimeCommand.pauseTime(context.sender(), world, store);
      }
   }

   private static class TimeDilationCommand extends AbstractWorldCommand {
      private static final float TIME_DILATION_MIN = 0.01F;
      private static final float TIME_DILATION_MAX = 4.0F;
      @Nonnull
      private final RequiredArg<Float> timeDilationArg;

      public TimeDilationCommand() {
         super("dilation", "server.commands.time.dilation.desc");
         this.timeDilationArg = (RequiredArg)((RequiredArg)this.withRequiredArg("timeDilation", "server.commands.time.dilation.timeDilation.desc", ArgTypes.FLOAT).addValidator(Validators.greaterThan(0.01F))).addValidator(Validators.max(4.0F));
         this.setPermissionGroup((GameMode)null);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         float timeDilation = (Float)this.timeDilationArg.get(context);
         World.setTimeDilation(timeDilation, store);
         context.sendMessage(Message.translation("server.commands.time.dilation.set.success").param("timeDilation", timeDilation));
      }
   }

   private static class TimeSetSubCommand extends AbstractCommandCollection {
      public TimeSetSubCommand() {
         super("set", "server.commands.time.set.desc");
         this.setPermissionGroup((GameMode)null);
         this.addUsageVariant(new SetTimeHourCommand());

         for(TimeOfDay value : TimeCommand.TimeOfDay.values()) {
            this.addSubCommand(new SetTimePeriodCommand(value));
         }

      }
   }

   private static class SetTimeHourCommand extends AbstractWorldCommand {
      @Nonnull
      private final RequiredArg<Float> timeArg;

      public SetTimeHourCommand() {
         super("server.commands.time.set.desc");
         this.timeArg = (RequiredArg)this.withRequiredArg("time", "server.commands.time.set.timeArg.desc", ArgTypes.FLOAT).addValidator(Validators.range(0.0F, (float)WorldTimeResource.HOURS_PER_DAY));
         this.setPermissionGroup((GameMode)null);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         float time = (Float)this.timeArg.get(context);
         WorldTimeResource worldTimeResource = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
         worldTimeResource.setDayTime((double)(time / (float)WorldTimeResource.HOURS_PER_DAY), world, store);
         context.sendMessage(Message.translation("server.commands.time.set").param("worldName", world.getName()).param("time", worldTimeResource.getGameTime().toString()));
      }
   }

   private static class SetTimePeriodCommand extends AbstractWorldCommand {
      @Nonnull
      private final TimeOfDay timeOfDay;

      public SetTimePeriodCommand(@Nonnull TimeOfDay timeOfDay) {
         super(timeOfDay.name(), "server.commands.time.period." + timeOfDay.name().toLowerCase() + ".desc");
         this.setPermissionGroup((GameMode)null);
         this.timeOfDay = timeOfDay;
         this.addAliases(timeOfDay.aliases);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         float daylightHours = (float)WorldTimeResource.HOURS_PER_DAY * 0.6F;
         float periodTime = Math.max(0.0F, (Float)this.timeOfDay.periodFunc.apply(daylightHours));
         WorldTimeResource worldTimeResource = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
         worldTimeResource.setDayTime((double)(periodTime / (float)WorldTimeResource.HOURS_PER_DAY), world, store);
         context.sendMessage(Message.translation("server.commands.time.set").param("worldName", world.getName()).param("time", String.format("%s (%s)", worldTimeResource.getGameTime().toString(), this.timeOfDay.name())));
      }
   }
}
