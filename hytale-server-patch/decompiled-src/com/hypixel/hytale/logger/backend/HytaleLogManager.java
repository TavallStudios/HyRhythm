package com.hypixel.hytale.logger.backend;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class HytaleLogManager extends LogManager {
   public static HytaleLogManager instance;

   public HytaleLogManager() {
      instance = this;
      this.getLogger("Hytale");
   }

   public void reset() {
   }

   private void reset0() {
      super.reset();
   }

   @Nonnull
   public Logger getLogger(@Nonnull String name) {
      Logger logger = super.getLogger(name);
      return (Logger)(logger != null ? logger : new HytaleJdkLogger(HytaleLoggerBackend.getLogger(name)));
   }

   public static void resetFinally() {
      HytaleConsole.INSTANCE.shutdown();
      HytaleFileHandler.INSTANCE.shutdown();
      if (instance != null) {
         instance.reset0();
      }

   }

   private static class HytaleJdkLogger extends Logger {
      @Nonnull
      private final HytaleLoggerBackend backend;

      public HytaleJdkLogger(@Nonnull HytaleLoggerBackend backend) {
         super(backend.getLoggerName(), (String)null);
         this.backend = backend;
      }

      public String getName() {
         return this.backend.getLoggerName();
      }

      @Nonnull
      public Level getLevel() {
         return this.backend.getLevel();
      }

      public boolean isLoggable(@Nonnull Level level) {
         return this.backend.isLoggable(level);
      }

      public void log(@Nonnull LogRecord record) {
         this.backend.log(record);
      }

      public void setLevel(@Nonnull Level newLevel) {
         this.backend.setLevel(newLevel);
      }
   }
}
