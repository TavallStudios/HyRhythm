package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class LogCommand extends CommandBase {
   @Nonnull
   private static final Level[] STANDARD_LEVELS;
   @Nonnull
   private static final String LEVELS_STRING;
   @Nonnull
   private static final SingleArgumentType<Level> LOG_LEVEL;
   @Nonnull
   private final RequiredArg<String> loggerArg;
   @Nonnull
   private final OptionalArg<Level> levelArg;
   @Nonnull
   private final FlagArg saveFlag;
   @Nonnull
   private final FlagArg resetFlag;

   public LogCommand() {
      super("log", "server.commands.log.desc");
      this.loggerArg = this.withRequiredArg("logger", "server.commands.log.logger.desc", ArgTypes.STRING);
      this.levelArg = this.withOptionalArg("level", "server.commands.log.level.desc", LOG_LEVEL);
      this.saveFlag = this.withFlagArg("save", "server.commands.log.save.desc");
      this.resetFlag = this.withFlagArg("reset", "server.commands.log.reset.desc");
   }

   protected void executeSync(@Nonnull CommandContext context) {
      String loggerName = (String)this.loggerArg.get(context);
      HytaleLoggerBackend logger;
      if (loggerName.equalsIgnoreCase("global")) {
         loggerName = "global";
         logger = HytaleLoggerBackend.getLogger();
      } else {
         logger = HytaleLoggerBackend.getLogger(loggerName);
      }

      if (this.levelArg.provided(context)) {
         Level level = (Level)this.levelArg.get(context);
         logger.setLevel(level);
         boolean saved = false;
         if ((Boolean)this.saveFlag.get(context)) {
            Map<String, Level> logLevels = new Object2ObjectOpenHashMap(HytaleServer.get().getConfig().getLogLevels());
            logLevels.put(logger.getLoggerName(), level);
            HytaleServer.get().getConfig().setLogLevels(logLevels);
            saved = true;
         }

         context.sendMessage(Message.translation("server.commands.log.setLogger").param("name", loggerName).param("level", level.getName()).param("saved", saved ? " and saved to config!!" : ""));
      } else {
         if ((Boolean)this.resetFlag.get(context)) {
            Map<String, Level> logLevels = new Object2ObjectOpenHashMap(HytaleServer.get().getConfig().getLogLevels());
            logLevels.remove(logger.getLoggerName());
            HytaleServer.get().getConfig().setLogLevels(logLevels);
            context.sendMessage(Message.translation("server.commands.log.removedLogger").param("name", loggerName));
         }

         context.sendMessage(Message.translation("server.commands.log.setLoggerNoSave").param("name", loggerName).param("level", logger.getLevel().getName()));
      }

   }

   static {
      STANDARD_LEVELS = new Level[]{Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL};
      LEVELS_STRING = (String)Arrays.stream(STANDARD_LEVELS).map(Level::getName).collect(Collectors.joining(", "));
      LOG_LEVEL = new SingleArgumentType<Level>("server.commands.parsing.argtype.logLevel.name", Message.translation("server.commands.parsing.argtype.logLevel.usage").param("levels", LEVELS_STRING), (String[])Arrays.stream(STANDARD_LEVELS).map(Level::getName).toArray((x$0) -> new String[x$0])) {
         @Nonnull
         public Level parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
            try {
               return Level.parse(input.toUpperCase());
            } catch (IllegalArgumentException var4) {
               parseResult.fail(Message.translation("server.commands.log.invalidLevel").param("input", input).param("level", Level.INFO.getName()));
               return Level.INFO;
            }
         }
      };
   }
}
